package edu.uci.ics.amber.engine.architecture.recovery

import com.twitter.util.Promise
import edu.uci.ics.amber.engine.architecture.logging.DeterminantLogger.INIT_STEP
import edu.uci.ics.amber.engine.architecture.logging.StepsOnChannel
import edu.uci.ics.amber.engine.common.ambermessage.ChannelEndpointID

import scala.collection.mutable

class ReplayOrderEnforcer(records: mutable.Queue[StepsOnChannel]) {
  private var recoveryEndPromise: Promise[Unit] = Promise()
  private var replayEndPromise: Promise[Unit] = Promise()
  private var switchStep = INIT_STEP
  private var replayTo = INIT_STEP
  private var nextChannel: ChannelEndpointID = _

  var currentChannel: ChannelEndpointID = _

  def setReplayTo(dest: Long, replayEndPromise:Promise[Unit]): Unit = {
    replayTo = dest
    this.replayEndPromise = replayEndPromise
  }

  def initialize(currentDPStep: Long,
                  onRecoveryComplete: Promise[Unit]
                ): Unit = {
    this.recoveryEndPromise = onRecoveryComplete
    // restore replay progress by dropping some of the entries
    switchStep = INIT_STEP
    while (records.nonEmpty && switchStep <= currentDPStep) {
      loadNextDeterminant()
    }
    if(records.isEmpty){
      //recovery already completed
      onRecoveryComplete.setValue(Unit)
    }
  }

  private def loadNextDeterminant(): Unit = {
    val cc = records.dequeue()
    nextChannel = cc.channel
    switchStep = cc.steps
  }

  def forwardReplayProcess(currentStep:Long): Unit ={
    if(replayTo == currentStep){
      replayEndPromise.setValue(Unit)
    }
    while(currentStep == switchStep){
      currentChannel = nextChannel
      if(records.nonEmpty){
        loadNextDeterminant()
      }else if(!recoveryEndPromise.isDone){
        // recovery completed
        recoveryEndPromise.setValue(Unit)
        switchStep = INIT_STEP - 1
      }
    }
  }

}
