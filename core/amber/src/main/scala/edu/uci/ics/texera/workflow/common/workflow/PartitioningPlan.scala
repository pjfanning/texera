package edu.uci.ics.texera.workflow.common.workflow

import edu.uci.ics.amber.engine.architecture.linksemantics.LinkStrategy
import edu.uci.ics.amber.engine.common.virtualidentity.PhysicalLinkIdentity

class PartitioningPlan(val strategies: Map[PhysicalLinkIdentity, LinkStrategy]) {}
