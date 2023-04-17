package edu.uci.ics.amber.engine.architecture.control.utils

import akka.actor.ActorRef
import com.softwaremill.macwire.wire
import edu.uci.ics.amber.engine.architecture.checkpoint.SavedCheckpoint
import edu.uci.ics.amber.engine.architecture.common.WorkflowActor
import edu.uci.ics.amber.engine.architecture.logging.AsyncLogWriter.SendRequest
import edu.uci.ics.amber.engine.architecture.messaginglayer.NetworkCommunicationActor.{NetworkAck, NetworkMessage, NetworkSenderActorRef}
import edu.uci.ics.amber.engine.architecture.messaginglayer.{NetworkInputPort, NetworkOutputPort}
import edu.uci.ics.amber.engine.architecture.recovery.{EmptyFIFOMarkerHandler, InternalPayloadManager}
import edu.uci.ics.amber.engine.architecture.worker.ReplayConfig
import edu.uci.ics.amber.engine.architecture.worker.processing.AmberProcessor
import edu.uci.ics.amber.engine.common.ambermessage.{ChannelEndpointID, ControlPayload, WorkflowFIFOMessage, WorkflowFIFOMessagePayload}
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCClient.{ControlInvocation, ReturnInvocation}
import edu.uci.ics.amber.engine.common.rpc.{AsyncRPCClient, AsyncRPCHandlerInitializer, AsyncRPCServer}
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity
import edu.uci.ics.amber.error.ErrorUtils.safely

class TrivialControlTester(
    id: ActorVirtualIdentity,
    parentNetworkCommunicationActorRef: NetworkSenderActorRef
) extends WorkflowActor(id, parentNetworkCommunicationActorRef, ReplayConfig(None,None,Array.empty),false) {
  override val internalMessageHandler: InternalPayloadManager = new EmptyFIFOMarkerHandler()
  private val processor = new AmberProcessor(actorId, determinantLogger){}

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

  override def handlePayload(channelEndpointID: ChannelEndpointID, payload: WorkflowFIFOMessagePayload): Unit = {
    payload match{
      case control:ControlPayload =>
        processor.processControlPayload(channelEndpointID, control)
      case other => //skip
    }
  }
}
