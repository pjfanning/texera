package edu.uci.ics.amber.engine.architecture.logging

import edu.uci.ics.amber.engine.common.ambermessage.{ChannelEndpointID, ControlPayload}


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

  def enableOutputCommit(enabled:Boolean):Unit

  def recordPayload(channelEndpointID: ChannelEndpointID, payload:ControlPayload):Unit

  def setCurrentSender(channel:ChannelEndpointID, step:Long): Unit

  def drainCurrentLogRecords(step:Long): Array[InMemDeterminant]

}
