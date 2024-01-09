package edu.uci.ics.texera.web.model.websocket.request

import edu.uci.ics.texera.workflow.common.operators.LogicalOp
import edu.uci.ics.texera.workflow.common.workflow.{BreakpointInfo, LogicalLink}
import org.jooq.types.UInteger

case class WorkflowExecuteRequest(
    executionName: String,
    engineVersion: String,
    logicalPlan: LogicalPlanPojo,
    replayFromExecution: Option[Long],
    environmentEid: UInteger,
) extends TexeraWebSocketRequest

case class LogicalPlanPojo(
    operators: List[LogicalOp],
    links: List[LogicalLink],
    breakpoints: List[BreakpointInfo],
    opsToViewResult: List[String],
    opsToReuseResult: List[String]
)
