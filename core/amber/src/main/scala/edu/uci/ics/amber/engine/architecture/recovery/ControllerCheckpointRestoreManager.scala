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
import edu.uci.ics.amber.engine.common.ambermessage.ClientEvent.{ReplayCompleted, RuntimeCheckpointCompleted, WorkflowStatusUpdate}


class ControllerCheckpointRestoreManager(@transient controller:Controller) extends CheckpointRestoreManager(controller) {

  @transient var replayConfig: WorkflowReplayConfig = _

  override def onCheckpointCompleted(pendingCheckpoint: PendingCheckpoint): Unit ={
    val stats = finalizeCheckpoint(pendingCheckpoint)
    controller.controlProcessor.outputPort.sendToClient(RuntimeCheckpointCompleted(actorId, pendingCheckpoint.logicalSnapshotId,pendingCheckpoint.checkpointId, stats))
  }

  def killAllExistingWorkers(): Unit = {
    // kill all existing workers
    controller.controlProcessor.execution.getAllWorkers.foreach {
      worker =>
        val workerRef = controller.controlProcessor.execution.getOperatorExecution(worker).getWorkerInfo(worker).ref
        workerRef ! PoisonPill
    }
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
            Array(LoadStateAndReplay("replay - restart", conf.fromCheckpoint, conf.replayTo, conf.checkpointConfig))
          )
      }.toSeq))
  }

  override def overwriteState(chkpt: SavedCheckpoint): Unit = {
    killAllExistingWorkers()
    controller.inputPort.setFIFOState(chkpt.load("fifoState"))
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
    checkpoint.chkpt.save("fifoState", controller.inputPort.getFIFOState)
    checkpoint.chkpt.save("controlState", controller.controlProcessor)
    System.currentTimeMillis() - startTime
  }

  override def doCheckpointDuringReplay(pendingCheckpoint: PendingCheckpoint, conf: ReplayCheckpointConfig): () => Unit = {
    () => {
      controller.controlProcessor.outputPort.broadcastMarker(TakeRuntimeGlobalCheckpoint(conf.checkpointId, Map.empty))
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
      controller.controlProcessor.asyncRPCClient.sendToClient(WorkflowStatusUpdate(controller.controlProcessor.execution.getWorkflowStatus))
      controller.controlProcessor.outputPort.sendToClient(ReplayCompleted(actorId, replayId))
    }
  }
}
