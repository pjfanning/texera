package edu.uci.ics.amber.engine.architecture.messaginglayer

import edu.uci.ics.amber.engine.architecture.worker.DataProcessor.{EndMarker, InputTuple}
import edu.uci.ics.amber.engine.architecture.worker.InputHub
import edu.uci.ics.amber.engine.common.ambermessage.{DataFrame, DataPayload, EndOfUpstream}
import edu.uci.ics.amber.engine.common.virtualidentity.{ActorVirtualIdentity, LinkIdentity}

class BatchToTupleConverter(
    replayGate:InputHub,
) {

  /** This method handles various data payloads and put different
    * element into the internal queue.
    * data payloads:
    * 1. Data Payload, it will be split into tuples and add to the queue.
    * 2. End Of Upstream, this payload will be received once per upstream actor.
    * Note that multiple upstream actors can be there for one upstream.
    * We emit EOU marker when one upstream exhausts. Also, we emit End Of All marker
    * when ALL upstreams exhausts.
    *
    * @param from
    * @param dataPayload
    */
  def processDataPayload(from: ActorVirtualIdentity, dataPayload: DataPayload): Unit = {
    dataPayload match {
      case DataFrame(payload) =>
        payload.foreach { i =>
          replayGate.addData(InputTuple(from, i))
        }
      case EndOfUpstream() =>
        replayGate.addData(EndMarker(from))
      case other =>
        throw new NotImplementedError()
    }
  }

//  /**
//    * This method is used by flow control logic. It returns the number of credits available for this particular sender
//    * worker.
//    * @param sender the worker sending the network message
//    * @return
//    */
//  def getSenderCredits(sender: ActorVirtualIdentity): Int =
//    creditMonitor.getSenderCredits(sender)

}
