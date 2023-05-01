package edu.uci.ics.amber.engine.architecture.recovery

import akka.actor.PoisonPill
import com.twitter.util.{Await, Future}
import edu.uci.ics.amber.engine.architecture.checkpoint.{CheckpointHolder, SavedCheckpoint}
import edu.uci.ics.amber.engine.architecture.controller.{Controller, WorkflowReplayConfig}
import edu.uci.ics.amber.engine.architecture.deploysemantics.locationpreference.AddressInfo
import edu.uci.ics.amber.engine.architecture.logging.StepsOnChannel
import edu.uci.ics.amber.engine.architecture.logging.storage.DeterminantLogStorage
import edu.uci.ics.amber.engine.architecture.recovery.InternalPayloadManager.{CheckpointStats, LoadStateAndReplay, TakeRuntimeGlobalCheckpoint}
import edu.uci.ics.amber.engine.architecture.worker.ReplayCheckpointConfig
import edu.uci.ics.amber.engine.common.ambermessage.{ChannelEndpointID, InternalChannelEndpointID, WorkflowFIFOMessagePayload}
import edu.uci.ics.amber.engine.common.ambermessage.ClientEvent.{ReplayCompleted, RuntimeCheckpointCompleted, WorkflowStatusUpdate}

import scala.collection.mutable

class ControllerCheckpointRestoreManager(@transient controller:Controller) extends CheckpointRestoreManager(controller) {

  @transient var replayConfig: WorkflowReplayConfig = _

  override def onCheckpointCompleted(pendingCheckpoint: PendingCheckpoint): Unit ={
    val stats = finalizeCheckpoint(pendingCheckpoint)
    controller.controlProcessor.outputPort.sendToClient(RuntimeCheckpointCompleted(actorId, pendingCheckpoint.checkpointId,pendingCheckpoint.markerId, stats))
  }

  def killAllExistingWorkers(): Unit = {
    // kill all existing workers
    controller.controlProcessor.execution.getAllWorkers.foreach {
      worker =>
        val workerRef = controller.controlProcessor.execution.getOperatorExecution(worker).getWorkerInfo(worker).ref
        workerRef ! PoisonPill
    }
  }

  def getProjectedProcessedCountForMarker(channel:ChannelEndpointID): Long ={
    var existing = controller.controlProcessor.processedPayloadCountMap.getOrElse(channel, 0L)
    if(controller.replayQueue != null){
      existing += controller.replayQueue.getQueuedMessageCount(channel)
    }
    existing
  }

  def restoreWorkers(): Unit = {
    Await.result(Future.collect(controller.controlProcessor.execution.getAllWorkers
      .map { worker =>
        val conf = replayConfig.confs(worker)
        controller.controlProcessor.workflow
          .getOperator(worker)
          .buildWorker(
            worker,
            AddressInfo(controller.getAvailableNodes(), controller.actorService.self.path.address),
            controller.actorService,
            controller.controlProcessor.execution.getOperatorExecution(worker),
            Array(LoadStateAndReplay("replay - restart", conf.fromCheckpoint, conf.inputSeqMap, conf.replayTo, conf.checkpointConfig))
          )
      }.toSeq))
  }

  override def overwriteState(chkpt: SavedCheckpoint): Unit = {
    killAllExistingWorkers()
    controller.controlProcessor = chkpt.load("controlState")
    logger.info("cp restored")
    controller.controlProcessor.initCP(controller)
    controller.controlProcessor.replayPlan = replayConfig
    restoreWorkers()
  }

  override def setupReplay(replayId: String, logReader: DeterminantLogStorage.DeterminantLogReader, replayTo: Option[Long]): ReplayOrderEnforcer = {
    val replayOrderEnforcer = new ReplayOrderEnforcer(logReader.getLogs[StepsOnChannel], () => {
      logger.info("recovery completed, continue normal processing")
      controller.controlProcessor.outputPort.sendToClient(ReplayCompleted(actorId, replayId))
      controller.replayQueue = null
    })
    val currentStep = controller.controlProcessor.cursor.getStep
    replayOrderEnforcer.initialize(currentStep)
    if (replayTo.isDefined) {
      replayOrderEnforcer.setReplayTo(controller.controlProcessor.cursor.getStep, replayTo.get)
    }
    controller.replayQueue = new ControllerReplayQueue(controller.controlProcessor, replayOrderEnforcer, controller.controlProcessor.processControlPayload)
    replayOrderEnforcer
  }

  override def fillCheckpoint(checkpoint: PendingCheckpoint): Long = {
    val startTime = System.currentTimeMillis()
    val markerCountMap = checkpoint.markerProcessedCountMap
    val processedCountMap = controller.controlProcessor.processedPayloadCountMap
    if(controller.replayQueue != null){
      controller.replayQueue.getAllMessages.foreach{
        case (d, messages) =>
          if(markerCountMap.contains(d)){
            val debt = markerCountMap(d) - processedCountMap.getOrElse(d, 0L)
            if(debt > 0) {
              messages.take(debt.toInt).foreach(x => checkpoint.chkpt.addInputData(d, x))
            }
          }else if(d != InternalChannelEndpointID){
            messages.foreach(x => checkpoint.chkpt.addInputData(d, x))
          }
      }
    }
    checkpoint.chkpt.save("controlState", controller.controlProcessor)
    System.currentTimeMillis() - startTime
  }

  override def doCheckpointDuringReplay(pendingCheckpoint: PendingCheckpoint, conf: ReplayCheckpointConfig): () => Unit = {
    () => {
      controller.controlProcessor.outputPort.broadcastMarker(TakeRuntimeGlobalCheckpoint(conf.id, Map.empty))
      pendingCheckpoint.fifoOutputState = controller.controlProcessor.outputPort.getFIFOState
      fillCheckpoint(pendingCheckpoint)
      pendingCheckpoint.checkpointDone = true
      pendingCheckpoint.checkCompletion()
    }
  }

  override def startProcessing(stateReloaded: Boolean, replayOrderEnforcer: ReplayOrderEnforcer): Unit = {
    logger.info(s"controller restored! input Seq: ${controller.inputPort.getFIFOState}")
    logger.info(s"controller restored! output Seq: ${controller.controlProcessor.outputPort.getFIFOState}")
    assert(controller.replayQueue != null)
    controller.replayQueue.triggerReplay()
  }

  override protected def replayCompletedCallback(replayId:String): () => Unit = {
    () => {
      logger.info("replay completed, waiting for next instruction")
      controller.controlProcessor.outputPort.sendToClient(ReplayCompleted(actorId, replayId))
    }
  }
}
