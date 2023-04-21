package edu.uci.ics.amber.engine.architecture.common

import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity

import scala.collection.mutable

class ProcessingHistory extends Serializable {

  private val history = mutable.HashMap[Long, LogicalExecutionSnapshot]()
  private val idMapping = mutable.HashMap[String, Long]()
  history(0) = new EmptyLogicalExecutionSnapshot()
  var inputConstant = 100

  def hasSnapshotWithID(id:String): Boolean ={
    idMapping.contains(id)
  }

  def hasSnapshotAtTime(time:Long):Boolean = {
    history.contains(time)
  }

  def addSnapshot(time: Long, snapshot: LogicalExecutionSnapshot, id:String = null): Unit = {
    history(time) = snapshot
    if(id != null){
      idMapping(id) = time
    }
  }

  def getTimeGap(start:Long, end:Long):Long = {
    if(start < 0){
      return history(end).timestamp - history(0).timestamp
    }
    history(end).timestamp - history(start).timestamp
  }

  def getSnapshots: Iterable[LogicalExecutionSnapshot] = history.values

  def getSnapshot(time: Long): LogicalExecutionSnapshot = {
    history(time)
  }

  def getSnapshot(id: String): LogicalExecutionSnapshot = {
    history(idMapping(id))
  }

  def getInteractionTimesAsSeconds: Array[Int] = {
    history.keys.map(x => x.toInt/1000).toArray.sorted
  }

  def getInteractionTimes: Array[Long] = {
    history.keys.toArray.sorted
  }

  def getOperatorCost(op: ActorVirtualIdentity, currentTime: Long, chkptPos:Map[ActorVirtualIdentity, Long]):Long = {
    var currentCost = 0L
    val info = getSnapshot(currentTime).getStats(op)
    currentCost += info.checkpointCost
    info.inputStatus.keys.map(_.endpointWorker).toSet.foreach{
      k:ActorVirtualIdentity =>
        val pos = chkptPos.getOrElse(k, 0L)
        if(pos >= currentTime){
          val toReceive = getSnapshot(pos).getStats(op).inputStatus.getToReceive(k)
          val received = info.inputStatus.getReceived(k)
          currentCost += (toReceive - received) / inputConstant
        }
    }
    currentCost
  }


  def getPlanCost(chkptPos:Map[ActorVirtualIdentity, Long]): Long ={
    var cost = 0L
    chkptPos.keys.foreach{
      k =>
        cost += getOperatorCost(k, chkptPos(k), chkptPos)
    }
    cost
  }

  override def toString: String = {
    s"${history.mkString("--------------------------------\n")} \n"

  }
}
