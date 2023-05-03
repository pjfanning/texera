package edu.uci.ics.amber.engine.architecture.recovery

import edu.uci.ics.amber.engine.architecture.common.WorkflowActor
import edu.uci.ics.amber.engine.architecture.controller.WorkflowReplayConfig
import edu.uci.ics.amber.engine.architecture.logging.{DeterminantLoggerImpl, EmptyDeterminantLogger, EmptyLogManagerImpl, LogManagerImpl}
import edu.uci.ics.amber.engine.architecture.logging.storage.{DeterminantLogStorage, EmptyLogStorage, LocalFSLogStorage}
import edu.uci.ics.amber.engine.architecture.worker.ReplayCheckpointConfig
import edu.uci.ics.amber.engine.common.ambermessage._
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity

import scala.collection.mutable


object InternalPayloadManager{

  // command that has no effect:
  case class NoOp() extends IdempotentInternalPayload

  // estimation related:
  case class EstimateCheckpointCost(id:String) extends OneTimeInternalPayload

  final case class CheckpointStats(step:Long, inputWatermarks: Map[ChannelEndpointID, Long],
                                   outputWatermarks: Map[ChannelEndpointID, Long],
                                   alignmentCost: Long,
                                   saveStateCost: Long)

  // replay related:
  case class LoadStateAndReplay(id:String, checkpointStep:Option[Long], replayTo:Option[Long], checkpointConfigs:Array[ReplayCheckpointConfig]) extends OneTimeInternalPayload
  case class ContinueReplay(id:String, replayTo: Option[Long], checkpointConfigs:Array[ReplayCheckpointConfig]) extends OneTimeInternalPayload
  case class PauseReplay(id:String) extends OneTimeInternalPayload

  // runtime fault-tolerance:
  case class SetupLogging() extends IdempotentInternalPayload

  def setupLoggingForWorkflowActor(actor:WorkflowActor, loggingEnabled:Boolean): Unit ={
    if(loggingEnabled){
      actor.determinantLogger = new DeterminantLoggerImpl()
      actor.logManager = new LogManagerImpl(actor.networkCommunicationActor, actor.determinantLogger)
      actor.logStorage = new LocalFSLogStorage(actor.getLogName)
      actor.logStorage.cleanPartiallyWrittenLogFile()
      actor.logManager.setupWriter(actor.logStorage.getWriter)
    }else{
      actor.logStorage = new EmptyLogStorage()
      actor.determinantLogger = new EmptyDeterminantLogger()
      actor.logManager = new EmptyLogManagerImpl(actor.networkCommunicationActor)
    }
  }

  def retrieveLogForWorkflowActor(actor:WorkflowActor):DeterminantLogStorage.DeterminantLogReader = {
    new LocalFSLogStorage(actor.getLogName).getReader
  }

  // checkpoint related:
  case class TakeRuntimeGlobalCheckpoint(id:String, alignmentMap:Map[ActorVirtualIdentity, Set[ChannelEndpointID]]) extends MarkerAlignmentInternalPayload

  // controller replay workflow:
  case class ReplayWorkflow(id:String, replayConfig: WorkflowReplayConfig) extends OneTimeInternalPayload

}

class EmptyInternalPayloadManager extends InternalPayloadManager{
  override def handlePayload(oneTimeInternalPayload: OneTimeInternalPayload): Unit = {}

  override def handlePayload(channel: ChannelEndpointID, idempotentInternalPayload: IdempotentInternalPayload): Unit = {}

  override def markerAlignmentStart(channel:ChannelEndpointID, markerAlignmentInternalPayload: MarkerAlignmentInternalPayload, pendingCollections: mutable.HashMap[String, MarkerCollectionSupport]): Unit = {}
}


abstract class InternalPayloadManager {

  protected val pending:mutable.HashMap[String, MarkerCollectionSupport] = mutable.HashMap[String, MarkerCollectionSupport]()
  private val seen = mutable.HashSet[String]()

  def handlePayload(oneTimeInternalPayload: OneTimeInternalPayload):Unit

  def handlePayload(channel:ChannelEndpointID, idempotentInternalPayload: IdempotentInternalPayload):Unit

  def markerAlignmentStart(channel: ChannelEndpointID, markerAlignmentInternalPayload: MarkerAlignmentInternalPayload, pendingCollections: mutable.HashMap[String, MarkerCollectionSupport]):Unit

  def inputMarker(channel: ChannelEndpointID, payload:AmberInternalPayload):Unit = {
    payload match {
      case ip: IdempotentInternalPayload =>
        handlePayload(channel, ip)
      case op: OneTimeInternalPayload =>
        if(!seen.contains(op.id)){
          seen.add(op.id)
          handlePayload(op)
        }
      case mp: MarkerAlignmentInternalPayload =>
        if(!pending.contains(mp.id)){
          markerAlignmentStart(channel, mp, pending)
        }else {
          pending(mp.id).onReceiveMarker(channel)
          if (pending(mp.id).isNoLongerPending) {
            pending.remove(mp.id)
          }
        }
    }
  }

  def inputPayload(channel:ChannelEndpointID, payload: WorkflowFIFOMessagePayload): Unit ={
    pending.foreach{
      case (id, marker) =>
        marker.onReceivePayload(channel, payload)
    }
  }
}
