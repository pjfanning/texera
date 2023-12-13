package edu.uci.ics.texera.workflow.common.workflow

import edu.uci.ics.amber.engine.architecture.deploysemantics.PhysicalLink
import edu.uci.ics.amber.engine.common.virtualidentity.PhysicalLinkIdentity

class PartitioningPlan(val strategies: Map[PhysicalLinkIdentity, PhysicalLink]) {}
