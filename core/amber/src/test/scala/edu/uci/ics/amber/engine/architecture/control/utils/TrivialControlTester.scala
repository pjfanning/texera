package edu.uci.ics.amber.engine.architecture.control.utils

import akka.actor.ActorRef
import edu.uci.ics.amber.engine.architecture.common.WorkflowActor
import edu.uci.ics.amber.engine.architecture.recovery.{EmptyInternalPayloadManager, InternalPayloadManager}
import edu.uci.ics.amber.engine.architecture.worker.processing.AmberProcessor
import edu.uci.ics.amber.engine.common.ambermessage.{ChannelEndpointID, ControlPayload, WorkflowFIFOMessagePayloadWithPiggyback}
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity

class TrivialControlTester(
    id: ActorVirtualIdentity,
    parentNetworkCommunicationActorRef: ActorRef
) extends WorkflowActor(id, parentNetworkCommunicationActorRef) {
  private val processor = new AmberProcessor(actorId, determinantLogger)

  override def preStart(): Unit = {
    super.preStart()
    processor.init(this)
    new TesterAsyncRPCHandlerInitializer(processor)
  }

  override def getLogName: String = s"trivial-control-tester-$id"

  /** flow-control */
  override def getSenderCredits(actorVirtualIdentity: ActorVirtualIdentity): Int = {
    1000
  }

  override def handlePayload(channelEndpointID: ChannelEndpointID, payload: WorkflowFIFOMessagePayloadWithPiggyback): Unit = {
    payload match{
      case control:ControlPayload =>
        processor.processControlPayload(channelEndpointID, control)
      case other => //skip
    }
  }

  override def internalPayloadManager: InternalPayloadManager = new EmptyInternalPayloadManager()

  override def initState(): Unit = {}
}
