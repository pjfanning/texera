package edu.uci.ics.amber.engine.architecture.controller

import akka.actor.Address
import edu.uci.ics.amber.engine.architecture.common.VirtualIdentityUtils
import edu.uci.ics.amber.engine.architecture.deploysemantics.layer.{OpExecConfig, WorkerInfo}
import edu.uci.ics.amber.engine.architecture.linksemantics._
import edu.uci.ics.amber.engine.architecture.scheduling.PipelinedRegion
import edu.uci.ics.amber.engine.common.virtualidentity._
import edu.uci.ics.texera.web.workflowruntimestate.{OperatorRuntimeStats, WorkflowAggregatedState}
import edu.uci.ics.texera.workflow.common.workflow.PhysicalPlan
import edu.uci.ics.texera.workflow.operators.udf.pythonV2.PythonUDFOpExecV2
import org.jgrapht.graph.{DefaultEdge, DirectedAcyclicGraph}

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class Workflow(val workflowId: WorkflowIdentity, val physicalPlan: PhysicalPlan)
    extends java.io.Serializable {

  def getDAG: DirectedAcyclicGraph[ActorVirtualIdentity, DefaultEdge] = {
    val dag =
      new DirectedAcyclicGraph[ActorVirtualIdentity, DefaultEdge](classOf[DefaultEdge])
    physicalPlan.operators.flatMap(_.identifiers).foreach(worker => dag.addVertex(worker))
    physicalPlan.linkStrategies.values.foreach {
      case one: AllToOne =>
        one.from.identifiers.foreach(worker => dag.addEdge(worker, one.to.identifiers.head))
      case one: OneToOne =>
        one.from.identifiers.indices.foreach(i =>
          dag.addEdge(one.from.identifiers(i), one.to.identifiers(i))
        )
      case other =>
        other.from.identifiers.indices.foreach(i =>
          other.to.identifiers.indices.foreach(j =>
            dag.addEdge(other.from.identifiers(i), other.to.identifiers(j))
          )
        )
    }
    dag
  }

  def getBlockingOutLinksOfRegion(region: PipelinedRegion): Set[LinkIdentity] = {
    val outLinks = new mutable.HashSet[LinkIdentity]()
    region.blockingDownstreamOperatorsInOtherRegions.foreach(opId => {
      physicalPlan
        .getUpstream(opId)
        .foreach(upstream => {
          if (region.operators.contains(upstream)) {
            outLinks.add(LinkIdentity(upstream, opId))
          }
        })
    })
    outLinks.toSet
  }

  /**
    * Returns the operators in a region whose all inputs are from operators that are not in this region.
    */
  def getSourcesOfRegion(region: PipelinedRegion): Array[LayerIdentity] = {
    val sources = new ArrayBuffer[LayerIdentity]()
    region
      .getOperators()
      .foreach(opId => {
        val isSource = physicalPlan.getUpstream(opId).forall(up => !region.containsOperator(up))
        if (isSource) {
          sources.append(opId)
        }
      })
    sources.toArray
  }

  def getAllWorkersOfRegion(region: PipelinedRegion): Array[ActorVirtualIdentity] = {
    val allOperatorsInRegion =
      region.getOperators() ++ region.blockingDownstreamOperatorsInOtherRegions

    allOperatorsInRegion.flatMap(opId => physicalPlan.operatorMap(opId).identifiers.toList)
  }

  def getWorkflowId(): WorkflowIdentity = workflowId

  def getDirectUpstreamWorkers(vid: ActorVirtualIdentity): Iterable[ActorVirtualIdentity] = {
    val opId = VirtualIdentityUtils.getOperator(vid)
    val upstreamLinks = physicalPlan.getUpstreamLinks(opId)
    val upstreamWorkers = mutable.HashSet[ActorVirtualIdentity]()
    upstreamLinks
      .map(link => physicalPlan.linkStrategies(link))
      .flatMap(linkStrategy => linkStrategy.getPartitioning.toList)
      .foreach {
        case (sender, _, _, receivers) =>
          if (receivers.contains(vid)) {
            upstreamWorkers.add(sender)
          }
      }
    upstreamWorkers
  }

  def getAllOperators: Iterable[OpExecConfig] = physicalPlan.operators

  /**
    * Returns the worker layer of the upstream operators that links to the `opId` operator's
    * worker layer.
    */
  def getUpStreamConnectedWorkerLayers(
      opID: LayerIdentity
  ): mutable.HashMap[LayerIdentity, OpExecConfig] = {
    val upstreamOperatorToLayers = new mutable.HashMap[LayerIdentity, OpExecConfig]()
    physicalPlan
      .getUpstream(opID)
      .foreach(uOpID => upstreamOperatorToLayers(uOpID) = physicalPlan.operatorMap(opID))
    upstreamOperatorToLayers
  }

  def getInlinksIdsToWorkerLayer(layerIdentity: LayerIdentity): Set[LinkIdentity] = {
    physicalPlan.getLayer(layerIdentity).ordinalMapping.input.keySet
  }

  def getOperator(workerID: ActorVirtualIdentity): OpExecConfig =
    physicalPlan.operatorMap(VirtualIdentityUtils.getOperator(workerID))

  def getOperator(opID: LayerIdentity): OpExecConfig = physicalPlan.operatorMap(opID)

  def getLink(linkID: LinkIdentity): LinkStrategy = physicalPlan.linkStrategies(linkID)

  def getPythonWorkers: Iterable[ActorVirtualIdentity] =
    physicalPlan.operators
      .filter(operator => operator.opExecClass == classOf[PythonUDFOpExecV2])
      .flatMap(_.identifiers)

  def getOperatorToWorkers: Iterable[(LayerIdentity, Seq[ActorVirtualIdentity])] = {
    physicalPlan.allOperatorIds.map(opId => {
      (opId, getAllWorkersForOperators(Array(opId)).toSeq)
    })
  }

  def getAllWorkersForOperators(
      operators: Array[LayerIdentity]
  ): Array[ActorVirtualIdentity] = {
    operators.flatMap(opId => physicalPlan.operatorMap(opId).identifiers)
  }

  def getPythonOperators(fromOperatorsList: Array[LayerIdentity]): Array[LayerIdentity] = {
    fromOperatorsList.filter(opId =>
      physicalPlan.operatorMap(opId).identifiers.nonEmpty &&
        physicalPlan.operatorMap(opId).isPythonOperator
    )
  }

  def getPythonWorkerToOperatorExec(
      pythonOperators: Array[LayerIdentity]
  ): Iterable[(ActorVirtualIdentity, OpExecConfig)] = {
    pythonOperators
      .map(opId => physicalPlan.operatorMap(opId))
      .filter(op => op.isPythonOperator)
      .flatMap(op => op.identifiers.map(worker => (worker, op)))
      .toList
  }

}
