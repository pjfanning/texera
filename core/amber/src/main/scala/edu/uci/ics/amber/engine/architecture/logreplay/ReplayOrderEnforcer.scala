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
    if (!isCompleted) {
      return
    }
    if (onComplete != null) {
      onComplete()
      onComplete = null // make sure the onComplete is called only once.
    }
  }

  // restore replay progress by dropping some of the entries
  while (channelStepOrder.nonEmpty && channelStepOrder.head.step <= startStep) {
    forwardNext()
  }

  var isCompleted: Boolean = channelStepOrder.isEmpty

  triggerOnComplete()

  private def forwardNext(): Unit = {
    if (channelStepOrder.nonEmpty) {
      val nextStep = channelStepOrder.dequeue()
      currentChannelID = nextStep.channelID
    }
  }

  def canProceed(channelID: ChannelID): Boolean = {
    val step = logManager.getStep
    // release the next log record if the step matches
    // Hack: We increment step before receiving the messages. When we finalize checkpoint,
    // we could push another processing step with the updated step count.
    // If such case happen, the log will contain 2 processing step
    // with different channel but the same step count. The following while loop finds
    // the last processing step.
    while (channelStepOrder.nonEmpty && channelStepOrder.head.step == step) {
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
