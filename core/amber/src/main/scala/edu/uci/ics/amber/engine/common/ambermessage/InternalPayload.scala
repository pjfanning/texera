package edu.uci.ics.amber.engine.common.ambermessage

import akka.actor.{ActorRef, Address}
import edu.uci.ics.amber.engine.architecture.controller.WorkflowReplayConfig
import edu.uci.ics.amber.engine.architecture.worker.processing.promisehandlers.TakeCheckpointHandler.CheckpointStats
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity

//sealed trait InternalPayload extends Serializable {}
//
//// Notify controller on worker recovery starts/ends
//final case class UpdateRecoveryStatus(isRecovering: Boolean) extends InternalPayload
//
//// Notify upstream worker to resend output to another worker for recovery
//final case class ResendOutputTo(vid: ActorVirtualIdentity, ref: ActorRef) extends InternalPayload
//
//// Notify controller when the machine fails and triggers recovery
//final case class NotifyFailedNode(addr: Address) extends InternalPayload
//
//// for replay prototype:
//final case class ContinueReplay(workflowStateRestoreConfig: WorkflowReplayConfig)
//    extends InternalPayload
//final case class ContinueReplayTo(replayTo: Long) extends InternalPayload
//final case class GetOperatorInternalState() extends InternalPayload
//final case class InterruptReplay() extends InternalPayload
//final case class PauseDuringReplay() extends InternalPayload
//final case class ResumeDuringReplay() extends InternalPayload
//final case class TakeGlobalCheckpoint() extends InternalPayload
//final case class CheckpointCompleted(id:Long, alignment:Long) extends InternalPayload
