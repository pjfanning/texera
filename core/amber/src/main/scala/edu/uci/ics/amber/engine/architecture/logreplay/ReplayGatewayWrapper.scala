package edu.uci.ics.amber.engine.architecture.logreplay

import edu.uci.ics.amber.engine.architecture.logreplay.storage.ReplayLogStorage
import edu.uci.ics.amber.engine.architecture.messaginglayer.{AmberFIFOChannel, InputGateway}
import edu.uci.ics.amber.engine.common.ambermessage.ChannelID

import scala.collection.mutable

class ReplayGatewayWrapper(val originalGateway: InputGateway, logManager: ReplayLogManager)
    extends InputGateway {

  val orderEnforcer: ReplayOrderEnforcer = new ReplayOrderEnforcer()

  def setupReplay(
      logStorage: ReplayLogStorage,
      replayTo: Long,
      onReplayComplete: () => Unit
  ): Unit = {
    val logs = logStorage.getReader.mkLogRecordIterator().toArray
    val steps = mutable.Queue[ProcessingStep]()
    logs.foreach {
      case s: ProcessingStep =>
        steps.enqueue(s)
      case MessageContent(message) =>
        originalGateway.getChannel(message.channel).acceptMessage(message)
      case other =>
        throw new RuntimeException(s"cannot handle $other in the log")
    }
    orderEnforcer.setReplayTo(
      steps,
      logManager.getStep,
      replayTo,
      onReplayComplete
    )
  }

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
    } else {
      originalGateway.tryPickControlChannel
    }
  }

  override def tryPickChannel: Option[AmberFIFOChannel] = {
    orderEnforcer.forwardReplayProcess(logManager.getStep)
    if (!orderEnforcer.isReplayCompleted) {
      pickInOrder(false)
    } else {
      originalGateway.tryPickChannel
    }
  }

  override def getAllDataChannels: Iterable[AmberFIFOChannel] = {
    originalGateway.getAllDataChannels
  }

  override def getChannel(channelId: ChannelID): AmberFIFOChannel = {
    originalGateway.getChannel(channelId)
  }

  override def getAllControlChannels: Iterable[AmberFIFOChannel] = {
    originalGateway.getAllControlChannels
  }
}
