package edu.uci.ics.amber.engine.architecture.control.utils

import edu.uci.ics.amber.engine.architecture.common.WorkflowActor
import edu.uci.ics.amber.engine.architecture.messaginglayer.NetworkCommunicationActor.NetworkSenderActorRef
import edu.uci.ics.amber.engine.architecture.recovery.{EmptyInternalPayloadManager, InternalPayloadManager}
import edu.uci.ics.amber.engine.architecture.worker.ReplayConfig
import edu.uci.ics.amber.engine.architecture.worker.processing.AmberProcessor
import edu.uci.ics.amber.engine.common.ambermessage.{ChannelEndpointID, ControlPayload, WorkflowFIFOMessagePayloadWithPiggyback}
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity

class TrivialControlTester(
    id: ActorVirtualIdentity,
    parentNetworkCommunicationActorRef: NetworkSenderActorRef
) extends WorkflowActor(id, parentNetworkCommunicationActorRef, ReplayConfig(None,None,Array.empty),false) {
  private val processor = new AmberProcessor(actorId, determinantLogger)

  override def preStart(): Unit = {
    super.preStart()
    processor.init(logManager)
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
}
