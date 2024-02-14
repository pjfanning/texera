package edu.uci.ics.texera.workflow.operators.loop

import edu.uci.ics.texera.workflow.common.operators.source.SourceOperatorExecutor
import edu.uci.ics.texera.workflow.common.tuple.Tuple
import edu.uci.ics.texera.workflow.common.tuple.schema.Schema

class GeneratorOpExec(
    val iteration: Int,
    val attributes: List[RangeAttribute],
    val outputSchema: Schema
) extends SourceOperatorExecutor {
  override def produceTexeraTuple(): Iterator[Tuple] = {
    attributes
      .map(attribute =>
        (attribute.start until attribute.start + attribute.step * iteration by attribute.step).map(
          i => (outputSchema.getAttribute(attribute.name), i)
        )
      )
      .transpose
      .map(x => {
        val b = Tuple.newBuilder(outputSchema)
        x.map(y => b.add(y._1, y._2))
        b.build()
      })
      .iterator
  }

  override def open(): Unit = {}

  override def close(): Unit = {}
}
