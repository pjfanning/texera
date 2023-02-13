package edu.uci.ics.texera.web.storage

import edu.uci.ics.amber.engine.architecture.deploysemantics.layer.OpExecConfig

case class JobReconfigurationStore(
    pendingReconfigurations: List[OpExecConfig] = List()
)
