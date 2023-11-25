package edu.uci.ics.amber.engine.faulttolerance

import edu.uci.ics.amber.engine.architecture.messaginglayer.{AmberFIFOChannel, InputGateway}
import edu.uci.ics.amber.engine.common.ambermessage.ChannelID

class ReplayGatewayWrapper(
    orderEnforcer: ReplayOrderEnforcer,
    val networkInputGateway: InputGateway
) extends InputGateway {

  def pickInOrder: Option[AmberFIFOChannel] = {
    val targetChannel = getChannel(orderEnforcer.currentChannel)
    if (targetChannel.hasMessage) {
      Some(targetChannel)
    } else {
      None
    }
  }

  override def tryPickControlChannel: Option[AmberFIFOChannel] = {
    if (!orderEnforcer.isReplayCompleted) {
      pickInOrder
    } else {
      networkInputGateway.tryPickControlChannel
    }
  }

  override def tryPickChannel: Option[AmberFIFOChannel] = {
    if (!orderEnforcer.isReplayCompleted) {
      pickInOrder
    } else {
      networkInputGateway.tryPickChannel
    }
  }

  override def getAllDataChannels: Iterable[AmberFIFOChannel] = {
    networkInputGateway.getAllDataChannels
  }

  override def getChannel(channelId: ChannelID): AmberFIFOChannel = {
    networkInputGateway.getChannel(channelId)
  }

  override def getAllControlChannels: Iterable[AmberFIFOChannel] = {
    networkInputGateway.getAllControlChannels
  }
}
