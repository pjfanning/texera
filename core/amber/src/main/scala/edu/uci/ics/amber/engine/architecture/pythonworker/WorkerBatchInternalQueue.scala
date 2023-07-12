package edu.uci.ics.amber.engine.architecture.pythonworker

import edu.uci.ics.amber.engine.architecture.pythonworker.WorkerBatchInternalQueue._
import edu.uci.ics.amber.engine.common.ambermessage.{
  ChannelEndpointID,
  ControlPayload,
  ControlPayloadV2,
  DataPayload
}
import edu.uci.ics.amber.engine.common.lbmq.LinkedBlockingMultiQueue
object WorkerBatchInternalQueue {
  final val DATA_QUEUE = 1
  final val CONTROL_QUEUE = 0

  // 4 kinds of elements can be accepted by internal queue
  sealed trait InternalQueueElement

  case class DataElement(dataPayload: DataPayload, channelID: ChannelEndpointID)
      extends InternalQueueElement

  case class ControlElement(cmd: ControlPayload, channelID: ChannelEndpointID)
      extends InternalQueueElement

  case class ControlElementV2(cmd: ControlPayloadV2, channelID: ChannelEndpointID)
      extends InternalQueueElement
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

  def enqueueData(elem: InternalQueueElement): Unit = {
    dataQueue.add(elem)
  }
  def enqueueMarker(elem: InternalQueueElement): Unit = {
    dataQueue.add(elem)
  }

  def enqueueCommand(cmd: ControlPayload, channelId: ChannelEndpointID): Unit = {
    controlQueue.add(ControlElement(cmd, channelId))
  }
  def enqueueCommand(cmd: ControlPayloadV2, channelId: ChannelEndpointID): Unit = {
    controlQueue.add(ControlElementV2(cmd, channelId))
  }

  def getElement: InternalQueueElement = lbmq.take()

  def disableDataQueue(): Unit = dataQueue.enable(false)

  def enableDataQueue(): Unit = dataQueue.enable(true)

  def getDataQueueLength: Int = dataQueue.size

  def getControlQueueLength: Int = controlQueue.size

  def isControlQueueEmpty: Boolean = controlQueue.isEmpty

}
