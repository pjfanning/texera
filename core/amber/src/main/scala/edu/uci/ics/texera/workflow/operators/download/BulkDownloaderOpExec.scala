package edu.uci.ics.texera.workflow.operators.download

import edu.uci.ics.amber.engine.architecture.worker.PauseManager
import edu.uci.ics.amber.engine.common.InputExhausted
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCClient
import edu.uci.ics.texera.web.resource.dashboard.file.UserFileResource
import edu.uci.ics.texera.workflow.common.WorkflowContext
import edu.uci.ics.texera.workflow.common.operators.OperatorExecutor
import edu.uci.ics.texera.workflow.common.tuple.Tuple
import edu.uci.ics.texera.workflow.common.tuple.schema.{AttributeType, OperatorSchemaInfo}
import org.jooq.types.UInteger

import java.io.{BufferedWriter, File, FileWriter}
import java.net.URL
import java.util
import java.util.UUID
import java.util.concurrent.LinkedBlockingQueue
import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.io.Source
import scala.concurrent.duration._
import scala.collection.JavaConversions._
import scala.collection.mutable

class BulkDownloaderOpExec(
    val workflowContext: WorkflowContext,
    val urlAttribute: String,
    val resultAttribute: String,
    val operatorSchemaInfo: OperatorSchemaInfo
) extends OperatorExecutor {
  private val downloading = new mutable.Queue[Future[Tuple]]()

  class DownloadResultIterator(blocking: Boolean) extends Iterator[Tuple] {
    override def hasNext: Boolean = {
      if (downloading.isEmpty) {
        return false
      }
      if (blocking) {
        Await.result(downloading.head, 5.seconds)
      }
      downloading.head.isCompleted
    }

    override def next(): Tuple = {
      downloading.dequeue().value.get.get
    }
  }

  override def processTexeraTuple(
      tuple: Either[Tuple, InputExhausted],
      input: Int,
      pauseManager: PauseManager,
      asyncRPCClient: AsyncRPCClient
  ): Iterator[Tuple] = {
    tuple match {
      case Left(t) =>
        downloading.enqueue(Future { downloadTuple(t) })
        new DownloadResultIterator(false)
      case Right(_) =>
        new DownloadResultIterator(true)
    }
  }

  override def open(): Unit = {}

  override def close(): Unit = {}

  def downloadTuple(tuple: Tuple): Tuple = {
    val builder = Tuple.newBuilder(operatorSchemaInfo.outputSchemas(0))
    operatorSchemaInfo
      .outputSchemas(0)
      .getAttributes
      .foreach(attr => {
        if (attr.getName == resultAttribute) {
          builder.add(
            resultAttribute,
            AttributeType.STRING,
            downloadUrl(tuple.getField(urlAttribute))
          )
        } else {
          builder.add(
            attr.getName,
            tuple.getSchema.getAttribute(attr.getName).getType,
            tuple.getField(attr.getName)
          )
        }
      })

    builder.build()
  }

  def downloadUrl(url: String): String = {
    try {
      Await.result(
        Future {
          val urlObj = new URL(url)
          val input = urlObj.openStream()
          if (input.available() > 0) {
            UserFileResource
              .saveUserFileSafe(
                workflowContext.userId.get,
                s"w${workflowContext.wId}-e${workflowContext.executionID}-${urlObj.getHost.replace(".", "")}.download",
                input,
                s"downloaded by execution ${workflowContext.executionID} of workflow ${workflowContext.wId}. Original URL = $url"
              )
          } else {
            throw new RuntimeException(s"content is not available for $url")
          }
        },
        5.seconds
      )
    } catch {
      case throwable: Throwable => s"Failed: ${throwable.getMessage}"
    }
  }

}
