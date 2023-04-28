package edu.uci.ics.amber.engine.architecture.recovery

import edu.uci.ics.amber.engine.architecture.checkpoint.SavedCheckpoint
import edu.uci.ics.amber.engine.architecture.logging.StepsOnChannel
import edu.uci.ics.amber.engine.architecture.logging.storage.DeterminantLogStorage.DeterminantLogReader
import edu.uci.ics.amber.engine.architecture.recovery.InternalPayloadManager.TakeRuntimeGlobalCheckpoint
import edu.uci.ics.amber.engine.architecture.worker.processing.DPThread
import edu.uci.ics.amber.engine.architecture.worker.{ReplayCheckpointConfig, WorkerInternalQueue, WorkerInternalQueueImpl, WorkflowWorker}
import edu.uci.ics.amber.engine.common.CheckpointSupport
import edu.uci.ics.amber.engine.common.ambermessage.{ChannelEndpointID, WorkflowFIFOMessagePayload}
import edu.uci.ics.amber.engine.common.ambermessage.ClientEvent.ReplayCompleted
import edu.uci.ics.amber.engine.common.tuple.ITuple

import scala.collection.mutable

class WorkerCheckpointRestoreManager(@transient worker:WorkflowWorker) extends CheckpointRestoreManager(worker) {

  override def overwriteState(chkpt:SavedCheckpoint):Unit = {
    var outputIter:Iterator[(ITuple, Option[Int])] = Iterator.empty
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
    if(replayTo.isDefined){
      val currentStep = worker.dataProcessor.cursor.getStep
      replayOrderEnforcer.setReplayTo(currentStep, replayTo.get,  () => {
        logger.info("replay completed, waiting for next instruction")
        worker.dpThread.blockingOnNextStep()
        worker.dataProcessor.outputPort.sendToClient(ReplayCompleted(actorId, replayId))
      })
      worker.dpThread.unblock() // in case it is blocked.
    }
    worker.internalQueue = new RecoveryInternalQueueImpl(worker.actorId, worker.creditMonitor, replayOrderEnforcer)
    replayOrderEnforcer
  }

  override def fillCheckpoint(checkpoint: PendingCheckpoint): Long = {
    val startTime = System.currentTimeMillis()
    logger.info("start to take checkpoint")
    val alreadyAligned = checkpoint.aligned
    worker.internalQueue.getAllMessages.foreach{
      case (d, messages) =>
        if(!alreadyAligned.contains(d)){
          messages.foreach(x => checkpoint.chkpt.addInputData(d, x.payload.asInstanceOf[WorkflowFIFOMessagePayload]))
        }
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
      worker.dpThread.dpInterrupted {
        logger.info(s"taking checkpoint during replay at step ${worker.dataProcessor.cursor.getStep}")
        pendingCheckpoint.recordingLock.lock()
        pendingCheckpoint.fifoOutputState = worker.dataProcessor.outputPort.getFIFOState
        worker.dataProcessor.outputPort.broadcastMarker(TakeRuntimeGlobalCheckpoint(conf.id, Map.empty))
        fillCheckpoint(pendingCheckpoint)
        pendingCheckpoint.checkpointDone = true
        pendingCheckpoint.recordingLock.unlock()
      }
    }
  }

  override def startProcessing(stateReloaded:Boolean, replayOrderEnforcer: ReplayOrderEnforcer): Unit = {
    logger.info(s"worker restored! input Seq: ${worker.inputPort.getFIFOState}")
    logger.info(s"worker restored! output Seq: ${worker.dataProcessor.outputPort.getFIFOState}")
    worker.dpThread = new DPThread(actorId, worker.dataProcessor, worker.internalQueue, replayOrderEnforcer)
    logger.info("starting new DP thread...")
    assert(worker.internalQueue.isInstanceOf[RecoveryInternalQueueImpl])
    worker.dpThread.start() // new DP is not started yet.
  }
}
