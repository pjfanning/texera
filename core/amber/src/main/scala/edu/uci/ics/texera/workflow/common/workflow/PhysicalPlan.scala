package edu.uci.ics.texera.workflow.common.workflow

import edu.uci.ics.amber.engine.architecture.deploysemantics.{PhysicalLink, PhysicalOp}
import edu.uci.ics.amber.engine.architecture.sendsemantics.partitionings.OneToOnePartitioning
import edu.uci.ics.amber.engine.common.AmberConfig.defaultBatchSize
import edu.uci.ics.amber.engine.common.VirtualIdentityUtils
import edu.uci.ics.amber.engine.common.virtualidentity.{
  ActorVirtualIdentity,
  OperatorIdentity,
  PhysicalLinkIdentity,
  PhysicalOpIdentity
}
import org.jgrapht.graph.{DefaultEdge, DirectedAcyclicGraph}
import org.jgrapht.traverse.TopologicalOrderIterator

import scala.collection.JavaConverters._
import scala.collection.mutable

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
      val fromOp = physicalPlan.getPhysicalOpForOutputPort(fromLogicalOp, fromPortName)

      val toLogicalOp = logicalPlan.getOperator(link.destination.operatorId).operatorIdentifier
      val toPort = link.destination.portOrdinal
      val toPortName = logicalPlan.operators
        .filter(op => op.operatorIdentifier == link.destination.operatorId)
        .head
        .operatorInfo
        .inputPorts(toPort)
        .displayName
      val toOp = physicalPlan.getPhysicalOpForInputPort(toLogicalOp, toPortName)

      physicalPlan = physicalPlan.addEdge(fromOp, fromPort, toOp, toPort)
    })

    physicalPlan.populatePartitioningOnLinks()
  }

}

case class PhysicalPlan(
    operators: List[PhysicalOp],
    links: List[PhysicalLink]
) {

  def getLink(linkId: PhysicalLinkIdentity): PhysicalLink = {
    linkMap(linkId)
  }
  @transient private lazy val operatorMap: Map[PhysicalOpIdentity, PhysicalOp] =
    operators.map(o => (o.id, o)).toMap

  private lazy val linkMap: Map[PhysicalLinkIdentity, PhysicalLink] =
    links.map(link => link.id -> link).toMap

  // the dag will be re-computed again once it reaches the coordinator.
  @transient lazy val dag: DirectedAcyclicGraph[PhysicalOpIdentity, DefaultEdge] = {
    val jgraphtDag = new DirectedAcyclicGraph[PhysicalOpIdentity, DefaultEdge](classOf[DefaultEdge])
    operatorMap.foreach(op => jgraphtDag.addVertex(op._1))
    links.foreach(l => jgraphtDag.addEdge(l.fromOp.id, l.toOp.id))
    jgraphtDag
  }

  @transient private lazy val sourceOperatorIds: List[PhysicalOpIdentity] =
    operatorMap.keys.filter(op => dag.inDegreeOf(op) == 0).toList

  @transient private lazy val sinkOperatorIds: List[PhysicalOpIdentity] =
    operatorMap.keys
      .filter(op => dag.outDegreeOf(op) == 0)
      .toList

  def getSourceOperatorIds: List[PhysicalOpIdentity] = this.sourceOperatorIds

  def getSinkOperatorIds: List[PhysicalOpIdentity] = this.sinkOperatorIds

  private def getPhysicalOpForInputPort(
      logicalOpId: OperatorIdentity,
      portName: String
  ): PhysicalOp = {
    val candidateLayers = getPhysicalOpsOfLogicalOp(logicalOpId).filter(op =>
      op.inputPorts.map(_.displayName).contains(portName)
    )
    assert(
      candidateLayers.size == 1,
      s"find no or multiple input port with name = $portName for operator $logicalOpId"
    )
    candidateLayers.head
  }

  private def getPhysicalOpForOutputPort(
      logicalOpId: OperatorIdentity,
      portName: String
  ): PhysicalOp = {
    val candidateLayers = getPhysicalOpsOfLogicalOp(logicalOpId).filter(op =>
      op.outputPorts.map(_.displayName).contains(portName)
    )
    assert(
      candidateLayers.size == 1,
      s"find no or multiple output port with name = $portName for operator $logicalOpId"
    )
    candidateLayers.head
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
      links.filter(link =>
        subOperators.contains(link.fromOp.id) && subOperators.contains(link.toOp.id)
      )
    PhysicalPlan(newOps, newLinks)
  }

  def getOperator(physicalOpId: PhysicalOpIdentity): PhysicalOp = operatorMap(physicalOpId)

  def getUpstreamPhysicalOpIds(physicalOpId: PhysicalOpIdentity): List[PhysicalOpIdentity] = {
    dag.incomingEdgesOf(physicalOpId).asScala.map(e => dag.getEdgeSource(e)).toList
  }

  def getUpstreamPhysicalLinks(physicalOpId: PhysicalOpIdentity): List[PhysicalLink] = {
    links.filter(l => l.toOp.id == physicalOpId)
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
    addEdge(physicalLink.fromOp, physicalLink.fromPort, physicalLink.toOp, physicalLink.toPort)
  }

  def addEdge(
      fromOp: PhysicalOp,
      fromPort: Int,
      toOp: PhysicalOp,
      toPort: Int
  ): PhysicalPlan = {

    val newOperators = operatorMap +
      (fromOp.id -> operatorMap(fromOp.id).addOutput(toOp, fromPort, toPort)) +
      (toOp.id -> operatorMap(toOp.id).addInput(fromOp, fromPort, toPort))

    val newLinks = links :+ PhysicalLink(fromOp, fromPort, toOp, toPort)
    this.copy(operators = newOperators.values.toList, links = newLinks)
  }

  // returns a new physical plan with the edges removed
  def removeEdge(
      link: PhysicalLink
  ): PhysicalPlan = {
    val from = link.fromOp.id
    val to = link.toOp.id
    val newOperators = operatorMap +
      (from -> operatorMap(from).removeOutput(link)) +
      (to -> operatorMap(to).removeInput(link))

    val newLinks = links.filter(l => l.id != link.id)
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

  def getLinksBetween(
      from: PhysicalOpIdentity,
      to: PhysicalOpIdentity
  ): List[PhysicalLink] = {
    links.filter(link => link.fromOp.id == from && link.toOp.id == to)

  }

  def populatePartitioningOnLinks(): PhysicalPlan = {
    val createdLinks = new mutable.ArrayBuffer[PhysicalLink]()
    // a map of an operator to its output partition info
    val outputPartitionInfos = new mutable.HashMap[PhysicalOpIdentity, PartitionInfo]()

    topologicalIterator()
      .foreach(physicalOpId => {
        val physicalOp = getOperator(physicalOpId)
        val outputPartitionInfo = if (getSourceOperatorIds.contains(physicalOpId)) {
          // get output partition info of the source operator
          physicalOp.partitionRequirement.headOption.flatten.getOrElse(UnknownPartition())
        } else {
          val inputPartitionings =
            enforcePartitionRequirement(physicalOp, outputPartitionInfos.toMap, createdLinks)
          assert(inputPartitionings.length == physicalOp.inputPorts.size)
          // derive the output partition info of this operator
          physicalOp.derivePartition(inputPartitionings.toList)
        }
        outputPartitionInfos.put(physicalOpId, outputPartitionInfo)
      })

    // returns the complete physical plan with link strategies
    this.copy(operators, createdLinks.toList)
  }

  private def enforcePartitionRequirement(
      physicalOp: PhysicalOp,
      partitionInfos: Map[PhysicalOpIdentity, PartitionInfo],
      links: mutable.ArrayBuffer[PhysicalLink]
  ): Array[PartitionInfo] = {
    // for each input port, enforce partition requirement
    physicalOp.inputPorts.indices
      .map(port => {
        // all input PhysicalOpIds connected to this port
        val inputPhysicalOps = physicalOp.getOpsOnInputPort(port)

        val fromPort = getUpstreamPhysicalLinks(physicalOp.id).head.fromPort

        // the output partition info of each link connected from each input PhysicalOp
        // for each input PhysicalOp connected on this port
        // check partition requirement to enforce corresponding LinkStrategy
        val outputPartitions = inputPhysicalOps.map(inputPhysicalOp => {
          val inputPartitionInfo = partitionInfos(inputPhysicalOp.id)
          val (physicalLink, outputPart) =
            getOutputPartitionInfo(
              inputPhysicalOp.id,
              fromPort,
              physicalOp.id,
              port,
              inputPartitionInfo
            )
          links.append(physicalLink)
          outputPart
        })

        assert(outputPartitions.size == inputPhysicalOps.size)

        outputPartitions.reduce((a, b) => a.merge(b))
      })
      .toArray
  }

  private def getOutputPartitionInfo(
      fromPhysicalOpId: PhysicalOpIdentity,
      fromPort: Int,
      toPhysicalOpId: PhysicalOpIdentity,
      inputPort: Int,
      upstreamPartitionInfo: PartitionInfo
  ): (PhysicalLink, PartitionInfo) = {
    val toPhysicalOp = getOperator(toPhysicalOpId)
    val fromPhysicalOp = getOperator(fromPhysicalOpId)

    // make sure this input is connected to this port
    assert(toPhysicalOp.getOpsOnInputPort(inputPort).map(_.id).contains(fromPhysicalOpId))

    // partition requirement of this PhysicalOp on this input port
    val requiredPartitionInfo =
      toPhysicalOp.partitionRequirement.lift(inputPort).flatten.getOrElse(UnknownPartition())

    // the upstream partition info satisfies the requirement, and number of worker match
    if (
      upstreamPartitionInfo.satisfies(
        requiredPartitionInfo
      ) && fromPhysicalOp.numWorkers == toPhysicalOp.numWorkers
    ) {
      val physicalLink = new PhysicalLink(
        fromPhysicalOp,
        fromPort,
        toPhysicalOp,
        inputPort,
        partitionings = fromPhysicalOp.identifiers.indices
          .map(i =>
            (
              OneToOnePartitioning(defaultBatchSize, Array(toPhysicalOp.identifiers(i))),
              Array(toPhysicalOp.identifiers(i))
            )
          )
          .toArray
      )
      val outputPart = upstreamPartitionInfo
      (physicalLink, outputPart)
    } else {
      // we must re-distribute the input partitions
      val physicalLink =
        PhysicalLink(fromPhysicalOp, fromPort, toPhysicalOp, inputPort, requiredPartitionInfo)
      val outputPart = requiredPartitionInfo
      (physicalLink, outputPart)
    }
  }

}
