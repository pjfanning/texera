package edu.uci.ics.amber.engine.architecture.recovery

import edu.uci.ics.amber.engine.architecture.logging.ChannelStepCursor.INIT_STEP
import edu.uci.ics.amber.engine.architecture.logging.StepsOnChannel
import edu.uci.ics.amber.engine.common.ambermessage.ChannelEndpointID

import scala.collection.mutable

class ReplayOrderEnforcer(records: mutable.Queue[StepsOnChannel], onRecoveryComplete: () => Unit) {
  private var onReplayComplete: () => Unit = () => {}
  private val checkpointStepQueue = mutable.Queue[(Long, () => Unit)]()
  private var switchStep = INIT_STEP
  private var replayTo = INIT_STEP
  private var nextChannel: ChannelEndpointID = _
  private var recoveryCompleted = false

  var currentChannel: ChannelEndpointID = _

  def setCheckpoint(checkpointStep:Long, callback: () => Unit): Unit ={
    checkpointStepQueue.enqueue((checkpointStep, callback))
  }

  def setReplayTo(currentDPStep: Long, dest: Long, replayEndPromise:() => Unit): Unit = {
    assert(currentDPStep <= dest)
    replayTo = dest
    this.onReplayComplete = replayEndPromise
  }

  def initialize(currentDPStep: Long): Unit = {
    // restore replay progress by dropping some of the entries
    switchStep = INIT_STEP
    while (records.nonEmpty && switchStep <= currentDPStep) {
      loadNextDeterminant()
    }
  }

  private def loadNextDeterminant(): Unit = {
    val cc = records.dequeue()
    currentChannel = nextChannel
    nextChannel = cc.channel
    switchStep = cc.steps
  }

  def forwardReplayProcess(currentStep:Long): Unit ={
    if(checkpointStepQueue.nonEmpty && checkpointStepQueue.head._1 == currentStep){
      checkpointStepQueue.head._2()
      checkpointStepQueue.dequeue()
    }
    if(replayTo == currentStep){
      onReplayComplete()
    }
    if(recoveryCompleted){
      // recovery completed
      onRecoveryComplete()
    }
    while(currentStep == switchStep){
      if(records.nonEmpty){
        loadNextDeterminant()
      }else{
        currentChannel = nextChannel
        switchStep = INIT_STEP - 1
        recoveryCompleted = true
      }
    }
  }

}
