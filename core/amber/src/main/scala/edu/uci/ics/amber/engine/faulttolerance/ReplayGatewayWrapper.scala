package edu.uci.ics.amber.engine.faulttolerance

import edu.uci.ics.amber.engine.architecture.common.ProcessingStepCursor
import edu.uci.ics.amber.engine.architecture.logging.{InMemDeterminant, ProcessingStep, TerminateSignal, TimeStamp}
import edu.uci.ics.amber.engine.architecture.messaginglayer.{AmberFIFOChannel, InputGateway, NetworkInputGateway}
import edu.uci.ics.amber.engine.common.ambermessage.ChannelID

import scala.collection.mutable

class ReplayGatewayWrapper(logs:mutable.Queue[InMemDeterminant],
                           cursor:ProcessingStepCursor,
                           val networkInputGateway: NetworkInputGateway) extends InputGateway {
  private def enforceChannelOrderInLog(): Option[AmberFIFOChannel] = {
    logs.front match {
      case ProcessingStep(msg, steps) =>
        if(cursor.getStep < steps){
          //continue processing data message
          None
        }else if(cursor.getStep > steps){
          throw new RuntimeException(s"cursor exceed the logged sequence! current cursor = ${cursor.getStep}, next log record = ${logs.front}")
        }else{
          val c = getChannel(msg.channel)
          if(msg.payload != null){
            c.acceptMessage(msg)
          }
          if(c.hasMessage){
            Some(c)
          }else{
            None
          }
        }
      case TimeStamp(value, steps) => ??? //TODO: add support later
      case TerminateSignal => throw new RuntimeException("TerminateSignal should not appear in log")
    }
  }


  override def tryPickControlChannel: Option[AmberFIFOChannel] = {
    if(logs.nonEmpty){
      enforceChannelOrderInLog()
    }else{
      networkInputGateway.tryPickControlChannel
    }
  }

  override def tryPickChannel: Option[AmberFIFOChannel] = {
    if(logs.nonEmpty){
      enforceChannelOrderInLog()
    }else{
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
