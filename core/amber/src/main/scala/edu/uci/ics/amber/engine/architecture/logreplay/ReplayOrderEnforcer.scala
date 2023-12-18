package edu.uci.ics.amber.engine.architecture.logreplay

import edu.uci.ics.amber.engine.common.ambermessage.ChannelID

import scala.collection.mutable

class ReplayOrderEnforcer(
    logManager: ReplayLogManager,
    channelStepOrder: mutable.Queue[ProcessingStep],
    startStep: Long,
    private var onComplete: () => Unit
) extends OrderEnforcer {
  private var currentChannelID: ChannelID = _

  private def triggerOnComplete(): Unit = {
    if (onComplete != null) {
      onComplete()
      onComplete = null // make sure the onComplete is called only once.
    }
  }

  var isCompleted: Boolean = channelStepOrder.isEmpty

  if (isCompleted) {
    triggerOnComplete()
  }

  // restore replay progress by dropping some of the entries
  while (channelStepOrder.nonEmpty && channelStepOrder.head.step <= startStep) {
    forwardNext()
  }

  private def forwardNext(): Unit = {
    if (channelStepOrder.nonEmpty) {
      val nextStep = channelStepOrder.dequeue()
      currentChannelID = nextStep.channelID
    }
  }

  def canProceed(channelID: ChannelID): Boolean = {
    val step = logManager.getStep
    // release the next log record if the step matches
    if (channelStepOrder.nonEmpty && channelStepOrder.head.step == step) {
      forwardNext()
    }
    // To terminate replay:
    // no next log record with step > current step, which means further processing is not logged.
    if (channelStepOrder.isEmpty) {
      isCompleted = true
      triggerOnComplete()
    }
    // only proceed if the current channel ID matches the channel ID of the log record
    currentChannelID == channelID
  }
}
