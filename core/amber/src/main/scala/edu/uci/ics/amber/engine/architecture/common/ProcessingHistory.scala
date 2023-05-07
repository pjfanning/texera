package edu.uci.ics.amber.engine.architecture.common

import edu.uci.ics.amber.engine.architecture.common.LogicalExecutionSnapshot.ProcessingStats
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity

import scala.collection.mutable

class ProcessingHistory extends Serializable {

  private val history = mutable.HashMap[Long, LogicalExecutionSnapshot]()
  private val idMapping = mutable.HashMap[String, Long]()
  var historyArray:Array[Long] = _
  var inputConstant = 1

  def hasSnapshotWithID(id:String): Boolean ={
    idMapping.contains(id)
  }

  def hasSnapshotAtTime(time:Long):Boolean = {
    history.contains(time)
  }

  def getTimeGap(start:Int, end:Int):Long = {
    if(start < 0){
      return history(historyArray(end)).timestamp - history(historyArray(0)).timestamp
    }
    history(historyArray(end)).timestamp - history(historyArray(start)).timestamp
  }


  def addSnapshot(time: Long, snapshot: LogicalExecutionSnapshot, id:String = null): Unit = {
    history(time) = snapshot
    if(id != null){
      idMapping(id) = time
    }
    historyArray = history.keys.toArray.sorted
  }

  def getSnapshots: Iterable[LogicalExecutionSnapshot] = history.values

  def getSnapshot(id: String): LogicalExecutionSnapshot = {
    history(idMapping(id))
  }

  def getSnapshot(time:Long):LogicalExecutionSnapshot = {
    history(time)
  }

  def getSnapshotTime(id:String):Long = idMapping(id)

  def getSnapshot(idx:Int): LogicalExecutionSnapshot ={
    history(historyArray(idx))
  }

  def getInteractionTimes: Array[Long] = {
    history.keys.filter(k => history(k).isInteraction).toArray.sorted
  }

  def getInteractionIdx(time:Long):Int = {
    historyArray.indices.find(i => historyArray(i) == time).get
  }

  def getInteractionIdxes: Array[Int] = {
    historyArray.indices.filter(i => history(historyArray(i)).isInteraction).toArray
  }

  def getSnapshotStatus:Array[String] = {
    historyArray.map(history).map(x => if(x.isAllCheckpointed){"all"}else if(x.isNoneCheckpointed){"none"}else{"partial"})
  }

  def getOperatorCost(op: ActorVirtualIdentity, currentIdx: Int, chkptPos:Map[ActorVirtualIdentity, Int]):Long = {
    var currentCost = 0L
    val info = getSnapshot(currentIdx).getStats(op)
    currentCost += info.checkpointCost
    info.inputStatus.keys.map(_.endpointWorker).toSet.foreach{
      k:ActorVirtualIdentity =>
        val pos = chkptPos.getOrElse(k, 0)
        if(pos >= currentIdx){
          val toReceive = getSnapshot(pos).getStats(op).inputStatus.getDataToReceive(k)
          val received = info.inputStatus.getDataReceived(k)
          currentCost += (toReceive - received) * inputConstant
        }
    }
    currentCost
  }

  def getOperatorCostOverTime(op:ActorVirtualIdentity): Seq[Long] ={
    historyArray.map(history).map(_.getStats(op).checkpointCost)
  }


  def getPlanCost(chkptPos:Map[ActorVirtualIdentity, Int]): Long ={
    var cost = 0L
    chkptPos.keys.foreach{
      k =>
        cost += getOperatorCost(k, chkptPos(k), chkptPos)
    }
    cost
  }

  def getPlanCost(idx: Int): Long ={
    val chkptPos = history(historyArray(idx)).getParticipants.map(x => x -> idx).toMap
    getPlanCost(chkptPos)
  }

  override def toString: String = {
    s"${history.mkString("--------------------------------\n")} \n"

  }
}
