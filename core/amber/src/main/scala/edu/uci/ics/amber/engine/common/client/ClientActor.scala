package edu.uci.ics.amber.engine.common.client

import akka.actor.{Actor, ActorRef, PoisonPill}
import akka.pattern.StatusReply.Ack
import akka.pattern.ask
import akka.util.Timeout
import com.twitter.util.Promise
import edu.uci.ics.amber.engine.architecture.common.{LogicalExecutionSnapshot, ProcessingHistory}
import edu.uci.ics.amber.engine.architecture.controller.ControllerEvent.{WorkflowCompleted, WorkflowPaused}
import edu.uci.ics.amber.engine.architecture.controller.{Controller, ControllerConfig, Workflow}
import edu.uci.ics.amber.engine.architecture.messaginglayer.NetworkCommunicationActor.{NetworkAck, NetworkMessage}
import edu.uci.ics.amber.engine.architecture.recovery.GlobalRecoveryManager
import edu.uci.ics.amber.engine.architecture.recovery.InternalPayloadManager.{EstimateCheckpointCost, EstimationCompleted}
import edu.uci.ics.amber.engine.common.AmberUtils
import edu.uci.ics.amber.engine.common.ambermessage.{AmberInternalPayload, ControlInvocation, ReturnInvocation, WorkflowFIFOMessage}
import edu.uci.ics.amber.engine.common.client.ClientActor.{ClosureRequest, CommandRequest, InitializeRequest, ObservableRequest}
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.ControlCommand
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity
import edu.uci.ics.texera.web.TexeraWebApplication

import scala.collection.mutable
import scala.concurrent.duration.DurationInt

// TODO: Rename or refactor it since it has mixed duties (send/receive messages, execute callbacks)
private[client] object ClientActor {
  case class InitializeRequest(workflow: Workflow, controllerConfig: ControllerConfig)
  case class ObservableRequest(pf: PartialFunction[Any, Unit])
  case class ClosureRequest[T](closure: () => T)
  case class CommandRequest(command: ControlCommand[_], promise: Promise[Any])
}

private[client] class ClientActor extends Actor {
  var controller: ActorRef = _
  var controlId = 0L
  val promiseMap = new mutable.LongMap[Promise[Any]]()
  var handlers: PartialFunction[Any, Unit] = PartialFunction.empty
  var handlersForInternalCommand:  PartialFunction[Any, Unit] = PartialFunction.empty
  private implicit val timeout: Timeout = Timeout(1.minute)
  val startTime:Long = System.currentTimeMillis()

  val estimationHandler = TexeraWebApplication.scheduleRecurringCallThroughActorSystem(1.seconds, 1.seconds){
    controller ! EstimateCheckpointCost(System.currentTimeMillis() - startTime)
  }

//  var globalRecoveryManager: GlobalRecoveryManager = new GlobalRecoveryManager(() =>{
//    if (handlersForInternalCommand.isDefinedAt(GlobalReplayStarted())) {
//      handlersForInternalCommand(GlobalReplayStarted())
//    }
//  },
//    () => {
//    if (handlersForInternalCommand.isDefinedAt(GlobalReplayCompleted())) {
//      handlersForInternalCommand(GlobalReplayCompleted())
//    }
//  })
//
  val pendingCheckpointStatus = new mutable.HashMap[Long, mutable.HashSet[ActorVirtualIdentity]]()

  val processingHistory = new ProcessingHistory()

  override def receive: Receive = {
    case InitializeRequest(workflow, controllerConfig) =>
      if (controller != null) {
        controller ! PoisonPill
      }
      controller = context.actorOf(Controller.props(workflow, controllerConfig))
      sender ! Ack
    case ClosureRequest(closure) =>
      try {
        sender ! closure()
      } catch {
        case e: Throwable =>
          sender ! e
      }
    case NetworkMessage(mId, WorkflowFIFOMessage(channel, _, EstimationCompleted(id, checkpointStats))) =>
      sender ! NetworkAck(mId)
      if(!processingHistory.hasSnapshotAtTime(id)){
        processingHistory.addSnapshot(id, new LogicalExecutionSnapshot())
      }
      processingHistory.getSnapshotAtTime(id).addParticipant(channel.endpointWorker, checkpointStats)
    case commandRequest: CommandRequest =>
      controller ! ControlInvocation(controlId, commandRequest.command)
      promiseMap(controlId) = commandRequest.promise
      controlId += 1
    case req: ObservableRequest =>
      handlers = req.pf orElse handlers
      sender ! scala.runtime.BoxedUnit.UNIT
    case NetworkMessage(
          mId,
          _ @WorkflowFIFOMessage(_, _, _ @ReturnInvocation(originalCommandID, controlReturn))
        ) =>
      sender ! NetworkAck(mId)
      if (handlers.isDefinedAt(controlReturn)) {
        handlers(controlReturn)
      }
      if (promiseMap.contains(originalCommandID)) {
        controlReturn match {
          case t: Throwable =>
            promiseMap(originalCommandID).setException(t)
          case other =>
            promiseMap(originalCommandID).setValue(other)
        }
        promiseMap.remove(originalCommandID)
      }
    case NetworkMessage(mId, _ @WorkflowFIFOMessage(_, _, _ @ControlInvocation(_, command))) =>
      sender ! NetworkAck(mId)
      if(command.isInstanceOf[WorkflowPaused] || command.isInstanceOf[WorkflowCompleted]){
        estimationHandler.cancel()
      }
      if (handlers.isDefinedAt(command)) {
        handlers(command)
      }
    case NetworkMessage(mId, _) =>
      sender ! NetworkAck(mId)
    case other =>
      println("client actor cannot handle " + other) //skip
  }
}
