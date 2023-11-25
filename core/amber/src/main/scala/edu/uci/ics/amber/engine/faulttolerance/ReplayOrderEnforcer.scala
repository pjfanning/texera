package edu.uci.ics.amber.engine.faulttolerance

import edu.uci.ics.amber.engine.architecture.common.ProcessingStepCursor.INIT_STEP
import edu.uci.ics.amber.engine.common.ambermessage.{ChannelID, WorkflowFIFOMessage}

import scala.collection.mutable

class ReplayOrderEnforcer(channelStepOrder: mutable.Queue[(Long, ChannelID)], onRecoveryComplete: () => Unit) {
  private var switchStep = INIT_STEP
  private var replayTo = INIT_STEP
  private var nextChannel: ChannelID = _
  private var replayCompleted = false

  def isReplayCompleted:Boolean = replayCompleted

  var currentChannel: ChannelID = _

  def initialize(currentDPStep: Long): Unit = {
    // restore replay progress by dropping some of the entries
    switchStep = INIT_STEP
    while (channelStepOrder.nonEmpty && switchStep <= currentDPStep) {
      loadNextDeterminant()
    }
  }

  def setReplayTo(currentDPStep: Long, dest: Long): Unit = {
    assert(currentDPStep <= dest)
    replayTo = dest
  }

  private def loadNextDeterminant(): Unit = {
    val channelStep = channelStepOrder.dequeue()
    currentChannel = nextChannel
    switchStep = channelStep._1
    nextChannel = channelStep._2
  }

  def forwardReplayProcess(currentStep:Long): Unit ={
    if(replayCompleted){
      // recovery completed
      onRecoveryComplete()
    }
    while(currentStep == switchStep){
      if(channelStepOrder.nonEmpty){
        loadNextDeterminant()
      }else{
        currentChannel = nextChannel
        switchStep = INIT_STEP - 1
        replayCompleted = true
      }
    }
  }

}

