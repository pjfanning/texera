package edu.uci.ics.amber.engine.faulttolerance

import akka.actor.{ActorContext, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import akka.util.Timeout
import com.twitter.chill.{KryoPool, KryoSerializer, ScalaKryoInstantiator}
import edu.uci.ics.amber.clustering.SingleNodeListener
import edu.uci.ics.amber.engine.architecture.logging.storage.{
  DeterminantLogStorage,
  EmptyLogStorage,
  LocalFSLogStorage
}
import edu.uci.ics.amber.engine.architecture.logging.{
  InMemDeterminant,
  StepsOnChannel
}
import edu.uci.ics.amber.engine.architecture.messaginglayer.{CreditMonitor, CreditMonitorImpl}
import edu.uci.ics.amber.engine.architecture.worker.RecoveryInternalQueueImpl
import edu.uci.ics.amber.engine.architecture.worker.statistics.WorkerState.COMPLETED
import edu.uci.ics.amber.engine.architecture.worker.statistics.WorkerStatistics
import edu.uci.ics.amber.engine.architecture.worker.workloadmetrics.SelfWorkloadMetrics
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCClient.ReturnInvocation
import edu.uci.ics.amber.engine.common.tuple.ITuple
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpecLike

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._

class RecoverySpec
    extends TestKit(ActorSystem("RecoverySpec"))
    with ImplicitSender
    with AnyFlatSpecLike
    with BeforeAndAfterAll {

  implicit val timeout: Timeout = Timeout(5.seconds)
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  private val kryoPool = {
    val r = KryoSerializer.registerAll
    val ki = (new ScalaKryoInstantiator).withRegistrar(r)
    KryoPool.withByteArrayOutputStream(Runtime.getRuntime.availableProcessors * 2, ki)
  }

  override def beforeAll: Unit = {
    system.actorOf(Props[SingleNodeListener], "cluster-info")
  }
  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }

//  "Kryo" should "serialize nested determinant correctly" in {
//    val selfworkload = SelfWorkloadMetrics(1, 1)
//    val buffer = ArrayBuffer[mutable.HashMap[ActorVirtualIdentity, ArrayBuffer[Long]]]()
//    val m = mutable.HashMap[ActorVirtualIdentity, ArrayBuffer[Long]]()
//    m(ActorVirtualIdentity("1")) = ArrayBuffer[Long](1, 2, 3, 4)
//    buffer.append(m)
//    val a = ProcessControlMessage(
//      ReturnInvocation(1, (selfworkload, buffer)),
//      ActorVirtualIdentity("test")
//    )
//    val bytes = kryoPool.toBytesWithClass(a)
//    val obj = kryoPool.fromBytes(bytes)
//    assert(a == obj)
//  }
//
//  "Logreader" should "W/R content in the log" in {
//    val workerName = "Test"
//    val logStorage = new LocalFSLogStorage(workerName)
//    logStorage.deleteLog()
//    val writer = logStorage.getWriter
//    val determinants: Array[InMemDeterminant] = Array(
//      ProcessControlMessage(
//        ReturnInvocation(16, WorkerStatistics(COMPLETED, 6, 2)),
//        ActorVirtualIdentity(
//          "WF-KeywordSearch-operator-44478988-0d44-43c0-ab0d-f52fd5885ba4-main-0"
//        )
//      ),
//      ProcessControlMessage(
//        ReturnInvocation(4, ()),
//        ActorVirtualIdentity("WF-SimpleSink-operator-06d5e7e6-dbd1-40e4-87d6-133d33559aa8-main-0")
//      ),
//      StepsOnChannel(null, 1),
//      StepDelta(
//        ActorVirtualIdentity("WF-SimpleSink-operator-06d5e7e6-dbd1-40e4-87d6-133d33559aa8-main-0"),
//        29
//      ),
//      ProcessControlMessage(
//        ReturnInvocation(9, (1, 2, 3, 4)),
//        ActorVirtualIdentity("WF-SimpleSink-operator-06d5e7e6-dbd1-40e4-87d6-133d33559aa8-main-0")
//      )
//    )
//    determinants.foreach(writer.writeLogRecord)
//    writer.flush()
//    writer.close()
//    val expected: Array[AnyRef] = Array(
//      ProcessControlMessage(
//        ReturnInvocation(16, WorkerStatistics(COMPLETED, 6, 2)),
//        ActorVirtualIdentity(
//          "WF-KeywordSearch-operator-44478988-0d44-43c0-ab0d-f52fd5885ba4-main-0"
//        )
//      ),
//      ProcessControlMessage(
//        ReturnInvocation(4, ()),
//        ActorVirtualIdentity("WF-SimpleSink-operator-06d5e7e6-dbd1-40e4-87d6-133d33559aa8-main-0")
//      ),
//      StepsOnChannel(null, 1),
//      StepDelta(
//        ActorVirtualIdentity("WF-SimpleSink-operator-06d5e7e6-dbd1-40e4-87d6-133d33559aa8-main-0"),
//        29
//      ),
//      ProcessControlMessage(
//        ReturnInvocation(9, (1, 2, 3, 4)),
//        ActorVirtualIdentity("WF-SimpleSink-operator-06d5e7e6-dbd1-40e4-87d6-133d33559aa8-main-0")
//      )
//    )
//    var idx = 0
//    DeterminantLogStorage.fetchAllLogRecords(logStorage).foreach { x =>
//      assert(x == expected(idx))
//      idx += 1
//    }
//    logStorage.deleteLog()
//  }
//
//  "RecoveryQueue" should "read log correctly" in {
//    val workerName = "Test"
//    val logStorage = new LocalFSLogStorage(workerName)
//    logStorage.deleteLog()
//    val writer = logStorage.getWriter
//    val determinants: Array[InMemDeterminant] = Array(
//      ProcessControlMessage(
//        ReturnInvocation(16, WorkerStatistics(COMPLETED, 6, 2)),
//        ActorVirtualIdentity(
//          "WF-KeywordSearch-operator-44478988-0d44-43c0-ab0d-f52fd5885ba4-main-0"
//        )
//      ),
//      ProcessControlMessage(
//        ReturnInvocation(4, ()),
//        ActorVirtualIdentity("WF-SimpleSink-operator-06d5e7e6-dbd1-40e4-87d6-133d33559aa8-main-0")
//      ),
//      StepDelta(ActorVirtualIdentity("Upstream"), 1),
//      StepDelta(ActorVirtualIdentity("Upstream"), 29),
//      ProcessControlMessage(
//        ReturnInvocation(9, (1, 2, 3, 4)),
//        ActorVirtualIdentity("WF-SimpleSink-operator-06d5e7e6-dbd1-40e4-87d6-133d33559aa8-main-0")
//      )
//    )
//    determinants.foreach(writer.writeLogRecord)
//    writer.flush()
//    writer.close()
//    var upstream: ActorVirtualIdentity = null
//    var stepAccumulated = 0
//    val creditMonitor = new CreditMonitorImpl()
//    val inputHub = new RecoveryInternalQueueImpl(creditMonitor)
//    inputHub.initialize(logStorage.getReader.mkLogRecordIterator(), 0, () => {})
//    var currentStep = 0L
//    determinants.foreach {
//      case StepsOnChannel(from, steps) =>
//        stepAccumulated += steps.toInt
//        upstream = from
//      case ProcessControlMessage(controlPayload, from) =>
//        if (stepAccumulated > 0) {
//          (0 until stepAccumulated).foreach { _ =>
//            inputHub.enqueueData(InputTuple(upstream, ITuple(1, 2, 3)))
//            currentStep += 1
//            assert(inputHub.take(currentStep) == InputTuple(upstream, ITuple(1, 2, 3)))
//          }
//        }
//        assert(inputHub.take(currentStep) == ControlElement(controlPayload, from))
//    }
//    logStorage.deleteLog()
//  }

  "Logreader" should "not read anything from empty log" in {
    val workerName = "WF1-SimpleSink-operator-058c6027-5134-4a0d-b345-77227115ee76-main-0"
    val logStorage = new LocalFSLogStorage(workerName)
    val iter = logStorage.getReader.mkLogRecordIterator()
    while (iter.hasNext) {
      println(iter.next())
    }
  }

}
