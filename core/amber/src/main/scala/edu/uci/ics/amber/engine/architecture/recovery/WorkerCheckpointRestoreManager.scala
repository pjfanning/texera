package edu.uci.ics.amber.engine.architecture.recovery

import edu.uci.ics.amber.engine.architecture.checkpoint.SavedCheckpoint
import edu.uci.ics.amber.engine.architecture.logging.StepsOnChannel
import edu.uci.ics.amber.engine.architecture.logging.storage.DeterminantLogStorage.DeterminantLogReader
import edu.uci.ics.amber.engine.architecture.recovery.InternalPayloadManager.TakeRuntimeGlobalCheckpoint
import edu.uci.ics.amber.engine.architecture.worker.processing.DPThread
import edu.uci.ics.amber.engine.architecture.worker.{ReplayCheckpointConfig, WorkerInternalQueue, WorkerInternalQueueImpl, WorkflowWorker}
import edu.uci.ics.amber.engine.common.CheckpointSupport
import edu.uci.ics.amber.engine.common.ambermessage.ChannelEndpointID
import edu.uci.ics.amber.engine.common.ambermessage.ClientEvent.ReplayCompleted
import edu.uci.ics.amber.engine.common.tuple.ITuple

import scala.collection.mutable

class WorkerCheckpointRestoreManager(@transient worker:WorkflowWorker) extends CheckpointRestoreManager(worker) {

  protected def overwriteState(chkpt:SavedCheckpoint):Unit = {
    var outputIter:Iterator[(ITuple, Option[Int])] = Iterator.empty
    worker.inputPort.setFIFOState(chkpt.load("fifoState"))
    logger.info("fifo state restored")
    worker.internalQueue = chkpt.load("internalQueue")
    logger.info(s"input queue restored")
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

  protected def setupReplay(replayId:String, logReader: DeterminantLogReader, replayTo:Option[Long]):ReplayOrderEnforcer = {
    val replayOrderEnforcer = new ReplayOrderEnforcer(logReader.getLogs[StepsOnChannel], () => {
      logger.info("recovery completed, continue normal processing")
      worker.dataProcessor.outputPort.sendToClient(ReplayCompleted(actorId, replayId))
      val normalQueue = new WorkerInternalQueueImpl(worker.creditMonitor)
      WorkerInternalQueue.transferContent(worker.internalQueue, normalQueue)
      worker.internalQueue = normalQueue
    })
    val currentStep = worker.dataProcessor.cursor.getStep
    replayOrderEnforcer.initialize(currentStep)
    if(replayTo.isDefined){
      val currentStep = worker.dataProcessor.cursor.getStep
      replayOrderEnforcer.setReplayTo(currentStep, replayTo.get,  () => {
        logger.info("replay completed, waiting for next instruction")
        worker.dpThread.blockingOnNextStep()
        worker.dataProcessor.outputPort.sendToClient(ReplayCompleted(actorId, replayId))
      })
      worker.dpThread.unblock() // in case it is blocked.
    }
    replayOrderEnforcer
  }

  protected def transferQueueContent(orderEnforcer: ReplayOrderEnforcer):Unit = {
    val recoveryQueue = new RecoveryInternalQueueImpl(worker.actorId, worker.creditMonitor, orderEnforcer)
    WorkerInternalQueue.transferContent(worker.internalQueue, recoveryQueue)
    worker.internalQueue = recoveryQueue
    logger.info(s"input queue restored")
  }

  override def fillCheckpoint(checkpoint: SavedCheckpoint): Long = {
    val startTime = System.currentTimeMillis()
    logger.info("start to take checkpoint")
    checkpoint.save("fifoState", worker.inputPort.getFIFOState)
    checkpoint.save("internalQueue", worker.internalQueue)
    if(worker.dataProcessor.operatorOpened){
      worker.dataProcessor.operator match {
        case support: CheckpointSupport =>
          worker.dataProcessor.outputIterator.setTupleOutput(
            support.serializeState(worker.dataProcessor.outputIterator.outputIter, checkpoint)
          )
        case _ =>
      }
    }
    checkpoint.save("dataProcessor", worker.dataProcessor)
    System.currentTimeMillis() - startTime
  }

  override protected def doCheckpointDuringReplay(pendingCheckpoint: PendingCheckpoint, conf: ReplayCheckpointConfig): () => Unit = {
    () => {
      // now inside DP thread
      worker.dpThread.dpInterrupted {
        logger.info(s"taking checkpoint during replay at step ${worker.dataProcessor.cursor.getStep}")
        pendingCheckpoint.recordingLock.lock()
        worker.dataProcessor.outputPort.broadcastMarker(TakeRuntimeGlobalCheckpoint(conf.id, Map.empty))
        fillCheckpoint(pendingCheckpoint.chkpt)
        pendingCheckpoint.checkpointDone = true
        pendingCheckpoint.recordingLock.unlock()
      }
    }
  }

  override protected def startProcessing(stateReloaded:Boolean, replayOrderEnforcer: ReplayOrderEnforcer): Unit = {
    logger.info(s"worker restored! input Seq: ${worker.inputPort.getFIFOState}")
    logger.info(s"worker restored! output Seq: ${worker.dataProcessor.outputPort.getFIFOState}")
    worker.dpThread = new DPThread(actorId, worker.dataProcessor, worker.internalQueue, replayOrderEnforcer)
    logger.info("starting new DP thread...")
    assert(worker.internalQueue.isInstanceOf[RecoveryInternalQueueImpl])
    worker.dpThread.start() // new DP is not started yet.
  }
}
