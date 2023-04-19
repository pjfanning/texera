package edu.uci.ics.amber.engine.architecture.logging

import edu.uci.ics.amber.engine.architecture.logging.DeterminantLogger.INIT_STEP
import edu.uci.ics.amber.engine.common.ambermessage.{ChannelEndpointID, ControlPayload}


object DeterminantLogger{
  def getDeterminantLogger(enabledLogging: Boolean): DeterminantLogger ={
    if(enabledLogging){
      new DeterminantLoggerImpl()
    }else{
      new EmptyDeterminantLogger()
    }
  }

  // step value before processing any incoming message
  // processing first message will have step = 0
  val INIT_STEP:Long = -1L
}

abstract class DeterminantLogger extends Serializable {

  protected var totalValidStep:Long = INIT_STEP

  def recordPayload(channelEndpointID: ChannelEndpointID, payload:ControlPayload):Unit

  def stepIncrement():Unit = {
    totalValidStep += 1
  }

  def getStep:Long = totalValidStep

  def setCurrentSender(channel:ChannelEndpointID): Unit

  def drainCurrentLogRecords(): Array[InMemDeterminant]

}
