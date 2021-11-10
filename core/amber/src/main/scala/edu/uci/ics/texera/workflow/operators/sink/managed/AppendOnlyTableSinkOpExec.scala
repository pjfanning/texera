package edu.uci.ics.texera.workflow.operators.sink.managed

import com.typesafe.scalalogging.LazyLogging
import edu.uci.ics.amber.engine.common.tuple.ITuple
import edu.uci.ics.amber.engine.common.virtualidentity.LinkIdentity
import edu.uci.ics.amber.engine.common.{IOperatorExecutor, ISinkOperatorExecutor, InputExhausted}
import edu.uci.ics.texera.workflow.common.tuple.Tuple
import edu.uci.ics.texera.workflow.operators.sink.storage.ShardedStorage

class AppendOnlyTableSinkOpExec(storage: ShardedStorage) extends ISinkOperatorExecutor with LazyLogging {

  override def open(): Unit = storage.open()

  override def close(): Unit = storage.close()

  override def consume(
      tuple: Either[ITuple, InputExhausted],
      input: LinkIdentity
  ): Unit = {
    tuple match {
      case Left(t)  => storage.putOne(t.asInstanceOf[Tuple])
      case Right(_) => //skip
    }
  }
}
