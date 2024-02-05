package edu.uci.ics.texera.workflow.operators.loop

import edu.uci.ics.amber.engine.architecture.worker.DataProcessor.EndOfIteration
import edu.uci.ics.amber.engine.architecture.worker.PauseManager
import edu.uci.ics.amber.engine.common.InputExhausted
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCClient
import edu.uci.ics.amber.engine.common.tuple.ITuple
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity
import edu.uci.ics.amber.engine.common.workflow.PortIdentity
import edu.uci.ics.texera.workflow.common.operators.OperatorExecutor
import edu.uci.ics.texera.workflow.common.tuple.Tuple

import scala.collection.mutable

class LoopStartOpExec(val workerId: ActorVirtualIdentity, val termination: Int) extends OperatorExecutor {
  var iteration = 0
  var buffer = new mutable.ArrayBuffer[ITuple]
  override def processTuple(
      tuple: Either[ITuple, InputExhausted],
      input: Int,
      pauseManager: PauseManager,
      asyncRPCClient: AsyncRPCClient
  ): Iterator[(ITuple, Option[PortIdentity])] = {
    tuple match {
      case Left(t) =>
        t match {
          case t: EndOfIteration =>
            iteration += 1;
          case t =>
            if(iteration == 0){
              buffer.append(t)
            }
        }
        Iterator((t, None))
      case Right(_) =>
        if(iteration == 0) {
          iteration += 1;
          Iterator((EndOfIteration(workerId), None))
        }else{
          Iterator.empty
        }
    }
  }

  override def open(): Unit = {}

  override def close(): Unit = {}

  override def processTexeraTuple(
      tuple: Either[Tuple, InputExhausted],
      input: Int,
      pauseManager: PauseManager,
      asyncRPCClient: AsyncRPCClient
  ): Iterator[Tuple] = ???
}
