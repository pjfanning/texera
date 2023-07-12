package edu.uci.ics.texera.workflow.operators.source.scan.csv

import com.univocity.parsers.csv.{CsvFormat, CsvParser, CsvParserSettings}
import edu.uci.ics.texera.workflow.common.operators.source.SourceOperatorExecutor
import edu.uci.ics.texera.workflow.common.tuple.Tuple
import edu.uci.ics.texera.workflow.common.tuple.schema.{AttributeTypeUtils, Schema}

import java.io.{File, FileInputStream, InputStreamReader}

class CSVScanSourceOpExec private[csv] (val desc: CSVScanSourceOpDesc)
    extends SourceOperatorExecutor {
  val schema: Schema = desc.inferSchema()
  var inputReader: InputStreamReader = _
  var parser: CsvParser = _

  var nextRow: Array[String] = _
  var sumLen: Array[Int] = _
  var numRowGenerated = 0

  class CSVSourceTupleIterator(iter: Iterator[Tuple]) extends Iterator[Tuple] {
    override def hasNext: Boolean = iter.hasNext

    override def next(): Tuple = {
      numRowGenerated += 1
      iter.next()
    }
  }

  def mkRowIterator: Iterator[Array[String]] = {
    new Iterator[Array[String]] {
      override def hasNext: Boolean = {
        if (nextRow != null) {
          return true
        }
        nextRow = parser.parseNext()
        nextRow != null
      }

      override def next(): Array[String] = {
        val ret = nextRow
        var idx = 0
        for (elem <- nextRow) {
          sumLen(idx) += (if (elem != null) { elem.length }
                          else { 0 })
          idx += 1
        }
        nextRow = null
        ret
      }
    }
  }

  def mkTupleIterator(iter: Iterator[Array[String]]): Iterator[Tuple] = {
    iter
      .map(row => {
        try {
          val parsedFields: Array[Object] = {
            AttributeTypeUtils.parseFields(row.asInstanceOf[Array[Object]], schema)
          }
          Tuple.newBuilder(schema).addSequentially(parsedFields).build
        } catch {
          case _: Throwable => null
        }
      })
      .filter(t => t != null)
  }

  override def produceTexeraTuple(): Iterator[Tuple] = {
    var tupleIterator = mkTupleIterator(
      mkRowIterator
        .drop(desc.offset.getOrElse(0))
    )
    if (desc.limit.isDefined) tupleIterator = tupleIterator.take(desc.limit.get)
    new CSVSourceTupleIterator(tupleIterator)
  }

  override def open(): Unit = {
    inputReader = new InputStreamReader(
      new FileInputStream(new File(desc.filePath.get)),
      desc.fileEncoding.getCharset
    )
    val csvFormat = new CsvFormat()
    csvFormat.setDelimiter(desc.customDelimiter.get.charAt(0))
    csvFormat.setComment(
      '\u0000'
    ) // disable skipping lines starting with # (default comment character)
    val csvSetting = new CsvParserSettings()
    csvSetting.setMaxCharsPerColumn(-1)
    csvSetting.setFormat(csvFormat)
    csvSetting.setHeaderExtractionEnabled(desc.hasHeader)
    sumLen = Array.fill(schema.getAttributes.size())(0)
    parser = new CsvParser(csvSetting)
    parser.beginParsing(inputReader)
  }

  override def close(): Unit = {
    if (parser != null) {
      parser.stopParsing()
    }
    if (inputReader != null) {
      inputReader.close()
    }
  }

  override def getStateInformation: String = {
    s"Scan: average length of each field in byte = ${sumLen.map(i => i / numRowGenerated).mkString(",")}, current Tuple = $nextRow"
  }
}
