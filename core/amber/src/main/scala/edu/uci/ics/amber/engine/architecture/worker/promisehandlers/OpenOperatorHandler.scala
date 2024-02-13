package edu.uci.ics.amber.engine.architecture.worker.promisehandlers

import edu.uci.ics.amber.engine.architecture.worker.DataProcessorRPCHandlerInitializer
import edu.uci.ics.amber.engine.architecture.worker.promisehandlers.OpenOperatorHandler.OpenOperator
import edu.uci.ics.amber.engine.common.{ISinkOperatorExecutor, ISourceOperatorExecutor}
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.ControlCommand
import edu.uci.ics.amber.engine.common.virtualidentity.util.{SOURCE_STARTER_ACTOR, SOURCE_STARTER_OP}
import edu.uci.ics.amber.engine.common.workflow.{PhysicalLink, PortIdentity}
import edu.uci.ics.texera.workflow.common.operators.OperatorExecutor
import edu.uci.ics.texera.workflow.operators.loop.LoopStartOpExec

object OpenOperatorHandler {

  final case class OpenOperator() extends ControlCommand[Unit]
}

trait OpenOperatorHandler {
  this: DataProcessorRPCHandlerInitializer =>
  registerHandler { (openOperator: OpenOperator, sender) =>
    dp.operator.open()
  }
}
