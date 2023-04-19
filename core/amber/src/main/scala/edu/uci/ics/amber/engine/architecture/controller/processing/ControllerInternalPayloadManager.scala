package edu.uci.ics.amber.engine.architecture.controller.processing

import akka.serialization.SerializationExtension
import edu.uci.ics.amber.engine.architecture.checkpoint.{CheckpointHolder, SavedCheckpoint}
import edu.uci.ics.amber.engine.architecture.controller.Controller
import edu.uci.ics.amber.engine.architecture.logging.StepsOnChannel
import edu.uci.ics.amber.engine.architecture.recovery.{ControllerReplayQueue, InternalPayloadManager, PendingCheckpoint, ReplayOrderEnforcer}
import edu.uci.ics.amber.engine.architecture.recovery.InternalPayloadManager._
import edu.uci.ics.amber.engine.common.AmberLogging
import edu.uci.ics.amber.engine.common.ambermessage.{AmberInternalPayload, ChannelEndpointID, IdempotentInternalPayload, MarkerAlignmentInternalPayload, MarkerCollectionSupport, OneTimeInternalPayload}
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity
import edu.uci.ics.amber.engine.common.virtualidentity.util.CLIENT

import scala.collection.mutable

class ControllerInternalPayloadManager(controller:Controller) extends InternalPayloadManager with AmberLogging{

  override def actorId: ActorVirtualIdentity = controller.actorId

  override def handlePayload(payload: OneTimeInternalPayload): Unit = {
    payload match {
      case LoadStateAndReplay(id, fromCheckpoint, replayTo, chkpts) =>
        var chkpt:SavedCheckpoint = null
        if(fromCheckpoint.isDefined){
          chkpt = CheckpointHolder.getCheckpoint(controller.actorId, fromCheckpoint.get)
          chkpt.attachSerialization(SerializationExtension(controller.context.system))
          controller.inputPort.setFIFOState(chkpt.load("fifoState"))
          controller.controlProcessor = chkpt.load("controlState")
          controller.controlProcessor.initCP(controller.workflow, controller.controllerConfig, controller.scheduler, controller.getAvailableNodes, controller.inputPort, controller.context, controller.logManager)

        }
        val replayOrderEnforcer = setupReplay()
        if(replayTo.isDefined){
          replayOrderEnforcer.setReplayTo(controller.controlProcessor.determinantLogger.getStep, replayTo.get,  () => {
            // TODO: notify client about replay completed
          })
        }else{
          replayOrderEnforcer.setRecovery(() => {
            controller.replayQueue = null
            // TODO: notify client about replay completed
          })
        }
        // TODO: notify client about replay started
        if(chkpt != null){
          chkpt.getInputData.foreach{
            case (c, payloads) =>
              payloads.foreach(x => controller.inputPort.handleFIFOPayload(c, x))
          }
        }
      case EstimateCheckpointCost(id) =>
        controller.controlProcessor.outputPort.broadcastMarker(payload)
        val stats = CheckpointStats(
          id,
          controller.inputPort.getFIFOState,
          controller.controlProcessor.outputPort.getFIFOState,
          controller.controlProcessor.determinantLogger.getStep,
          0)
        controller.controlProcessor.outputPort.sendTo(CLIENT, EstimationCompleted(id, stats))
      case _ => ???
    }
  }


  def setupReplay(): ReplayOrderEnforcer ={
    val replayOrderEnforcer = new ReplayOrderEnforcer(controller.logStorage.getReader.getLogs[StepsOnChannel])
    val currentStep = controller.controlProcessor.determinantLogger.getStep
    replayOrderEnforcer.initialize(currentStep)
    controller.replayQueue = new ControllerReplayQueue(controller.controlProcessor, replayOrderEnforcer, controller.controlProcessor.processControlPayload)
    replayOrderEnforcer
  }

  override def handlePayload(channel: ChannelEndpointID, idempotentInternalPayload: IdempotentInternalPayload): Unit ={
    idempotentInternalPayload match {
      case SetupLogging() =>
        InternalPayloadManager.setupLoggingForWorkflowActor(controller)
      case _ => ???
    }

  }

  override def markerAlignmentStart(markerAlignmentInternalPayload: MarkerAlignmentInternalPayload): MarkerCollectionSupport = {
    markerAlignmentInternalPayload match {
      case TakeCheckpoint(id, _) =>
        logger.info("start to take global checkpoint")
        val startTime = System.currentTimeMillis()
        val chkpt = new SavedCheckpoint()
        chkpt.attachSerialization(SerializationExtension(controller.context.system))
        chkpt.save("fifoState", controller.inputPort.getFIFOState)
        chkpt.save("controlState", controller.controlProcessor)
        val toAlign = new mutable.HashSet[ChannelEndpointID]
        val markerCollectionCountMap = controller.controlProcessor.execution.getAllWorkers.map{
          worker =>
            toAlign.add(ChannelEndpointID(worker, true))
            val mutableSet = controller.controlProcessor.execution.getOperatorExecution(worker).getWorkerInfo(worker).upstreamChannels
            worker -> mutableSet.toSet
        }.toMap
        controller.controlProcessor.outputPort.broadcastMarker(TakeCheckpoint(id, markerCollectionCountMap))
        val numControlSteps = controller.controlProcessor.determinantLogger.getStep
        new PendingCheckpoint(actorId, startTime, numControlSteps, chkpt, toAlign.toSet)
      case _ => ???
    }
  }

  override def markerAlignmentEnd(markerAlignmentInternalPayload: MarkerAlignmentInternalPayload, support: MarkerCollectionSupport): Unit = {
    markerAlignmentInternalPayload match {
      case TakeCheckpoint(id, _) =>
        val pendingCheckpoint = support.asInstanceOf[PendingCheckpoint]
        CheckpointHolder.addCheckpoint(controller.actorId, pendingCheckpoint.stepCursorAtCheckpoint, pendingCheckpoint.chkpt)
      case _ => ???
    }
  }
}
