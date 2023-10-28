package edu.uci.ics.amber.engine.architecture.common

import akka.actor.Cancellable
import edu.uci.ics.amber.engine.architecture.common.WorkflowActor.NetworkMessage
import edu.uci.ics.amber.engine.architecture.messaginglayer.{CongestionControl, FlowControl}
import edu.uci.ics.amber.engine.common.{AmberLogging, Constants}
import edu.uci.ics.amber.engine.common.ambermessage.{ChannelID, WorkflowFIFOMessage}
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity

import scala.collection.mutable
import scala.concurrent.duration.DurationInt

class AkkaMessageTransferService(
    actorService: AkkaActorService,
    refService: AkkaActorRefMappingService,
    handleBackpressure: Boolean => Unit
) extends AmberLogging {

  override def actorId: ActorVirtualIdentity = actorService.id

  var resendHandle: Cancellable = Cancellable.alreadyCancelled
  var creditPollingHandle: Cancellable = Cancellable.alreadyCancelled

  // add congestion control and flow control here
  val channelToCC = new mutable.HashMap[ChannelID, CongestionControl]()
  val channelToFC = new mutable.HashMap[ChannelID, FlowControl]()
  val messageIDToIdentity = new mutable.LongMap[ChannelID]

  /** keeps track of every outgoing message.
    * Each message is identified by this monotonic increasing ID.
    * It's different from the sequence number and it will only
    * be used by the output gate.
    */
  var networkMessageID = 0L

  def initialize(): Unit = {
    if (Constants.flowControlEnabled) {
      creditPollingHandle = actorService.scheduleWithFixedDelay(
        Constants.creditPollingInitialDelayInMs.millis,
        Constants.creditPollingIntervalinMs.millis,
        triggerCreditPolling
      )
    }
    resendHandle = actorService.scheduleWithFixedDelay(30.seconds, 30.seconds, triggerResend)
  }

  def stop(): Unit = {
    resendHandle.cancel()
    creditPollingHandle.cancel()
  }

  def send(msg: WorkflowFIFOMessage): Unit = {
    if (Constants.flowControlEnabled) {
      forwardToFlowControl(msg, out => forwardToCongestionControl(out, refService.forwardToActor))
    } else {
      forwardToCongestionControl(msg, refService.forwardToActor)
    }
  }

  private def forwardToFlowControl(
      msg: WorkflowFIFOMessage,
      chainedStep: WorkflowFIFOMessage => Unit
  ): Unit = {
    val flowControl = channelToFC.getOrElseUpdate(msg.channel, new FlowControl())
    flowControl.inputMessage(msg).foreach { outMsg =>
      chainedStep(outMsg)
    }
    checkForBackPressure()
  }

  private def forwardToCongestionControl(
      msg: WorkflowFIFOMessage,
      chainedStep: NetworkMessage => Unit
  ): Unit = {
    val congestionControl = channelToCC.getOrElseUpdate(msg.channel, new CongestionControl())
    val data = NetworkMessage(networkMessageID, msg)
    messageIDToIdentity(networkMessageID) = msg.channel
    if (congestionControl.canSend) {
      congestionControl.markMessageInTransit(data)
      chainedStep(data)
    } else {
      congestionControl.enqueueMessage(data)
    }
    networkMessageID += 1
  }

  def receiveAck(msgId: Long, credit: Int): Unit = {
    if (!messageIDToIdentity.contains(msgId)) {
      return
    }
    val channelId = messageIDToIdentity.remove(msgId).get
    val congestionControl = channelToCC.getOrElseUpdate(channelId, new CongestionControl())
    congestionControl.ack(msgId)
    congestionControl.getBufferedMessagesToSend.foreach(refService.forwardToActor)
    if (Constants.flowControlEnabled) {
      updateCredit(channelId, credit)
    }
  }

  def updateCredit(channel: ChannelID, credit: Int): Unit = {
    val flowControl = channelToFC.getOrElseUpdate(channel, new FlowControl())
    flowControl.updateCredit(credit)
    flowControl.getMessagesToForward.foreach(out =>
      forwardToCongestionControl(out, refService.forwardToActor)
    )
    checkForBackPressure()
  }

  private def triggerCreditPolling(): Unit = {
    channelToFC.foreach {
      case (channel, fc) =>
        if (fc.isOverloaded) {
          refService.askForCredit(channel)
        }
    }
  }

  private def checkForBackPressure(): Unit = {
    handleBackpressure(channelToFC.values.exists(_.isOverloaded))
  }

  private def triggerResend(): Unit = {
    refService.clearQueriedActorRefs()
    channelToCC.foreach {
      case (channel, cc) =>
        val msgsNeedResend = cc.getTimedOutInTransitMessages
        if (msgsNeedResend.nonEmpty) {
          logger.info(s"output for $channel: ${cc.getStatusReport}")
        }
        if (refService.hasActorRef(channel.from)) {
          msgsNeedResend.foreach { msg =>
            refService.forwardToActor(msg)
          }
        }
    }
  }
}
