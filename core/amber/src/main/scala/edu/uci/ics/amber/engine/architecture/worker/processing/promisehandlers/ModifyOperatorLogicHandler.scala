package edu.uci.ics.amber.engine.architecture.worker.processing.promisehandlers

import edu.uci.ics.amber.engine.architecture.deploysemantics.layer.OpExecConfig
import edu.uci.ics.amber.engine.architecture.worker.processing.DataProcessorRPCHandlerInitializer
import edu.uci.ics.amber.engine.architecture.worker.processing.promisehandlers.ModifyOperatorLogicHandler.{WorkerModifyLogic, WorkerModifyLogicMultiple}
import edu.uci.ics.amber.engine.common.ambermessage.ClientEvent.WorkerModifyLogicComplete
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.{ControlCommand, SkipReply}
import edu.uci.ics.texera.workflow.common.operators.StateTransferFunc

object ModifyOperatorLogicHandler {
  case class WorkerModifyLogic(
      opExecConfig: OpExecConfig,
      stateTransferFunc: Option[StateTransferFunc]
  ) extends ControlCommand[Unit]

  case class WorkerModifyLogicMultiple(modifyLogicList: List[WorkerModifyLogic])
      extends ControlCommand[Unit]
      with SkipReply
}

/** Get queue and other resource usage of this worker
  *
  * possible sender: controller(by ControllerInitiateMonitoring)
  */
trait ModifyOperatorLogicHandler {
  this: DataProcessorRPCHandlerInitializer =>

  registerHandler { (msg: WorkerModifyLogic, _) =>
    performModifyLogic(msg)
    sendToClient(WorkerModifyLogicComplete(this.actorId))
  }

  registerHandler { (msg: WorkerModifyLogicMultiple, _) =>
    val modifyLogic =
      msg.modifyLogicList.find(o => o.opExecConfig.id == dp.getOperatorId)
    if (modifyLogic.nonEmpty) {
      performModifyLogic(modifyLogic.get)
      sendToClient(WorkerModifyLogicComplete(this.actorId))
    }
  }

  private def performModifyLogic(modifyLogic: WorkerModifyLogic): Unit = {
    val newOpExecConfig = modifyLogic.opExecConfig
    val newOperator =
      newOpExecConfig.initIOperatorExecutor((dp.getWorkerIndex, newOpExecConfig))
    if (modifyLogic.stateTransferFunc.nonEmpty) {
      modifyLogic.stateTransferFunc.get.apply(dp.operator, newOperator)
    }
    dp.worker.operator = newOperator
    dp.operator.open()
  }

}
