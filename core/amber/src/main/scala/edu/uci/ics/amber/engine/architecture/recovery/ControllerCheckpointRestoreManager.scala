package edu.uci.ics.amber.engine.architecture.recovery

import akka.actor.PoisonPill
import com.twitter.util.{Await, Future}
import edu.uci.ics.amber.engine.architecture.checkpoint.{CheckpointHolder, SavedCheckpoint}
import edu.uci.ics.amber.engine.architecture.common.WorkflowActor.CheckInitialized
import edu.uci.ics.amber.engine.architecture.controller.{Controller, WorkflowReplayConfig}
import edu.uci.ics.amber.engine.architecture.deploysemantics.locationpreference.AddressInfo
import edu.uci.ics.amber.engine.architecture.logging.StepsOnChannel
import edu.uci.ics.amber.engine.architecture.logging.storage.DeterminantLogStorage
import edu.uci.ics.amber.engine.architecture.messaginglayer.NetworkCommunicationActor.RegisterActorRef
import edu.uci.ics.amber.engine.architecture.recovery.InternalPayloadManager.{CheckpointStats, SetupReplay, StartReplay, TakeRuntimeGlobalCheckpoint}
import edu.uci.ics.amber.engine.architecture.worker.ReplayCheckpointConfig
import edu.uci.ics.amber.engine.common.ambermessage.{ChannelEndpointID, ControlPayload}
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
        controller.actorService.registerActorForNetworkCommunication(worker, null)
        workerRef ! PoisonPill
    }
  }

  def restoreWorkers(): Unit = {
    val workerRefs = controller.controlProcessor.execution.getAllWorkers
      .map { worker =>
        replayConfig.confs = replayConfig.confs - worker
        (worker, controller.controlProcessor.workflow
          .getOperator(worker)
          .buildWorker(
            worker,
            AddressInfo(controller.getAvailableNodes(), controller.actorService.self.path.address),
            controller.actorService))
      }
    Thread.sleep(1000)
    logger.info("starting replay...")
    Await.result(Future.collect(workerRefs.map{
      case (worker, ref) =>
        val conf = replayConfig.confs(worker)
        val cmds = Array(SetupReplay("replay - load", conf.fromCheckpoint, conf.replayTo, conf.checkpointConfig))
        val opExecution = controller.controlProcessor.execution.getOperatorExecution(worker)
        controller.actorService.ask(ref, CheckInitialized(cmds)).map {
          _ =>
            println(s"Worker Built! Actor for $worker is at $ref")
            controller.actorService.registerActorForNetworkCommunication(worker, ref)
            opExecution.getWorkerInfo(worker).ref = ref
            ref ! StartReplay("replay - restart")
        }
    }.toSeq))
  }

  override def overwriteState(chkpt: SavedCheckpoint): Unit = {
    killAllExistingWorkers()
    controller.inputPort.setFIFOState(chkpt.load("fifoState"))
    controller.controlProcessor = chkpt.load("controlState")
    controller.replayQueue = chkpt.load("replayQueue")
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
    var queueState:Map[ChannelEndpointID, Iterable[ControlPayload]] = null
    if(controller.replayQueue != null){
      queueState = controller.replayQueue.getAllMessages
    }
    controller.replayQueue = new ControllerReplayQueue(controller.controlProcessor, replayOrderEnforcer, controller.controlProcessor.processControlPayload)
    if(queueState != null){
      controller.replayQueue.loadState(queueState)
    }
    replayOrderEnforcer
  }

  override def fillCheckpoint(checkpoint: PendingCheckpoint): Long = {
    val startTime = System.currentTimeMillis()
    checkpoint.chkpt.save("fifoState", controller.inputPort.getFIFOState)
    checkpoint.chkpt.save("controlState", controller.controlProcessor)
    checkpoint.chkpt.save("replayQueue", controller.replayQueue)
    System.currentTimeMillis() - startTime
  }

  override def doCheckpointDuringReplay(pendingCheckpoint: PendingCheckpoint, conf: ReplayCheckpointConfig): () => Unit = {
    () => {
      logger.info(s"checkpoint controller: input seq = ${controller.inputPort.getFIFOState}")
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
