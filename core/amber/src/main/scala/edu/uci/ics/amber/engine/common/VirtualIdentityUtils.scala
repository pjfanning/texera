package edu.uci.ics.amber.engine.common

import edu.uci.ics.amber.engine.common.virtualidentity.{
  ActorVirtualIdentity,
  OperatorIdentity,
  PhysicalOpIdentity
}

import scala.util.matching.Regex

object VirtualIdentityUtils {

  private val workerNamePattern: Regex = raw"Worker:WF(\w+)-(.+)-(\w+)-(\d+)".r

  def createWorkerIdentity(
      executionId: Long,
      operator: String,
      layer: String,
      workerId: Int
  ): ActorVirtualIdentity = {
    ActorVirtualIdentity(s"Worker:WF$executionId-$operator-$layer-$workerId")
  }

  def createWorkerIdentity(
      executionId: Long,
      layer: PhysicalOpIdentity,
      workerId: Int
  ): ActorVirtualIdentity = {
    ActorVirtualIdentity(
      s"Worker:WF$executionId-${layer.logicalOpId.id}-${layer.layerName}-$workerId"
    )
  }

  def getPhysicalOpId(workerId: ActorVirtualIdentity): PhysicalOpIdentity = {
    workerId.name match {
      case workerNamePattern(_, operator, layerName, _) =>
        PhysicalOpIdentity(OperatorIdentity(operator), layerName)
    }
  }

  def getWorkerIndex(workerId: ActorVirtualIdentity): Int = {
    workerId.name match {
      case workerNamePattern(_, _, _, idx) =>
        idx.toInt
    }
  }
}
