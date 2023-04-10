package edu.uci.ics.amber.engine.architecture.common

import edu.uci.ics.amber.engine.architecture.common.LogicalExecutionSnapshot.{ChannelStats, ChannelStatsMapping, ProcessingStats}
import edu.uci.ics.amber.engine.architecture.worker.processing.promisehandlers.TakeCheckpointHandler.CheckpointStats
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity

import scala.collection.mutable


object LogicalExecutionSnapshot{

  class ChannelStatsMapping extends Serializable {
    private val map = mutable.HashMap[(ActorVirtualIdentity, Boolean), ChannelStats]()
    def get(channel:(ActorVirtualIdentity, Boolean)):ChannelStats = {
      map.getOrElseUpdate(channel, ChannelStats(0,0))
    }

    def keys:Iterable[(ActorVirtualIdentity, Boolean)] = map.keys

    def getToReceive(actorVirtualIdentity: ActorVirtualIdentity):Long = {
      map.filter(_._1._1 == actorVirtualIdentity).map(_._2.toReceive).sum
    }

    def getReceived(actorVirtualIdentity: ActorVirtualIdentity):Long = {
      map.filter(_._1._1 == actorVirtualIdentity).map(_._2.actualReceived).sum
    }
  }

  final case class ChannelStats(var toReceive:Long, var actualReceived:Long)

  final case class ProcessingStats(checkpointCost:Long, alignment:Long, inputStatus:ChannelStatsMapping)
}

class LogicalExecutionSnapshot extends Serializable{

  private val participants = mutable.HashMap[ActorVirtualIdentity, ProcessingStats]()

  def addParticipant(
      id: ActorVirtualIdentity,
      info: CheckpointStats
  ): Unit = {
    val cur = participants.getOrElseUpdate(id, ProcessingStats(0, 0, new ChannelStatsMapping()))
    participants(id) = cur.copy(checkpointCost = info.saveStateCost, alignment = info.alignment)
    info.inputWatermarks.foreach{
      case (channel, received) =>
        cur.inputStatus.get(channel).actualReceived = received
    }
    info.outputWatermarks.foreach{
      case (channel, sent) =>
        val down = participants.getOrElseUpdate(channel._1, ProcessingStats(0, 0, new ChannelStatsMapping()))
        val downChannelId = (id, channel._2)
        down.inputStatus.get(downChannelId).toReceive = sent
    }
  }

  def getParticipants: Iterable[ActorVirtualIdentity] = participants.keys

  def getStats(actorVirtualIdentity: ActorVirtualIdentity):ProcessingStats = {
    participants.getOrElse(actorVirtualIdentity, ProcessingStats(0,0,new ChannelStatsMapping()))
  }

}


class EmptyLogicalExecutionSnapshot() extends LogicalExecutionSnapshot{
  private val stats = ProcessingStats(0,0,new ChannelStatsMapping())
  override def getStats(actorVirtualIdentity: ActorVirtualIdentity): ProcessingStats = stats
}

