package edu.uci.ics.amber.engine.architecture.scheduling.config

import edu.uci.ics.amber.engine.common.virtualidentity.{PhysicalLinkIdentity, PhysicalOpIdentity}

case class RegionConfig(
    workerConfigs: Map[PhysicalOpIdentity, List[WorkerConfig]],
    linkConfigs: Map[PhysicalLinkIdentity, LinkConfig]
)
