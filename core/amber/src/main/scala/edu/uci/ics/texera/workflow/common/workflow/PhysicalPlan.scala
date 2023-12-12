package edu.uci.ics.texera.workflow.common.workflow

import edu.uci.ics.amber.engine.architecture.deploysemantics.layer.PhysicalOp
import edu.uci.ics.amber.engine.common.VirtualIdentityUtils
import edu.uci.ics.amber.engine.common.virtualidentity.{ActorVirtualIdentity, OperatorIdentity, PhysicalLink, PhysicalOpIdentity}
import org.jgrapht.graph.{DefaultEdge, DirectedAcyclicGraph}
import org.jgrapht.traverse.TopologicalOrderIterator

import scala.collection.JavaConverters._

object PhysicalPlan {

  def apply(operatorList: Array[PhysicalOp], links: Array[PhysicalLink]): PhysicalPlan = {
    new PhysicalPlan(operatorList.toList, links.toList)
  }

  def apply(executionId: Long, logicalPlan: LogicalPlan): PhysicalPlan = {

    var physicalPlan = PhysicalPlan(List(), List())

    logicalPlan.operators.foreach(op => {
      val subPlan =
        op.getPhysicalPlan(
          executionId,
          logicalPlan.getOpSchemaInfo(op.operatorIdentifier)
        )
      physicalPlan = physicalPlan.addSubPlan(subPlan)
    })

    // connect inter-operator links
    logicalPlan.links.foreach(link => {
      val fromLogicalOp = link.origin.operatorId
      val fromPort = link.origin.portOrdinal
      val fromPortName = logicalPlan.operators
        .filter(op => op.operatorIdentifier == link.origin.operatorId)
        .head
        .operatorInfo
        .outputPorts(fromPort)
        .displayName
      val fromLayer = physicalPlan.findLayerForOutputPort(fromLogicalOp, fromPortName)

      val toLogicalOp = logicalPlan.getOperator(link.destination.operatorId).operatorIdentifier
      val toPort = link.destination.portOrdinal
      val toPortName = logicalPlan.operators
        .filter(op => op.operatorIdentifier == link.destination.operatorId)
        .head
        .operatorInfo
        .inputPorts(toPort)
        .displayName
      val toLayer = physicalPlan.findLayerForInputPort(toLogicalOp, toPortName)

      physicalPlan = physicalPlan.addEdge(fromLayer, fromPort, toLayer, toPort)
    })

    physicalPlan
  }

}

case class PhysicalPlan(
    operators: List[PhysicalOp],
    links: List[PhysicalLink]
) {

  @transient lazy val operatorMap: Map[PhysicalOpIdentity, PhysicalOp] =
    operators.map(o => (o.id, o)).toMap

  // the dag will be re-computed again once it reaches the coordinator.
  @transient lazy val dag: DirectedAcyclicGraph[PhysicalOpIdentity, DefaultEdge] = {
    val jgraphtDag = new DirectedAcyclicGraph[PhysicalOpIdentity, DefaultEdge](classOf[DefaultEdge])
    operatorMap.foreach(op => jgraphtDag.addVertex(op._1))
    links.foreach(l => jgraphtDag.addEdge(l.from, l.to))
    jgraphtDag
  }

  @transient lazy val allOperatorIds: Iterable[PhysicalOpIdentity] = operatorMap.keys

  @transient lazy val sourceOperatorIds: List[PhysicalOpIdentity] =
    operatorMap.keys.filter(op => dag.inDegreeOf(op) == 0).toList

  @transient lazy val sinkOperatorIds: List[PhysicalOpIdentity] =
    operatorMap.keys
      .filter(op => dag.outDegreeOf(op) == 0)
      .toList

  def getSourceOperatorIds: List[PhysicalOpIdentity] = this.sourceOperatorIds

  def getSinkOperatorIds: List[PhysicalOpIdentity] = this.sinkOperatorIds

  private def findLayerForInputPort(
      logicalOpId: OperatorIdentity,
      portName: String
  ): PhysicalOpIdentity = {
    val candidateLayers = getPhysicalOpsOfLogicalOp(logicalOpId).filter(op =>
      op.inputPorts.map(_.displayName).contains(portName)
    )
    assert(
      candidateLayers.size == 1,
      s"find no or multiple input port with name = $portName for operator $logicalOpId"
    )
    candidateLayers.head.id
  }

  private def findLayerForOutputPort(
      logicalOpId: OperatorIdentity,
      portName: String
  ): PhysicalOpIdentity = {
    val candidateLayers = getPhysicalOpsOfLogicalOp(logicalOpId).filter(op =>
      op.outputPorts.map(_.displayName).contains(portName)
    )
    assert(
      candidateLayers.size == 1,
      s"find no or multiple output port with name = $portName for operator $logicalOpId"
    )
    candidateLayers.head.id
  }

  def getPhysicalOpsOfLogicalOp(logicalOpId: OperatorIdentity): List[PhysicalOp] = {
    topologicalIterator()
      .filter(physicalOpId => physicalOpId.logicalOpId == logicalOpId)
      .map(physicalOpId => getOperator(physicalOpId))
      .toList
  }

  def getSinglePhysicalOpOfLogicalOp(logicalOpId: OperatorIdentity): PhysicalOp = {
    val physicalOps = getPhysicalOpsOfLogicalOp(logicalOpId)
    if (physicalOps.size != 1) {
      val msg =
        s"logical operator $logicalOpId has ${physicalOps.size} physical operators, expecting a single one"
      throw new RuntimeException(msg)
    }
    physicalOps.head
  }

  // returns a sub-plan that contains the specified operators and the links connected within these operators
  def subPlan(subOperators: Set[PhysicalOpIdentity]): PhysicalPlan = {
    val newOps = operators.filter(op => subOperators.contains(op.id))
    val newLinks =
      links.filter(link => subOperators.contains(link.from) && subOperators.contains(link.to))
    PhysicalPlan(newOps, newLinks)
  }

  def getOperator(physicalOpId: PhysicalOpIdentity): PhysicalOp = operatorMap(physicalOpId)

  def getUpstreamPhysicalOpIds(physicalOpId: PhysicalOpIdentity): List[PhysicalOpIdentity] = {
    dag.incomingEdgesOf(physicalOpId).asScala.map(e => dag.getEdgeSource(e)).toList
  }

  def getUpstreamPhysicalLinks(physicalOpId: PhysicalOpIdentity): List[PhysicalLink] = {
    links.filter(l => l.to == physicalOpId)
  }

  def getDownstreamPhysicalOpIds(physicalOpId: PhysicalOpIdentity): List[PhysicalOpIdentity] = {
    dag.outgoingEdgesOf(physicalOpId).asScala.map(e => dag.getEdgeTarget(e)).toList
  }

  def getDescendantPhysicalOpIds(physicalOpId: PhysicalOpIdentity): List[PhysicalOpIdentity] = {
    dag.getDescendants(physicalOpId).asScala.toList
  }

  def topologicalIterator(): Iterator[PhysicalOpIdentity] = {
    new TopologicalOrderIterator(dag).asScala
  }

  // returns a new physical plan with the operators added
  def addOperator(physicalOp: PhysicalOp): PhysicalPlan = {
    this.copy(operators = physicalOp :: operators)
  }

  def addEdge(physicalLink: PhysicalLink): PhysicalPlan = {
    addEdge(physicalLink.from, physicalLink.fromPort, physicalLink.to, physicalLink.toPort)
  }

  def addEdge(
      from: PhysicalOpIdentity,
      fromPort: Int,
      to: PhysicalOpIdentity,
      toPort: Int
  ): PhysicalPlan = {

    val newOperators = operatorMap +
      (from -> operatorMap(from).addOutput(to, fromPort, toPort)) +
      (to -> operatorMap(to).addInput(from, fromPort, toPort))

    val newLinks = links :+ PhysicalLink(from, fromPort, to, toPort)
    this.copy(operators = newOperators.values.toList, links = newLinks)
  }

  // returns a new physical plan with the edges removed
  def removeEdge(
      physicalLink: PhysicalLink
  ): PhysicalPlan = {
    val from = physicalLink.from
    val to = physicalLink.to
    val newOperators = operatorMap +
      (from -> operatorMap(from).removeOutput(physicalLink)) +
      (to -> operatorMap(to).removeInput(physicalLink))

    val newLinks = links.filter(l => l != physicalLink)
    this.copy(operators = newOperators.values.toList, links = newLinks)
  }

  def setOperator(physicalOp: PhysicalOp): PhysicalPlan = {
    this.copy(operators = (operatorMap + (physicalOp.id -> physicalOp)).values.toList)
  }

  private def addSubPlan(subPlan: PhysicalPlan): PhysicalPlan = {
    var resultPlan = this.copy(operators, links)
    // add all physical operators to physical DAG
    subPlan.operators.foreach(op => resultPlan = resultPlan.addOperator(op))
    // connect intra-operator links
    subPlan.links.foreach((physicalLink: PhysicalLink) =>
      resultPlan = resultPlan.addEdge(physicalLink)
    )
    resultPlan
  }

  def getPhysicalOpByWorkerId(workerId: ActorVirtualIdentity): PhysicalOp =
    getOperator(VirtualIdentityUtils.getPhysicalOpId(workerId))

}
