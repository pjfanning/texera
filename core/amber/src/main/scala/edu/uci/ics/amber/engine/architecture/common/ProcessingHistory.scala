package edu.uci.ics.amber.engine.architecture.common

import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity

import scala.collection.mutable

class ProcessingHistory extends Serializable {

  private val history = mutable.ArrayBuffer[LogicalExecutionSnapshot](new EmptyLogicalExecutionSnapshot())
  private val timestamps = mutable.ArrayBuffer[Long](0)
  private val interactions = mutable.ArrayBuffer[Int]()
  var inputConstant = 100

  def addSnapshot(time: Long, snapshot: LogicalExecutionSnapshot): Int = {
    val idx = timestamps.size
    timestamps.append(time)
    history.append(snapshot)
    idx
  }

  def addInteraction(time: Long, snapshot: LogicalExecutionSnapshot): Int ={
    val idx = addSnapshot(time, snapshot)
    interactions.append(idx)
    idx
  }

  def getTimeGap(start:Int, end:Int):Long = {
    if(start < 0){
      return timestamps(end) - timestamps(0)
    }
    timestamps(end) - timestamps(start)
  }

  def getSnapshots: Iterable[LogicalExecutionSnapshot] = history

  def getSnapshot(idx: Int): LogicalExecutionSnapshot = history(idx)

  def getInteractionIdxes: Array[Int] = {
    interactions.toArray
  }

  def getInteractionTimes: Array[Int] = {
    interactions.map(x => timestamps(x).toInt/1000).toArray
  }

  def getOperatorCost(op: ActorVirtualIdentity, currentIdx: Int, chkptPos:Map[ActorVirtualIdentity, Int]):Long = {
    var currentCost = 0L
    val info = getSnapshot(currentIdx).getStats(op)
    currentCost += info.checkpointCost
    info.inputStatus.keys.map(_.endpointWorker).toSet.foreach{
      k:ActorVirtualIdentity =>
        val pos = chkptPos.getOrElse(k, 0)
        if(pos >= currentIdx){
          val toReceive = getSnapshot(pos).getStats(op).inputStatus.getToReceive(k)
          val received = info.inputStatus.getReceived(k)
          currentCost += (toReceive - received) / inputConstant
        }
    }
    currentCost
  }


  def getPlanCost(chkptPos:Map[ActorVirtualIdentity, Int]): Long ={
    var cost = 0L
    chkptPos.keys.foreach{
      k =>
        cost += getOperatorCost(k, chkptPos(k), chkptPos)
    }
    cost
  }

  override def toString: String = {
    s"time = ${timestamps.mkString(",")}\n${history.mkString("--------------------------------\n")} \n"

  }
}
