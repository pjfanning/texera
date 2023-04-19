package edu.uci.ics.amber.engine.architecture.worker.processing

import akka.serialization.SerializationExtension
import edu.uci.ics.amber.engine.architecture.checkpoint.{CheckpointHolder, SavedCheckpoint}
import edu.uci.ics.amber.engine.architecture.recovery.InternalPayloadManager._
import edu.uci.ics.amber.engine.architecture.recovery.{InternalPayloadManager, PendingCheckpoint}
import edu.uci.ics.amber.engine.architecture.worker.WorkflowWorker
import edu.uci.ics.amber.engine.common.{AmberLogging, CheckpointSupport}
import edu.uci.ics.amber.engine.common.ambermessage.{AmberInternalPayload, ChannelEndpointID, IdempotentInternalPayload, MarkerAlignmentInternalPayload, MarkerCollectionSupport, OneTimeInternalPayload}
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity
import edu.uci.ics.amber.engine.common.virtualidentity.util.CONTROLLER

class WorkerInternalPayloadManager(worker:WorkflowWorker) extends InternalPayloadManager with AmberLogging {

  override def actorId: ActorVirtualIdentity = worker.actorId

//  override def doCheckpointEstimation(marker: EstimationMarker): Unit = {
//    worker.internalQueue.enqueueSystemCommand(TakeCursor(marker, worker.inputPort.getFIFOState))
//  }
//
//  override def prepareGlobalCheckpoint(channel: ChannelEndpointID, marker: GlobalCheckpointMarker): PendingCheckpoint = {
//    val chkpt = new SavedCheckpoint()
//    chkpt.attachSerialization(SerializationExtension(worker.context.system))
//    logger.info("start to take checkpoint")
//    // fill in checkpoint
//    chkpt.save("fifoState", worker.inputPort.getFIFOState)
//    val syncFuture = new CompletableFuture[Long]()
//    worker.internalQueue.enqueueSystemCommand(TakeCheckpoint(marker, chkpt, syncFuture))
//    val totalStep = syncFuture.get()
//    val onComplete = () => {worker.dataProcessor.asyncRPCClient.send(ReportCheckpointCompleted(), CONTROLLER)}
//    new PendingCheckpoint(actorId, System.currentTimeMillis(), chkpt, totalStep, marker.markerCollectionCount.getOrElse(actorId, 1), onComplete)
//  }
//
//  override def restoreStateFrom(savedCheckpoint: Option[SavedCheckpoint], replayTo: Option[Long]): Unit = {
//    var outputIter:Iterator[(ITuple, Option[Int])] = Iterator.empty
//    savedCheckpoint match {
//      case Some(chkpt) =>
//        outputIter = reloadState(chkpt)
//      case None =>
//        worker.internalQueue = new WorkerInternalQueueImpl(worker.creditMonitor)
//    }
//    initReplay(replayTo)
//    worker.dataProcessor.initDP(
//      worker.operator,
//      outputIter,
//      worker.context,
//      worker.logManager,
//      worker.internalQueue
//    )
//    worker.dpThread = new DPThread(actorId, worker.dataProcessor, worker.internalQueue)
//    worker.dpThread.start()
//    logger.info(s"Worker:$actorId = ${worker.context.self} started")
//  }
//
//  def reloadState(chkpt:SavedCheckpoint): Iterator[(ITuple, Option[Int])] ={
//    worker.internalQueue = chkpt.load("internalQueueState")
//    logger.info("input queue restored")
//    worker.dataProcessor = chkpt.load("controlState")
//    logger.info(s"DP restored ${worker.dataProcessor.upstreamLinkStatus.upstreamMapReverse}")
//    var outputIter:Iterator[(ITuple, Option[Int])] = Iterator.empty
//    worker.operator match {
//      case support: CheckpointSupport =>
//        outputIter = support.deserializeState(chkpt)
//      case _ =>
//    }
//    logger.info("operator restored")
//    outputIter
//  }
//
//  def initReplay(replayConf:Option[Long]): Unit ={
//    // set replay
//    replayConf match {
//      case Some(replayTo) =>
//        val queue = worker.internalQueue match {
//          case impl: RecoveryInternalQueueImpl =>
//            impl
//          case impl: WorkerInternalQueueImpl   =>
//            val replayOrderEnforcer = new ReplayOrderEnforcer(worker.logStorage.getReader.getLogs[StepsOnChannel])
//            replayOrderEnforcer.initialize(worker.dataProcessor.determinantLogger.getStep, () =>{
//              // this MUST happen inside DP thread.
//              val syncFuture = new CompletableFuture[Unit]()
//              worker.context.self ! ReplaceRecoveryQueue(syncFuture)
//              syncFuture.get()
//            })
//            val newQueue = new RecoveryInternalQueueImpl(worker.creditMonitor, replayOrderEnforcer)
//            impl.getAllMessages.foreach(newQueue.enqueuePayload)
//            newQueue
//        }
//        queue.replayOrderEnforcer.setReplayTo(replayTo, () => {
//          logger.info("replay completed!")
//          worker.dataProcessor.asyncRPCClient.send(ReportReplayStatus(false), CONTROLLER)
//        })
//        worker.internalQueue = queue
//        worker.recoveryManager.registerOnStart(() => {}
//          // context.parent ! WorkflowRecoveryMessage(actorId, UpdateRecoveryStatus(true))
//        )
//        worker.recoveryManager.setNotifyReplayCallback(() => {}
//          // context.parent ! WorkflowRecoveryMessage(actorId, UpdateRecoveryStatus(false))
//        )
//        worker.recoveryManager.Start()
//        worker.recoveryManager.registerOnEnd(() => {
//          logger.info("recovery complete! restoring stashed inputs...")
//          worker.logManager.terminate()
//          worker.logStorage.cleanPartiallyWrittenLogFile()
//          worker.logManager.setupWriter(worker.logStorage.getWriter)
//          logger.info("stashed inputs restored!")
//          // context.parent ! WorkflowRecoveryMessage(actorId, UpdateRecoveryStatus(false))
//        })
//      case None =>
//        worker.internalQueue match {
//          case impl: RecoveryInternalQueueImpl =>
//            val newQueue = new WorkerInternalQueueImpl(worker.creditMonitor)
//            WorkerInternalQueue.transferContent(impl, newQueue)
//            worker.internalQueue = newQueue
//          case impl: WorkerInternalQueueImpl =>
//          // do nothing
//        }
//    }
//  }

  override def handlePayload(payload: OneTimeInternalPayload): Unit = {
    payload match {
      case EstimateCheckpointCost(id) =>
        worker.executeThroughDP(() =>{
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
          worker.dataProcessor.outputPort.broadcastMarker(payload)
        })
      case _ => ???
    }
  }

  override def handlePayload(channel:ChannelEndpointID, payload: IdempotentInternalPayload): Unit = {
    payload match {
      case NoOp() =>
        worker.executeThroughDP(() => {})
      case RestoreFromCheckpoint(fromCheckpoint, replayTo) => ???
      case ShutdownDP() =>
        worker.executeThroughDP(() =>{
          worker.dataProcessor.logManager.terminate()
          worker.dataProcessor.dpThread.stop()
          throw new InterruptedException() // actively interrupt itself
        })
      case _ => ???
    }
  }

  override def markerAlignmentStart(payload: MarkerAlignmentInternalPayload): MarkerCollectionSupport = {
    payload match {
      case TakeCheckpoint(_, alignmentMap) =>
        worker.executeThroughDP(() =>{
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
          chkpt.save("DataProcessor", worker.dataProcessor)
          worker.dataProcessor.outputPort.broadcastMarker(payload)
          if(restoreAdaptiveBatching){
            worker.dataProcessor.outputManager.adaptiveBatchingMonitor.enableAdaptiveBatching(worker.context)
          }
          new PendingCheckpoint(worker.actorId, System.currentTimeMillis(), worker.dataProcessor.determinantLogger.getStep, chkpt, alignmentMap(worker.actorId))
        })
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
