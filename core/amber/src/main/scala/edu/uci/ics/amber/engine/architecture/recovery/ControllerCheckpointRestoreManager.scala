package edu.uci.ics.amber.engine.architecture.recovery

import akka.actor.PoisonPill
import com.twitter.util.{Await, Future}
import edu.uci.ics.amber.engine.architecture.checkpoint.SavedCheckpoint
import edu.uci.ics.amber.engine.architecture.controller.{Controller, WorkflowReplayConfig}
import edu.uci.ics.amber.engine.architecture.deploysemantics.locationpreference.AddressInfo
import edu.uci.ics.amber.engine.architecture.logging.StepsOnChannel
import edu.uci.ics.amber.engine.architecture.logging.storage.DeterminantLogStorage
import edu.uci.ics.amber.engine.architecture.recovery.InternalPayloadManager.{LoadStateAndReplay, TakeRuntimeGlobalCheckpoint}
import edu.uci.ics.amber.engine.architecture.worker.ReplayCheckpointConfig
import edu.uci.ics.amber.engine.common.ambermessage.ChannelEndpointID
import edu.uci.ics.amber.engine.common.ambermessage.ClientEvent.{ReplayCompleted, WorkflowStatusUpdate}

import scala.collection.mutable

class ControllerCheckpointRestoreManager(@transient controller:Controller) extends CheckpointRestoreManager(controller) {

  @transient var replayConfig: WorkflowReplayConfig = _

  def killAllExistingWorkers(): Unit ={
    // kill all existing workers
    controller.controlProcessor.execution.getAllWorkers.foreach{
      worker =>
        val workerRef = controller.controlProcessor.execution.getOperatorExecution(worker).getWorkerInfo(worker).ref
        workerRef ! PoisonPill
    }
  }

  def restoreWorkers(): Unit ={
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
            Array(LoadStateAndReplay("replay - restart",conf.fromCheckpoint, conf.replayTo, conf.checkpointConfig))
          )
      }.toSeq))
  }

  override protected def overwriteState(chkpt: SavedCheckpoint): Unit = {
    killAllExistingWorkers()
    controller.inputPort.setFIFOState(chkpt.load("fifoState"))
    logger.info("fifo state restored")
    controller.controlProcessor = chkpt.load("controlState")
    logger.info("cp restored")
    controller.replayQueue = chkpt.load("replayQueue")
    controller.controlProcessor.initCP(controller)
    controller.controlProcessor.replayPlan = replayConfig
    restoreWorkers()
  }

  override protected def setupReplay(replayId: String, logReader: DeterminantLogStorage.DeterminantLogReader, replayTo: Option[Long]): ReplayOrderEnforcer = {
    val replayOrderEnforcer = new ReplayOrderEnforcer(logReader.getLogs[StepsOnChannel], () => {
      logger.info("recovery completed, continue normal processing")
      controller.controlProcessor.outputPort.sendToClient(ReplayCompleted(actorId, replayId))
      controller.replayQueue = null
    })
    val currentStep = controller.controlProcessor.cursor.getStep
    replayOrderEnforcer.initialize(currentStep)
    if(replayTo.isDefined){
      replayOrderEnforcer.setReplayTo(controller.controlProcessor.cursor.getStep, replayTo.get,  () => {
        logger.info("replay completed, waiting for next instruction")
        controller.controlProcessor.asyncRPCClient.sendToClient(WorkflowStatusUpdate(controller.controlProcessor.execution.getWorkflowStatus))
        controller.controlProcessor.outputPort.sendToClient(ReplayCompleted(actorId, replayId))
      })
    }
    replayOrderEnforcer
  }

  override def fillCheckpoint(checkpoint: SavedCheckpoint): Long = {
    val startTime = System.currentTimeMillis()
    checkpoint.save("fifoState", controller.inputPort.getFIFOState)
    checkpoint.save("controlState", controller.controlProcessor)
    checkpoint.save("replayQueue", controller.replayQueue)
    System.currentTimeMillis() - startTime
  }

  override protected def doCheckpointDuringReplay(pendingCheckpoint: PendingCheckpoint, conf: ReplayCheckpointConfig): () => Unit = {
    () =>{
      controller.controlProcessor.outputPort.broadcastMarker(TakeRuntimeGlobalCheckpoint(conf.id, Map.empty))
      fillCheckpoint(pendingCheckpoint.chkpt)
      pendingCheckpoint.checkpointDone = true
    }
  }

  override protected def startProcessing(stateReloaded: Boolean, replayOrderEnforcer: ReplayOrderEnforcer): Unit = {
    logger.info(s"controller restored! input Seq: ${controller.inputPort.getFIFOState}")
    logger.info(s"controller restored! output Seq: ${controller.controlProcessor.outputPort.getFIFOState}")
    replayOrderEnforcer.forwardReplayProcess(controller.controlProcessor.cursor.getStep)
  }

  override protected def transferQueueContent(orderEnforcer: ReplayOrderEnforcer): Unit = {
    val newQueue = new ControllerReplayQueue(controller.controlProcessor, orderEnforcer, controller.controlProcessor.processControlPayload)
    if (controller.replayQueue != null) {
      // in case we have some message left in the old queue
      controller.replayQueue.getAllMessages.foreach {
        case (channel, messages) =>
          messages.foreach(msg => newQueue.enqueuePayload(channel, msg))
      }
    }
    controller.replayQueue = newQueue
  }
}
