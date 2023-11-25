package edu.uci.ics.amber.engine.faulttolerance

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import edu.uci.ics.amber.engine.architecture.common.ProcessingStepCursor
import edu.uci.ics.amber.engine.architecture.logging.{InMemDeterminant, ProcessingStep}
import edu.uci.ics.amber.engine.architecture.messaginglayer.NetworkInputGateway
import edu.uci.ics.amber.engine.architecture.worker.promisehandlers.StartHandler.StartWorker
import edu.uci.ics.amber.engine.common.ambermessage.{ChannelID, DataFrame, WorkflowFIFOMessage}
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCClient.ControlInvocation
import edu.uci.ics.amber.engine.common.tuple.ITuple
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity
import edu.uci.ics.amber.engine.common.virtualidentity.util.CONTROLLER
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpecLike

import scala.collection.mutable

class ReplaySpec extends TestKit(ActorSystem("ReplaySpec"))
  with ImplicitSender
  with AnyFlatSpecLike
  with BeforeAndAfterAll {

  private val actorId = ActorVirtualIdentity("test")
  private val actorId2 = ActorVirtualIdentity("upstream1")
  private val actorId3 = ActorVirtualIdentity("upstream2")
  private val channelId1 = ChannelID(CONTROLLER, actorId, isControl = true)
  private val channelId2 = ChannelID(actorId2, actorId, isControl = false)
  private val channelId3 = ChannelID(actorId3, actorId, isControl = false)
  private val channelId4 = ChannelID(actorId2, actorId, isControl = true)

  "replay input gate" should "replay the message payload in log order" in {
    val networkInputGateway = new NetworkInputGateway(actorId)
    val logRecords = mutable.Queue[InMemDeterminant](
      ProcessingStep(channelId1, -1),
      ProcessingStep(channelId4, 1),
      ProcessingStep(channelId3, 2),
      ProcessingStep(channelId1, 3),
      ProcessingStep(channelId2, 4)
    )

    def inputMessage(channelID: ChannelID, seq:Long): Unit ={
      networkInputGateway.getChannel(channelID).acceptMessage(WorkflowFIFOMessage(channelID,  seq, ControlInvocation(0, StartWorker())))
    }

    val cursor = new ProcessingStepCursor()
    val wrapper = new ReplayGatewayWrapper(logRecords, 1000, cursor, networkInputGateway)
    assert(wrapper.tryPickChannel.isEmpty)
    assert(networkInputGateway.tryPickChannel.isEmpty)
    inputMessage(channelId2, 0)
    assert(wrapper.tryPickChannel.isEmpty)
    assert(networkInputGateway.tryPickChannel.nonEmpty  && networkInputGateway.tryPickChannel.get.channelId == channelId2)
    inputMessage(channelId4, 0)
    assert(wrapper.tryPickChannel.isEmpty)
    assert(networkInputGateway.tryPickChannel.nonEmpty && networkInputGateway.tryPickChannel.get.channelId == channelId4)
    inputMessage(channelId1, 0)
    inputMessage(channelId1, 1)
    inputMessage(channelId1, 2)
    assert(wrapper.tryPickChannel.nonEmpty && wrapper.tryPickChannel.get.channelId == channelId1)
    val msg1 = wrapper.tryPickChannel.get.take
    assert(msg1.channel == channelId1 && msg1.sequenceNumber == 0)
    cursor.stepIncrement()
    assert(wrapper.tryPickChannel.nonEmpty)
    assert(networkInputGateway.tryPickChannel.nonEmpty)
    val msg2 = wrapper.tryPickChannel.get.take
    assert(msg2.channel == channelId1 && msg2.sequenceNumber == 1)
    cursor.stepIncrement()
    assert(wrapper.tryPickChannel.nonEmpty)
    assert(networkInputGateway.tryPickChannel.nonEmpty)
    val msg3 = wrapper.tryPickChannel.get.take
    assert(msg3.channel == channelId4 && msg3.sequenceNumber == 0)
    cursor.stepIncrement()
    assert(wrapper.tryPickChannel.isEmpty)
    assert(networkInputGateway.tryPickChannel.nonEmpty)
    inputMessage(channelId3, 0)
    val msg4 = wrapper.tryPickChannel.get.take
    assert(msg4.channel == channelId3 && msg4.sequenceNumber == 0)
    cursor.stepIncrement()
    assert(wrapper.tryPickChannel.nonEmpty)
    assert(networkInputGateway.tryPickChannel.nonEmpty)
    val msg5 = wrapper.tryPickChannel.get.take
    assert(msg5.channel == channelId1 && msg5.sequenceNumber == 2)
    cursor.stepIncrement()
    assert(wrapper.tryPickChannel.nonEmpty)
    assert(networkInputGateway.tryPickChannel.nonEmpty)
    val msg6 = wrapper.tryPickChannel.get.take
    assert(msg6.channel == channelId2 && msg6.sequenceNumber == 0)
  }

}
