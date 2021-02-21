package edu.uci.ics.amber.engine.recovery

import akka.actor.{ActorRef, ActorSystem, PoisonPill, Props}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit, TestProbe}
import akka.util.Timeout
import edu.uci.ics.amber.clustering.SingleNodeListener
import edu.uci.ics.amber.engine.architecture.controller.promisehandlers.QueryWorkerStatisticsHandler.QueryWorkerStatistics
import edu.uci.ics.amber.engine.architecture.messaginglayer.ControlInputPort.WorkflowControlMessage
import edu.uci.ics.amber.engine.architecture.messaginglayer.DataInputPort.WorkflowDataMessage
import edu.uci.ics.amber.engine.architecture.messaginglayer.NetworkCommunicationActor.{
  NetworkAck,
  NetworkMessage,
  RegisterActorRef
}
import edu.uci.ics.amber.engine.architecture.sendsemantics.datatransferpolicy.OneToOnePolicy
import edu.uci.ics.amber.engine.architecture.worker.WorkflowWorker
import edu.uci.ics.amber.engine.architecture.worker.promisehandlers.AddOutputPolicyHandler.AddOutputPolicy
import edu.uci.ics.amber.engine.architecture.worker.promisehandlers.QueryStatisticsHandler.QueryStatistics
import edu.uci.ics.amber.engine.architecture.worker.promisehandlers.StartHandler.StartWorker
import edu.uci.ics.amber.engine.architecture.worker.promisehandlers.UpdateInputLinkingHandler.UpdateInputLinking
import edu.uci.ics.amber.engine.common.{IOperatorExecutor, ISourceOperatorExecutor, InputExhausted}
import edu.uci.ics.amber.engine.common.ambermessage.{
  ControlPayload,
  DataFrame,
  EndOfUpstream,
  WorkflowMessage
}
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCClient.ControlInvocation
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.{CommandCompleted, ControlCommand}
import edu.uci.ics.amber.engine.common.tuple.ITuple
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity.WorkerActorVirtualIdentity
import edu.uci.ics.amber.engine.common.virtualidentity.{
  ActorVirtualIdentity,
  LayerIdentity,
  LinkIdentity
}
import edu.uci.ics.amber.engine.recovery.MainLogStorage.{FromID, IdentifierMapping}
import edu.uci.ics.amber.engine.recovery.mem.{
  InMemoryLogStorage,
  InMemoryMainLogStorage,
  InMemorySecondaryLogStorage
}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalamock.scalatest.MockFactory

import scala.collection.mutable
import scala.concurrent.{Await, ExecutionContextExecutor, Future}
import scala.concurrent.duration._

class RecoverySpec
    extends TestKit(ActorSystem("RecoverySpec"))
    with ImplicitSender
    with AnyFlatSpecLike
    with BeforeAndAfterAll
    with MockFactory {

  implicit val timeout: Timeout = Timeout(5.seconds)
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  override def beforeAll: Unit = {
    system.actorOf(Props[SingleNodeListener], "cluster-info")
  }
  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }
  val id = WorkerActorVirtualIdentity("testRecovery1")
  val sender1 = WorkerActorVirtualIdentity("sender1")
  val sender2 = WorkerActorVirtualIdentity("sender2")
  val sender3 = WorkerActorVirtualIdentity("sender3")
  val receiverID = WorkerActorVirtualIdentity("receiver")
  val fakeLink: LinkIdentity =
    LinkIdentity(
      LayerIdentity("testRecovery", "mockOp", "src"),
      LayerIdentity("testRecovery", "mockOp", "dst")
    )

  class SourceOperatorForRecoveryTest(outputLimit: Int = 15, generateInterval: Int = 100)
      extends ISourceOperatorExecutor {
    override def open(): Unit = {}
    override def close(): Unit = {}
    override def produce(): Iterator[ITuple] = {
      (for (i <- (1 to outputLimit).view) yield {
        Thread.sleep(generateInterval); println(s"generating tuple $i"); ITuple(i)
      }).toIterator
    }
  }

  class DummyOperatorForRecoveryTest() extends IOperatorExecutor {
    override def open(): Unit = {}
    override def close(): Unit = {}
    override def processTuple(
        tuple: Either[ITuple, InputExhausted],
        input: LinkIdentity
    ): Iterator[ITuple] = {
      tuple match {
        case Left(value) =>
          println(s"received tuple $value")
          Iterator(value)
        case Right(value) =>
          println(s"received tuple $value")
          Iterator.empty
      }
    }
  }

  def forAllNetworkMessages(probe: TestProbe, action: (WorkflowMessage) => Unit): Unit = {
    if (probe != null) {
      probe.receiveWhile(idle = 3.seconds) {
        case NetworkMessage(id, content) =>
          probe.sender() ! NetworkAck(id)
          action(content)
        case other => //skip
      }
    }
  }

  def initWorker(
      id: ActorVirtualIdentity,
      op: IOperatorExecutor,
      controller: TestProbe,
      actorMappingToRegister: Seq[(ActorVirtualIdentity, ActorRef)]
  ): ActorRef = {
    val mainLogStorage: MainLogStorage = new InMemoryMainLogStorage(id)
    val secondaryLogStorage: SecondaryLogStorage = new InMemorySecondaryLogStorage(id)
    val worker = TestActorRef(
      new WorkflowWorker(id, op, controller.ref, mainLogStorage, secondaryLogStorage) {
        networkCommunicationActor ! RegisterActorRef(
          ActorVirtualIdentity.Controller,
          controller.ref
        )
        actorMappingToRegister.foreach {
          case (id, ref) =>
            networkCommunicationActor ! RegisterActorRef(id, ref)
        }
      }
    )
    worker
  }

  def sendMessagesAsync(worker: ActorRef, controls: Seq[ControlCommand[_]]): Future[Boolean] = {
    Future {
      val messages = controls.indices.map(i =>
        WorkflowControlMessage(
          ActorVirtualIdentity.Controller,
          i,
          ControlInvocation(i, controls(i))
        )
      )
      messages.foreach { x =>
        worker ! NetworkMessage(0, x)
        Thread.sleep(400)
      }
      true
    }
  }

  def blockingWaitResponsesAndKillWorker(
      worker: ActorRef,
      controller: TestProbe,
      receiver: TestProbe
  ): mutable.Queue[Any] = {
    val receivedMessages = mutable.Queue[Any]()
    forAllNetworkMessages(controller, x => receivedMessages.enqueue(x))
    forAllNetworkMessages(receiver, x => receivedMessages.enqueue(x))
    worker ! PoisonPill
    println("received messages: \n" + receivedMessages.mkString("\n"))
    receivedMessages
  }

  def testRecovery(
      worker: ActorRef,
      controller: TestProbe,
      receiver: TestProbe,
      receivedMessages: mutable.Queue[Any]
  ): Unit = {
    Thread.sleep(15000)
    forAllNetworkMessages(controller, x => assert(receivedMessages.dequeue() == x))
    forAllNetworkMessages(receiver, x => assert(receivedMessages.dequeue() == x))
  }

  def smallWorkerChain(): (
      ISourceOperatorExecutor,
      IOperatorExecutor,
      ActorRef,
      ActorRef,
      TestProbe,
      TestProbe,
      TestProbe
  ) = {
    val source = new SourceOperatorForRecoveryTest()
    val dummy = new DummyOperatorForRecoveryTest()
    val controller1 = TestProbe()
    val controller2 = TestProbe()
    val receiver = TestProbe()
    val controlsForSource = Seq(
      QueryStatistics(),
      QueryStatistics(),
      AddOutputPolicy(new OneToOnePolicy(fakeLink, 1, Array(sender2))),
      StartWorker(),
      QueryStatistics(),
      QueryStatistics(),
      QueryStatistics()
    )
    val controlsForDummy = Seq(
      UpdateInputLinking(sender1, fakeLink),
      AddOutputPolicy(new OneToOnePolicy(fakeLink, 1, Array(receiverID))),
      QueryStatistics(),
      QueryStatistics(),
      QueryStatistics(),
      QueryStatistics()
    )
    val dummyWorker = initWorker(sender2, dummy, controller2, Seq((receiverID, receiver.ref)))
    val sourceWorker = initWorker(sender1, source, controller1, Seq((sender2, dummyWorker)))
    val f1 = sendMessagesAsync(sourceWorker, controlsForSource)
    val f2 = sendMessagesAsync(dummyWorker, controlsForDummy)
    Await.result(f1, 20.seconds)
    Await.result(f2, 20.seconds)
    (source, dummy, sourceWorker, dummyWorker, controller1, controller2, receiver)
  }

  "worker" should "write logs during normal processing" in {
    val messages = Seq(
      WorkflowControlMessage(
        sender1,
        0,
        ControlInvocation(-1, UpdateInputLinking(sender1, fakeLink))
      ),
      WorkflowControlMessage(
        sender2,
        0,
        ControlInvocation(-1, UpdateInputLinking(sender2, fakeLink))
      ),
      WorkflowControlMessage(
        sender3,
        0,
        ControlInvocation(-1, UpdateInputLinking(sender3, fakeLink))
      ),
      WorkflowDataMessage(sender1, 0, DataFrame(Array.empty)),
      WorkflowDataMessage(sender2, 0, DataFrame(Array.empty)),
      WorkflowDataMessage(sender2, 1, DataFrame(Array.empty)),
      WorkflowControlMessage(sender2, 1, ControlInvocation(-1, QueryStatistics())),
      WorkflowDataMessage(sender3, 0, DataFrame(Array.empty)),
      WorkflowControlMessage(sender3, 1, ControlInvocation(-1, QueryStatistics())),
      WorkflowDataMessage(sender1, 1, DataFrame(Array.empty))
    )
    val op = mock[IOperatorExecutor]
    (op.open _).expects().anyNumberOfTimes()
    (op.close _).expects().anyNumberOfTimes()
    val mainLogStorage: MainLogStorage = new InMemoryMainLogStorage(id)
    val secondaryLogStorage: SecondaryLogStorage = new InMemorySecondaryLogStorage(id)
    val worker =
      TestActorRef(new WorkflowWorker(id, op, TestProbe().ref, mainLogStorage, secondaryLogStorage))
    messages.take(3).foreach { x =>
      worker ! NetworkMessage(0, x)
    }
    Thread.sleep(3000)
    messages.drop(3).foreach { x =>
      worker ! NetworkMessage(0, x)
    }
    Thread.sleep(3000)
    assert(InMemoryLogStorage.getMainLogOf(id.toString).size == 13)
    assert(InMemoryLogStorage.getSecondaryLogOf(id.toString).size == 5)
  }

  "worker" should "recover with the log after restarting" in {
    val op = new SourceOperatorForRecoveryTest()
    val controller = TestProbe()
    val receiver = TestProbe()
    val controls = Seq(
      UpdateInputLinking(sender1, fakeLink),
      AddOutputPolicy(new OneToOnePolicy(fakeLink, 1, Array(receiverID))),
      StartWorker(),
      QueryStatistics(),
      QueryStatistics(),
      QueryStatistics()
    )
    val worker = initWorker(id, op, controller, Seq((receiverID, receiver.ref)))
    Await.result(sendMessagesAsync(worker, controls), 20.seconds)
    val received = blockingWaitResponsesAndKillWorker(worker, controller, receiver)
    val recovered = initWorker(id, op, controller, Seq((receiverID, receiver.ref)))
    testRecovery(recovered, controller, receiver, received)
  }

  "multiple workers" should "recover with their logs after restarting" in {
    val (source, dummy, sourceWorker, dummyWorker, controller1, controller2, receiver) =
      smallWorkerChain()
    val receivedMessageForSource =
      blockingWaitResponsesAndKillWorker(sourceWorker, controller1, null)
    val receivedMessageForDummy =
      blockingWaitResponsesAndKillWorker(dummyWorker, controller2, receiver)
    val recoveredDummy = initWorker(sender2, dummy, controller2, Seq((receiverID, receiver.ref)))
    val recoveredSource = initWorker(sender1, source, controller1, Seq((sender2, recoveredDummy)))
    testRecovery(recoveredSource, controller1, null, receivedMessageForSource)
    testRecovery(recoveredDummy, controller2, receiver, receivedMessageForDummy)
  }

  "one worker" should "recover correctly while the other worker are still alive" in {
    val (source, dummy, sourceWorker, dummyWorker, controller1, controller2, receiver) =
      smallWorkerChain()
    val receivedMessageForSource =
      blockingWaitResponsesAndKillWorker(sourceWorker, controller1, null)
    val recoveredSource = initWorker(sender1, source, controller1, Seq((sender2, dummyWorker)))
    testRecovery(recoveredSource, controller1, null, receivedMessageForSource)
    val expectedData = ((0 until 15).map(x =>
      WorkflowDataMessage(sender2, x, DataFrame(Array(ITuple(x + 1))))
    ) ++ Seq(WorkflowDataMessage(sender2, 15, EndOfUpstream()))).to[mutable.Queue]
    forAllNetworkMessages(receiver, w => assert(w == expectedData.dequeue()))
    val receivedControl = mutable.Queue[WorkflowMessage]()
    forAllNetworkMessages(controller2, w => receivedControl.enqueue(w))
    assert(receivedControl.size == 9)
  }

}
