package edu.uci.ics.amber.engine.architecture.controller

import edu.uci.ics.amber.engine.architecture.scheduling.config.{LinkConfig, OperatorConfig}
import edu.uci.ics.amber.engine.common.virtualidentity.PhysicalOpIdentity
import edu.uci.ics.amber.engine.common.workflow.PhysicalLink
import edu.uci.ics.texera.web.workflowruntimestate.WorkflowAggregatedState
import edu.uci.ics.texera.workflow.common.WorkflowContext

import scala.collection.mutable

class RegionExecution {

  private val linkExecutions: mutable.Map[PhysicalLink, LinkExecution] = mutable.HashMap()

  private val operatorExecutions: mutable.Map[PhysicalOpIdentity, OperatorExecution] =
    mutable.HashMap()

  def isRunning: Boolean = getState == WorkflowAggregatedState.RUNNING

  def isCompleted: Boolean = getState == WorkflowAggregatedState.COMPLETED

  def getOperatorExecution(op: PhysicalOpIdentity): Option[OperatorExecution] = {
    operatorExecutions.get(op)
  }

  def getLinkExecution(link: PhysicalLink): Option[LinkExecution] = {
    linkExecutions.get(link)
  }

  def getAllOperatorExecutions: List[(PhysicalOpIdentity, OperatorExecution)] =
    operatorExecutions.toList

  def getAllLinkExecutions(): List[(PhysicalLink, LinkExecution)] = linkExecutions.toList

  def initOperatorExecution(
      workflowContext: WorkflowContext,
      physicalOpId: PhysicalOpIdentity,
      operatorConfig: OperatorConfig
  ): OperatorExecution = {
    operatorExecutions += physicalOpId -> new OperatorExecution(
      workflowContext.workflowId,
      workflowContext.executionId,
      physicalOpId,
      numWorkers = operatorConfig.workerConfigs.length
    )
    operatorExecutions(physicalOpId)
  }

  def setOperatorExecution(
      physicalOpId: PhysicalOpIdentity,
      operatorExecution: OperatorExecution
  ): OperatorExecution = {
    operatorExecutions += physicalOpId -> operatorExecution
    operatorExecutions(physicalOpId)
  }

  def getState: WorkflowAggregatedState = {
    val operatorStates =
      operatorExecutions.values.map(operatorExecution => operatorExecution.getState).toList
    if (operatorStates.isEmpty) {
      return WorkflowAggregatedState.UNINITIALIZED
    }
    if (operatorStates.forall(_ == WorkflowAggregatedState.COMPLETED)) {
      return WorkflowAggregatedState.COMPLETED
    }
    if (operatorStates.contains(WorkflowAggregatedState.RUNNING)) {
      return WorkflowAggregatedState.RUNNING
    }
    val unCompletedOperatorStates = operatorStates.filter(_ != WorkflowAggregatedState.COMPLETED)
    if (unCompletedOperatorStates.forall(_ == WorkflowAggregatedState.UNINITIALIZED)) {
      WorkflowAggregatedState.UNINITIALIZED
    } else if (unCompletedOperatorStates.forall(_ == WorkflowAggregatedState.PAUSED)) {
      WorkflowAggregatedState.PAUSED
    } else if (unCompletedOperatorStates.forall(_ == WorkflowAggregatedState.READY)) {
      WorkflowAggregatedState.READY
    } else {
      WorkflowAggregatedState.UNKNOWN
    }
  }

  def initLinkExecution(
      physicalLink: PhysicalLink,
      linkConfig: LinkConfig
  ) = {
    linkExecutions += physicalLink -> new LinkExecution(
      linkConfig.channelConfigs.length
    )
    linkExecutions(physicalLink)
  }
}
