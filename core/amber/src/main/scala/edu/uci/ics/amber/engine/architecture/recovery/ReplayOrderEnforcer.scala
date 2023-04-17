package edu.uci.ics.amber.engine.architecture.recovery

import com.twitter.util.Promise
import edu.uci.ics.amber.engine.architecture.logging.StepsOnChannel
import edu.uci.ics.amber.engine.common.ambermessage.ChannelEndpointID

import scala.collection.mutable

class ReplayOrderEnforcer(records: mutable.Queue[StepsOnChannel]) {
  private var recoveryEndPromise: Promise[Unit] = Promise()
  private var replayEndPromise: Promise[Unit] = Promise()
  private var switchStep = 0L
  private var replayTo = -1L
  private var nextChannel: ChannelEndpointID = _

  var currentChannel: ChannelEndpointID = _

  def isReplayCompleted(currentStep:Long):Boolean = {
    val completed = replayTo <= currentStep
    if(completed && !replayEndPromise.isDone){
      replayEndPromise.setValue(Unit)
    }
    completed
  }

  def setReplayTo(dest: Long, replayEndPromise:Promise[Unit]): Unit = {
    replayTo = dest
    this.replayEndPromise = replayEndPromise
  }

  def initialize(currentDPStep: Long,
                  onRecoveryComplete: Promise[Unit]
                ): Unit = {
    this.recoveryEndPromise = onRecoveryComplete
    // restore replay progress by dropping some of the entries
    switchStep = 0L
    while (records.nonEmpty && switchStep < currentDPStep) {
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
    while(currentStep == switchStep){
      currentChannel = nextChannel
      if(records.nonEmpty){
        loadNextDeterminant()
      }else if(!recoveryEndPromise.isDone){
        // recovery completed
        recoveryEndPromise.setValue(Unit)
      }
    }
  }

}
