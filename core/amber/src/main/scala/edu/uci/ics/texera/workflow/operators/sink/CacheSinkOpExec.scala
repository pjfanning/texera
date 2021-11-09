package edu.uci.ics.texera.workflow.operators.sink

import com.typesafe.scalalogging.LazyLogging
import edu.uci.ics.amber.engine.common.tuple.ITuple
import edu.uci.ics.amber.engine.common.virtualidentity.LinkIdentity
import edu.uci.ics.amber.engine.common.{ITupleSinkOperatorExecutor, InputExhausted}
import edu.uci.ics.texera.workflow.common.IncrementalOutputMode
import edu.uci.ics.texera.workflow.common.storage.OpResultStorage
import edu.uci.ics.texera.workflow.common.tuple.Tuple
import edu.uci.ics.texera.workflow.operators.sink.storage.{ShardedStorage, SinkStorage}

import scala.collection.mutable

class CacheSinkOpExec(storage: ShardedStorage) extends ITupleSinkOperatorExecutor with LazyLogging {

  override def getResultTuples(): List[ITuple] = {
    List.empty
  }

  override def getOutputMode(): IncrementalOutputMode = IncrementalOutputMode.SET_SNAPSHOT

  override def open(): Unit = storage.open()

  override def close(): Unit = storage.close()

  override def processTuple(
      tuple: Either[ITuple, InputExhausted],
      input: LinkIdentity
  ): Iterator[ITuple] = {
    tuple match {
      case Left(t)  => storage.putOne(t.asInstanceOf[Tuple])
      case Right(_) => //skip
    }
    Iterator()
  }
}
