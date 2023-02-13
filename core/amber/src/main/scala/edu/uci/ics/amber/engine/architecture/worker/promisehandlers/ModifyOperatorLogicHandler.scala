package edu.uci.ics.amber.engine.architecture.worker.promisehandlers

import edu.uci.ics.amber.engine.architecture.deploysemantics.layer.OpExecConfig
import edu.uci.ics.amber.engine.architecture.worker.WorkerAsyncRPCHandlerInitializer
import edu.uci.ics.amber.engine.architecture.worker.promisehandlers.ModifyOperatorLogicHandler.ModifyOperatorLogic
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.ControlCommand

object ModifyOperatorLogicHandler {
  case class ModifyOperatorLogic(opExecConfig: OpExecConfig) extends ControlCommand[Unit]

  case class ModifyWorkerLogicMultiple(modifyLogic: List[ModifyOperatorLogic])
      extends ControlCommand[Unit]
}

/** Get queue and other resource usage of this worker
  *
  * possible sender: controller(by ControllerInitiateMonitoring)
  */
trait ModifyOperatorLogicHandler {
  this: WorkerAsyncRPCHandlerInitializer =>

  registerHandler { (msg: ModifyOperatorLogic, _) =>
    val operator =
      msg.opExecConfig.initIOperatorExecutor((dataProcessor.workerIndex, msg.opExecConfig))
    dataProcessor.opExecConfig = msg.opExecConfig
    dataProcessor.operator = operator
    this.operator = operator
    operator.open()
  }

}
