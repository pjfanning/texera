package edu.uci.ics.amber.engine.architecture.logging

import edu.uci.ics.amber.engine.common.ambermessage.{ChannelEndpointID, ControlPayload, WorkflowFIFOMessagePayload, WorkflowExecutionPayload}


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

  def setCurrentSenderWithPayload(channelEndpointID: ChannelEndpointID,step:Long, payload:WorkflowExecutionPayload):Unit

  def drainCurrentLogRecords(step:Long): Array[InMemDeterminant]

}
