package edu.uci.ics.amber.engine.architecture.pythonworker

import edu.uci.ics.amber.engine.architecture.pythonworker.WorkerBatchInternalQueue._
import edu.uci.ics.amber.engine.common.Constants
import edu.uci.ics.amber.engine.common.ambermessage.{
  ChannelID,
  ControlPayload,
  ControlPayloadV2,
  DataFrame,
  DataPayload
}
import lbmq.LinkedBlockingMultiQueue

import scala.collection.mutable
object WorkerBatchInternalQueue {
  final val DATA_QUEUE = 1
  final val CONTROL_QUEUE = 0

  // 4 kinds of elements can be accepted by internal queue
  sealed trait InternalQueueElement

  case class DataElement(dataPayload: DataPayload, from: ChannelID) extends InternalQueueElement

  case class ControlElement(cmd: ControlPayload, from: ChannelID) extends InternalQueueElement

  case class ControlElementV2(cmd: ControlPayloadV2, from: ChannelID) extends InternalQueueElement
}

/** Inspired by the mailbox-ed thread, the internal queue should
  * be a part of DP thread.
  */
trait WorkerBatchInternalQueue {

  private val lbmq = new LinkedBlockingMultiQueue[Int, InternalQueueElement]()

  lbmq.addSubQueue(DATA_QUEUE, DATA_QUEUE)
  lbmq.addSubQueue(CONTROL_QUEUE, CONTROL_QUEUE)

  private val dataQueue = lbmq.getSubQueue(DATA_QUEUE)

  private val controlQueue = lbmq.getSubQueue(CONTROL_QUEUE)

  // the values in below maps are in batches
  private val consumedCredit =
    new mutable.HashMap[ChannelID, Long]() // written by DP thread, read by main thread

  def enqueueData(elem: InternalQueueElement): Unit = {
    dataQueue.add(elem)
  }
  def enqueueMarker(elem: InternalQueueElement): Unit = {
    dataQueue.add(elem)
  }

  def enqueueCommand(cmd: ControlPayload, from: ChannelID): Unit = {
    controlQueue.add(ControlElement(cmd, from))
  }
  def enqueueCommand(cmd: ControlPayloadV2, from: ChannelID): Unit = {
    controlQueue.add(ControlElementV2(cmd, from))
  }

  def getElement: InternalQueueElement = {
    val elem = lbmq.take()
    if (Constants.flowControlEnabled) {
      elem match {
        case DataElement(dataPayload, from) =>
          synchronized {
            consumedCredit(from) =
              consumedCredit.getOrElseUpdate(from, 0L) + (dataPayload match {
                case frame: DataFrame =>
                  frame.inMemSize
                case _ =>
                  200L
              })
          }
        case _ =>
        // do nothing
      }
    }
    elem
  }

  def disableDataQueue(): Unit = dataQueue.enable(false)

  def enableDataQueue(): Unit = dataQueue.enable(true)

  def getDataQueueLength: Int = dataQueue.size

  def getControlQueueLength: Int = controlQueue.size

  def isControlQueueEmpty: Boolean = controlQueue.isEmpty

  def getSenderCredits(sender: ChannelID): Int = {
    if (sender.isControlChannel) {
      Constants.unprocessedBatchesSizeLimitInBytesPerWorkerPair
    } else {
      synchronized {
        val consumed = consumedCredit.getOrElseUpdate(sender, 0L)
        consumedCredit(sender) = 0L //clear
        consumed.toInt
      }
    }
  }

}
