package edu.uci.ics.texera.compilation.core.operators.unneststring

import edu.uci.ics.amber.core.tuple.{Tuple, TupleLike}
import edu.uci.ics.texera.compilation.core.operators.flatmap.FlatMapOpExec

class UnnestStringOpExec(attributeName: String, delimiter: String) extends FlatMapOpExec {

  setFlatMapFunc(splitByDelimiter)
  private def splitByDelimiter(tuple: Tuple): Iterator[TupleLike] = {
    delimiter.r
      .split(tuple.getField(attributeName).toString)
      .filter(_.nonEmpty)
      .iterator
      .map(split => TupleLike(tuple.getFields ++ Seq(split)))
  }
}
