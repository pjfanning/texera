package edu.uci.ics.amber.engine.recovery

import java.io.File
import java.nio.file.{Files, Paths}

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
import edu.uci.ics.amber.engine.architecture.worker.promisehandlers.PauseHandler.PauseWorker
import edu.uci.ics.amber.engine.architecture.worker.promisehandlers.QueryStatisticsHandler.QueryStatistics
import edu.uci.ics.amber.engine.architecture.worker.promisehandlers.ResumeHandler.ResumeWorker
import edu.uci.ics.amber.engine.architecture.worker.promisehandlers.StartHandler.StartWorker
import edu.uci.ics.amber.engine.common.{IOperatorExecutor, ISourceOperatorExecutor, InputExhausted}
import edu.uci.ics.amber.engine.common.ambermessage.{
  ControlPayload,
  DataFrame,
  EndOfUpstream,
  InputLinking,
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
import edu.uci.ics.amber.engine.recovery.local.{
  LocalDiskMainLogStorage,
  LocalDiskSecondaryLogStorage
}
import edu.uci.ics.amber.engine.recovery.mem.{
  InMemoryLogStorage,
  InMemoryMainLogStorage,
  InMemorySecondaryLogStorage
}
import org.apache.commons.io.FileUtils
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalamock.scalatest.MockFactory

import scala.collection.mutable
import scala.concurrent.{Await, ExecutionContext, ExecutionContextExecutor, Future}
import scala.concurrent.duration._

class RecoverySpec
    extends TestKit(ActorSystem("RecoverySpec"))
    with ImplicitSender
    with AnyFlatSpecLike
    with BeforeAndAfterAll {

  implicit val timeout: Timeout = Timeout(5.seconds)
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  def deleteFolderSafely(path: String): Unit = {
    val folder = new File(path)
    if (folder.exists()) {
      FileUtils.cleanDirectory(folder)
      FileUtils.deleteDirectory(folder)
    }
  }

  override def beforeAll: Unit = {
    deleteFolderSafely("./logs")
    system.actorOf(Props[SingleNodeListener], "cluster-info")
  }
  override def afterAll: Unit = {
    deleteFolderSafely("./logs")
    TestKit.shutdownActorSystem(system)
  }
  val receiverID = WorkerActorVirtualIdentity("receiver")
  val fakeLink: LinkIdentity =
    LinkIdentity(
      LayerIdentity("testRecovery", "mockOp", "src"),
      LayerIdentity("testRecovery", "mockOp", "dst")
    )

  def getMainLogStorage(id: ActorVirtualIdentity) = new LocalDiskMainLogStorage(id)

  def getSecondaryLogStorage(id: ActorVirtualIdentity) = new LocalDiskSecondaryLogStorage(id)

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
      actorMappingToRegister: Seq[(ActorVirtualIdentity, ActorRef)],
      mainLogStorage: MainLogStorage,
      secondaryLogStorage: SecondaryLogStorage
  ): ActorRef = {
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
      sendMessages(worker, controls)
      true
    }(ExecutionContext.global)
  }

  def sendMessages(worker: ActorRef, controls: Seq[ControlCommand[_]]): Unit = {
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
  }

  def waitResponsesAndKillWorker(
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

  def smallWorkerChain(
      sender1: ActorVirtualIdentity,
      sender2: ActorVirtualIdentity,
      sender1MainLog: MainLogStorage,
      sender2MainLog: MainLogStorage,
      sender1SecondaryLog: SecondaryLogStorage,
      sender2SecondaryLog: SecondaryLogStorage
  ): (
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
      AddOutputPolicy(new OneToOnePolicy(fakeLink, 1, Array(sender2))),
      StartWorker(),
      QueryStatistics(),
      QueryStatistics(),
      QueryStatistics()
    )
    val controlsForDummy = Seq(
      AddOutputPolicy(new OneToOnePolicy(fakeLink, 1, Array(receiverID))),
      QueryStatistics(),
      QueryStatistics(),
      QueryStatistics(),
      QueryStatistics()
    )
    val dummyWorker = initWorker(
      sender2,
      dummy,
      controller2,
      Seq((receiverID, receiver.ref)),
      sender2MainLog,
      sender2SecondaryLog
    )
    val sourceWorker = initWorker(
      sender1,
      source,
      controller1,
      Seq((sender2, dummyWorker)),
      sender1MainLog,
      sender1SecondaryLog
    )
    val f1 = sendMessagesAsync(sourceWorker, controlsForSource)
    val f2 = sendMessagesAsync(dummyWorker, controlsForDummy)
    Await.result(f1, 20.seconds)
    Await.result(f2, 20.seconds)
    (source, dummy, sourceWorker, dummyWorker, controller1, controller2, receiver)
  }
//  The following test will randomly fail in github action, the reason is still unclear.

//  "worker" should "write logs during normal processing" in {
//    val id = WorkerActorVirtualIdentity("testRecovery1")
//    val sender1 = WorkerActorVirtualIdentity("sender1")
//    val sender2 = WorkerActorVirtualIdentity("sender2")
//    val sender3 = WorkerActorVirtualIdentity("sender3")
//    val messages = Seq(
//      WorkflowDataMessage(
//        sender1,
//        0,
//        InputLinking(fakeLink)
//      ),
//      WorkflowDataMessage(
//        sender2,
//        0,
//        InputLinking(fakeLink)
//      ),
//      WorkflowDataMessage(
//        sender3,
//        0,
//        InputLinking(fakeLink)
//      ),
//      WorkflowDataMessage(sender1, 1, DataFrame(Array.empty)),
//      WorkflowDataMessage(sender2, 1, DataFrame(Array.empty)),
//      WorkflowDataMessage(sender2, 2, DataFrame(Array.empty)),
//      WorkflowControlMessage(sender2, 0, ControlInvocation(0, PauseWorker())),
//      WorkflowDataMessage(sender3, 1, DataFrame(Array.empty)),
//      WorkflowControlMessage(sender3, 0, ControlInvocation(0, ResumeWorker())),
//      WorkflowDataMessage(sender1, 2, DataFrame(Array.empty))
//    )
//    val op = new SourceOperatorForRecoveryTest()
//    val mainLogStorage: MainLogStorage = new InMemoryMainLogStorage(id)
//    val secondaryLogStorage: SecondaryLogStorage = new InMemorySecondaryLogStorage(id)
//    val worker = system.actorOf(
//      WorkflowWorker.props(id, op, TestProbe().ref, mainLogStorage, secondaryLogStorage)
//    )
//    messages.foreach { x =>
//      worker ! NetworkMessage(0, x)
//    }
//    Thread.sleep(2000)
//    assert(InMemoryLogStorage.getMainLogOf(id.toString).size == 13)
//    assert(InMemoryLogStorage.getSecondaryLogOf(id.toString).size == 2)
//    mainLogStorage.clear()
//    secondaryLogStorage.clear()
//  }

  "source worker" should "recover with the log after restarting" in {
    val id = WorkerActorVirtualIdentity("testRecovery2")
    val sender1 = WorkerActorVirtualIdentity("sender4")
    val op = new SourceOperatorForRecoveryTest()
    val controller = TestProbe()
    val receiver = TestProbe()
    val controls = Seq(
      AddOutputPolicy(new OneToOnePolicy(fakeLink, 1, Array(receiverID))),
      StartWorker(),
      QueryStatistics(),
      QueryStatistics(),
      QueryStatistics()
    )
    val workerMainLog = getMainLogStorage(id)
    val workerSecondaryLog = getSecondaryLogStorage(id)
    val worker = initWorker(
      id,
      op,
      controller,
      Seq((receiverID, receiver.ref)),
      workerMainLog,
      workerSecondaryLog
    )
    sendMessages(worker, controls)
    val received = waitResponsesAndKillWorker(worker, controller, receiver)
    val recovered = initWorker(
      id,
      op,
      controller,
      Seq((receiverID, receiver.ref)),
      workerMainLog,
      workerSecondaryLog
    )
    testRecovery(recovered, controller, receiver, received)
    workerMainLog.clear()
    workerSecondaryLog.clear()
  }

  "multiple workers" should "recover with their logs after restarting" in {
    val sourceID = WorkerActorVirtualIdentity("source1")
    val dummyID = WorkerActorVirtualIdentity("dummy1")
    val sourceMainLog = getMainLogStorage(sourceID)
    val sourceSecondaryLog = getSecondaryLogStorage(sourceID)
    val dummyMainLog = getMainLogStorage(dummyID)
    val dummySecondaryLog = getSecondaryLogStorage(dummyID)
    val (source, dummy, sourceWorker, dummyWorker, controller1, controller2, receiver) =
      smallWorkerChain(
        sourceID,
        dummyID,
        sourceMainLog,
        dummyMainLog,
        sourceSecondaryLog,
        dummySecondaryLog
      )
    val receivedMessageForSource =
      waitResponsesAndKillWorker(sourceWorker, controller1, null)
    val receivedMessageForDummy =
      waitResponsesAndKillWorker(dummyWorker, controller2, receiver)
    val recoveredDummy = initWorker(
      dummyID,
      dummy,
      controller2,
      Seq((receiverID, receiver.ref)),
      dummyMainLog,
      dummySecondaryLog
    )
    val recoveredSource = initWorker(
      sourceID,
      source,
      controller1,
      Seq((dummyID, recoveredDummy)),
      sourceMainLog,
      sourceSecondaryLog
    )
    testRecovery(recoveredSource, controller1, null, receivedMessageForSource)
    testRecovery(recoveredDummy, controller2, receiver, receivedMessageForDummy)
    sourceMainLog.clear()
    sourceSecondaryLog.clear()
    dummyMainLog.clear()
    dummySecondaryLog.clear()
  }

  "one worker" should "recover correctly while the other worker are still alive" in {
    val sourceID = WorkerActorVirtualIdentity("source2")
    val dummyID = WorkerActorVirtualIdentity("dummy2")
    val sourceMainLog = getMainLogStorage(sourceID)
    val sourceSecondaryLog = getSecondaryLogStorage(sourceID)
    val dummyMainLog = getMainLogStorage(dummyID)
    val dummySecondaryLog = getSecondaryLogStorage(dummyID)
    val (source, dummy, sourceWorker, dummyWorker, controller1, controller2, receiver) =
      smallWorkerChain(
        sourceID,
        dummyID,
        sourceMainLog,
        dummyMainLog,
        sourceSecondaryLog,
        dummySecondaryLog
      )
    val receivedMessageForSource =
      waitResponsesAndKillWorker(sourceWorker, controller1, null)
    val recoveredSource = initWorker(
      sourceID,
      source,
      controller1,
      Seq((dummyID, dummyWorker)),
      sourceMainLog,
      sourceSecondaryLog
    )
    testRecovery(recoveredSource, controller1, null, receivedMessageForSource)
    val expectedData =
      (Seq(WorkflowDataMessage(dummyID, 0, InputLinking(fakeLink))) ++ (1 until 16).map(x =>
        WorkflowDataMessage(dummyID, x, DataFrame(Array(ITuple(x))))
      ) ++ Seq(WorkflowDataMessage(dummyID, 16, EndOfUpstream()))).to[mutable.Queue]
    forAllNetworkMessages(receiver, w => assert(w == expectedData.dequeue()))
    val receivedControl = mutable.Queue[WorkflowMessage]()
    forAllNetworkMessages(controller2, w => receivedControl.enqueue(w))
    assert(receivedControl.size == 8)
    sourceMainLog.clear()
    sourceSecondaryLog.clear()
    dummyMainLog.clear()
    dummySecondaryLog.clear()
  }

}
