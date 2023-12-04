package edu.uci.ics.amber.engine.architecture.messaginglayer

import edu.uci.ics.amber.engine.common.ambermessage.{ChannelID, WorkflowFIFOMessage}

trait InputGateway {
  def tryPickControlChannel: Option[AmberFIFOChannel]

  def tryPickChannel: Option[AmberFIFOChannel]

  def getAllDataChannels: Iterable[AmberFIFOChannel]

  def getChannel(channelId: ChannelID): AmberFIFOChannel

  def getAllControlChannels: Iterable[AmberFIFOChannel]

  def acceptMessage(message:WorkflowFIFOMessage) :Unit
}
