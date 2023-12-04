package edu.uci.ics.amber.engine.architecture.logreplay

import edu.uci.ics.amber.engine.architecture.messaginglayer.{AmberFIFOChannel, InputGateway}
import edu.uci.ics.amber.engine.common.ambermessage.{ChannelID, WorkflowFIFOMessage}

import scala.collection.mutable

class ReplayGateway(logManager: ReplayLogManager)
    extends InputGateway {

  val orderEnforcer: ReplayOrderEnforcer = new ReplayOrderEnforcer()
  private val inputChannels =
    new mutable.HashMap[ChannelID, AmberFIFOChannel]()

  private def pickInOrder(forceControl: Boolean): Option[AmberFIFOChannel] = {
    assert(orderEnforcer.currentChannel != null)
    if (forceControl && !orderEnforcer.currentChannel.isControl) {
      return None
    }
    val targetChannel = getChannel(orderEnforcer.currentChannel)
    if (targetChannel.hasMessage) {
      Some(targetChannel)
    } else {
      None
    }
  }

  override def tryPickControlChannel: Option[AmberFIFOChannel] = {
    orderEnforcer.forwardReplayProcess(logManager.getStep)
    if (!orderEnforcer.isReplayCompleted) {
      pickInOrder(true)
    }else{
      None
    }
  }

  override def tryPickChannel: Option[AmberFIFOChannel] = {
    orderEnforcer.forwardReplayProcess(logManager.getStep)
    if (!orderEnforcer.isReplayCompleted) {
      pickInOrder(false)
    } else {
      None
    }
  }

  def getAllDataChannels: Iterable[AmberFIFOChannel] =
    inputChannels.filter(!_._1.isControl).values

  // this function is called by both main thread(for getting credit)
  // and DP thread(for enqueuing messages) so a lock is required here
  def getChannel(channelId: ChannelID): AmberFIFOChannel = {
    synchronized {
      inputChannels.getOrElseUpdate(channelId, new AmberFIFOChannel(channelId))
    }
  }

  def getAllControlChannels: Iterable[AmberFIFOChannel] =
    inputChannels.filter(_._1.isControl).values

  def acceptMessage(message: WorkflowFIFOMessage): Unit = {
    getChannel(message.channel).acceptMessage(message)
  }

}
