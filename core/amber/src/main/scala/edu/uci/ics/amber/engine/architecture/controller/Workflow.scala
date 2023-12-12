package edu.uci.ics.amber.engine.architecture.controller

import edu.uci.ics.amber.engine.architecture.scheduling.{ExecutionPlan, PipelinedRegion}
import edu.uci.ics.amber.engine.common.virtualidentity._
import edu.uci.ics.texera.workflow.common.workflow.{LogicalPlan, PartitioningPlan, PhysicalPlan}

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class Workflow(
    val workflowId: WorkflowIdentity,
    val originalLogicalPlan: LogicalPlan,
    val logicalPlan: LogicalPlan,
    val physicalPlan: PhysicalPlan,
    val executionPlan: ExecutionPlan,
    val partitioningPlan: PartitioningPlan
) extends java.io.Serializable {

  def getBlockingOutLinksOfRegion(region: PipelinedRegion): Set[PhysicalLink] = {
    val outLinks = new mutable.HashSet[PhysicalLink]()
    region.blockingDownstreamOperatorsInOtherRegions.foreach {
      case (opId, toPort) =>
        physicalPlan
          .getUpstreamPhysicalOpIds(opId)
          .foreach(upstream => {
            if (region.operators.contains(upstream)) {
              outLinks.add(PhysicalLink(upstream, 0, opId, toPort))
            }
          })
    }
    outLinks.toSet
  }

  /**
    * Returns the operators in a region whose all inputs are from operators that are not in this region.
    */
  def getSourcesOfRegion(region: PipelinedRegion): Array[PhysicalOpIdentity] = {
    val sources = new ArrayBuffer[PhysicalOpIdentity]()
    region.getOperators
      .foreach(opId => {
        val isSource =
          physicalPlan.getUpstreamPhysicalOpIds(opId).forall(up => !region.containsOperator(up))
        if (isSource) {
          sources.append(opId)
        }
      })
    sources.toArray
  }

}
