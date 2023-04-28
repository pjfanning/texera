package edu.uci.ics.amber.engine.architecture.recovery

import edu.uci.ics.amber.engine.architecture.logging.ChannelStepCursor.INIT_STEP
import edu.uci.ics.amber.engine.architecture.logging.storage.DeterminantLogStorage.DeterminantLogReader
import edu.uci.ics.amber.engine.architecture.logging.{InMemDeterminant, StepsOnChannel}
import edu.uci.ics.amber.engine.common.ambermessage.{ChannelEndpointID, WorkflowFIFOMessagePayload, WorkflowFIFOMessagePayloadWithPiggyback}

import scala.collection.mutable

class ReplayOrderEnforcer( records: mutable.Queue[StepsOnChannel], onRecoveryComplete: () => Unit) {
  private var onReplayComplete: () => Unit = () => {}
  private val checkpointStepQueue = mutable.Queue[(Long, () => Unit)]()
  private var switchStep = INIT_STEP
  private var replayTo = INIT_STEP
  private var nextChannel: ChannelEndpointID = _
  private var recoveryCompleted = false
  private var recordedPayload:WorkflowFIFOMessagePayloadWithPiggyback = _
  private var nextRecordedPayload:WorkflowFIFOMessagePayloadWithPiggyback = _

  var currentChannel: ChannelEndpointID = _

  def setCheckpoint(checkpointStep:Long, callback: () => Unit): Unit ={
    checkpointStepQueue.enqueue((checkpointStep, callback))
  }

  def isPayloadRecorded: Boolean ={
    recordedPayload != null
  }

  def getRecordedPayload:WorkflowFIFOMessagePayloadWithPiggyback ={
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

  def setReplayTo(currentDPStep: Long, dest: Long, replayEndPromise:() => Unit): Unit = {
    assert(currentDPStep <= dest)
    replayTo = dest
    this.onReplayComplete = replayEndPromise
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
