package edu.uci.ics.amber.engine.faulttolerance

import edu.uci.ics.amber.engine.architecture.common.ProcessingStepCursor.INIT_STEP
import edu.uci.ics.amber.engine.architecture.logging.ProcessingStep
import edu.uci.ics.amber.engine.common.ambermessage.ChannelID

import scala.collection.mutable

class ReplayOrderEnforcer() {
  private var switchStep = INIT_STEP
  private var replayTo = INIT_STEP
  private var nextChannel: ChannelID = _
  private var replayCompleted = true
  private var channelStepOrder: mutable.Queue[ProcessingStep] = mutable.Queue.empty
  private var onComplete: () => Unit = () => {}

  def isReplayCompleted: Boolean = replayCompleted

  var currentChannel: ChannelID = _

  def setReplayTo(
      stepsInLog: mutable.Queue[ProcessingStep],
      currentDPStep: Long,
      dest: Long,
      onComplete: () => Unit
  ): Unit = {
    assert(currentDPStep <= dest)
    this.replayCompleted = false
    this.switchStep = INIT_STEP
    this.channelStepOrder = stepsInLog
    this.onComplete = onComplete
    // restore replay progress by dropping some of the entries
    while (channelStepOrder.nonEmpty && switchStep <= currentDPStep) {
      loadNextDeterminant()
    }
    this.replayTo = dest
  }

  private def loadNextDeterminant(): Unit = {
    val channelStep = channelStepOrder.dequeue()
    currentChannel = nextChannel
    switchStep = channelStep.steps
    nextChannel = channelStep.channelID
  }

  def forwardReplayProcess(currentStep: Long): Unit = {
    if (replayCompleted) {
      // recovery completed
      onComplete()
    }
    while (currentStep == switchStep) {
      if (channelStepOrder.nonEmpty) {
        loadNextDeterminant()
      } else {
        currentChannel = nextChannel
        switchStep = INIT_STEP - 1
        replayCompleted = true
      }
    }
  }

}
