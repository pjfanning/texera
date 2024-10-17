package edu.uci.ics.texera.compilation.core.operators.source

import com.google.common.base.Preconditions
import edu.uci.ics.amber.core.tuple.Schema
import edu.uci.ics.texera.compilation.core.operators.LogicalOp

abstract class SourceOperatorDescriptor extends LogicalOp {

  override def getOutputSchema(schemas: Array[Schema]): Schema = {
    Preconditions.checkArgument(schemas.isEmpty)
    sourceSchema()
  }

  def sourceSchema(): Schema

}
