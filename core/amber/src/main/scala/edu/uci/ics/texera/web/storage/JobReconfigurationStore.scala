package edu.uci.ics.texera.web.storage

import edu.uci.ics.amber.engine.architecture.deploysemantics.layer.OpExecConfig
import edu.uci.ics.amber.engine.common.virtualidentity.{ActorVirtualIdentity, LayerIdentity}

case class JobReconfigurationStore(
    currentReconfigId: Option[String] = None,
    unscheduledReconfigs: List[OpExecConfig] = List(),
    completedReconfigs: Set[ActorVirtualIdentity] = Set()
)
