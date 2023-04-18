package edu.uci.ics.amber.engine.architecture.logging
import edu.uci.ics.amber.engine.common.ambermessage.{ChannelEndpointID, ControlPayload}

class EmptyDeterminantLogger extends DeterminantLogger {

  override def setCurrentSender(channel: ChannelEndpointID): Unit = {}

  override def drainCurrentLogRecords(): Array[InMemDeterminant] = { Array.empty }

  override def recordPayload(channelEndpointID: ChannelEndpointID, payload: ControlPayload): Unit = {}
}
