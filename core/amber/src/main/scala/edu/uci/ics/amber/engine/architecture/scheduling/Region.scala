package edu.uci.ics.amber.engine.architecture.scheduling

import edu.uci.ics.amber.engine.architecture.deploysemantics.PhysicalOp
import edu.uci.ics.amber.engine.architecture.scheduling.config.ResourceConfig
import edu.uci.ics.amber.engine.common.virtualidentity.PhysicalOpIdentity
import edu.uci.ics.amber.engine.common.workflow.PhysicalLink
import org.jgrapht.graph.{DefaultEdge, DirectedAcyclicGraph}
import org.jgrapht.traverse.TopologicalOrderIterator

import scala.jdk.CollectionConverters.asScalaIteratorConverter

case class RegionLink(fromRegion: Region, toRegion: Region)

case class RegionIdentity(id: Long)

case class Region(
    id: RegionIdentity,
    physicalOps: Set[PhysicalOp],
    physicalLinks: Set[PhysicalLink],
    resourceConfig: Option[ResourceConfig] = None,
    // operators and links in part of the downstream region.
    downstreamOps: Set[PhysicalOp] = Set.empty,
    downstreamLinks: Set[PhysicalLink] = Set.empty
) {

  private val operators: Map[PhysicalOpIdentity, PhysicalOp] =
    getEffectiveOperators.map(op => op.id -> op).toMap

  @transient lazy val dag: DirectedAcyclicGraph[PhysicalOpIdentity, DefaultEdge] = {
    val jgraphtDag = new DirectedAcyclicGraph[PhysicalOpIdentity, DefaultEdge](classOf[DefaultEdge])
    (physicalOps ++ downstreamOps).foreach(op => jgraphtDag.addVertex(op.id))
    (physicalLinks ++ downstreamLinks)
      .filter(link => physicalOps.map(_.id).contains(link.fromOpId))
      .foreach(link => jgraphtDag.addEdge(link.fromOpId, link.toOpId))
    jgraphtDag
  }
  def topologicalIterator(): Iterator[PhysicalOpIdentity] = {
    new TopologicalOrderIterator(dag).asScala
  }

  /**
    * Return all PhysicalOps that this region may affect.
    * This includes:
    *   1) operators in this region;
    *   2) operators not in this region but receiving input from by this region.
    */
  def getEffectiveOperators: Set[PhysicalOp] = physicalOps ++ downstreamOps

  def getEffectiveLinks: Set[PhysicalLink] = {
    physicalLinks ++ downstreamLinks
  }

  def getEffectiveOperator(physicalOpId: PhysicalOpIdentity): PhysicalOp = {
    operators(physicalOpId)
  }

  /**
    * Source operators in region are effective operators that have 0 input links in the region.
    * @return
    */
  def getEffectiveSourceOpIds: Set[PhysicalOpIdentity] = {
    physicalOps
      .filter(physicalOp =>
        physicalOp
          .getInputLinks()
          .map(link => link.fromOpId)
          .forall(upstreamOpId => !physicalOps.map(_.id).contains(upstreamOpId))
      )
      .map(_.id)
  }

}
