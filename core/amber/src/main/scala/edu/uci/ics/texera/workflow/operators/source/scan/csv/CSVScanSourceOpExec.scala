package edu.uci.ics.texera.workflow.operators.source.scan.csv

import com.univocity.parsers.csv.{CsvFormat, CsvParser, CsvParserSettings}
import edu.uci.ics.amber.engine.common.tuple.ITuple
import edu.uci.ics.amber.engine.common.workflow.PortIdentity
import edu.uci.ics.amber.engine.common.{CheckpointState, CheckpointSupport}
import edu.uci.ics.texera.workflow.common.operators.source.SourceOperatorExecutor
import edu.uci.ics.texera.workflow.common.tuple.Tuple
import edu.uci.ics.texera.workflow.common.tuple.schema.{AttributeTypeUtils, Schema}

import java.io.{File, FileInputStream, InputStreamReader}

class CSVScanSourceOpExec private[csv] (val desc: CSVScanSourceOpDesc)
    extends SourceOperatorExecutor with CheckpointSupport {
  val schema: Schema = desc.inferSchema()
  var inputReader: InputStreamReader = _
  var parser: CsvParser = _

  var nextRow: Array[String] = _
  var numRowGenerated = 0

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
        numRowGenerated += 1
        nextRow = null
        ret
      }
    }

    var tupleIterator = rowIterator
      .drop(desc.offset.getOrElse(0))
      .map(row => {
        try {
          val parsedFields: Array[Object] =
            AttributeTypeUtils.parseFields(row.asInstanceOf[Array[Object]], schema)
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
    inputReader = new InputStreamReader(
      new FileInputStream(new File(desc.filePath.get)),
      desc.fileEncoding.getCharset
    )

    val csvFormat = new CsvFormat()
    csvFormat.setDelimiter(desc.customDelimiter.get.charAt(0))
    csvFormat.setLineSeparator("\n")
    csvFormat.setComment(
      '\u0000'
    ) // disable skipping lines starting with # (default comment character)
    val csvSetting = new CsvParserSettings()
    csvSetting.setMaxCharsPerColumn(-1)
    csvSetting.setFormat(csvFormat)
    csvSetting.setHeaderExtractionEnabled(desc.hasHeader)

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

  override def serializeState(currentIteratorState: Iterator[(ITuple, Option[PortIdentity])], checkpoint: CheckpointState): Iterator[(ITuple, Option[PortIdentity])] = {
    checkpoint.save(
      "numOutputRows",
      numRowGenerated
    )
    currentIteratorState
  }

  override def deserializeState(checkpoint: CheckpointState): Iterator[(ITuple, Option[PortIdentity])] = {
    open()
    numRowGenerated = checkpoint.load("numOutputRows")
    var tupleIterator = produceTexeraTuple() .drop(numRowGenerated)
    if (desc.limit.isDefined) tupleIterator = tupleIterator.take(desc.limit.get - numRowGenerated)
    tupleIterator.map(tuple => (tuple, Option.empty))
  }

  override def getEstimatedCheckpointCost: Long = 0L

  override def getState: String = {
    s"numRowGenerated\n$numRowGenerated"
  }
}
