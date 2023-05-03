package edu.uci.ics.amber.engine.architecture.recovery

import edu.uci.ics.amber.engine.architecture.logging.ChannelStepCursor.INIT_STEP
import edu.uci.ics.amber.engine.architecture.logging.storage.DeterminantLogStorage.DeterminantLogReader
import edu.uci.ics.amber.engine.architecture.logging.{InMemDeterminant, StepsOnChannel}
import edu.uci.ics.amber.engine.common.ambermessage.{ChannelEndpointID, WorkflowFIFOMessagePayload, WorkflowExecutionPayload}

import scala.collection.mutable

class ReplayOrderEnforcer( records: mutable.Queue[StepsOnChannel], onRecoveryComplete: () => Unit) {
  private val callbacks = mutable.HashMap[Long, mutable.ArrayBuffer[() => Unit]]()
  private var switchStep = INIT_STEP
  private var replayTo = INIT_STEP
  private var nextChannel: ChannelEndpointID = _
  private var recoveryCompleted = false
  private var recordedPayload:WorkflowExecutionPayload = _
  private var nextRecordedPayload:WorkflowExecutionPayload = _

  def isReplayCompleted(currentStep:Long):Boolean = replayTo == currentStep

  var currentChannel: ChannelEndpointID = _

  def setCallbackOnStep(step:Long, callback: () => Unit): Unit ={
    callbacks.getOrElseUpdate(step, new mutable.ArrayBuffer[() => Unit]()).append(callback)
  }

  def isPayloadRecorded: Boolean ={
    recordedPayload != null
  }

  def getRecordedPayload:WorkflowExecutionPayload ={
    val res = recordedPayload
    recordedPayload = null
    res
  }

  def initialize(currentDPStep: Long): Unit = {
    // restore replay progress by dropping some of the entries
    switchStep = INIT_STEP
    while (records.nonEmpty && switchStep <= currentDPStep) {
      loadNextDeterminant()
    }
  }

  def setReplayTo(currentDPStep: Long, dest: Long): Unit = {
    assert(currentDPStep <= dest)
    replayTo = dest
  }

  def getAllReplayChannels:Set[ChannelEndpointID] = {
    val res = mutable.HashSet[ChannelEndpointID]()
    records.foreach(s => res.add(s.channel))
    if(currentChannel!= null){
      res.add(currentChannel)
    }
    if(nextChannel!=null){
      res.add(nextChannel)
    }
    res.toSet
  }

  private def loadNextDeterminant(): Unit = {
    val cc = records.dequeue()
    currentChannel = nextChannel
    recordedPayload = nextRecordedPayload
    nextChannel = cc.channel
    switchStep = cc.steps
    nextRecordedPayload = cc.payload
  }

  def triggerCallbacks(currentStep:Long): Unit = {
    if(callbacks.contains(currentStep)){
      callbacks(currentStep).foreach{
        func => func()
      }
      callbacks.remove(currentStep)
    }
  }

  def forwardReplayProcess(currentStep:Long): Unit ={
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
