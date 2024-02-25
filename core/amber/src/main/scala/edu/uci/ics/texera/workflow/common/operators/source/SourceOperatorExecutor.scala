package edu.uci.ics.texera.workflow.common.operators.source

import edu.uci.ics.amber.engine.common.ISourceOperatorExecutor
import edu.uci.ics.amber.engine.common.tuple.amber.TupleLike
import edu.uci.ics.texera.workflow.common.tuple.Tuple

trait SourceOperatorExecutor extends ISourceOperatorExecutor {

  override def produceTuple(): Iterator[TupleLike] = {
    produceTexeraTuple()
  }

  def produceTexeraTuple(): Iterator[TupleLike]

}
