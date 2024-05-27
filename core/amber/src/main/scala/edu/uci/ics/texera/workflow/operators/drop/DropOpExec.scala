package edu.uci.ics.texera.workflow.operators.drop

import com.google.common.base.Preconditions
import edu.uci.ics.amber.engine.common.tuple.amber.TupleLike
import edu.uci.ics.texera.workflow.common.operators.map.MapOpExec
import edu.uci.ics.texera.workflow.common.tuple.Tuple

import scala.collection.mutable

class DropOpExec(selectedAttributes: List[String], isDrop: Boolean) extends MapOpExec {

  setMapFunc(project)
  def project(tuple: Tuple): TupleLike = {
    Preconditions.checkArgument(selectedAttributes.nonEmpty)
    val allAttribute = tuple.schema.getAttributeNames
    var attributeUnits = selectedAttributes
    if (isDrop) {
      attributeUnits = allAttribute.diff(selectedAttributes)
    }

    val fields = mutable.LinkedHashMap[String, Any]()
    attributeUnits.foreach { attributeUnit =>
      val alias = attributeUnit
      if (fields.contains(alias)) {
        throw new RuntimeException("have duplicated attribute name/alias")
      }
      fields(alias) = tuple.getField[Any](attributeUnit)
    }

    TupleLike(fields.toSeq: _*)
  }

}
