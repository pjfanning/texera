package edu.uci.ics.amber.engine.architecture.logreplay

import edu.uci.ics.amber.engine.common.ambermessage.ChannelID

import scala.collection.mutable

class ReplayOrderEnforcer(
                           logManager: ReplayLogManager,
                           channelStepOrder: mutable.Queue[ProcessingStep],
                           startStep: Long,
                           replayTo: Long,
                           var onComplete: () => Unit
) extends OrderEnforcer {
  private var currentStep: Long = 0
  private var currentChannelID: ChannelID = _
  private var replayCompleting: Boolean = false
  var isCompleted: Boolean = startStep > replayTo

  // restore replay progress by dropping some of the entries
  while (channelStepOrder.nonEmpty && currentStep <= startStep) {
    forwardNext()
  }

  private def forwardNext(): Unit = {
    if (channelStepOrder.nonEmpty) {
      val nextStep = channelStepOrder.dequeue()
      currentStep = nextStep.steps
      currentChannelID = nextStep.channelID
    } else {
      replayCompleting = true
    }

  }

  def canProceed(channelID: ChannelID): Boolean = {
    val step = logManager.getStep
    val ret = if (step < currentStep) {
      // still processing data
      false
    } else if (currentChannelID != channelID) {
      false // skip
    } else if (step == currentStep) {
      // getting a ProcessStep of a data, which is generated from a flush
      forwardNext()
      if (replayCompleting) {
        isCompleted = true
        if (onComplete!= null){
          onComplete()
          onComplete = null
        }
      }
      currentChannelID == channelID
    } else {
      throw new RuntimeException("step > currentStep, it should not happen")
    }

    if (step == this.replayTo) {
      false
    } else {
      ret
    }

  }
}
