package edu.uci.ics.amber.engine.architecture.common

import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity
import edu.uci.ics.texera.web.service.ReplayPlanner
import edu.uci.ics.texera.web.service.ReplayPlanner.getLastCheckpoint

import scala.collection.mutable

class InteractionHistory extends Serializable {

  private val history = mutable.ArrayBuffer[Interaction](new Interaction(true))
  private val timestamps = mutable.ArrayBuffer[Long](0)
  private val completions = mutable.HashMap[ActorVirtualIdentity, Long]()

  def addCompletion(id: ActorVirtualIdentity, completion: Long): Unit = {
    completions(id) = completion
  }

  def addInteraction(time: Long, interaction: Interaction): Int = {
    val idx = timestamps.size
    timestamps.append(time)
    history.append(interaction)
    idx
  }

  def findInteractionIdx(id: ActorVirtualIdentity, alignment: Long): Int = {
    history.indexWhere(x => x.containsAlignment(id, alignment))
  }

  def computeGlobalCheckpointCutoff(idx: Int):Map[ActorVirtualIdentity, Map[ActorVirtualIdentity, Long]] = {
    val interaction = getInteraction(idx)
    interaction.allReceiverChannelStates
  }

  def getCheckpointReverseMapping(checkpointMapping: Map[Int, Set[ActorVirtualIdentity]]): Map[ActorVirtualIdentity, Int] ={
    val lastCheckpointMapping = mutable.HashMap[ActorVirtualIdentity, Int]()
    checkpointMapping.foreach{
      case (i, identities) =>
        identities.foreach(x => lastCheckpointMapping(x) = i)
    }
    lastCheckpointMapping.toMap
  }

  def computeLocalCheckpointCutoff(idx: Int, toCheckpoint:Set[ActorVirtualIdentity], checkpoints: Map[Int, Set[ActorVirtualIdentity]]):Map[ActorVirtualIdentity, Map[ActorVirtualIdentity, Long]] = {
    val lastCheckpoint = ReplayPlanner.getLastCheckpoint(checkpoints, idx, toCheckpoint)
    val lastCheckpointMapping = getCheckpointReverseMapping(lastCheckpoint)
    val result = mutable.HashMap[ActorVirtualIdentity, mutable.HashMap[ActorVirtualIdentity, Long]]()
    toCheckpoint.foreach {
      worker =>
        val downstreams = getInteraction(idx).allReceiverChannelStates.keys
        downstreams.foreach {
          downstream =>
            val received = if (toCheckpoint.contains(downstream)) {
              getInteraction(idx).allReceiverChannelStates
            } else if (lastCheckpointMapping.contains(downstream)) {
              getInteraction(lastCheckpointMapping(downstream)).allReceiverChannelStates
            } else {
              Map[ActorVirtualIdentity, Map[ActorVirtualIdentity, Long]]()
            }
            if (received.contains(worker) && received(worker).contains(downstream)) {
              result.getOrElseUpdate(worker, mutable.HashMap[ActorVirtualIdentity, Long]())(downstream) = received(worker)(downstream)
            }
        }
    }
    result.mapValues(_.toMap).toMap
  }


  def getInteractions: Iterable[Interaction] = history

  def getInteraction(idx: Int): Interaction = history(idx)

  def getCheckpointCost(
                         idx: Int,
                         checkpointed: Map[Int, Set[ActorVirtualIdentity]]
  ): Long = {
    val interaction = history(idx)
    val existingCheckpoints = mutable.HashMap[ActorVirtualIdentity, mutable.HashSet[Long]]()
    checkpointed.foreach{
      case (i, identities) =>
        identities.foreach{
          id => existingCheckpoints.getOrElseUpdate(id, new mutable.HashSet[Long]()).add(getInteraction(i).getAlignment(id).getOrElse(0L))
        }
    }
    val toChkpt = interaction.getParticipants.filter(p => {
      val existing = existingCheckpoints.get(p)
      if (existing.isDefined && completions.contains(p)) {
        interaction.getAlignment(p).get < completions(p) || !existing.get.exists(x =>
          x >= completions(p)
        )
      } else {
        true
      }
    })
    toChkpt.toArray
      .map(interaction.getCheckpointCost)
      .map {
        case Some(value) => value
        case None        => 0
      }
      .sum
  }

  def getCheckpointCost(
      idxes: Iterable[Int],
      checkpointed: Map[Int, Set[ActorVirtualIdentity]]
  ): Long = {
    var existing = checkpointed
    var cost = 0L
    idxes.foreach { idx =>
      cost += getCheckpointCost(idx, existing)
      existing = existing ++ Map(idx -> getInteraction(idx).getParticipants.toSet)
    }
    cost
  }

  def getGlobalReplayTime(from: Int, to: Int): Long = {
    timestamps(to) - timestamps(from)
  }

  def getWorkerReplayTime(id:ActorVirtualIdentity, from: Int, to:Int):Long = {
    history(to).getBusyTime(id).getOrElse(0L) - history(from).getBusyTime(id).getOrElse(0L)
  }

  def getTotalReplayTime(
      checkpoint: Map[Int, Set[ActorVirtualIdentity]],
      replayPoints: Array[Int]
  ): Long = {
    var res = 0L
    replayPoints.foreach { rp =>
      val lastChkpt = getLastCheckpoint(checkpoint, rp, history(rp).getParticipants.toSet)
      res += getReplayTime(lastChkpt, rp)
    }
    res
  }


  def getReplayTime(lastChkpt: Map[Int, Set[ActorVirtualIdentity]], rp:Int): Long ={
    var maxLoadCost = 0L
    var maxReplayTime = 0L
    lastChkpt.foreach{
      case (chkptTime, chkptWorkers) =>
        chkptWorkers.foreach{
          worker =>
            maxLoadCost = Math.max(history(chkptTime).getLoadCost(worker).getOrElse(0L), maxLoadCost)
            maxReplayTime = Math.max(getWorkerReplayTime(worker, chkptTime, rp), maxReplayTime)
        }
    }
    maxLoadCost+maxReplayTime
  }

  def validateReplayTime(
      checkpoint: Map[Int, Set[ActorVirtualIdentity]],
      replayPoints: Array[Int],
      threshold: Int
  ): Int = {
    var res = 0
    replayPoints.foreach { rp =>
      val lastChkpt = getLastCheckpoint(checkpoint, rp, history(rp).getParticipants.toSet)
      val replayTime = getReplayTime(lastChkpt, rp)
      if (replayTime > threshold) {
        res += 1
      }
    }
    res
  }

  def getInteractionTimes: Array[Int] = {
    timestamps.drop(1).map(x => (x / 1000).toInt).toArray
  }

  override def toString: String = {
    s"time = ${timestamps.mkString(",")}\n${history.mkString("--------------------------------\n")} \n completions = ${completions}"

  }
}
