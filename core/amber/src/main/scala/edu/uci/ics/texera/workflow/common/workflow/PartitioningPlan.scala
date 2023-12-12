package edu.uci.ics.texera.workflow.common.workflow

import edu.uci.ics.amber.engine.architecture.linksemantics.LinkStrategy
import edu.uci.ics.amber.engine.common.virtualidentity.PhysicalLink

class PartitioningPlan(val strategies: Map[PhysicalLink, LinkStrategy]) {}
