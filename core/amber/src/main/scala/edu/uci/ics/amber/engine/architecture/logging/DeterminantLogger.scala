package edu.uci.ics.amber.engine.architecture.logging

import edu.uci.ics.amber.engine.common.ambermessage.ChannelEndpointID


object DeterminantLogger{
  def getDeterminantLogger(enabledLogging: Boolean): DeterminantLogger ={
    if(enabledLogging){
      new DeterminantLoggerImpl()
    }else{
      new EmptyDeterminantLogger()
    }
  }
}

abstract class DeterminantLogger extends Serializable {

  protected var totalValidStep = 0L

  def stepIncrement():Unit = {
    totalValidStep += 1
  }

  def getStep:Long = totalValidStep

  def setCurrentSender(channel:ChannelEndpointID): Unit

  def drainCurrentLogRecords(): Array[InMemDeterminant]

}
