package edu.uci.ics.amber.engine.architecture.worker.processing

import akka.serialization.SerializationExtension
import edu.uci.ics.amber.engine.architecture.checkpoint.{CheckpointHolder, SavedCheckpoint}
import edu.uci.ics.amber.engine.architecture.logging.StepsOnChannel
import edu.uci.ics.amber.engine.architecture.recovery.InternalPayloadManager._
import edu.uci.ics.amber.engine.architecture.recovery.{InternalPayloadManager, PendingCheckpoint, RecoveryInternalQueueImpl, ReplayOrderEnforcer}
import edu.uci.ics.amber.engine.architecture.worker.{WorkerInternalQueue, WorkerInternalQueueImpl, WorkflowWorker}
import edu.uci.ics.amber.engine.common.{AmberLogging, CheckpointSupport}
import edu.uci.ics.amber.engine.common.ambermessage.{ChannelEndpointID, IdempotentInternalPayload, MarkerAlignmentInternalPayload, MarkerCollectionSupport, OneTimeInternalPayload}
import edu.uci.ics.amber.engine.common.tuple.ITuple
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity
import edu.uci.ics.amber.engine.common.virtualidentity.util.{CLIENT, CONTROLLER}

class WorkerInternalPayloadManager(worker:WorkflowWorker) extends InternalPayloadManager with AmberLogging {

  override def actorId: ActorVirtualIdentity = worker.actorId

  override def handlePayload(payload: OneTimeInternalPayload): Unit = {
    payload match {
      case EstimateCheckpointCost(id) =>
        worker.executeThroughDPNoReturn(() =>{
          worker.dataProcessor.outputPort.broadcastMarker(payload)
          var estimatedCheckpointCost = 0
          worker.dataProcessor.operator match {
            case support: CheckpointSupport =>
              estimatedCheckpointCost = support.getEstimatedCheckpointTime
            case _ =>
          }
          val stats = CheckpointStats(
            id,
            worker.inputPort.getFIFOState,
            worker.dataProcessor.outputPort.getFIFOState,
            worker.dataProcessor.determinantLogger.getStep,
            estimatedCheckpointCost + worker.dataProcessor.internalQueue.getDataQueueLength)
          worker.dataProcessor.outputPort.sendTo(CLIENT, EstimationCompleted(id, stats))
        })
      case LoadStateAndReplay(id, fromCheckpoint, replayTo, chkpts) =>
        var chkpt:SavedCheckpoint = null
        if(fromCheckpoint.isDefined){
          worker.dpThread.stop() // intentionally kill DP
          chkpt = CheckpointHolder.getCheckpoint(worker.actorId, fromCheckpoint.get)
          chkpt.attachSerialization(SerializationExtension(worker.context.system))
          worker.inputPort.setFIFOState(chkpt.load("fifoState"))
          logger.info("fifo state restored")
          worker.internalQueue = chkpt.load("internalQueue")
          logger.info("input queue restored")
          worker.dataProcessor = chkpt.load("dataProcessor")
          logger.info(s"DP restored ${worker.dataProcessor.upstreamLinkStatus.upstreamMapReverse}")
          var outputIter:Iterator[(ITuple, Option[Int])] = Iterator.empty
          worker.operator match {
            case support: CheckpointSupport =>
              outputIter = support.deserializeState(chkpt)
            case _ =>
          }
          logger.info("operator restored")
          worker.dataProcessor.initDP(
            worker.operator,
            outputIter,
            worker.context,
            worker.logManager,
            worker.internalQueue
          )
          worker.dpThread = new DPThread(actorId, worker.dataProcessor, worker.internalQueue)
        }
        val replayOrderEnforcer = setupReplay()
        if(replayTo.isDefined){
          val currentStep = worker.dataProcessor.determinantLogger.getStep
          replayOrderEnforcer.setReplayTo(currentStep, replayTo.get,  () => {
            worker.dpThread.blockingOnNextStep()
            worker.dataProcessor.outputPort.sendTo(CLIENT, ReplayCompleted(id))
          })
          worker.dpThread.unblock() // in case it is blocked.
        }else{
          replayOrderEnforcer.setRecovery(() => {
            val normalQueue = new WorkerInternalQueueImpl(worker.creditMonitor)
            WorkerInternalQueue.transferContent(worker.internalQueue, normalQueue)
            worker.internalQueue = normalQueue
            worker.dataProcessor.outputPort.sendTo(CLIENT, ReplayCompleted(id))
          })
        }
        if(chkpt != null){
          worker.dpThread.start() // new DP is not started yet.
          chkpt.getInputData.foreach{
            case (c, payloads) =>
              payloads.foreach(x => worker.inputPort.handleFIFOPayload(c, x))
          }
        }
      case _ => ???
    }
  }

  def setupReplay(): ReplayOrderEnforcer ={
    val replayOrderEnforcer = new ReplayOrderEnforcer(worker.logStorage.getReader.getLogs[StepsOnChannel])
    val currentStep = worker.dataProcessor.determinantLogger.getStep
    replayOrderEnforcer.initialize(currentStep)
    val recoveryQueue = new RecoveryInternalQueueImpl(worker.creditMonitor, replayOrderEnforcer)
    WorkerInternalQueue.transferContent(worker.internalQueue, recoveryQueue)
    worker.internalQueue = recoveryQueue
    replayOrderEnforcer
  }

  override def handlePayload(channel:ChannelEndpointID, payload: IdempotentInternalPayload): Unit = {
    payload match {
      case ShutdownDP() =>
        val f = worker.executeThroughDP(() =>{
          worker.dataProcessor.logManager.terminate()
          throw new InterruptedException() // actively interrupt DP
        })
        f.get()
        worker.dataProcessor.dpThread.stop()
      case SetupLogging() =>
        InternalPayloadManager.setupLoggingForWorkflowActor(worker)
      case _ => ???
    }
  }

  override def markerAlignmentStart(payload: MarkerAlignmentInternalPayload): MarkerCollectionSupport = {
    payload match {
      case TakeCheckpoint(_, alignmentMap) =>
        val f = worker.executeThroughDP(() =>{
          val chkpt = new SavedCheckpoint()
          chkpt.attachSerialization(SerializationExtension(worker.context.system))
          var restoreAdaptiveBatching = false
          if(worker.dataProcessor.outputManager.adaptiveBatchingMonitor.adaptiveBatchingHandle.isDefined){
            restoreAdaptiveBatching = true
            worker.dataProcessor.outputManager.adaptiveBatchingMonitor.pauseAdaptiveBatching()
          }
          logger.info("start to take checkpoint")
          chkpt.save("fifoState", worker.inputPort.getFIFOState)
          chkpt.save("internalQueue", worker.internalQueue)
          worker.dataProcessor.operator match {
            case support: CheckpointSupport =>
              worker.dataProcessor.outputIterator.setTupleOutput(
                support.serializeState(worker.dataProcessor.outputIterator.outputIter, chkpt)
              )
            case _ =>
          }
          chkpt.save("dataProcessor", worker.dataProcessor)
          worker.dataProcessor.outputPort.broadcastMarker(payload)
          if(restoreAdaptiveBatching){
            worker.dataProcessor.outputManager.adaptiveBatchingMonitor.enableAdaptiveBatching(worker.context)
          }
          new PendingCheckpoint(worker.actorId, System.currentTimeMillis(), worker.dataProcessor.determinantLogger.getStep, chkpt, alignmentMap(worker.actorId))
        })
        f.get()
      case _ => ???
    }
  }

  override def markerAlignmentEnd(payload: MarkerAlignmentInternalPayload, support: MarkerCollectionSupport): Unit = {
    payload match {
      case TakeCheckpoint(id, _) =>
        worker.executeThroughDP(() =>{
          val pendingCheckpoint = support.asInstanceOf[PendingCheckpoint]
          CheckpointHolder.addCheckpoint(worker.actorId, pendingCheckpoint.stepCursorAtCheckpoint, pendingCheckpoint.chkpt)
          worker.dataProcessor.outputPort.sendTo(CONTROLLER, CheckpointCompleted(id, pendingCheckpoint.stepCursorAtCheckpoint))
        })
      case _ => ???
    }
  }
}
