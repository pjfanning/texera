package edu.uci.ics.amber.engine.faulttolerance

import edu.uci.ics.amber.engine.architecture.logging.{LogManager, MessageContent, ProcessingStep}
import edu.uci.ics.amber.engine.architecture.logging.storage.DeterminantLogStorage
import edu.uci.ics.amber.engine.architecture.messaginglayer.{AmberFIFOChannel, InputGateway}
import edu.uci.ics.amber.engine.common.ambermessage.ChannelID

import scala.collection.mutable

class ReplayGatewayWrapper(val inputGateway: InputGateway, logManager: LogManager)
    extends InputGateway {

  val orderEnforcer: ReplayOrderEnforcer = new ReplayOrderEnforcer()

  def setupReplay(
      logStorage: DeterminantLogStorage,
      replayTo: Long,
      onReplayComplete: () => Unit
  ): Unit = {
    val logs = logStorage.getReader.mkLogRecordIterator().toArray
    val steps = mutable.Queue[ProcessingStep]()
    logs.foreach {
      case s: ProcessingStep =>
        steps.enqueue(s)
      case MessageContent(message) =>
        inputGateway.getChannel(message.channel).acceptMessage(message)
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

  private def pickInOrder: Option[AmberFIFOChannel] = {
    orderEnforcer.forwardReplayProcess(logManager.getStep)
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
      inputGateway.tryPickControlChannel
    }
  }

  override def tryPickChannel: Option[AmberFIFOChannel] = {
    if (!orderEnforcer.isReplayCompleted) {
      pickInOrder
    } else {
      inputGateway.tryPickChannel
    }
  }

  override def getAllDataChannels: Iterable[AmberFIFOChannel] = {
    inputGateway.getAllDataChannels
  }

  override def getChannel(channelId: ChannelID): AmberFIFOChannel = {
    inputGateway.getChannel(channelId)
  }

  override def getAllControlChannels: Iterable[AmberFIFOChannel] = {
    inputGateway.getAllControlChannels
  }
}
