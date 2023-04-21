package edu.uci.ics.amber.engine.architecture.recovery

import edu.uci.ics.amber.engine.architecture.common.WorkflowActor
import edu.uci.ics.amber.engine.architecture.logging.{DeterminantLoggerImpl, LogManagerImpl}
import edu.uci.ics.amber.engine.architecture.logging.storage.LocalFSLogStorage
import edu.uci.ics.amber.engine.architecture.worker.ReplayCheckpointConfig
import edu.uci.ics.amber.engine.common.ambermessage._
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity

import scala.collection.mutable


object InternalPayloadManager{

  // worker lifecycle related:
  case class ShutdownDP() extends IdempotentInternalPayload

  // estimation related:
  case class EstimateCheckpointCost(id:String) extends OneTimeInternalPayload
  case class EstimationCompleted(id:String, checkpointStats: CheckpointStats) extends OneTimeInternalPayload

  final case class CheckpointStats(step:Long, inputWatermarks: Map[ChannelEndpointID, Long],
                                   outputWatermarks: Map[ChannelEndpointID, Long],
                                   alignmentCost: Long,
                                   saveStateCost: Long)

  // replay related:
  case class LoadStateAndReplay(id:String, checkpointStep:Option[Long], replayTo:Option[Long], checkpointConfigs:Array[ReplayCheckpointConfig]) extends OneTimeInternalPayload
  case class ReplayCompleted(id:String) extends OneTimeInternalPayload

  // runtime fault-tolerance:
  case class SetupLogging() extends IdempotentInternalPayload

  def setupLoggingForWorkflowActor(actor:WorkflowActor): Unit ={
    actor.determinantLogger = new DeterminantLoggerImpl()
    actor.logManager = new LogManagerImpl(actor.networkCommunicationActor, actor.determinantLogger)
    actor.logStorage = new LocalFSLogStorage(actor.getLogName)
    actor.logStorage.cleanPartiallyWrittenLogFile()
    actor.logManager.setupWriter(actor.logStorage.getWriter)
  }

  // checkpoint related:
  case class TakeRuntimeGlobalCheckpoint(id:String, alignmentMap:Map[ActorVirtualIdentity, Set[ChannelEndpointID]]) extends MarkerAlignmentInternalPayload
  case class RuntimeCheckpointCompleted(id:String, checkpointStats: CheckpointStats) extends OneTimeInternalPayload
}

class EmptyInternalPayloadManager extends InternalPayloadManager{
  override def handlePayload(oneTimeInternalPayload: OneTimeInternalPayload): Unit = {}

  override def handlePayload(channel: ChannelEndpointID, idempotentInternalPayload: IdempotentInternalPayload): Unit = {}

  override def markerAlignmentStart(markerAlignmentInternalPayload: MarkerAlignmentInternalPayload): MarkerCollectionSupport = {null}

  override def markerAlignmentEnd(markerAlignmentInternalPayload: MarkerAlignmentInternalPayload, support: MarkerCollectionSupport): Unit = {}
}


abstract class InternalPayloadManager {

  private val pending = mutable.HashMap[String, MarkerCollectionSupport]()
  private val seen = mutable.HashSet[String]()

  def handlePayload(oneTimeInternalPayload: OneTimeInternalPayload):Unit

  def handlePayload(channel:ChannelEndpointID, idempotentInternalPayload: IdempotentInternalPayload):Unit

  def markerAlignmentStart(markerAlignmentInternalPayload: MarkerAlignmentInternalPayload):MarkerCollectionSupport

  def markerAlignmentEnd(markerAlignmentInternalPayload: MarkerAlignmentInternalPayload, support: MarkerCollectionSupport):Unit

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
          pending(mp.id) = markerAlignmentStart(mp)
        }
        pending(mp.id).onReceiveMarker(channel)
        if(pending(mp.id).isCompleted){
          markerAlignmentEnd(mp, pending(mp.id))
          pending.remove(mp.id)
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
