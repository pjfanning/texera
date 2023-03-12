package edu.uci.ics.amber.engine.architecture.common

import akka.actor.{Actor, ActorRef, Stash}
import edu.uci.ics.amber.engine.architecture.logging.storage.{
  DeterminantLogStorage,
  EmptyLogStorage,
}
import edu.uci.ics.amber.engine.architecture.logging.{AsyncLogWriter, LogManager}
import edu.uci.ics.amber.engine.architecture.messaginglayer.NetworkCommunicationActor.{
  GetActorRef,
  NetworkSenderActorRef,
  RegisterActorRef
}
import edu.uci.ics.amber.engine.architecture.messaginglayer.{
  NetworkCommunicationActor,
  NetworkOutputPort
}
import edu.uci.ics.amber.engine.architecture.recovery.LocalRecoveryManager
import edu.uci.ics.amber.engine.common.{AmberLogging, AmberUtils}
import edu.uci.ics.amber.engine.common.amberexception.WorkflowRuntimeException
import edu.uci.ics.amber.engine.common.ambermessage.{
  ControlPayload,
  ResendOutputTo
}
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity

abstract class WorkflowActor(
    val actorId: ActorVirtualIdentity,
    parentNetworkCommunicationActorRef: NetworkSenderActorRef,
    supportFaultTolerance: Boolean
) extends Actor
    with Stash
    with AmberLogging {
  val networkCommunicationActor: NetworkSenderActorRef = NetworkSenderActorRef(
    // create a network communication actor on the same machine as the WorkflowActor itself
    context.actorOf(
      NetworkCommunicationActor.props(
        parentNetworkCommunicationActorRef.ref,
        actorId
      )
    )
  )
  val logStorage: DeterminantLogStorage = {
    DeterminantLogStorage.getLogStorage(supportFaultTolerance, getLogName)
  }
  val recoveryManager = new LocalRecoveryManager()
  val logManager: LogManager =
    LogManager.getLogManager(supportFaultTolerance, networkCommunicationActor)
  if (!logStorage.isLogAvailableForRead) {
    logManager.setupWriter(logStorage.getWriter)
  } else {
    logManager.setupWriter(new EmptyLogStorage().getWriter)
  }

  // Get log file name
  def getLogName: String = "worker-actor"

  def forwardResendRequest: Receive = {
    case resend: ResendOutputTo =>
      networkCommunicationActor ! resend
  }

  def disallowActorRefRelatedMessages: Receive = {
    case GetActorRef =>
      throw new WorkflowRuntimeException(
        "workflow actor should never receive get actor ref message"
      )

    case RegisterActorRef =>
      throw new WorkflowRuntimeException(
        "workflow actor should never receive register actor ref message"
      )
  }

}
