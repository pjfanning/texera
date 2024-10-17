package edu.uci.ics.texera.compilation.core.operators.regex

import edu.uci.ics.amber.core.tuple.Tuple
import edu.uci.ics.texera.compilation.core.operators.filter.FilterOpExec

import java.util.regex.Pattern

class RegexOpExec(regex: String, caseInsensitive: Boolean, attributeName: String)
    extends FilterOpExec {
  lazy val pattern: Pattern =
    Pattern.compile(regex, if (caseInsensitive) Pattern.CASE_INSENSITIVE else 0)
  this.setFilterFunc(this.matchRegex)
  private def matchRegex(tuple: Tuple): Boolean =
    Option[Any](tuple.getField(attributeName).toString)
      .map(_.toString)
      .exists(value => pattern.matcher(value).find)
}
