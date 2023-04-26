package edu.uci.ics.amber.engine.architecture.controller.processing

import akka.actor.PoisonPill
import akka.pattern.ask
import akka.serialization.SerializationExtension
import akka.util.Timeout
import com.twitter.util.{Await, Future}
import edu.uci.ics.amber.engine.architecture.checkpoint.{CheckpointHolder, SavedCheckpoint}
import edu.uci.ics.amber.engine.architecture.common.WorkflowActor.CheckInitialized
import edu.uci.ics.amber.engine.common.ambermessage.ClientEvent.{EstimationCompleted, ReplayCompleted, RuntimeCheckpointCompleted, WorkflowStatusUpdate}
import edu.uci.ics.amber.engine.architecture.controller.{Controller, WorkflowReplayConfig}
import edu.uci.ics.amber.engine.architecture.deploysemantics.locationpreference.AddressInfo
import edu.uci.ics.amber.engine.architecture.logging.storage.DeterminantLogStorage.DeterminantLogReader
import edu.uci.ics.amber.engine.architecture.logging.{RecordedPayload, StepsOnChannel}
import edu.uci.ics.amber.engine.architecture.recovery.{ControllerReplayQueue, InternalPayloadManager, PendingCheckpoint, RecoveryInternalQueueImpl, ReplayOrderEnforcer}
import edu.uci.ics.amber.engine.architecture.recovery.InternalPayloadManager._
import edu.uci.ics.amber.engine.architecture.worker.{ReplayCheckpointConfig, WorkerInternalQueue}
import edu.uci.ics.amber.engine.common.AmberLogging
import edu.uci.ics.amber.engine.common.ambermessage.{ChannelEndpointID, IdempotentInternalPayload, MarkerAlignmentInternalPayload, MarkerCollectionSupport, OneTimeInternalPayload, WorkflowFIFOMessage}
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity
import edu.uci.ics.amber.engine.common.virtualidentity.util.CONTROLLER

import scala.collection.mutable
import scala.concurrent.duration.DurationInt

class ControllerInternalPayloadManager(controller:Controller) extends InternalPayloadManager with AmberLogging{

  override def actorId: ActorVirtualIdentity = controller.actorId

  implicit val initializeTimeout:Timeout = 10.seconds

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
      controller.controlProcessor.replayPlan = replayConfig
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
    chkpt
  }


  def addRecordedPayloadBack(logReader: DeterminantLogReader): Unit ={
    // add recorded payload back to the queue from log
    val recoveredSeqMap = mutable.HashMap[ChannelEndpointID, Long]()
    logReader.getLogs[RecordedPayload].foreach(elem => {
      val seqNum = recoveredSeqMap.getOrElseUpdate(elem.channel, 0L)
      val message = WorkflowFIFOMessage(elem.channel, seqNum, elem.payload)
      controller.inputPort.handleMessage(message)
      recoveredSeqMap(elem.channel) += 1
    })
  }

  def transferQueueContent(chkpt:SavedCheckpoint, replayOrderEnforcer: ReplayOrderEnforcer, excludedChannels:Set[ChannelEndpointID]): Unit ={
    val newQueue = new ControllerReplayQueue(controller.controlProcessor, replayOrderEnforcer, controller.controlProcessor.processControlPayload)
    val oldQueue:ControllerReplayQueue = chkpt.load("replayQueue")
    if(oldQueue != null){
      // in case we have some message left in the old queue
      oldQueue.getAllMessages.foreach(x => {
        if(!excludedChannels.contains(x._1)){
          newQueue.enqueuePayload(x._1,x._2)
        }
      })
    }
    controller.replayQueue = newQueue
  }


  def setupReplay(id:String,logReader: DeterminantLogReader, replayTo:Option[Long]): ReplayOrderEnforcer ={
    val replayOrderEnforcer = new ReplayOrderEnforcer(logReader.getLogs[StepsOnChannel], () => {
      logger.info("recovery completed, continue normal processing")
      controller.replayQueue = null
    })
    val currentStep = controller.controlProcessor.cursor.getStep
    replayOrderEnforcer.initialize(currentStep)
    if(replayTo.isDefined){
      replayOrderEnforcer.setReplayTo(controller.controlProcessor.cursor.getStep, replayTo.get,  () => {
        logger.info("replay completed, waiting for next instruction")
        controller.controlProcessor.asyncRPCClient.sendToClient(WorkflowStatusUpdate(controller.controlProcessor.execution.getWorkflowStatus))
        controller.controlProcessor.outputPort.sendToClient(ReplayCompleted(actorId, id))
      })
    }
    replayOrderEnforcer
  }

  def setupCheckpointsDuringReplay(replayOrderEnforcer: ReplayOrderEnforcer, confs:Array[ReplayCheckpointConfig]): Unit ={
    // setup checkpoints during replay
    // create empty checkpoints to fill
    confs.foreach(conf => {
      val planned = new SavedCheckpoint()
      planned.attachSerialization(SerializationExtension(controller.context.system))
      val pendingCheckpoint = new PendingCheckpoint(
        conf.estimationId,
        controller.actorId,
        0,
        conf.checkpointAt,
        Map.empty,
        Map.empty,
        0,
        planned,
        conf.waitingForMarker)
      addPendingCheckpoint(conf.id, pendingCheckpoint)
      replayOrderEnforcer.setCheckpoint(conf.checkpointAt, () =>{
        controller.controlProcessor.outputPort.broadcastMarker(TakeRuntimeGlobalCheckpoint(conf.id, Map.empty))
        fillCheckpoint(planned)
        pendingCheckpoint.startRecording = true
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
        controller.isReplaying = true
        val controllerReplayConf = replayConfig.confs(actorId)
        killAllExistingWorkers()
        val chkpt = loadFromCheckpoint(id, controllerReplayConf.fromCheckpoint, replayConfig)
        val logReader = InternalPayloadManager.retrieveLogForWorkflowActor(controller)
        val replayOrderEnforcer = setupReplay(id,logReader, controllerReplayConf.replayTo)
        val checkpointedChannels = if(chkpt != null){
          Set[ChannelEndpointID]()
        }else{
          Set[ChannelEndpointID]()
        }
        if(chkpt!= null){
          transferQueueContent(chkpt, replayOrderEnforcer, checkpointedChannels)
        }
        setupCheckpointsDuringReplay(replayOrderEnforcer, controllerReplayConf.checkpointConfig)
        // disable logging
        InternalPayloadManager.setupLoggingForWorkflowActor(controller, false)
        logger.info(s"controller restored! input Seq: ${controller.inputPort.getFIFOState}")
        logger.info(s"controller restored! output Seq: ${controller.controlProcessor.outputPort.getFIFOState}")
        logger.info(s"recorded data: ${chkpt.getInputData.map(x => s"${x._1} -> ${x._2.size}")}")
        restoreInputs(chkpt)
        addRecordedPayloadBack(logReader)
        replayOrderEnforcer.forwardReplayProcess(controller.controlProcessor.cursor.getStep)
      case _ => ???
    }
  }

  def fillCheckpoint(chkpt: SavedCheckpoint): Long ={
    val startTime = System.currentTimeMillis()
    chkpt.save("fifoState", controller.inputPort.getFIFOState)
    chkpt.save("controlState", controller.controlProcessor)
    chkpt.save("replayQueue", controller.replayQueue)
    System.currentTimeMillis() - startTime
  }

  override def markerAlignmentStart(markerAlignmentInternalPayload: MarkerAlignmentInternalPayload): MarkerCollectionSupport = {
    markerAlignmentInternalPayload match {
      case TakeRuntimeGlobalCheckpoint(id, _) =>
        logger.info("start to take global checkpoint")
        val toAlign = mutable.HashSet[ChannelEndpointID](ChannelEndpointID(CONTROLLER, true))
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
        val pending = new PendingCheckpoint(
          id,
          actorId,
          System.currentTimeMillis(),
          numControlSteps,controller.inputPort.getFIFOState,
          controller.controlProcessor.outputPort.getFIFOState,
          elapsed, chkpt, toAlign.toSet)
          pending.startRecording = true
        pending
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
        controller.controlProcessor.outputPort.sendToClient(RuntimeCheckpointCompleted(actorId, pendingCheckpoint.checkpointId, stats))
      case _ => ???
    }
  }
}
