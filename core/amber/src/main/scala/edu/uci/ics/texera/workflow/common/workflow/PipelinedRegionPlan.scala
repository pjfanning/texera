package edu.uci.ics.texera.workflow.common.workflow

import edu.uci.ics.amber.engine.architecture.scheduling.{PipelinedRegion, PipelinedRegionIdentity}
import org.jgrapht.graph.{DefaultEdge, DirectedAcyclicGraph}
import org.jgrapht.traverse.TopologicalOrderIterator

import scala.collection.JavaConverters.asScalaIterator
import scala.collection.mutable
import scala.jdk.CollectionConverters.{asScalaIteratorConverter, iterableAsScalaIterableConverter}

case class PipelinedRegionPlan(
    pipelinedRegionsDAG: DirectedAcyclicGraph[PipelinedRegion, DefaultEdge] = null
) {

  lazy private val pipelinedRegionMap = pipelinedRegionsDAG.asScala.map(x => x.id -> x).toMap

  def getAllRegions(): List[PipelinedRegion] = {
    asScalaIterator(pipelinedRegionsDAG.iterator()).toList
  }

  def getRegionScheduleOrder(): mutable.Buffer[PipelinedRegionIdentity] = {
    new TopologicalOrderIterator(pipelinedRegionsDAG).asScala.map(_.id).toBuffer
  }

//  def getOperatorsInRegion(physicalPlan: PhysicalPlan, region: PipelinedRegion): PhysicalPlan = {
//    val newOpIds = region.getOperators()
//    val newOps = physicalPlan.operators.filter(op => newOpIds.contains(op.id))
//    val newLinks = physicalPlan.links.filter(l => newOpIds.contains(l.from) && newOpIds.contains(l.to))
//    val newLinkStrategies = physicalPlan.linkStrategies.filter(l => newLinks.contains(l._1))
//    PhysicalPlan(newOps, newLinks, newLinkStrategies)
//  }

  def getPipelinedRegion(id: PipelinedRegionIdentity): PipelinedRegion = pipelinedRegionMap(id)
}
