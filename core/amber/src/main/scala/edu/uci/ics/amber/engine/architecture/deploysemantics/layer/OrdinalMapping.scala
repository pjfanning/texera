package edu.uci.ics.amber.engine.architecture.deploysemantics.layer

import edu.uci.ics.amber.engine.common.virtualidentity.LinkIdentity

case class OrdinalMapping(
    input: Map[LinkIdentity, Int] = Map(),
    output: Map[LinkIdentity, Int] = Map()
)
