package edu.uci.ics.amber.engine.architecture.recovery

import edu.uci.ics.amber.engine.architecture.checkpoint.{CheckpointHolder, SavedCheckpoint}
import edu.uci.ics.amber.engine.architecture.logging.StepsOnChannel
import edu.uci.ics.amber.engine.architecture.logging.storage.DeterminantLogStorage.DeterminantLogReader
import edu.uci.ics.amber.engine.architecture.recovery.InternalPayloadManager.{CheckpointStats, TakeRuntimeGlobalCheckpoint}
import edu.uci.ics.amber.engine.architecture.worker.processing.DPThread
import edu.uci.ics.amber.engine.architecture.worker.{ReplayCheckpointConfig, WorkerInternalQueue, WorkerInternalQueueImpl, WorkflowWorker}
import edu.uci.ics.amber.engine.common.CheckpointSupport
import edu.uci.ics.amber.engine.common.ambermessage.ClientEvent.{ReplayCompleted, RuntimeCheckpointCompleted}
import edu.uci.ics.amber.engine.common.ambermessage.WorkflowFIFOMessagePayload
import edu.uci.ics.amber.engine.common.tuple.ITuple

class WorkerCheckpointRestoreManager(@transient worker:WorkflowWorker) extends CheckpointRestoreManager(worker) {


  def onCheckpointCompleted(pendingCheckpoint: PendingCheckpoint): Unit ={
    worker.initiateSyncActionFromMain(() =>{
      val stats = finalizeCheckpoint(pendingCheckpoint)
      worker.dataProcessor.outputPort.sendToClient(RuntimeCheckpointCompleted(actorId, pendingCheckpoint.logicalSnapshotId, pendingCheckpoint.checkpointId, stats))
    })
  }

  override def overwriteState(chkpt:SavedCheckpoint):Unit = {
    var outputIter:Iterator[(ITuple, Option[Int])] = Iterator.empty
    worker.inputPort.setFIFOState(chkpt.load("fifoState"))
    worker.dataProcessor = chkpt.load("dataProcessor")
    logger.info(s"DP restored")
    if(worker.dataProcessor.operatorOpened) {
      worker.operator match {
        case support: CheckpointSupport =>
          outputIter = support.deserializeState(chkpt)
        case _ =>
      }
    }
    logger.info(s"operator restored current step = ${worker.dataProcessor.cursor.getStep}")
    worker.dataProcessor.initDP(
      worker,
      outputIter
    )
  }

  override def setupReplay(replayId:String, logReader: DeterminantLogReader, replayTo:Option[Long]):ReplayOrderEnforcer = {
    val replayOrderEnforcer = new ReplayOrderEnforcer(logReader.getLogs[StepsOnChannel], () => {
      logger.info("recovery completed, continue normal processing")
      worker.dataProcessor.outputPort.sendToClient(ReplayCompleted(actorId, replayId))
      val normalQueue = new WorkerInternalQueueImpl(worker.creditMonitor)
      WorkerInternalQueue.transferContent(worker.internalQueue, normalQueue)
      worker.internalQueue = normalQueue
    })
    val currentStep = worker.dataProcessor.cursor.getStep
    replayOrderEnforcer.initialize(currentStep)
    if(replayTo.isDefined) {
      val currentStep = worker.dataProcessor.cursor.getStep
      replayOrderEnforcer.setReplayTo(currentStep, replayTo.get)
    }
    worker.internalQueue = new RecoveryInternalQueueImpl(worker.actorId, worker.creditMonitor, replayOrderEnforcer)
    replayOrderEnforcer
  }

  override def fillCheckpoint(checkpoint: PendingCheckpoint): Long = {
    val startTime = System.currentTimeMillis()
    checkpoint.chkpt.save("fifoState", worker.inputPort.getFIFOState)
    worker.internalQueue.getAllMessages.foreach{
      case (d, messages) =>
      messages.foreach(x => checkpoint.chkpt.addInternalData(d, x.payload.asInstanceOf[WorkflowFIFOMessagePayload]))
    }
    if(worker.dataProcessor.operatorOpened){
      worker.dataProcessor.operator match {
        case support: CheckpointSupport =>
          worker.dataProcessor.outputIterator.setTupleOutput(
            support.serializeState(worker.dataProcessor.outputIterator.outputIter, checkpoint.chkpt)
          )
        case _ =>
      }
    }
    checkpoint.chkpt.save("dataProcessor", worker.dataProcessor)
    System.currentTimeMillis() - startTime
  }

  override def doCheckpointDuringReplay(pendingCheckpoint: PendingCheckpoint, conf: ReplayCheckpointConfig): () => Unit = {
    () => {
      // now inside DP thread
      worker.initiateSyncActionFromDP(()=>{
        logger.info(s"taking checkpoint during replay at step ${worker.dataProcessor.cursor.getStep}")
        worker.dataProcessor.outputPort.broadcastMarker(TakeRuntimeGlobalCheckpoint(conf.checkpointId, Map.empty))
        fillCheckpoint(pendingCheckpoint)
        pendingCheckpoint.fifoOutputState = worker.dataProcessor.outputPort.getFIFOState
        pendingCheckpoint.checkpointDone = true
        logger.info(s"state serialization done, pending checkpoint aligned = ${pendingCheckpoint.isNoLongerPending}")
        pendingCheckpoint.checkCompletion()
      })
    }
  }

  override def startProcessing(stateReloaded:Boolean, replayOrderEnforcer: ReplayOrderEnforcer): Unit = {
    logger.info(s"worker restored! input Seq: ${worker.inputPort.getFIFOState}")
    logger.info(s"worker restored! output Seq: ${worker.dataProcessor.outputPort.getFIFOState}")
    worker.dpThread = new DPThread(actorId, worker.dataProcessor, worker.internalQueue, worker, replayOrderEnforcer)
    logger.info("starting new DP thread...")
    assert(worker.internalQueue.isInstanceOf[RecoveryInternalQueueImpl])
    worker.dpThread.start() // new DP is not started yet.
  }

  override protected def replayCompletedCallback(replayId: String): () => Unit = {
    () => {
      logger.info("replay completed, waiting for next instruction")
      worker.dataProcessor.outputPort.sendToClient(ReplayCompleted(actorId, replayId))
    }
  }
}
