package edu.uci.ics.amber.engine.architecture.recovery

import com.twitter.util.Promise
import edu.uci.ics.amber.engine.architecture.logging.DeterminantLogger.INIT_STEP
import edu.uci.ics.amber.engine.architecture.logging.StepsOnChannel
import edu.uci.ics.amber.engine.common.ambermessage.ChannelEndpointID

import scala.collection.mutable

class ReplayOrderEnforcer(records: mutable.Queue[StepsOnChannel]) {
  private var onReplayComplete: () => Unit = () => {}
  private var onRecoveryComplete: () => Unit = () => {}
  private var switchStep = INIT_STEP
  private var replayTo = INIT_STEP
  private var nextChannel: ChannelEndpointID = _

  var currentChannel: ChannelEndpointID = _

  def setReplayTo(currentDPStep: Long, dest: Long, replayEndPromise:() => Unit): Unit = {
    assert(currentDPStep < dest)
    replayTo = dest
    this.onReplayComplete = replayEndPromise
  }

  def setRecovery(onRecoveryComplete: () => Unit): Unit ={
    this.onRecoveryComplete = onRecoveryComplete
    if(records.isEmpty){
      //recovery already completed
      onRecoveryComplete()
    }
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
    nextChannel = cc.channel
    switchStep = cc.steps
  }

  def forwardReplayProcess(currentStep:Long): Unit ={
    if(replayTo == currentStep){
      onReplayComplete()
    }
    while(currentStep == switchStep){
      currentChannel = nextChannel
      if(records.nonEmpty){
        loadNextDeterminant()
      }else{
        // recovery completed
        onRecoveryComplete()
        switchStep = INIT_STEP - 1
      }
    }
  }

}
