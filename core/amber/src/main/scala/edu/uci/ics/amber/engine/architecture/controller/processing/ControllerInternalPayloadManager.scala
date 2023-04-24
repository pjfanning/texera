package edu.uci.ics.amber.engine.architecture.controller.processing

import akka.actor.PoisonPill
import akka.pattern.ask
import akka.remote.transport.ActorTransportAdapter.AskTimeout
import akka.serialization.SerializationExtension
import edu.uci.ics.amber.engine.architecture.checkpoint.{CheckpointHolder, SavedCheckpoint}
import edu.uci.ics.amber.engine.architecture.common.WorkflowActor.CheckInitialized
import edu.uci.ics.amber.engine.common.ambermessage.ClientEvent.{EstimationCompleted, ReplayCompleted, RuntimeCheckpointCompleted, WorkflowStatusUpdate}
import edu.uci.ics.amber.engine.architecture.controller.{Controller, WorkflowReplayConfig}
import edu.uci.ics.amber.engine.architecture.deploysemantics.locationpreference.AddressInfo
import edu.uci.ics.amber.engine.architecture.logging.storage.DeterminantLogStorage.DeterminantLogReader
import edu.uci.ics.amber.engine.architecture.logging.{RecordedPayload, StepsOnChannel}
import edu.uci.ics.amber.engine.architecture.recovery.{ControllerReplayQueue, InternalPayloadManager, PendingCheckpoint, ReplayOrderEnforcer}
import edu.uci.ics.amber.engine.architecture.recovery.InternalPayloadManager._
import edu.uci.ics.amber.engine.architecture.worker.ReplayCheckpointConfig
import edu.uci.ics.amber.engine.common.AmberLogging
import edu.uci.ics.amber.engine.common.ambermessage.{ChannelEndpointID, IdempotentInternalPayload, MarkerAlignmentInternalPayload, MarkerCollectionSupport, OneTimeInternalPayload, WorkflowFIFOMessage}
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity
import edu.uci.ics.amber.engine.common.virtualidentity.util.CLIENT

import scala.collection.mutable
import scala.concurrent.duration.DurationInt

class ControllerInternalPayloadManager(controller:Controller) extends InternalPayloadManager with AmberLogging{

  override def actorId: ActorVirtualIdentity = controller.actorId


  override def handlePayload(channel: ChannelEndpointID, idempotentInternalPayload: IdempotentInternalPayload): Unit ={
    idempotentInternalPayload match {
      case SetupLogging() =>
        InternalPayloadManager.setupLoggingForWorkflowActor(controller, true)
      case NoOp() =>
        // no op
      case _ => ???
    }

  }

  def killAllExistingWorkers(): Unit ={
    // kill all existing workers
    controller.controlProcessor.execution.getAllWorkers.foreach{
      worker =>
        val workerRef = controller.controlProcessor.execution.getOperatorExecution(worker).getWorkerInfo(worker).ref
        workerRef ! PoisonPill
    }
  }

  def loadFromCheckpoint(id:String, fromCheckpoint:Option[Long], replayConfig: WorkflowReplayConfig): SavedCheckpoint ={
    var chkpt:SavedCheckpoint = null
    if(fromCheckpoint.isDefined){
      chkpt = CheckpointHolder.getCheckpoint(controller.actorId, fromCheckpoint.get)
      chkpt.attachSerialization(SerializationExtension(controller.context.system))
      controller.inputPort.setFIFOState(chkpt.load("fifoState"))
      controller.controlProcessor = chkpt.load("controlState")
      controller.controlProcessor.initCP(controller)
      val futures = controller.controlProcessor.execution.getAllWorkers
        .map { worker =>
          (worker, controller.controlProcessor.workflow
            .getOperator(worker)
            .buildWorker(
              worker,
              AddressInfo(controller.getAvailableNodes(), controller.actorService.self.path.address),
              controller.actorService,
              controller.controlProcessor.execution.getOperatorExecution(worker)
            ))
        }
        .map { case (worker, ref) =>
          val conf = replayConfig.confs(worker)
          ref ? CheckInitialized(Array(LoadStateAndReplay(id, conf.fromCheckpoint, conf.replayTo, conf.checkpointConfig)))
        }.toSeq
      controller.actorService.waitUntil(600.seconds,futures)
    }
    chkpt
  }


  def addRecordedMessages(logReader: DeterminantLogReader): Unit ={
    // add recorded payload back to the queue from log
    val recoveredSeqMap = mutable.HashMap[ChannelEndpointID, Long]()
    logReader.getLogs[RecordedPayload].foreach(elem => {
      val seqNum = recoveredSeqMap.getOrElseUpdate(elem.channel, 0L)
      val message = WorkflowFIFOMessage(elem.channel, seqNum, elem.payload)
      controller.inputPort.handleMessage(message)
      recoveredSeqMap(elem.channel) += 1
    })
  }


  def setupReplay(id:String, replayTo:Option[Long]): ReplayOrderEnforcer ={
    val logReader = InternalPayloadManager.retrieveLogForWorkflowActor(controller)
    val replayOrderEnforcer = new ReplayOrderEnforcer(logReader.getLogs[StepsOnChannel], () => {
      logger.info("recovery completed, continue normal processing")
      controller.replayQueue = null
    })
    val currentStep = controller.controlProcessor.cursor.getStep
    replayOrderEnforcer.initialize(currentStep)
    controller.replayQueue = new ControllerReplayQueue(controller.controlProcessor, replayOrderEnforcer, controller.controlProcessor.processControlPayload)
    if(replayTo.isDefined){
      replayOrderEnforcer.setReplayTo(controller.controlProcessor.cursor.getStep, replayTo.get,  () => {
        logger.info("replay completed, waiting for next instruction")
        controller.controlProcessor.asyncRPCClient.sendToClient(WorkflowStatusUpdate(controller.controlProcessor.execution.getWorkflowStatus))
        controller.controlProcessor.outputPort.sendToClient(ReplayCompleted(actorId, id))
      })
    }
    addRecordedMessages(logReader)
    replayOrderEnforcer
  }

  def setupCheckpointsDuringReplay(replayOrderEnforcer: ReplayOrderEnforcer, confs:Array[ReplayCheckpointConfig]): Unit ={
    // setup checkpoints during replay
    // create empty checkpoints to fill
    confs.foreach(conf => {
      val planned = new SavedCheckpoint()
      planned.attachSerialization(SerializationExtension(controller.context.system))
      val pendingCheckpoint = new PendingCheckpoint(
        controller.actorId,
        0,
        conf.checkpointAt,
        null,
        null,
        0,
        planned,
        conf.waitingForMarker)
      addPendingCheckpoint(conf.id, pendingCheckpoint)
      replayOrderEnforcer.setCheckpoint(conf.checkpointAt, () =>{
        controller.controlProcessor.outputPort.broadcastMarker(TakeRuntimeGlobalCheckpoint(conf.id, Map.empty))
        fillCheckpoint(planned)
      })
    })
  }


  def restoreInputs(chkpt:SavedCheckpoint): Unit ={
    if(chkpt != null){
      chkpt.getInputData.foreach{
        case (c, payloads) =>
          payloads.foreach(x => controller.inputPort.handleFIFOPayload(c, x))
      }
    }
  }

  override def handlePayload(payload: OneTimeInternalPayload): Unit = {
    payload match {
      case EstimateCheckpointCost(id) =>
        controller.controlProcessor.outputPort.broadcastMarker(payload)
        val stats = CheckpointStats(
          controller.controlProcessor.cursor.getStep,
          controller.inputPort.getFIFOState,
          controller.controlProcessor.outputPort.getFIFOState,
          0,
          0)
        controller.controlProcessor.outputPort.sendToClient(EstimationCompleted(actorId, id, stats))
      case ReplayWorkflow(id, replayConfig) =>
        val controllerReplayConf = replayConfig.confs(actorId)
        killAllExistingWorkers()
        val chkpt = loadFromCheckpoint(id, controllerReplayConf.fromCheckpoint, replayConfig)
        val replayOrderEnforcer = setupReplay(id,controllerReplayConf.replayTo)
        setupCheckpointsDuringReplay(replayOrderEnforcer, controllerReplayConf.checkpointConfig)
        // disable logging
        InternalPayloadManager.setupLoggingForWorkflowActor(controller, false)
        restoreInputs(chkpt)
        replayOrderEnforcer.forwardReplayProcess(controller.controlProcessor.cursor.getStep)
      case _ => ???
    }
  }

  def fillCheckpoint(chkpt: SavedCheckpoint): Long ={
    val startTime = System.currentTimeMillis()
    chkpt.save("fifoState", controller.inputPort.getFIFOState)
    chkpt.save("controlState", controller.controlProcessor)
    System.currentTimeMillis() - startTime
  }

  override def markerAlignmentStart(markerAlignmentInternalPayload: MarkerAlignmentInternalPayload): MarkerCollectionSupport = {
    markerAlignmentInternalPayload match {
      case TakeRuntimeGlobalCheckpoint(id, _) =>
        logger.info("start to take global checkpoint")
        val toAlign = new mutable.HashSet[ChannelEndpointID]
        val markerCollectionCountMap = controller.controlProcessor.execution.getAllWorkers.map{
          worker =>
            toAlign.add(ChannelEndpointID(worker, true))
            val mutableSet = controller.controlProcessor.execution.getOperatorExecution(worker).getWorkerInfo(worker).upstreamChannels
            worker -> mutableSet.toSet
        }.toMap
        controller.controlProcessor.outputPort.broadcastMarker(TakeRuntimeGlobalCheckpoint(id, markerCollectionCountMap))
        val chkpt = new SavedCheckpoint()
        chkpt.attachSerialization(SerializationExtension(controller.context.system))
        val elapsed = fillCheckpoint(chkpt)
        val numControlSteps = controller.controlProcessor.cursor.getStep
        new PendingCheckpoint(
          actorId,
          System.currentTimeMillis(),
          numControlSteps,controller.inputPort.getFIFOState,
          controller.controlProcessor.outputPort.getFIFOState,
          elapsed, chkpt, toAlign.toSet)
      case _ => ???
    }
  }

  override def markerAlignmentEnd(markerAlignmentInternalPayload: MarkerAlignmentInternalPayload, support: MarkerCollectionSupport): Unit = {
    markerAlignmentInternalPayload match {
      case TakeRuntimeGlobalCheckpoint(id, _) =>
        val pendingCheckpoint = support.asInstanceOf[PendingCheckpoint]
        CheckpointHolder.addCheckpoint(
          actorId,
          pendingCheckpoint.stepCursorAtCheckpoint,
          pendingCheckpoint.chkpt
        )
        logger.info(
          s"local checkpoint completed! initial time spent = ${pendingCheckpoint.initialCheckpointTime / 1000f}s alignment time = ${(System.currentTimeMillis() - pendingCheckpoint.startTime) / 1000f}s"
        )
        val alignmentCost = System.currentTimeMillis() - pendingCheckpoint.startTime
        val stats = CheckpointStats(
          pendingCheckpoint.stepCursorAtCheckpoint,
          pendingCheckpoint.fifoInputState,
          pendingCheckpoint.fifoOutputState,
          alignmentCost,
          pendingCheckpoint.initialCheckpointTime)
        controller.controlProcessor.outputPort.sendToClient(RuntimeCheckpointCompleted(actorId, id, stats))
      case _ => ???
    }
  }
}
