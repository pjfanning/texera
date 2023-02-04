package edu.uci.ics.texera.workflow.operators.keywordSearch

import edu.uci.ics.texera.workflow.common.operators.filter.FilterOpExec
import edu.uci.ics.texera.workflow.common.tuple.Tuple
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.analysis.core.SimpleAnalyzer
import org.apache.lucene.index.memory.MemoryIndex
import org.apache.lucene.search.Query

class KeywordSearchOpExec(val opDesc: KeywordSearchOpDesc) extends FilterOpExec {
  @transient lazy val analyzer = new SimpleAnalyzer()
  @transient lazy val query: Query =
    new QueryParser(opDesc.attribute, analyzer).parse(opDesc.keyword)
  @transient lazy val memoryIndex: MemoryIndex = new MemoryIndex()

  private var last_matched:Tuple = _

  this.setFilterFunc(this.findKeyword)

  def findKeyword(tuple: Tuple): Boolean = {
    if (tuple.getField(opDesc.attribute) == null) {
      false
    } else {
      val fieldValue = tuple.getField(opDesc.attribute).toString
      memoryIndex.addField(opDesc.attribute, fieldValue, analyzer)
      val isMatch = memoryIndex.search(query) > 0.0f
      memoryIndex.reset()
      if(isMatch){
        last_matched = tuple
      }
      isMatch
    }
  }

  override def getStateInformation: String = "Keyword search: Last matched tuple = "+last_matched.getFields.toArray.mkString(",")

}
