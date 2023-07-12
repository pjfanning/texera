package edu.uci.ics.amber.engine.architecture.controller.processing.promisehandlers

import com.twitter.util.Future
import edu.uci.ics.amber.engine.architecture.controller.Controller
import EvaluatePythonExpressionHandler.EvaluatePythonExpression
import edu.uci.ics.amber.engine.architecture.controller.processing.ControllerAsyncRPCHandlerInitializer
import edu.uci.ics.amber.engine.architecture.pythonworker.promisehandlers.EvaluateExpressionHandler.EvaluateExpression
import edu.uci.ics.amber.engine.architecture.worker.controlreturns.EvaluatedValue
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.ControlCommand
import edu.uci.ics.amber.engine.common.virtualidentity.OperatorIdentity

object EvaluatePythonExpressionHandler {
  final case class EvaluatePythonExpression(expression: String, operatorId: String)
      extends ControlCommand[List[EvaluatedValue]]
}

trait EvaluatePythonExpressionHandler {
  this: ControllerAsyncRPCHandlerInitializer =>
  registerHandler { (msg: EvaluatePythonExpression, sender) =>
    {
      val operatorId = new OperatorIdentity(cp.workflow.workflowId.id, msg.operatorId)
      val operator = cp.workflow.physicalPlan.getSingleLayerOfLogicalOperator(operatorId)

      Future
        .collect(
          operator.identifiers
            .map(worker => send(EvaluateExpression(msg.expression), worker))
            .toList
        )
        .map(evaluatedValues => {
          evaluatedValues.toList
        })
    }
  }
}
