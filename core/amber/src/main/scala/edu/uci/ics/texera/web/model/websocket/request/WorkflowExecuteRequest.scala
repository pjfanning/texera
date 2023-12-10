package edu.uci.ics.texera.web.model.websocket.request

import edu.uci.ics.texera.workflow.common.operators.LogicalOp
import edu.uci.ics.texera.workflow.common.workflow.{BreakpointInfo, OperatorLink}

case class WorkflowExecuteRequest(
    executionName: String,
    engineVersion: String,
    logicalPlan: LogicalPlanPojo
) extends TexeraWebSocketRequest

case class LogicalPlanPojo(
                            operators: List[LogicalOp],
                            links: List[OperatorLink],
                            breakpoints: List[BreakpointInfo],
                            opsToViewResult: List[String],
                            opsToReuseResult: List[String]
)
