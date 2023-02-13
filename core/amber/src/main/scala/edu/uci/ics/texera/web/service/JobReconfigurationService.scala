package edu.uci.ics.texera.web.service

import edu.uci.ics.amber.engine.architecture.controller.Workflow
import edu.uci.ics.amber.engine.architecture.controller.promisehandlers.ModifyLogicHandler.ModifyLogic
import edu.uci.ics.amber.engine.architecture.deploysemantics.layer.OpExecConfig
import edu.uci.ics.amber.engine.common.Constants
import edu.uci.ics.amber.engine.common.client.AmberClient
import edu.uci.ics.texera.web.model.websocket.event.TexeraWebSocketEvent
import edu.uci.ics.texera.web.model.websocket.request.ModifyLogicRequest
import edu.uci.ics.texera.web.model.websocket.response.ModifyLogicResponse
import edu.uci.ics.texera.web.storage.{JobReconfigurationStore, JobStateStore}
import edu.uci.ics.texera.workflow.common.workflow.WorkflowCompiler

import java.util.UUID
import scala.util.{Failure, Success}

class JobReconfigurationService(
    client: AmberClient,
    stateStore: JobStateStore,
    workflowCompiler: WorkflowCompiler,
    workflow: Workflow
) {

  // handles reconfigure workflow logic from frontend
  // validate the modify logic request and notifies the frontend
  def modifyOperatorLogic(modifyLogicRequest: ModifyLogicRequest): TexeraWebSocketEvent = {
    val newOp = modifyLogicRequest.operator
    val opId = newOp.operatorID
    workflowCompiler.initOperator(newOp)
    val currentOp = workflowCompiler.logicalPlan.operatorMap(opId)
    val reconfiguredPhysicalOp =
      currentOp.runtimeReconfiguration(newOp, workflowCompiler.logicalPlan.opSchemaInfo(opId))
    reconfiguredPhysicalOp match {
      case Failure(exception) => ModifyLogicResponse(opId, isValid = false, exception.getMessage)
      case Success(op) => {
        stateStore.reconfigurationStore.updateState(old =>
          JobReconfigurationStore(old.pendingReconfigurations :+ op)
        )
        ModifyLogicResponse(opId, isValid = true, "")
      }
    }
  }

  def performReconfigurationOnResume(): Unit = {
    val reconfigurations = stateStore.reconfigurationStore.getState.pendingReconfigurations
    if (reconfigurations.isEmpty) {
      return
    }

    // schedule all pending reconfigurations to the engine
    if (!Constants.enableTransactionalReconfiguration) {
      reconfigurations.foreach(newOp => {
        client.sendAsync(ModifyLogic(newOp))
      })
    } else {
      FriesAlg.computeMCS(workflow.physicalPlan, reconfigurations, UUID.randomUUID().toString)
    }

    // clear all pending reconfigurations
    stateStore.reconfigurationStore.updateState(old => JobReconfigurationStore(List()))
  }

}
