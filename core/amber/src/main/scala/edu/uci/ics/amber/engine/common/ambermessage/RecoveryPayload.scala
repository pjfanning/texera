package edu.uci.ics.amber.engine.common.ambermessage

import akka.actor.{ActorRef, Address}
import edu.uci.ics.amber.engine.architecture.controller.WorkflowStateRestoreConfig
import edu.uci.ics.amber.engine.architecture.worker.processing.promisehandlers.TakeCheckpointHandler.CheckpointStats
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity

sealed trait RecoveryPayload extends Serializable {}

// Notify controller on worker recovery starts/ends
final case class UpdateRecoveryStatus(isRecovering: Boolean) extends RecoveryPayload

// Notify upstream worker to resend output to another worker for recovery
final case class ResendOutputTo(vid: ActorVirtualIdentity, ref: ActorRef) extends RecoveryPayload

// Notify controller when the machine fails and triggers recovery
final case class NotifyFailedNode(addr: Address) extends RecoveryPayload

// for replay prototype:
final case class ContinueReplay(workflowStateRestoreConfig: WorkflowStateRestoreConfig)
    extends RecoveryPayload
final case class ContinueReplayTo(replayTo: Long) extends RecoveryPayload
final case class GetOperatorInternalState() extends RecoveryPayload
final case class InterruptReplay() extends RecoveryPayload
final case class PauseDuringReplay() extends RecoveryPayload
final case class ResumeDuringReplay() extends RecoveryPayload
final case class TakeGlobalCheckpoint(involved:Set[ActorVirtualIdentity], cutoffMap:Map[ActorVirtualIdentity, Map[(ActorVirtualIdentity,Boolean), Long]]) extends RecoveryPayload
final case class TakeLocalCheckpoint(cutoffs:Map[(ActorVirtualIdentity,Boolean), Long]) extends RecoveryPayload
