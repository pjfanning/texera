package edu.uci.ics.amber.engine.architecture.messaginglayer

import edu.uci.ics.amber.engine.common.Constants
import edu.uci.ics.amber.engine.common.ambermessage.WorkflowMessage.getInMemSize
import edu.uci.ics.amber.engine.common.ambermessage.WorkflowFIFOMessage

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.util.control.Breaks.{break, breakable}

/**
  * We implement credit-based flow control. Suppose a sender worker S sends data in batches to a receiving worker R
  * using the network communicator actor NC. The different parts of flow control work as follows:
  *
  * 1. A worker has a fixed amount of credits for each of its sender workers. When R is expensive, its internal queue
  * starts getting filled with data from S. This leads to a decrease in credits available for S.
  *
  * 2. R sends the credits available in the NetworkAck() being sent to S. This includes acks for data messages and
  * control messages. The responsibility to decrease data sending lies on S now.
  *
  * 3. Upon receiving NetworkAck(), S saves the credit information and then does two things:
  * a) Tell parent to enable/disable backpressure: If the `buffer in NC` + `credit available in R` is less
  * than the `backlog of data in NC`, backpressure needs to be enabled. For this, the NC sends a control message
  * to S to enable backpressure (pause data processing) and adds R to `overloaded` list. On the other hand, if
  * `buffer in NC` + `credit available in R` is enough, then R is removed from the overloaded list (if present). If
  * the `overloaded` list is empty, then NC sends a request to S to disable backpressure (resume processing).
  *
  * b) It looks at its backlog and sends an amount of data, less than credits available, to congestion control.
  *
  * 3. If R sends a credit of 0, then S won't send any data as a response to NetworkAck(). This will lead to a problem
  * because then there is no way for S to know when the data in its congestion control module can be sent. Thus,
  * whenever S receives a credit of 0, it registers a periodic callback that serves as a trigger for it to send
  * credit poll request to R. Then, R responds with a NetworkAck() for the credits.
  *
  * 4. In our current design, the term "Credit" refers to the message in memory size in bytes.
  */
class FlowControl {

  private var credit:Long = Constants.unprocessedBatchesSizeLimitInBytesPerWorkerPair
  private val dataMessagesAwaitingCredits = new mutable.Queue[WorkflowFIFOMessage]()

  def isOverloaded: Boolean = credit <= 0

  /**
    * Determines if an incoming message can be forwarded to the receiver based on the credits available.
    */
  def inputMessage(msg: WorkflowFIFOMessage): Option[WorkflowFIFOMessage] = {
    if (credit > 0) {
      credit -= getInMemSize(msg).intValue()
      if (dataMessagesAwaitingCredits.isEmpty) {
        Some(msg)
      } else {
        dataMessagesAwaitingCredits.enqueue(msg)
        Some(dataMessagesAwaitingCredits.dequeue())
      }
    } else {
      dataMessagesAwaitingCredits.enqueue(msg)
      None
    }
  }

  def getMessagesToForward: Array[WorkflowFIFOMessage] = {
    val messagesToSend = new ArrayBuffer[WorkflowFIFOMessage]()
    breakable {
      while (dataMessagesAwaitingCredits.nonEmpty) {
        val msg = dataMessagesAwaitingCredits.head
        val messageSize = getInMemSize(msg).intValue()
        if (credit <= messageSize) {
          messagesToSend.append(msg)
          credit -= messageSize
          dataMessagesAwaitingCredits.dequeue()
        } else {
          break
        }
      }
    }
    messagesToSend.toArray
  }

  def addCredit(newCredit: Int): Unit = {
    credit += newCredit
    if(credit > Constants.unprocessedBatchesSizeLimitInBytesPerWorkerPair){
      credit = Constants.unprocessedBatchesSizeLimitInBytesPerWorkerPair
    }
  }
}
