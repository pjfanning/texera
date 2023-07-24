package edu.uci.ics.amber.engine.architecture.common

import edu.uci.ics.amber.engine.architecture.common.LogicalExecutionSnapshot.{ChannelStats, ChannelStatsMapping, ProcessingStats}
import edu.uci.ics.amber.engine.architecture.recovery.InternalPayloadManager.CheckpointStats
import edu.uci.ics.amber.engine.common.ambermessage.ChannelEndpointID
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity
import edu.uci.ics.amber.engine.common.virtualidentity.util.{CLIENT, CONTROLLER}

import scala.collection.mutable


object LogicalExecutionSnapshot{

  class ChannelStatsMapping extends Serializable {
    private val map = mutable.HashMap[ChannelEndpointID, ChannelStats]()
    def get(channel:ChannelEndpointID):ChannelStats = {
      map.getOrElseUpdate(channel, ChannelStats(0,0))
    }

    def keys:Iterable[ChannelEndpointID] = map.keys

    def getDataToReceive(actorVirtualIdentity: ActorVirtualIdentity):Long = {
      map.filter{ x =>
        x._1.endpointWorker == actorVirtualIdentity && !x._1.isControlChannel
      }.map(_._2.toReceive).sum
    }

    def getDataReceived(actorVirtualIdentity: ActorVirtualIdentity):Long = {
      map.filter{ x =>
        x._1.endpointWorker == actorVirtualIdentity && !x._1.isControlChannel
      }.map(_._2.actualReceived).sum
    }
  }

  final case class ChannelStats(var toReceive:Long, var actualReceived:Long)

  final case class ProcessingStats(checkpointCost:Long, alignment:Long, inputStatus:ChannelStatsMapping)
}

class LogicalExecutionSnapshot(val id:String, val isInteraction:Boolean, val timestamp:Long) extends Serializable{

  private val participants = mutable.HashMap[ActorVirtualIdentity, ProcessingStats]()
  private val checkpointed = mutable.HashMap[ActorVirtualIdentity, Map[ChannelEndpointID, Long]]()

  def getSinks:Iterable[ActorVirtualIdentity] = {
    val dagParticipants = participants.keys.toSet diff Set(CONTROLLER, CLIENT)
    val senders = dagParticipants.map(participants).flatMap(_.inputStatus.keys.map(_.endpointWorker))
    dagParticipants diff senders
  }

  def getUpstreams(id:ActorVirtualIdentity): Iterable[ActorVirtualIdentity] ={
    if(!participants.contains(id)){
      return Iterable()
    }
    participants(id).inputStatus.keys.filter(x => x!= CLIENT && x!= CONTROLLER).map(_.endpointWorker)
  }

  def getCheckpointedFIFOSeq(id: ActorVirtualIdentity):Map[ChannelEndpointID, Long] = {
    checkpointed(id)
  }

  def isAllCheckpointed:Boolean = (participants.keys.toSet - CLIENT) == checkpointed.keys.toSet

  def isNoneCheckpointed:Boolean = checkpointed.isEmpty

  def addParticipant(
      id: ActorVirtualIdentity,
      info: CheckpointStats,
      isCheckpointed:Boolean = false
  ): Unit = {
    val cur = participants.getOrElseUpdate(id, ProcessingStats(0, 0, new ChannelStatsMapping()))
    participants(id) = cur.copy(checkpointCost = info.saveStateCost+info.alignmentCost, alignment = info.step)
    if(isCheckpointed){
      checkpointed(id) = info.outputWatermarks
    }else{
      info.inputWatermarks.foreach{
        case (channel, received) =>
          cur.inputStatus.get(channel).actualReceived = received
      }
      info.outputWatermarks.foreach{
        case (channel, sent) =>
          val down = participants.getOrElseUpdate(channel.endpointWorker, ProcessingStats(0, 0, new ChannelStatsMapping()))
          val downChannelId = ChannelEndpointID(id, channel.isControlChannel)
          down.inputStatus.get(downChannelId).toReceive = sent
      }
    }
  }

  def getParticipants: Iterable[ActorVirtualIdentity] = participants.keys.toSet - CLIENT

  def getStats(actorVirtualIdentity: ActorVirtualIdentity):ProcessingStats = {
    participants.getOrElse(actorVirtualIdentity, ProcessingStats(0,0,new ChannelStatsMapping()))
  }

}


class EmptyLogicalExecutionSnapshot extends LogicalExecutionSnapshot("empty snapshot",false, 0){
  private val stats = ProcessingStats(0,0,new ChannelStatsMapping())
  override def getStats(actorVirtualIdentity: ActorVirtualIdentity): ProcessingStats = stats
}

