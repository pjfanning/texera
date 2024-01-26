package edu.uci.ics.amber.engine.architecture.worker

import akka.actor.Props
import edu.uci.ics.amber.engine.architecture.common.WorkflowActor.NetworkAck
import edu.uci.ics.amber.engine.architecture.common.WorkflowActor
import edu.uci.ics.amber.engine.architecture.controller.Controller.ReplayStatusUpdate
import edu.uci.ics.amber.engine.architecture.controller.promisehandlers.FatalErrorHandler.FatalError
import edu.uci.ics.amber.engine.architecture.deploysemantics.PhysicalOp
import edu.uci.ics.amber.engine.architecture.messaginglayer.WorkerTimerService
import edu.uci.ics.amber.engine.architecture.scheduling.config.{OperatorConfig, WorkerConfig}
import edu.uci.ics.amber.engine.common.actormessage.{ActorCommand, Backpressure}
import edu.uci.ics.amber.engine.common.ambermessage.WorkflowMessage.getInMemSize
import edu.uci.ics.amber.engine.architecture.worker.WorkflowWorker.{
  ActorCommandElement,
  DPInputQueueElement,
  FIFOMessageElement,
  MainThreadDelegate,
  TimerBasedControlElement,
  WorkerReplayInitialization
}
import edu.uci.ics.amber.engine.common.{CheckpointState, SerializedState, VirtualIdentityUtils}
import edu.uci.ics.amber.engine.common.ambermessage.WorkflowFIFOMessage
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCClient.ControlInvocation
import edu.uci.ics.amber.engine.common.virtualidentity.{
  ActorVirtualIdentity,
  ChannelIdentity,
  ChannelMarkerIdentity
}
import edu.uci.ics.amber.engine.common.virtualidentity.util.CONTROLLER

import java.net.URI
import java.util.concurrent.LinkedBlockingQueue
import scala.collection.mutable

object WorkflowWorker {
  def props(
      workerConfig: WorkerConfig,
      physicalOp: PhysicalOp,
      operatorConfig: OperatorConfig,
      replayInitialization: WorkerReplayInitialization
  ): Props =
    Props(
      new WorkflowWorker(
        workerConfig,
        physicalOp,
        operatorConfig,
        replayInitialization
      )
    )

  def getWorkerLogName(id: ActorVirtualIdentity): String = id.name.replace("Worker:", "")

  final case class TriggerSend(msg: WorkflowFIFOMessage)

  final case class MainThreadDelegate(closure: WorkflowWorker => Unit)

  sealed trait DPInputQueueElement

  final case class FIFOMessageElement(msg: WorkflowFIFOMessage) extends DPInputQueueElement
  final case class TimerBasedControlElement(control: ControlInvocation) extends DPInputQueueElement
  final case class ActorCommandElement(cmd: ActorCommand) extends DPInputQueueElement

  final case class WorkerReplayInitialization(
      restoreConfOpt: Option[StateRestoreConfig] = None,
      replayLogConfOpt: Option[FaultToleranceConfig] = None
  )
  final case class StateRestoreConfig(readFrom: URI, replayDestination: ChannelMarkerIdentity)

  final case class FaultToleranceConfig(writeTo: URI)
}

class WorkflowWorker(
    workerConfig: WorkerConfig,
    physicalOp: PhysicalOp,
    operatorConfig: OperatorConfig,
    replayInitialization: WorkerReplayInitialization
) extends WorkflowActor(replayInitialization.replayLogConfOpt, workerConfig.workerId) {
  val inputQueue: LinkedBlockingQueue[DPInputQueueElement] =
    new LinkedBlockingQueue()
  var dp = new DataProcessor(
    workerConfig.workerId,
    logManager.sendCommitted
  )
  val timerService = new WorkerTimerService(actorService)

  val dpThread =
    new DPThread(workerConfig.workerId, dp, logManager, inputQueue)

  val inputRecordings =
    new mutable.HashMap[ChannelMarkerIdentity, mutable.ArrayBuffer[WorkflowFIFOMessage]]()

  override def initState(): Unit = {
    dp.initTimerService(timerService)
    dp.initOperator(
      VirtualIdentityUtils.getWorkerIndex(workerConfig.workerId),
      physicalOp,
      operatorConfig,
      None
    )
    if (replayInitialization.restoreConfOpt.isDefined) {
      context.parent ! ReplayStatusUpdate(actorId, status = true)
      setupReplay(
        dp,
        replayInitialization.restoreConfOpt.get,
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

  def handleTriggerClosure: Receive = {
    case t: MainThreadDelegate =>
      t.closure(this)
  }

  def handleActorCommand: Receive = {
    case c: ActorCommand =>
      println(c)
  }

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    super.preRestart(reason, message)
    logger.error(s"Encountered fatal error, worker is shutting done.", reason)
    postStop()
    dp.asyncRPCClient.send(
      FatalError(reason, Some(workerConfig.workerId)),
      CONTROLLER
    )
  }

  override def receive: Receive = {
    super.receive orElse handleDirectInvocation orElse handleTriggerClosure
  }

  override def handleInputMessage(id: Long, workflowMsg: WorkflowFIFOMessage): Unit = {
    inputQueue.put(FIFOMessageElement(workflowMsg))
    inputRecordings.values.foreach(_.append(workflowMsg))
    sender ! NetworkAck(id, getInMemSize(workflowMsg), getQueuedCredit(workflowMsg.channelId))
  }

  /** flow-control */
  override def getQueuedCredit(channelId: ChannelIdentity): Long = {
    dp.getQueuedCredit(channelId)
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

  override def initFromCheckpoint(chkpt: CheckpointState): Unit = {
    val inflightMessages: mutable.ArrayBuffer[WorkflowFIFOMessage] =
      chkpt.load(SerializedState.IN_FLIGHT_MSG_KEY)
    val dpState: DataProcessor = chkpt.load(SerializedState.DP_STATE_KEY)
    val queuedMessages: mutable.ArrayBuffer[WorkflowFIFOMessage] =
      chkpt.load(SerializedState.DP_QUEUED_MSG_KEY)
    val outputMessages: Array[WorkflowFIFOMessage] = chkpt.load(SerializedState.OUTPUT_MSG_KEY)
    dp = dpState // overwrite dp state
    dp.outputHandler = logManager.sendCommitted
    dp.initTimerService(timerService)
    dp.initOperator(
      VirtualIdentityUtils.getWorkerIndex(workerConfig.workerId),
      physicalOp,
      operatorConfig,
      Some(chkpt)
    )
    queuedMessages.foreach(msg => inputQueue.put(FIFOMessageElement(msg)))
    inflightMessages.foreach(msg => inputQueue.put(FIFOMessageElement(msg)))
    outputMessages.foreach(transferService.send)
    context.parent ! ReplayStatusUpdate(actorId, status = false)
  }
}
