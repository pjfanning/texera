package edu.uci.ics.amber.engine.architecture.worker

import akka.actor.Props
import edu.uci.ics.amber.engine.architecture.common.WorkflowActor.NetworkAck
import edu.uci.ics.amber.engine.architecture.common.WorkflowActor
import edu.uci.ics.amber.engine.architecture.controller.Controller.ReplayStatusUpdate
import edu.uci.ics.amber.engine.architecture.controller.promisehandlers.FatalErrorHandler.FatalError
import edu.uci.ics.amber.engine.architecture.deploysemantics.PhysicalOp
import edu.uci.ics.amber.engine.architecture.messaginglayer.WorkerTimerService
import edu.uci.ics.amber.engine.common.actormessage.{ActorCommand, Backpressure}
import edu.uci.ics.amber.engine.common.ambermessage.WorkflowMessage.getInMemSize
import edu.uci.ics.amber.engine.architecture.worker.WorkflowWorker.{
  ActorCommandElement,
  DPInputQueueElement,
  FIFOMessageElement,
  TimerBasedControlElement,
  WorkflowWorkerConfig
}
import edu.uci.ics.amber.engine.common.ambermessage.{ChannelID, WorkflowFIFOMessage}
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCClient.ControlInvocation
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity
import edu.uci.ics.amber.engine.common.virtualidentity.util.CONTROLLER

import java.net.URI
import java.util.concurrent.LinkedBlockingQueue

object WorkflowWorker {
  def props(
      id: ActorVirtualIdentity,
      workerIndex: Int,
      physicalOp: PhysicalOp,
      workerConf: WorkflowWorkerConfig
  ): Props =
    Props(
      new WorkflowWorker(
        id,
        workerIndex: Int,
        physicalOp: PhysicalOp,
        workerConf
      )
    )

  def getWorkerLogName(id: ActorVirtualIdentity): String = id.name.replace("Worker:", "")

  final case class TriggerSend(msg: WorkflowFIFOMessage)

  sealed trait DPInputQueueElement

  final case class FIFOMessageElement(msg: WorkflowFIFOMessage) extends DPInputQueueElement
  final case class TimerBasedControlElement(control: ControlInvocation) extends DPInputQueueElement
  final case class ActorCommandElement(cmd: ActorCommand) extends DPInputQueueElement

  final case class WorkflowWorkerConfig(
      restoreConfOpt: Option[WorkerStateRestoreConfig],
      replayLogConfOpt: Option[WorkerReplayLoggingConfig]
  )

  final case class WorkerStateRestoreConfig(readFrom: URI, replayDestination: String)

  final case class WorkerReplayLoggingConfig(writeTo: URI)
}

class WorkflowWorker(
    actorId: ActorVirtualIdentity,
    workerIndex: Int,
    physicalOp: PhysicalOp,
    workerConf: WorkflowWorkerConfig
) extends WorkflowActor(workerConf.replayLogConfOpt, actorId) {
  val inputQueue: LinkedBlockingQueue[DPInputQueueElement] =
    new LinkedBlockingQueue()
  var dp = new DataProcessor(
    actorId,
    logManager.sendCommitted
  )
  val timerService = new WorkerTimerService(actorService)

  val dpThread =
    new DPThread(actorId, dp, logManager, inputQueue)

  override def initState(): Unit = {
    dp.initTimerService(timerService)
    dp.initOperator(workerIndex, physicalOp, currentOutputIterator = Iterator.empty)
    if (workerConf.restoreConfOpt.isDefined) {
      context.parent ! ReplayStatusUpdate(actorId, status = true)
      setupReplay(
        dp,
        workerConf.restoreConfOpt.get,
        () => {
          logger.info("replay completed!")
          context.parent ! ReplayStatusUpdate(actorId, status = false)
        }
      )
    }
    dpThread.start()
  }

  def handleDirectInvocation: Receive = {
    case c: ControlInvocation =>
      inputQueue.put(TimerBasedControlElement(c))
  }

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    super.preRestart(reason, message)
    logger.error(s"Encountered fatal error, worker is shutting done.", reason)
    postStop()
    dp.asyncRPCClient.send(
      FatalError(reason, Some(actorId)),
      CONTROLLER
    )
  }

  override def receive: Receive = {
    super.receive orElse handleDirectInvocation
  }

  override def handleInputMessage(id: Long, workflowMsg: WorkflowFIFOMessage): Unit = {
    inputQueue.put(FIFOMessageElement(workflowMsg))
    sender ! NetworkAck(id, getInMemSize(workflowMsg), getQueuedCredit(workflowMsg.channel))
  }

  /** flow-control */
  override def getQueuedCredit(channelID: ChannelID): Long = {
    dp.getQueuedCredit(channelID)
  }

  override def postStop(): Unit = {
    super.postStop()
    timerService.stopAdaptiveBatching()
    dpThread.stop()
    logManager.terminate()
  }

  override def handleBackpressure(isBackpressured: Boolean): Unit = {
    inputQueue.put(ActorCommandElement(Backpressure(isBackpressured)))
  }
}
