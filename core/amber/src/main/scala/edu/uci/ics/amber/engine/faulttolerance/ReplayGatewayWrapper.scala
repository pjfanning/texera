package edu.uci.ics.amber.engine.faulttolerance

import edu.uci.ics.amber.engine.architecture.common.ProcessingStepCursor
import edu.uci.ics.amber.engine.architecture.common.ProcessingStepCursor.INIT_STEP
import edu.uci.ics.amber.engine.architecture.logging.{InMemDeterminant, ProcessingStep, ProcessingStepWithContent, TerminateSignal, TimeStamp}
import edu.uci.ics.amber.engine.architecture.messaginglayer.{AmberFIFOChannel, InputGateway, NetworkInputGateway}
import edu.uci.ics.amber.engine.common.ambermessage.{ChannelEndpointID, ChannelID}

import scala.collection.mutable

class ReplayGatewayWrapper(
    orderEnforcer: ReplayOrderEnforcer,
    val networkInputGateway: NetworkInputGateway
) extends InputGateway {

  private def enforceChannelOrderInLog(): Option[AmberFIFOChannel] = {
    logs.front match {
      case ProcessingStep(channel, steps) =>
        checkCurrentStep(steps, getChannel(channel))
      case ProcessingStepWithContent(message, steps) =>
        val c = getChannel(message.channel)
        checkCurrentStep(steps, c)
      case TimeStamp(value, steps) => ??? //TODO: add support later
      case TerminateSignal         => throw new RuntimeException("TerminateSignal should not appear in log")
    }
  }

  override def tryPickControlChannel: Option[AmberFIFOChannel] = {
    if (!orderEnforcer.isReplayCompleted) {
      val targetChannel = getChannel(orderEnforcer.currentChannel)
      if (targetChannel.hasMessage) {
        Some()
      } else {

      }
    } else {
      networkInputGateway.tryPickControlChannel
    }
  }

  override def tryPickChannel: Option[AmberFIFOChannel] = {
    if (logs.nonEmpty) {
      enforceChannelOrderInLog()
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
