package edu.uci.ics.amber.engine.faulttolerance

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import edu.uci.ics.amber.engine.architecture.logging.storage.DeterminantLogStorage
import edu.uci.ics.amber.engine.architecture.logging.storage.DeterminantLogStorage.DeterminantLogReader
import edu.uci.ics.amber.engine.architecture.logging.{
  InMemDeterminant,
  LogManagerImpl,
  ProcessingStep
}
import edu.uci.ics.amber.engine.architecture.messaginglayer.NetworkInputGateway
import edu.uci.ics.amber.engine.architecture.worker.promisehandlers.StartHandler.StartWorker
import edu.uci.ics.amber.engine.common.ambermessage.{ChannelID, WorkflowFIFOMessage}
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCClient.ControlInvocation
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity
import edu.uci.ics.amber.engine.common.virtualidentity.util.CONTROLLER
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpecLike

import scala.collection.mutable

class ReplaySpec
    extends TestKit(ActorSystem("ReplaySpec"))
    with ImplicitSender
    with AnyFlatSpecLike
    with BeforeAndAfterAll {

  class IterableReadOnlyLogStore(iter: Iterable[InMemDeterminant]) extends DeterminantLogStorage {
    override def getWriter: DeterminantLogStorage.DeterminantLogWriter = ???

    override def getReader: DeterminantLogStorage.DeterminantLogReader =
      new DeterminantLogReader(null) {
        override def mkLogRecordIterator(): Iterator[InMemDeterminant] = iter.toIterator
      }

    override def isLogAvailableForRead: Boolean = true

    override def deleteLog(): Unit = ???
  }

  private val actorId = ActorVirtualIdentity("test")
  private val actorId2 = ActorVirtualIdentity("upstream1")
  private val actorId3 = ActorVirtualIdentity("upstream2")
  private val channelId1 = ChannelID(CONTROLLER, actorId, isControl = true)
  private val channelId2 = ChannelID(actorId2, actorId, isControl = false)
  private val channelId3 = ChannelID(actorId3, actorId, isControl = false)
  private val channelId4 = ChannelID(actorId2, actorId, isControl = true)
  private val logManager = new LogManagerImpl(x => {})

  "replay input gate" should "replay the message payload in log order" in {
    val networkInputGateway = new NetworkInputGateway(actorId)
    val logRecords = mutable.Queue[ProcessingStep](
      ProcessingStep(channelId1, -1),
      ProcessingStep(channelId4, 1),
      ProcessingStep(channelId3, 2),
      ProcessingStep(channelId1, 3),
      ProcessingStep(channelId2, 4)
    )

    def inputMessage(channelID: ChannelID, seq: Long): Unit = {
      networkInputGateway
        .getChannel(channelID)
        .acceptMessage(WorkflowFIFOMessage(channelID, seq, ControlInvocation(0, StartWorker())))
    }
    val wrapper = new ReplayGatewayWrapper(networkInputGateway, logManager)
    wrapper.setupReplay(new IterableReadOnlyLogStore(logRecords), 1000, () => {})
    def processMessage(channelID: ChannelID, seq: Long): Unit = {
      val msg = wrapper.tryPickChannel.get.take
      logManager.doFaultTolerantProcessing(msg.channel, Some(msg)) {
        assert(msg.channel == channelID && msg.sequenceNumber == seq)
      }
    }
    assert(wrapper.tryPickChannel.isEmpty)
    assert(networkInputGateway.tryPickChannel.isEmpty)
    inputMessage(channelId2, 0)
    assert(wrapper.tryPickChannel.isEmpty)
    assert(
      networkInputGateway.tryPickChannel.nonEmpty && networkInputGateway.tryPickChannel.get.channelId == channelId2
    )
    inputMessage(channelId4, 0)
    assert(wrapper.tryPickChannel.isEmpty)
    assert(
      networkInputGateway.tryPickChannel.nonEmpty && networkInputGateway.tryPickChannel.get.channelId == channelId4
    )
    inputMessage(channelId1, 0)
    inputMessage(channelId1, 1)
    inputMessage(channelId1, 2)
    assert(wrapper.tryPickChannel.nonEmpty && wrapper.tryPickChannel.get.channelId == channelId1)
    processMessage(channelId1, 0)
    assert(wrapper.tryPickChannel.nonEmpty)
    assert(networkInputGateway.tryPickChannel.nonEmpty)
    processMessage(channelId1, 1)
    assert(wrapper.tryPickChannel.nonEmpty)
    assert(networkInputGateway.tryPickChannel.nonEmpty)
    processMessage(channelId4, 0)
    assert(wrapper.tryPickChannel.isEmpty)
    assert(networkInputGateway.tryPickChannel.nonEmpty)
    inputMessage(channelId3, 0)
    processMessage(channelId3, 0)
    assert(wrapper.tryPickChannel.nonEmpty)
    assert(networkInputGateway.tryPickChannel.nonEmpty)
    processMessage(channelId1, 2)
    assert(wrapper.tryPickChannel.nonEmpty)
    assert(networkInputGateway.tryPickChannel.nonEmpty)
    processMessage(channelId2, 0)
  }

}
