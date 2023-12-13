package edu.uci.ics.amber.engine.architecture.deploysemantics

import akka.actor.Deploy
import akka.remote.RemoteScope
import edu.uci.ics.amber.engine.architecture.common.{AkkaActorRefMappingService, AkkaActorService}
import edu.uci.ics.amber.engine.architecture.controller.{ControllerConfig, OperatorExecution}
import edu.uci.ics.amber.engine.architecture.deploysemantics.layer.{
  OpExecInitInfo,
  OpExecInitInfoWithCode,
  OpExecInitInfoWithFunc
}
import edu.uci.ics.amber.engine.architecture.deploysemantics.locationpreference.{
  AddressInfo,
  LocationPreference,
  PreferController,
  RoundRobinPreference
}
import edu.uci.ics.amber.engine.architecture.pythonworker.PythonWorkflowWorker
import edu.uci.ics.amber.engine.architecture.worker.WorkflowWorker
import edu.uci.ics.amber.engine.architecture.worker.WorkflowWorker.WorkflowWorkerConfig
import edu.uci.ics.amber.engine.common.virtualidentity.{
  ActorVirtualIdentity,
  OperatorIdentity,
  PhysicalLinkIdentity,
  PhysicalOpIdentity
}
import edu.uci.ics.amber.engine.common.{AmberConfig, VirtualIdentityUtils}
import edu.uci.ics.texera.workflow.common.metadata.{InputPort, OperatorInfo, OutputPort}
import edu.uci.ics.texera.workflow.common.tuple.schema.{OperatorSchemaInfo, Schema}
import edu.uci.ics.texera.workflow.common.workflow.{HashPartition, PartitionInfo, SinglePartition}
import edu.uci.ics.texera.workflow.operators.hashJoin.HashJoinOpExec
import org.jgrapht.graph.{DefaultEdge, DirectedAcyclicGraph}
import org.jgrapht.traverse.TopologicalOrderIterator

import scala.collection.mutable.ArrayBuffer

object PhysicalOp {

  /** all source operators should use sourcePhysicalOp to give the following configs:
    *  1) it initializes at the controller jvm.
    *  2) it only has 1 worker actor.
    *  3) it has no input ports.
    */
  def sourcePhysicalOperator(
      executionId: Long,
      logicalOpId: OperatorIdentity,
      opExecInitInfo: OpExecInitInfo
  ): PhysicalOp =
    sourcePhysicalOp(executionId, PhysicalOpIdentity(logicalOpId, "main"), opExecInitInfo)

  def sourcePhysicalOp(
      executionId: Long,
      physicalOpId: PhysicalOpIdentity,
      opExecInitInfo: OpExecInitInfo
  ): PhysicalOp =
    PhysicalOp(
      executionId,
      physicalOpId,
      opExecInitInfo,
      numWorkers = 1,
      locationPreference = Option(new PreferController()),
      inputPorts = List.empty
    )

  def oneToOnePhysicalOp(
      executionId: Long,
      logicalOpId: OperatorIdentity,
      opExecInitInfo: OpExecInitInfo
  ): PhysicalOp =
    oneToOnePhysicalOp(executionId, PhysicalOpIdentity(logicalOpId, "main"), opExecInitInfo)

  def oneToOnePhysicalOp(
      executionId: Long,
      physicalOpId: PhysicalOpIdentity,
      opExecInitInfo: OpExecInitInfo
  ): PhysicalOp =
    PhysicalOp(executionId, physicalOpId, opExecInitInfo = opExecInitInfo)

  def manyToOnePhysicalOp(
      executionId: Long,
      logicalOpId: OperatorIdentity,
      opExecInitInfo: OpExecInitInfo
  ): PhysicalOp =
    manyToOnePhysicalOp(executionId, PhysicalOpIdentity(logicalOpId, "main"), opExecInitInfo)

  def manyToOnePhysicalOp(
      executionId: Long,
      physicalOpId: PhysicalOpIdentity,
      opExecInitInfo: OpExecInitInfo
  ): PhysicalOp = {
    PhysicalOp(
      executionId,
      physicalOpId,
      opExecInitInfo,
      numWorkers = 1,
      partitionRequirement = List(Option(SinglePartition())),
      derivePartition = _ => SinglePartition()
    )
  }

  def localPhysicalOp(
      executionId: Long,
      logicalOpId: OperatorIdentity,
      opExecInitInfo: OpExecInitInfo
  ): PhysicalOp =
    localPhysicalOp(executionId, PhysicalOpIdentity(logicalOpId, "main"), opExecInitInfo)

  def localPhysicalOp(
      executionId: Long,
      physicalOpId: PhysicalOpIdentity,
      opExecInitInfo: OpExecInitInfo
  ): PhysicalOp = {
    manyToOnePhysicalOp(executionId, physicalOpId, opExecInitInfo)
      .copy(locationPreference = Option(new PreferController()))
  }

  def hashPhysicalOp(
      executionId: Long,
      logicalOpId: OperatorIdentity,
      opExec: OpExecInitInfo,
      hashColumnIndices: Array[Int]
  ): PhysicalOp =
    hashPhysicalOp(executionId, PhysicalOpIdentity(logicalOpId, "main"), opExec, hashColumnIndices)

  def hashPhysicalOp(
      executionId: Long,
      physicalOpId: PhysicalOpIdentity,
      opExecInitInfo: OpExecInitInfo,
      hashColumnIndices: Array[Int]
  ): PhysicalOp = {
    PhysicalOp(
      executionId,
      physicalOpId,
      opExecInitInfo,
      partitionRequirement = List(Option(HashPartition(hashColumnIndices))),
      derivePartition = _ => HashPartition(hashColumnIndices)
    )
  }

}

case class PhysicalOp(
    // the execution id number
    executionId: Long,
    // the identifier of this PhysicalOp
    id: PhysicalOpIdentity,
    // information regarding initializing an operator executor instance
    opExecInitInfo: OpExecInitInfo,
    // preference of parallelism (total number of workers)
    numWorkers: Int = AmberConfig.numWorkerPerOperatorByDefault,
    // input/output schemas
    schemaInfo: Option[OperatorSchemaInfo] = None,
    // preference of worker placement
    locationPreference: Option[LocationPreference] = None,
    // requirement of partition policy (hash/range/single/none) on inputs
    partitionRequirement: List[Option[PartitionInfo]] = List(),
    // derive the output partition info given the input partitions
    // if not specified, by default the output partition is the same as input partition
    derivePartition: List[PartitionInfo] => PartitionInfo = inputParts => inputParts.head,
    // input/output ports of the physical operator
    // for operators with multiple input/output ports: must set these variables properly
    inputPorts: List[InputPort] = List(InputPort()),
    outputPorts: List[OutputPort] = List(OutputPort()),
    // mapping of all input/output operators connected on a specific input/output port index
    inputToOrdinalMapping: Map[PhysicalLinkIdentity, Int] = Map(),
    outputToOrdinalMapping: Map[PhysicalLinkIdentity, Int] = Map(),
    // input ports that are blocking
    blockingInputs: List[Int] = List(),
    // execution dependency of ports
    dependency: Map[Int, Int] = Map(),
    isOneToManyOp: Boolean = false
) {

  // all the "dependee" links are also blocking inputs
  private lazy val realBlockingInputs: List[Int] = (blockingInputs ++ dependency.values).distinct

  private lazy val isInitWithCode: Boolean = opExecInitInfo.isInstanceOf[OpExecInitInfoWithCode]

  /*
   * Helper functions related to compile-time operations
   */

  def isSourceOperator: Boolean = {
    inputPorts.isEmpty
  }

  def isSinkOperator: Boolean = {
    outputPorts.isEmpty
  }

  def isPythonOperator: Boolean = {
    isInitWithCode // currently, only Python operators are initialized with code
  }

  def isHashJoinOperator: Boolean = {
    opExecInitInfo match {
      case OpExecInitInfoWithCode(codeGen) => false
      case OpExecInitInfoWithFunc(opGen)   => opGen((0, this)).isInstanceOf[HashJoinOpExec[_]]
    }
  }

  def getPythonCode: String = {
    if (!isPythonOperator) {
      throw new RuntimeException("operator " + id + " is not a python operator")
    }
    opExecInitInfo.asInstanceOf[OpExecInitInfoWithCode].codeGen((0, this))
  }

  def getOutputSchema: Schema = {
    if (!isPythonOperator) {
      throw new RuntimeException("operator " + id + " is not a python operator")
    }
    schemaInfo.get.outputSchemas.head
  }

  // creates a copy with the specified port information
  def withPorts(operatorInfo: OperatorInfo): PhysicalOp = {
    this.copy(inputPorts = operatorInfo.inputPorts, outputPorts = operatorInfo.outputPorts)
  }

  def withLocationPreference(preference: Option[LocationPreference]): PhysicalOp = {
    this.copy(locationPreference = preference)
  }

  def withInputPorts(inputs: List[InputPort]): PhysicalOp = {
    this.copy(inputPorts = inputs)
  }
  def withOutputPorts(outputs: List[OutputPort]): PhysicalOp = {
    this.copy(outputPorts = outputs)
  }

  // creates a copy with an additional input operator specified on an input port
  def addInput(from: PhysicalOpIdentity, fromPort: Int, toPort: Int): PhysicalOp = {
    this.copy(inputToOrdinalMapping =
      inputToOrdinalMapping + (PhysicalLinkIdentity(from, fromPort, this.id, toPort) -> toPort)
    )
  }

  // creates a copy with an additional output operator specified on an output port
  def addOutput(to: PhysicalOpIdentity, fromPort: Int, toPort: Int): PhysicalOp = {
    this.copy(outputToOrdinalMapping =
      outputToOrdinalMapping + (PhysicalLinkIdentity(this.id, fromPort, to, toPort) -> fromPort)
    )
  }

  // creates a copy with a removed input operator
  def removeInput(link: PhysicalLinkIdentity): PhysicalOp = {
    this.copy(inputToOrdinalMapping = inputToOrdinalMapping - link)
  }

  // creates a copy with a removed output operator
  def removeOutput(link: PhysicalLinkIdentity): PhysicalOp = {
    this.copy(outputToOrdinalMapping = outputToOrdinalMapping - link)
  }

  // creates a copy with the new ID
  def withId(id: PhysicalOpIdentity): PhysicalOp = this.copy(id = id)

  // creates a copy with the number of workers specified
  def withNumWorkers(numWorkers: Int): PhysicalOp = this.copy(numWorkers = numWorkers)

  // creates a copy with the specified property that whether this operator is one-to-many
  def withIsOneToManyOp(isOneToManyOp: Boolean): PhysicalOp =
    this.copy(isOneToManyOp = isOneToManyOp)

  // creates a copy with the schema information
  def withOperatorSchemaInfo(schemaInfo: OperatorSchemaInfo): PhysicalOp =
    this.copy(schemaInfo = Some(schemaInfo))

  // returns all input links on a specific input port
  def getInputLinks(portIndex: Int): List[PhysicalLinkIdentity] = {
    inputToOrdinalMapping.filter(p => p._2 == portIndex).keys.toList
  }

  // returns all the input operators on a specific input port
  def getInputOperators(portIndex: Int): List[PhysicalOpIdentity] = {
    getInputLinks(portIndex).map(link => link.from)
  }

  def identifiers: Array[ActorVirtualIdentity] = {
    (0 until numWorkers).map { i => identifier(i) }.toArray
  }

  def identifier(i: Int): ActorVirtualIdentity = {
    VirtualIdentityUtils.createWorkerIdentity(executionId, id.logicalOpId.id, id.layerName, i)
  }

  /**
    * Tells whether the input on this link is blocking i.e. the operator doesn't output anything till this link
    * outputs all its tuples
    */
  def isInputBlocking(input: PhysicalLinkIdentity): Boolean = {
    inputToOrdinalMapping.get(input).exists(port => realBlockingInputs.contains(port))
  }

  /**
    * Some operators process their inputs in a particular order. Eg: 2 phase hash join first
    * processes the build input, then the probe input.
    */
  def getInputProcessingOrder(): Array[PhysicalLinkIdentity] = {
    val dependencyDag =
      new DirectedAcyclicGraph[PhysicalLinkIdentity, DefaultEdge](classOf[DefaultEdge])
    dependency.foreach(dep => {
      val prevInOrder = inputToOrdinalMapping.find(pair => pair._2 == dep._2).get._1
      val nextInOrder = inputToOrdinalMapping.find(pair => pair._2 == dep._1).get._1
      if (!dependencyDag.containsVertex(prevInOrder)) {
        dependencyDag.addVertex(prevInOrder)
      }
      if (!dependencyDag.containsVertex(nextInOrder)) {
        dependencyDag.addVertex(nextInOrder)
      }
      dependencyDag.addEdge(prevInOrder, nextInOrder)
    })
    val topologicalIterator =
      new TopologicalOrderIterator[PhysicalLinkIdentity, DefaultEdge](dependencyDag)
    val processingOrder = new ArrayBuffer[PhysicalLinkIdentity]()
    while (topologicalIterator.hasNext) {
      processingOrder.append(topologicalIterator.next())
    }
    processingOrder.toArray
  }

  def build(
      controllerActorService: AkkaActorService,
      actorRefService: AkkaActorRefMappingService,
      opExecution: OperatorExecution,
      controllerConf: ControllerConfig
  ): Unit = {
    val addressInfo = AddressInfo(
      controllerActorService.getClusterNodeAddresses,
      controllerActorService.self.path.address
    )
    (0 until numWorkers)
      .foreach(i => {
        val workerId: ActorVirtualIdentity =
          VirtualIdentityUtils.createWorkerIdentity(opExecution.executionId, id, i)
        val locationPreference = this.locationPreference.getOrElse(new RoundRobinPreference())
        val preferredAddress = locationPreference.getPreferredLocation(addressInfo, this, i)

        val workflowWorker = if (this.isPythonOperator) {
          PythonWorkflowWorker.props(workerId)
        } else {
          WorkflowWorker.props(
            workerId,
            i,
            workerLayer = this,
            WorkflowWorkerConfig(
              logStorageType = AmberConfig.faultToleranceLogStorage,
              replayTo = None
            )
          )
        }
        val ref =
          controllerActorService.actorOf(
            workflowWorker.withDeploy(Deploy(scope = RemoteScope(preferredAddress)))
          )
        actorRefService.registerActorRef(workerId, ref)
        opExecution.getWorkerInfo(workerId).ref = ref
      })
  }
}
