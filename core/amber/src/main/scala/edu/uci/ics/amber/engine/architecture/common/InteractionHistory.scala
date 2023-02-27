package edu.uci.ics.amber.engine.architecture.common

import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity
import edu.uci.ics.texera.web.service.ReplayPlanner.getLastCheckpoint

import scala.collection.mutable

class InteractionHistory {

  private val history = mutable.ArrayBuffer[Interaction](new Interaction())
  private val timestamps = mutable.ArrayBuffer[Long](0)
  private val completions = mutable.HashMap[ActorVirtualIdentity, Long]()

  def addCompletion(id:ActorVirtualIdentity, completion:Long):Unit = {
    completions(id) = completion
  }

  def addInteraction(time:Long, interaction: Interaction):Unit = {
    timestamps.append(time)
    history.append(interaction)
  }

  def findInteractionIdx(id:ActorVirtualIdentity, alignment:Long): Int = {
    history.indexWhere(x => x.containsAlignment(id, alignment))
  }

  def getInteractions:Iterable[Interaction] = history

  def getInteraction(idx:Int):Interaction = history(idx)

  def getCheckpointCost(idx:Int, existingCheckpoints:Map[ActorVirtualIdentity, Set[Long]]):Long = {
    val interaction = history(idx)
    val toChkpt = interaction.getParticipants.filter(p => {
      val existing = existingCheckpoints.get(p)
      if(existing.isDefined && completions.contains(p)){
        interaction.getAlignment(p).get < completions(p) || !existing.get.exists(x => x >= completions(p))
      }else{
        true
      }
    })
    toChkpt.toArray.map(interaction.getCheckpointCost).map {
      case Some(value) => value
      case None => 0
    }.sum
  }


  def getCheckpointCost(idxes:Iterable[Int], existingCheckpoints:Map[ActorVirtualIdentity, Set[Long]]): Long ={
    var existing = existingCheckpoints
    var cost = 0L
    idxes.foreach{
      idx =>
        cost += getCheckpointCost(idx, existing)
        existing = existing ++ getInteraction(idx).getAlignmentMap.mapValues(Set(_))
    }
    cost
  }

  def getReplayTime(from: Int, to:Int): Long ={
    timestamps(to) - timestamps(from)
  }

  def getTotalReplayTime(
                          checkpoint: Iterable[Int],
                          replayPoints: Array[Int]
                        ): Long = {
    var res = 0L
    replayPoints.foreach { rp =>
      val lastChkpt = getLastCheckpoint(checkpoint, rp)
      val replayTime = history(lastChkpt).getTotalLoadCost + getReplayTime(lastChkpt, rp)
      res += replayTime
    }
    res
  }

  def validateReplayTime(checkpoint: Iterable[Int],
                         replayPoints: Array[Int], threshold:Int):Int = {
    var res = 0
    replayPoints.foreach { rp =>
      val lastChkpt = getLastCheckpoint(checkpoint, rp)
      val replayTime = history(lastChkpt).getTotalLoadCost + getReplayTime(lastChkpt, rp)
      if(replayTime > threshold){
        res += 1
      }
    }
    res
  }

  def getInteractionTimes: Array[Int] ={
    timestamps.drop(1).map(x => (x / 1000).toInt).toArray
  }

  override def toString: String = {
    s"time = ${timestamps.mkString(",")}\n${history.mkString("--------------------------------\n")} \n completions = ${completions}"

  }
}
