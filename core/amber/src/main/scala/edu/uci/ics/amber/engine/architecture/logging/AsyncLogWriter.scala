package edu.uci.ics.amber.engine.architecture.logging

import com.google.common.collect.Queues
import edu.uci.ics.amber.engine.architecture.logging.AsyncLogWriter.{GetMessageInQueueSync, LogWriterOutputMessage, SendRequest}
import edu.uci.ics.amber.engine.architecture.logging.storage.DeterminantLogStorage.DeterminantLogWriter
import edu.uci.ics.amber.engine.architecture.messaginglayer.NetworkCommunicationActor
import edu.uci.ics.amber.engine.architecture.messaginglayer.NetworkCommunicationActor.{GetMessageInQueue, NetworkMessage}
import edu.uci.ics.amber.engine.common.AmberUtils
import edu.uci.ics.amber.engine.common.ambermessage.WorkflowMessage
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity

import java.util
import java.util.concurrent.CompletableFuture
import scala.collection.JavaConverters._
import scala.concurrent.Await
import akka.pattern.ask
import akka.remote.transport.ActorTransportAdapter.AskTimeout

import scala.concurrent.duration.DurationInt

object AsyncLogWriter{
  sealed trait LogWriterOutputMessage

  final case class SendRequest(id: ActorVirtualIdentity, message: WorkflowMessage) extends LogWriterOutputMessage

  final case class GetMessageInQueueSync(sync:CompletableFuture[Array[(ActorVirtualIdentity, Iterable[NetworkMessage])]]) extends LogWriterOutputMessage
}


class AsyncLogWriter(
    networkCommunicationActor: NetworkCommunicationActor.NetworkSenderActorRef,
    writer: DeterminantLogWriter
) extends Thread {
  private val drained = new util.ArrayList[Either[InMemDeterminant, LogWriterOutputMessage]]()
  private val writerQueue =
    Queues.newLinkedBlockingQueue[Either[InMemDeterminant, LogWriterOutputMessage]]()
  private var stopped = false
  private val logInterval =
    AmberUtils.amberConfig.getLong("fault-tolerance.log-flush-interval-ms")
  private val gracefullyStopped = new CompletableFuture[Unit]()

  def putDeterminants(determinants: Array[InMemDeterminant]): Unit = {
    assert(!stopped)
    determinants.foreach(x => {
      writerQueue.put(Left(x))
    })
  }

  def putOutput(output: LogWriterOutputMessage): Unit = {
    assert(!stopped)
    writerQueue.put(Right(output))
  }

  def terminate(): Unit = {
    stopped = true
    writerQueue.put(Left(TerminateSignal))
    gracefullyStopped.get()
  }

  override def run(): Unit = {
    var internalStop = false
    while (!internalStop) {
      if (logInterval > 0) {
        Thread.sleep(logInterval)
      }
      internalStop = drainWriterQueueAndProcess()
    }
    writer.close()
    gracefullyStopped.complete(())
  }

  def drainWriterQueueAndProcess(): Boolean = {
    var stop = false
    if (writerQueue.drainTo(drained) == 0) {
      drained.add(writerQueue.take())
    }
    var drainedScala = drained.asScala
    if (drainedScala.last == Left(TerminateSignal)) {
      drainedScala = drainedScala.dropRight(1)
      stop = true
    }
    drainedScala
      .filter(_.isLeft)
      .map(_.left.get)
      .foreach{
        case TerminateSignal => stop = true
        case other => writer.writeLogRecord(other)
      }
    writer.flush()
    drainedScala.filter(_.isRight).map(_.right.get).foreach{
      case GetMessageInQueueSync(sync) => sync.complete(Await.result(networkCommunicationActor.ref ? GetMessageInQueue, 5.seconds).asInstanceOf[Array[(ActorVirtualIdentity, Iterable[NetworkMessage])]])
      case x: SendRequest => networkCommunicationActor ! x
    }
    drained.clear()
    stop
  }

}
