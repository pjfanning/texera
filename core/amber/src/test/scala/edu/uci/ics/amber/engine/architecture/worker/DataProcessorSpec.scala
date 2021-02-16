package edu.uci.ics.amber.engine.architecture.worker

import com.softwaremill.macwire.wire
import edu.uci.ics.amber.engine.architecture.controller.promisehandlers.WorkerExecutionCompletedHandler.WorkerExecutionCompleted
import edu.uci.ics.amber.engine.architecture.messaginglayer.{
  BatchToTupleConverter,
  ControlInputPort,
  ControlOutputPort,
  DataInputPort,
  DataOutputPort,
  TupleToBatchConverter
}
import edu.uci.ics.amber.engine.architecture.worker.WorkerInternalQueue._
import edu.uci.ics.amber.engine.common.ambermessage.ControlPayload
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCClient.ControlInvocation
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.{CommandCompleted, ControlCommand}
import edu.uci.ics.amber.engine.common.{InputExhausted, WorkflowLogger}
import edu.uci.ics.amber.engine.common.rpc.{AsyncRPCClient, AsyncRPCServer}
import edu.uci.ics.amber.engine.common.statetransition.WorkerStateManager
import edu.uci.ics.amber.engine.common.statetransition.WorkerStateManager.{
  Completed,
  Ready,
  Running
}
import edu.uci.ics.amber.engine.common.tuple.ITuple
import edu.uci.ics.amber.engine.common.virtualidentity.{
  ActorVirtualIdentity,
  LayerIdentity,
  LinkIdentity
}
import edu.uci.ics.texera.workflow.common.operators.OperatorExecutor
import org.scalamock.scalatest.MockFactory
import org.scalatest.flatspec.AnyFlatSpec

class DataProcessorSpec extends AnyFlatSpec with MockFactory {
  lazy val logger: WorkflowLogger = WorkflowLogger("testDP")
  lazy val pauseManager: PauseManager = mock[PauseManager]
  lazy val dataOutputPort: DataOutputPort = mock[DataOutputPort]
  lazy val batchProducer: TupleToBatchConverter = mock[TupleToBatchConverter]
  lazy val breakpointManager: BreakpointManager = mock[BreakpointManager]
  lazy val controlInputPort: ControlInputPort = mock[WorkerControlInputPort]
  lazy val controlOutputPort: ControlOutputPort = mock[ControlOutputPort]
  val linkID: LinkIdentity =
    LinkIdentity(LayerIdentity("testDP", "mockOp", "src"), LayerIdentity("testDP", "mockOp", "dst"))
  val tuples: Seq[ITuple] = (0 until 400).map(ITuple(_))

  case class DummyControl() extends ControlCommand[CommandCompleted]

  def sendDataToDP(dp: DataProcessor, data: Seq[ITuple], interval: Long = -1): Unit = {
    new Thread {
      override def run {
        dp.appendElement(SenderChangeMarker(linkID))
        data.foreach { x =>
          dp.appendElement(InputTuple(x))
          if (interval > 0) {
            Thread.sleep(interval)
          }
        }
        dp.appendElement(EndMarker)
        dp.appendElement(EndOfAllMarker)
      }
    }.start()
  }

  def sendControlToDP(
      dp: DataProcessor,
      control: Seq[ControlPayload],
      interval: Long = -1
  ): Unit = {
    new Thread {
      override def run {
        control.foreach { x =>
          dp.enqueueCommand(x, ActorVirtualIdentity.Controller)
          if (interval > 0) {
            Thread.sleep(interval)
          }
        }
      }
    }.start()
  }

  def waitForDataProcessing(workerStateManager: WorkerStateManager): Unit = {
    while (workerStateManager.getCurrentState != Completed) {
      //wait
    }
  }

  def waitForControlProcessing(dp: DataProcessor): Unit = {
    while (!dp.isControlQueueEmpty) {
      //wait
    }
  }

  "data processor" should "process data messages" in {
    val asyncRPCClient: AsyncRPCClient = mock[AsyncRPCClient]
    val operator = mock[OperatorExecutor]
    val asyncRPCServer: AsyncRPCServer = null
    val workerStateManager: WorkerStateManager = new WorkerStateManager(Running)
    inAnyOrder {
      (pauseManager.isPaused _).expects().anyNumberOfTimes()
      (batchProducer.emitEndOfUpstream _).expects().anyNumberOfTimes()
      (asyncRPCClient.send[CommandCompleted] _).expects(*, *).anyNumberOfTimes()
      inSequence {
        (operator.open _).expects().once()
        tuples.foreach { x =>
          (operator.processTuple _).expects(Left(x), linkID)
        }
        (operator.processTuple _).expects(Right(InputExhausted()), linkID)
        (operator.close _).expects().once()
      }
    }

    val dp = wire[DataProcessor]
    sendDataToDP(dp, tuples)
    waitForDataProcessing(workerStateManager)
    dp.shutdown()

  }

  "data processor" should "prioritize control messages" in {
    val asyncRPCClient: AsyncRPCClient = mock[AsyncRPCClient]
    val operator = mock[OperatorExecutor]
    val workerStateManager: WorkerStateManager = new WorkerStateManager(Running)
    val asyncRPCServer: AsyncRPCServer = mock[AsyncRPCServer]
    inAnyOrder {
      (pauseManager.isPaused _).expects().anyNumberOfTimes()
      (asyncRPCServer.logControlInvocation _).expects(*, *).anyNumberOfTimes()
      (asyncRPCClient.send[CommandCompleted] _).expects(*, *).anyNumberOfTimes()
      inSequence {
        (operator.open _).expects().once()
        inAnyOrder {
          tuples.map { x =>
            (operator.processTuple _).expects(Left(x), linkID)
          }
          (asyncRPCServer.receive _).expects(*, *).atLeastOnce() //process controls during execution
        }
        (operator.processTuple _).expects(Right(InputExhausted()), linkID)
        (asyncRPCServer.receive _)
          .expects(*, *)
          .anyNumberOfTimes() // process controls before execution completes
        (batchProducer.emitEndOfUpstream _).expects().once()
        (asyncRPCServer.receive _)
          .expects(*, *)
          .anyNumberOfTimes() // process controls after execution
        (operator.close _).expects().once()
      }
    }
    val dp = wire[DataProcessor]
    sendDataToDP(dp, tuples, 2)
    sendControlToDP(dp, (0 until 100).map(_ => ControlInvocation(0, DummyControl())), 3)
    waitForDataProcessing(workerStateManager)
    waitForControlProcessing(dp)
    dp.shutdown()
  }

  "data processor" should "unblock once getting special message" in {
    val asyncRPCClient: AsyncRPCClient = mock[AsyncRPCClient]
    val operator = mock[OperatorExecutor]
    val workerStateManager: WorkerStateManager = new WorkerStateManager(Running)
    val asyncRPCServer: AsyncRPCServer = mock[AsyncRPCServer]
    inAnyOrder {
      (operator.open _).expects().once()
      (pauseManager.isPaused _).expects().anyNumberOfTimes()
      (asyncRPCServer.logControlInvocation _).expects(*, *).anyNumberOfTimes()
      (asyncRPCClient.send[CommandCompleted] _).expects(*, *).anyNumberOfTimes()
      (operator.close _).expects().once()
    }
    val dp = wire[DataProcessor]
    (asyncRPCServer.receive _).expects(*, *).never()
    sendControlToDP(dp, (0 until 3).map(_ => ControlInvocation(0, DummyControl())))
    Thread.sleep(3000)
    (asyncRPCServer.receive _).expects(*, *).repeat(3)
    dp.appendElement(UnblockForControlCommands)
    waitForControlProcessing(dp)
    dp.shutdown()
  }

}
