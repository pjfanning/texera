package edu.uci.ics.texera.workflow.operators.source.scan.csv

import akka.serialization.Serialization
import com.univocity.parsers.csv.{CsvFormat, CsvParser, CsvParserSettings}
import edu.uci.ics.amber.engine.architecture.checkpoint.SerializedState
import edu.uci.ics.amber.engine.common.tuple.ITuple
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
  var numRowOutputted = 0

  override def produceTexeraTuple(): Iterator[Tuple] = {

    val rowIterator = new Iterator[Array[String]] {
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
          sumLen(idx) += elem.length
          idx += 1
        }
        numRowOutputted += 1
        nextRow = null
        ret
      }
    }

    var tupleIterator = rowIterator
      .drop(desc.offset.getOrElse(0))
      .map(row => {
        try {
          val parsedFields: Array[Object] = {
            Thread.sleep(20)
            AttributeTypeUtils.parseFields(row.asInstanceOf[Array[Object]], schema)
          }
          Tuple.newBuilder(schema).addSequentially(parsedFields).build
        } catch {
          case _: Throwable => null
        }
      })
      .filter(t => t != null)

    if (desc.limit.isDefined) tupleIterator = tupleIterator.take(desc.limit.get)

    tupleIterator
  }

  override def open(): Unit = {
    inputReader = new InputStreamReader(new FileInputStream(new File(desc.filePath.get)))

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
    s"Scan: average length of each field in byte: ${sumLen.map(i => i / numRowOutputted).mkString(",")}, current Tuple = $nextRow"
  }

  override def serializeState(
                      currentIteratorState: Iterator[(ITuple, Option[Int])],
                      serializer: Serialization
                    ): SerializedState = {
    SerializedState.fromObject(Int.box(numRowOutputted), serializer)
  }

  override def deserializeState(
                        serializedState: SerializedState,
                        deserializer: Serialization
                      ): Iterator[(ITuple, Option[Int])] = {
    open()
    numRowOutputted = serializedState.toObject(deserializer)
    produceTexeraTuple().drop(numRowOutputted - 1).map(tuple => (tuple, Option.empty))
  }
}
