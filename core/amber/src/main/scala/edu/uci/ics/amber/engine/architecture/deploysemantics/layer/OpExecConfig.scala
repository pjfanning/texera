package edu.uci.ics.amber.engine.architecture.deploysemantics.layer

import akka.actor.{ActorContext, ActorRef, Address, Deploy, Props}
import akka.remote.RemoteScope
import akka.pattern.ask
import akka.util.Timeout
import com.twitter.util.{Await, Future}
import edu.uci.ics.amber.engine.architecture.common.WorkflowActor.CheckInitialized
import edu.uci.ics.amber.engine.architecture.common.{VirtualIdentityUtils, WorkflowActorService}
import edu.uci.ics.amber.engine.architecture.controller.WorkflowReplayConfig
import edu.uci.ics.amber.engine.architecture.deploysemantics.locationpreference.{AddressInfo, LocationPreference, PreferController, RoundRobinPreference}
import edu.uci.ics.amber.engine.architecture.execution.OperatorExecution
import edu.uci.ics.amber.engine.architecture.messaginglayer.NetworkOutputPort
import edu.uci.ics.amber.engine.architecture.pythonworker.PythonWorkflowWorker
import edu.uci.ics.amber.engine.architecture.recovery.InternalPayloadManager.{SetupLogging, SetupReplay, StartReplay}
import edu.uci.ics.amber.engine.architecture.worker.{ReplayConfig, WorkflowWorker}
import edu.uci.ics.amber.engine.common.ambermessage.{AmberInternalPayload, ChannelEndpointID}
import edu.uci.ics.amber.engine.common.virtualidentity.util.makeLayer
import edu.uci.ics.amber.engine.common.virtualidentity.{ActorVirtualIdentity, LayerIdentity, LinkIdentity, OperatorIdentity}
import edu.uci.ics.amber.engine.common.{Constants, IOperatorExecutor, ISinkOperatorExecutor, ISourceOperatorExecutor}
import edu.uci.ics.texera.workflow.common.metadata.{InputPort, OperatorInfo, OutputPort}
import edu.uci.ics.texera.workflow.common.workflow.{HashPartition, PartitionInfo, SinglePartition}
import edu.uci.ics.texera.workflow.operators.udf.pythonV2.PythonUDFOpExecV2
import org.jgrapht.graph.{DefaultEdge, DirectedAcyclicGraph}
import org.jgrapht.traverse.TopologicalOrderIterator

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.duration.DurationInt

trait OpExecFunc extends (((Int, OpExecConfig)) => IOperatorExecutor) with java.io.Serializable

object OpExecConfig {

  def oneToOneLayer(opId: OperatorIdentity, opExec: OpExecFunc): OpExecConfig =
    oneToOneLayer(layerId = makeLayer(opId, "main"), opExec)

  def oneToOneLayer(layerId: LayerIdentity, opExec: OpExecFunc): OpExecConfig =
    OpExecConfig(layerId, initIOperatorExecutor = opExec)

  def manyToOneLayer(opId: OperatorIdentity, opExec: OpExecFunc): OpExecConfig =
    manyToOneLayer(makeLayer(opId, "main"), opExec)

  def manyToOneLayer(layerId: LayerIdentity, opExec: OpExecFunc): OpExecConfig = {
    OpExecConfig(
      layerId,
      initIOperatorExecutor = opExec,
      numWorkers = 1,
      partitionRequirement = List(Option(SinglePartition())),
      derivePartition = _ => SinglePartition()
    )
  }

  def localLayer(opId: OperatorIdentity, opExec: OpExecFunc): OpExecConfig =
    localLayer(makeLayer(opId, "main"), opExec)

  def localLayer(layerId: LayerIdentity, opExec: OpExecFunc): OpExecConfig = {
    manyToOneLayer(layerId, opExec).copy(locationPreference = Option(new PreferController()))
  }

  def hashLayer(
      opId: OperatorIdentity,
      opExec: OpExecFunc,
      hashColumnIndices: Array[Int]
  ): OpExecConfig = hashLayer(makeLayer(opId, "main"), opExec, hashColumnIndices)

  def hashLayer(
      layerId: LayerIdentity,
      opExec: OpExecFunc,
      hashColumnIndices: Array[Int]
  ): OpExecConfig = {
    OpExecConfig(
      id = layerId,
      initIOperatorExecutor = opExec,
      partitionRequirement = List(Option(HashPartition(hashColumnIndices))),
      derivePartition = _ => HashPartition(hashColumnIndices)
    )
  }

}

case class OpExecConfig(
    id: LayerIdentity,
    // function to create an operator executor instance
    // parameters: 1: worker index, 2: this worker layer object
    initIOperatorExecutor: OpExecFunc,
    // preference of parallelism (number of workers)
    numWorkers: Int = Constants.numWorkerPerNode,
    // preference of worker placement
    locationPreference: Option[LocationPreference] = None,
    // requirement of partition policy (hash/range/single/none) on inputs
    partitionRequirement: List[Option[PartitionInfo]] = List(),
    // derive the output partition info given the input partitions
    // if not specified, by default the output partition is the same as input partition
    derivePartition: List[PartitionInfo] => PartitionInfo = inputParts => inputParts.head,
    // input/output ports of the physical operator
    // for operators with multiple input/output ports: must set these variables properly
    inputPorts: List[InputPort] = List(InputPort("")),
    outputPorts: List[OutputPort] = List(OutputPort("")),
    // mapping of all input/output operators connected on a specific input/output port index
    ordinalMapping: OrdinalMapping = OrdinalMapping(),
    // input ports that are blocking
    blockingInputs: List[Int] = List(),
    // execution dependency of ports
    dependency: Map[Int, Int] = Map(),
    isOneToManyOp: Boolean = false
) {

  implicit val timeout:Timeout = 5.seconds

  // return the runtime class of the corresponding OperatorExecutor
  lazy private val tempOperatorInstance: IOperatorExecutor = initIOperatorExecutor((0, this))
  lazy val opExecClass: Class[_ <: IOperatorExecutor] =
    tempOperatorInstance.getClass

  /*
   * Helper functions related to compile-time operations
   */

  def isSourceOperator: Boolean =
    classOf[ISourceOperatorExecutor].isAssignableFrom(opExecClass)

  def isPythonOperator: Boolean =
    classOf[PythonUDFOpExecV2].isAssignableFrom(opExecClass)

  def getPythonCode: String = {
    if (!isPythonOperator) {
      throw new RuntimeException("operator " + id + " is not a python operator")
    }
    tempOperatorInstance.asInstanceOf[PythonUDFOpExecV2].getCode
  }

  // creates a copy with the specified port information
  def withPorts(operatorInfo: OperatorInfo): OpExecConfig = {
    this.copy(inputPorts = operatorInfo.inputPorts, outputPorts = operatorInfo.outputPorts)
  }

  // creates a copy with an additional input operator specified on an input port
  def addInput(from: LayerIdentity, port: Int): OpExecConfig = {
    assert(port < this.inputPorts.size, s"cannot add input on port $port, all ports: $inputPorts")
    this.copy(ordinalMapping =
      OrdinalMapping(
        ordinalMapping.input + (LinkIdentity(from, this.id) -> port),
        ordinalMapping.output
      )
    )
  }

  // creates a copy with an additional output operator specified on an output port
  def addOutput(to: LayerIdentity, port: Int): OpExecConfig = {
    assert(
      port < this.outputPorts.size,
      s"cannot add output on port $port, all ports: $outputPorts"
    )
    this.copy(ordinalMapping =
      OrdinalMapping(
        ordinalMapping.input,
        ordinalMapping.output + (LinkIdentity(this.id, to) -> port)
      )
    )
  }

  // creates a copy with a removed input operator
  def removeInput(from: LayerIdentity): OpExecConfig = {
    this.copy(ordinalMapping =
      OrdinalMapping(ordinalMapping.input - LinkIdentity(from, this.id), ordinalMapping.output)
    )
  }

  // creates a copy with a removed output operator
  def removeOutput(to: LayerIdentity): OpExecConfig = {
    this.copy(ordinalMapping =
      OrdinalMapping(ordinalMapping.input, ordinalMapping.output - LinkIdentity(this.id, to))
    )
  }

  // creates a copy with the new ID
  def withId(id: LayerIdentity): OpExecConfig = this.copy(id = id)

  // creates a copy with the number of workers specified
  def withNumWorkers(numWorkers: Int): OpExecConfig = this.copy(numWorkers = numWorkers)

  // creates a copy with the specified property that whether this operator is one-to-many
  def withIsOneToManyOp(isOneToManyOp: Boolean): OpExecConfig =
    this.copy(isOneToManyOp = isOneToManyOp)

  // returns all input links on a specific input port
  def getInputLinks(portIndex: Int): List[LinkIdentity] = {
    ordinalMapping.input.filter(p => p._2 == portIndex).keys.toList
  }

  // returns all the input operators on a specific input port
  def getInputOperators(portIndex: Int): List[LayerIdentity] = {
    getInputLinks(portIndex).map(link => link.from)
  }

  def identifiers: Array[ActorVirtualIdentity] = {
    (0 until numWorkers).map { i => identifier(i) }.toArray
  }

  def identifier(i: Int): ActorVirtualIdentity = {
    VirtualIdentityUtils.createWorkerIdentity(id.workflow, id.operator, id.layerID, i)
  }

  /**
    * Tells whether the input on this link is blocking i.e. the operator doesn't output anything till this link
    * outputs all its tuples
    */
  def isInputBlocking(input: LinkIdentity): Boolean = {
    ordinalMapping.input.get(input).exists(port => blockingInputs.contains(port))
  }

  /**
    * Some operators process their inputs in a particular order. Eg: 2 phase hash join first
    * processes the build input, then the probe input.
    */
  def getInputProcessingOrder(): Array[LinkIdentity] = {
    val dependencyDag = new DirectedAcyclicGraph[LinkIdentity, DefaultEdge](classOf[DefaultEdge])
    dependency.foreach(dep => {
      val prevInOrder = ordinalMapping.input.find(pair => pair._2 == dep._2).get._1
      val nextInOrder = ordinalMapping.input.find(pair => pair._2 == dep._1).get._1
      if (!dependencyDag.containsVertex(prevInOrder)) {
        dependencyDag.addVertex(prevInOrder)
      }
      if (!dependencyDag.containsVertex(nextInOrder)) {
        dependencyDag.addVertex(nextInOrder)
      }
      dependencyDag.addEdge(prevInOrder, nextInOrder)
    })
    val topologicalIterator =
      new TopologicalOrderIterator[LinkIdentity, DefaultEdge](dependencyDag)
    val processingOrder = new ArrayBuffer[LinkIdentity]()
    while (topologicalIterator.hasNext) {
      processingOrder.append(topologicalIterator.next())
    }
    processingOrder.toArray
  }

  /*
   * Functions related to runtime operations
   */

  def build(
      addressInfo: AddressInfo,
      actorService: WorkflowActorService,
      opExecution: OperatorExecution,
      replayConf: WorkflowReplayConfig
  ): Unit = {
    Await.result(Future.collect((0 until numWorkers)
      .map(i => {
        val workerId: ActorVirtualIdentity = identifier(i)
          buildWorker(
            workerId,
            addressInfo,
            actorService,
            opExecution,
            (if(replayConf.confs.contains(workerId)){
              val replayConfig = replayConf.confs(workerId)
              Array(SetupReplay("replay - respawn", replayConfig.fromCheckpoint, replayConfig.replayTo, replayConfig.checkpointConfig), StartReplay("replay - restart"))
            }else{
              Array(SetupLogging())
            })
          )
      })))
  }

  def buildWorker(
      workerId: ActorVirtualIdentity,
      addressInfo: AddressInfo,
      actorService: WorkflowActorService,
      opExecution: OperatorExecution,
      extraCommands: Array[AmberInternalPayload]
  ): Future[ActorRef] = {
    val i = VirtualIdentityUtils.getWorkerIndex(workerId)
    val locationPreference = this.locationPreference.getOrElse(new RoundRobinPreference())
    val preferredAddress = locationPreference.getPreferredLocation(addressInfo, this, i)
    val workflowWorker = if (this.isPythonOperator) {
      PythonWorkflowWorker.props(workerId, i, this, actorService.networkCommunicationActor)
    } else {
      WorkflowWorker.props(
        workerId,
        i,
        this,
        actorService.networkCommunicationActor
      )
    }
    val ref =
      actorService.actorOf(workflowWorker.withDeploy(Deploy(scope = RemoteScope(preferredAddress))))
    actorService.registerActorForNetworkCommunication(workerId, ref)
    actorService.ask(ref, CheckInitialized(extraCommands)).map{
      _ =>
        println(s"Worker Built! Actor for $workerId is at $ref")
        opExecution.getWorkerInfo(workerId).ref = ref
        ref
    }
  }
}
