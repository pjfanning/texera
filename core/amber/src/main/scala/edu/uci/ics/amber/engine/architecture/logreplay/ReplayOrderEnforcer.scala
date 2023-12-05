package edu.uci.ics.amber.engine.architecture.logreplay

import edu.uci.ics.amber.engine.architecture.common.ProcessingStepCursor.INIT_STEP
import edu.uci.ics.amber.engine.common.ambermessage.ChannelID

import scala.collection.mutable

class ReplayOrderEnforcer(
    logManager: ReplayLogManager,
    channelStepOrder: mutable.Queue[ProcessingStep],
    startStep: Long,
    replayTo: Long,
    var onComplete: () => Unit
) extends OrderEnforcer {
  private var currentStep: Long = INIT_STEP - 1
  private var currentChannelID: ChannelID = _
  var isCompleted: Boolean = startStep > replayTo

  // restore replay progress by dropping some of the entries
  while (channelStepOrder.nonEmpty && currentStep < startStep) {
    forwardNext()
  }

  private def forwardNext(): Unit = {
    if (channelStepOrder.nonEmpty) {
      val nextStep = channelStepOrder.dequeue()
      currentStep = nextStep.steps
      currentChannelID = nextStep.channelID
    }
  }

  def canProceed(channelID: ChannelID): Boolean = {
    val step = logManager.getStep
    while (step > currentStep) {
      forwardNext()
    }
    if (currentChannelID != channelID) {
      false
    } else {
      if (step == this.replayTo || channelStepOrder.isEmpty) {
        isCompleted = true
        if (onComplete != null) {
          onComplete()
          onComplete = null
        }
      }
      true
    }
  }
}
