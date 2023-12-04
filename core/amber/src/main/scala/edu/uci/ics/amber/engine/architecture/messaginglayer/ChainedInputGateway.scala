package edu.uci.ics.amber.engine.architecture.messaginglayer

import edu.uci.ics.amber.engine.common.ambermessage.{ChannelID, WorkflowFIFOMessage}

import scala.collection.mutable


class ChainedInputGateway(gateways: mutable.ListBuffer[InputGateway]) extends InputGateway {
  override def tryPickControlChannel: Option[AmberFIFOChannel] = {
      var msg: Option[WorkflowFIFOMessage] = None
      gateways.init.foreach(gateway =>{
              msg match {
                case Some(msg) => gateway.acceptMessage( msg)
                case None =>
              }

              msg = gateway.tryPickControlChannel match {
                case Some(channel) => Some(channel.take)
                case None=> return None
              }
      })
      gateways.last.tryPickControlChannel
  }

  override def tryPickChannel: Option[AmberFIFOChannel] = {
    var msg: Option[WorkflowFIFOMessage] = None
    gateways.init.foreach(gateway => {
      msg match {
        case Some(msg) => gateway.acceptMessage(msg)
        case None =>
      }

      msg = gateway.tryPickChannel match {
        case Some(channel) => Some(channel.take)
        case None => return None
      }
    })
    gateways.last.tryPickChannel
  }

  override def getAllDataChannels: Iterable[AmberFIFOChannel] = {
    gateways.last.getAllDataChannels
  }

  override def getChannel(channelId: ChannelID): AmberFIFOChannel = {
      gateways.last.getChannel(channelId)
  }

  override def getAllControlChannels: Iterable[AmberFIFOChannel] = {
    gateways.last.getAllControlChannels
  }

  override def acceptMessage(message: WorkflowFIFOMessage): Unit = {
    gateways.head.acceptMessage( message)
  }

  def append(gateway: InputGateway): Unit = {
    gateways+=gateway
  }

  def remove(gateway:InputGateway):Unit ={
    gateways-=gateway
  }
}
