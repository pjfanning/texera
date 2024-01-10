package edu.uci.ics.amber.engine.architecture.messaginglayer

import com.twitter.util.{Future, Promise}
import edu.uci.ics.amber.engine.common.AmberLogging
import edu.uci.ics.amber.engine.common.ambermessage.{ChannelID, MarkerPayload, WorkflowFIFOMessage}
import edu.uci.ics.amber.engine.common.ambermessage.WorkflowMessage.getInMemSize
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity

import java.util.concurrent.atomic.AtomicLong
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

/* The abstracted FIFO/exactly-once logic */
class AmberFIFOChannel(val channelId: ChannelID) extends AmberLogging {

  private final case class MessageCollector(promise:Promise[Iterable[WorkflowFIFOMessage]], collectedMessages:mutable.ArrayBuffer[WorkflowFIFOMessage])

  override def actorId: ActorVirtualIdentity = channelId.to

  private val ofoMap = new mutable.HashMap[Long, WorkflowFIFOMessage]
  private var current = 0L
  private var enabled = true
  private val fifoQueue = new mutable.Queue[WorkflowFIFOMessage]
  private val holdCredit = new AtomicLong()
  private val messageCollectors = new mutable.HashMap[String, MessageCollector]

  def acceptMessage(msg: WorkflowFIFOMessage): Unit = {
    val seq = msg.sequenceNumber
    val payload = msg.payload
    if (isDuplicated(seq)) {
      logger.debug(
        s"received duplicated message $payload with seq = $seq while current seq = $current"
      )
    } else if (isAhead(seq)) {
      logger.debug(s"received ahead message $payload with seq = $seq while current seq = $current")
      stash(seq, msg)
    } else {
      enforceFIFO(msg)
    }
  }

  def collectMessagesUntilMarker(markerId:String):Future[Iterable[WorkflowFIFOMessage]] = {
    val promise = Promise[Iterable[WorkflowFIFOMessage]]()
    messageCollectors(markerId) = MessageCollector(promise, new ArrayBuffer[WorkflowFIFOMessage]())
    promise
  }

  def getCurrentSeq: Long = current

  private def isDuplicated(sequenceNumber: Long): Boolean =
    sequenceNumber < current || ofoMap.contains(sequenceNumber)

  private def isAhead(sequenceNumber: Long): Boolean = sequenceNumber > current

  private def stash(sequenceNumber: Long, data: WorkflowFIFOMessage): Unit = {
    ofoMap(sequenceNumber) = data
  }

  private def enforceFIFO(data: WorkflowFIFOMessage): Unit = {
    fifoQueue.enqueue(data)
    afterEnqueueMessage(data)
    while (ofoMap.contains(current)) {
      val msg = ofoMap(current)
      fifoQueue.enqueue(msg)
      ofoMap.remove(current)
      afterEnqueueMessage(msg)
    }
  }

   @inline private def afterEnqueueMessage(msg:WorkflowFIFOMessage): Unit = {
    holdCredit.getAndAdd(getInMemSize(msg))
    messageCollectors.values.foreach{
      collector: MessageCollector =>
        collector.collectedMessages.append(msg)
    }
    msg.payload match {
      case payload: MarkerPayload =>
        if(messageCollectors.contains(payload.id)){
          val collector = messageCollectors(payload.id)
          collector.promise.setValue(collector.collectedMessages)
          messageCollectors.remove(payload.id)
        }
      case _ => // skip
    }
    current += 1
  }

  def take: WorkflowFIFOMessage = {
    val msg = fifoQueue.dequeue()
    holdCredit.getAndAdd(-getInMemSize(msg))
    msg
  }

  def hasMessage: Boolean = fifoQueue.nonEmpty

  def enable(isEnabled: Boolean): Unit = {
    this.enabled = isEnabled
  }

  def isEnabled: Boolean = enabled

  def getTotalMessageSize: Long = {
    if (fifoQueue.nonEmpty) {
      fifoQueue.map(getInMemSize(_)).sum
    } else {
      0
    }
  }

  def getTotalStashedSize: Long =
    if (ofoMap.nonEmpty) {
      ofoMap.values.map(getInMemSize(_)).sum
    } else {
      0
    }

  def getQueuedCredit: Long = {
    holdCredit.get()
  }
}
