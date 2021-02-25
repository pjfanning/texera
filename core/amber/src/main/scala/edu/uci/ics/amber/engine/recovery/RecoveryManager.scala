package edu.uci.ics.amber.engine.recovery

import akka.actor.{ActorContext, Address}
import edu.uci.ics.amber.engine.architecture.controller.Workflow
import edu.uci.ics.amber.engine.architecture.messaginglayer.NetworkCommunicationActor.{
  GetActorRef,
  NetworkSenderActorRef,
  SendRequest
}
import edu.uci.ics.amber.engine.common.ambermessage.WorkflowMessage
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity
import edu.uci.ics.amber.engine.recovery.empty.{EmptyMainLogStorage, EmptySecondaryLogStorage}

import scala.collection.mutable

object RecoveryManager {
  def defaultMainLogStorage(id: ActorVirtualIdentity): EmptyMainLogStorage = {
    if (id == ActorVirtualIdentity.Controller) {
      new EmptyMainLogStorage()
    } else {
      new EmptyMainLogStorage()
    }
  }
  def defaultSecondLogStorage(id: ActorVirtualIdentity) = new EmptySecondaryLogStorage()

  sealed trait RecoveryMessage extends WorkflowMessage
  final case class TriggerRecovery(id: ActorVirtualIdentity) extends RecoveryMessage
  final case class RecoveryCompleted(id: ActorVirtualIdentity) extends RecoveryMessage
}

class RecoveryManager(
    workflow: Workflow,
    context: ActorContext,
    communicationActor: NetworkSenderActorRef
) {

  private val isRecovering = mutable.HashSet[ActorVirtualIdentity]()

  def recoverWorkerChainFor(id: ActorVirtualIdentity, onNode: Address): Unit = {
    if (!isRecovering.contains(id)) {
      recoverWorker(id, onNode)
      val upstreamWorkersToReplay =
        workflow.getUpstreamWorkers(id).filter(x => !isRecovering.contains(x))
      upstreamWorkersToReplay.foreach(recoverWorker(_, onNode))
    }
  }

  def setRecoverCompleted(id: ActorVirtualIdentity): Unit = {
    if(isRecovering.contains(id)){
      isRecovering.remove(id)
    }
  }

  private def recoverWorker(id: ActorVirtualIdentity, onNode: Address): Unit = {
    workflow
      .getWorkerLayer(id)
      .killAndReBuild(
        id,
        onNode,
        context,
        communicationActor.ref,
        RecoveryManager.defaultMainLogStorage(id),
        RecoveryManager.defaultSecondLogStorage(id)
      )
    isRecovering.add(id)
  }

}
