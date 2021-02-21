package edu.uci.ics.amber.engine.architecture.messaginglayer

import edu.uci.ics.amber.engine.architecture.messaginglayer.DataInputPort.WorkflowDataMessage
import edu.uci.ics.amber.engine.common.ambermessage.{DataPayload, WorkflowMessage}
import edu.uci.ics.amber.engine.common.virtualidentity.VirtualIdentity
import edu.uci.ics.amber.engine.recovery.{MainLogReplayManager, MainLogStorage}

import scala.collection.mutable

object DataInputPort {
  final case class WorkflowDataMessage(
      from: VirtualIdentity,
      sequenceNumber: Long,
      payload: DataPayload
  ) extends WorkflowMessage
}

class DataInputPort(
    tupleProducer: BatchToTupleConverter,
    mainLogStorage: MainLogStorage,
    mainLogReplayManager: MainLogReplayManager
) {
  private val idToOrderingEnforcers =
    new mutable.AnyRefMap[VirtualIdentity, OrderingEnforcer[DataPayload]]()

  def handleDataMessage(msg: WorkflowDataMessage): Unit = {
    OrderingEnforcer.reorderMessage(
      idToOrderingEnforcers,
      msg.from,
      msg.sequenceNumber,
      msg.payload
    ) match {
      case Some(iterable) =>
        iterable.foreach { data =>
          if (mainLogReplayManager.isReplaying) {
            mainLogReplayManager.filterMessage(msg.from, data).foreach {
              case (vid, payload) => tupleProducer.processDataPayload(vid, payload)
            }
          } else {
            mainLogStorage.receivedDataFrom(msg.from)
            tupleProducer.processDataPayload(msg.from, data)
          }
        }
      case None =>
      // discard duplicate
    }
  }

}
