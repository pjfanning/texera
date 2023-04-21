package edu.uci.ics.amber.engine.architecture.worker.processing

import akka.serialization.SerializationExtension
import edu.uci.ics.amber.engine.architecture.checkpoint.{CheckpointHolder, PlannedCheckpoint, SavedCheckpoint}
import edu.uci.ics.amber.engine.architecture.logging.{RecordedPayload, StepsOnChannel}
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
            worker.dataProcessor.determinantLogger.getStep,
            worker.inputPort.getFIFOState,
            worker.dataProcessor.outputPort.getFIFOState,
            0,
            estimatedCheckpointCost + worker.dataProcessor.internalQueue.getDataQueueLength)
          worker.dataProcessor.outputPort.sendTo(CLIENT, EstimationCompleted(id, stats))
        })
      case LoadStateAndReplay(id, fromCheckpoint, replayTo, confs) =>
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
            worker,
            outputIter
          )
          worker.dpThread = new DPThread(actorId, worker.dataProcessor, worker.internalQueue)
        }
        // setup replay infra
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
        // setup checkpoints during replay
        // create empty checkpoints to fill
        confs.foreach(conf => {
          val planned = new PlannedCheckpoint(actorId, conf, SerializationExtension(worker.context.system))
          worker.inputPort.setRecordingForFutureInput(planned)
          replayOrderEnforcer.setCheckpoint(conf.checkpointAt, () =>{
            // now inside DP thread
            fillCheckpoint(planned.chkpt)
            planned.decreaseCompletionCount()
          })
        })
        // put recorded input payload from checkpoint back to queue
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
    val logReader = worker.logStorage.getReader
    val replayOrderEnforcer = new ReplayOrderEnforcer(logReader.getLogs[StepsOnChannel])
    val currentStep = worker.dataProcessor.determinantLogger.getStep
    replayOrderEnforcer.initialize(currentStep)
    val recoveryQueue = new RecoveryInternalQueueImpl(worker.creditMonitor, replayOrderEnforcer)
    WorkerInternalQueue.transferContent(worker.internalQueue, recoveryQueue)
    worker.internalQueue = recoveryQueue
    // add recorded payload back to the queue from log
    logReader.getLogs[RecordedPayload].foreach(elem => worker.handlePayloadAndMarker(elem.channel, elem.payload))
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

  def fillCheckpoint(chkpt: SavedCheckpoint): Long ={
    val startTime = System.currentTimeMillis()
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
    if(restoreAdaptiveBatching){
      worker.dataProcessor.outputManager.adaptiveBatchingMonitor.enableAdaptiveBatching(worker.actorService)
    }
    System.currentTimeMillis() - startTime
  }

  override def markerAlignmentStart(payload: MarkerAlignmentInternalPayload): MarkerCollectionSupport = {
    payload match {
      case TakeRuntimeGlobalCheckpoint(_, alignmentMap) =>
        val f = worker.executeThroughDP(() =>{
          val chkpt = new SavedCheckpoint()
          chkpt.attachSerialization(SerializationExtension(worker.context.system))
          worker.dataProcessor.outputPort.broadcastMarker(payload)
          val elapsed = fillCheckpoint(chkpt)
          new PendingCheckpoint(
            worker.actorId,
            System.currentTimeMillis(),
            worker.dataProcessor.determinantLogger.getStep,
            worker.inputPort.getFIFOState,
            worker.dataProcessor.outputPort.getFIFOState,
            elapsed,
            chkpt,
            alignmentMap(worker.actorId))
        })
        f.get()
      case _ => ???
    }
  }

  override def markerAlignmentEnd(payload: MarkerAlignmentInternalPayload, support: MarkerCollectionSupport): Unit = {
    payload match {
      case TakeRuntimeGlobalCheckpoint(id, _) =>
        worker.executeThroughDP(() =>{
          val pendingCheckpoint = support.asInstanceOf[PendingCheckpoint]
          val alignmentCost = System.currentTimeMillis() - pendingCheckpoint.startTime
          val stats = CheckpointStats(
            pendingCheckpoint.stepCursorAtCheckpoint,
            pendingCheckpoint.fifoInputState,
            pendingCheckpoint.fifoOutputState,
            alignmentCost,
            pendingCheckpoint.initialCheckpointTime)
          worker.dataProcessor.outputPort.sendTo(CLIENT, RuntimeCheckpointCompleted(id, stats))
        })
      case _ => ???
    }
  }
}
