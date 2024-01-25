package edu.uci.ics.amber.engine.architecture.scheduling

import edu.uci.ics.amber.engine.architecture.deploysemantics.PhysicalOp
import edu.uci.ics.amber.engine.architecture.scheduling.config.ResourceConfig
import edu.uci.ics.amber.engine.common.virtualidentity.PhysicalOpIdentity
import edu.uci.ics.amber.engine.common.workflow.PhysicalLink

case class RegionLink(fromRegion: Region, toRegion: Region)

case class RegionIdentity(id: String)

case class Region(
                   id: RegionIdentity,
                   physicalOps: Set[PhysicalOp],
                   physicalLinks: Set[PhysicalLink],
                   resourceConfig: Option[ResourceConfig] = None,
                   // links to downstream regions, where this region generates outputs to
                   downstreamLinks: Set[PhysicalLink] = Set.empty
) {

  /**
    * Return all PhysicalOpIds that this region may affect.
    * This includes:
    *   1) operators in this region;
    *   2) operators not in this region but blocked by this region (connected by the downstream links).
    */
  def getEffectiveOpIds: Set[PhysicalOpIdentity] = {
    physicalOps.map(_.id) ++ downstreamLinks.map(link => link.toOpId)
  }

  def getEffectiveLinks: Set[PhysicalLink] = {
    physicalLinks ++ downstreamLinks
  }

  def getSourceOpIds: Set[PhysicalOpIdentity] = {
    physicalOps
      .filter(physicalOp =>
          physicalOp.getInputLinks().map(link => link.fromOpId).forall(upstreamOpId => !physicalOps.map(_.id).contains(upstreamOpId))
      ).map(_.id)
  }


}
