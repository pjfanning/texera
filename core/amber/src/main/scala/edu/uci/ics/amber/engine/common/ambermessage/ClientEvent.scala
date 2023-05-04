package edu.uci.ics.amber.engine.common.ambermessage

import edu.uci.ics.amber.engine.architecture.breakpoint.FaultedTuple
import edu.uci.ics.amber.engine.architecture.common.ProcessingHistory
import edu.uci.ics.amber.engine.architecture.recovery.InternalPayloadManager.CheckpointStats
import edu.uci.ics.amber.engine.common.tuple.ITuple
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity
import edu.uci.ics.texera.web.workflowruntimestate.{ConsoleMessage, OperatorRuntimeStats, WorkflowAggregatedState}

import scala.collection.mutable

object ClientEvent {

  sealed trait ClientEvent

  case class WorkflowRecoveryStatus(isRecovering: Boolean) extends ClientEvent

  case class WorkflowStateUpdate(aggState: WorkflowAggregatedState) extends ClientEvent

  case class WorkflowStatusUpdate(
      operatorStatistics: Map[String, OperatorRuntimeStats]
  ) extends ClientEvent

  case class BreakpointTriggered(
      report: mutable.HashMap[(ActorVirtualIdentity, FaultedTuple), Array[String]],
      operatorID: String = null
  ) extends ClientEvent

  case class ConsoleMessageTriggered(
      operatorId: String,
      consoleMessage: ConsoleMessage
  ) extends ClientEvent

  case class ReportCurrentProcessingTuple(
      operatorID: String,
      tuple: Array[(ITuple, ActorVirtualIdentity)]
  ) extends ClientEvent

  case class WorkerAssignmentUpdate(workerMapping: Map[String, Seq[String]]) extends ClientEvent

  case class WorkerModifyLogicComplete(workerID: ActorVirtualIdentity) extends ClientEvent

  case class ReplayCompleted(actorId:ActorVirtualIdentity, id:String) extends ClientEvent

  case class EstimationCompleted(actorId:ActorVirtualIdentity, id:String, checkpointStats: CheckpointStats) extends ClientEvent

  case class RuntimeCheckpointCompleted(actorId:ActorVirtualIdentity, logicalSnapshotId:String, checkpointId:String, checkpointStats: CheckpointStats) extends ClientEvent

  case class FatalErrorToClient(e:Throwable) extends ClientEvent

}
