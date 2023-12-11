package edu.uci.ics.texera.workflow.common.workflow

import com.google.common.base.Verify
import com.typesafe.scalalogging.LazyLogging
import edu.uci.ics.amber.engine.common.virtualidentity.OperatorIdentity
import edu.uci.ics.texera.web.model.websocket.request.LogicalPlanPojo
import edu.uci.ics.texera.workflow.common.WorkflowContext
import edu.uci.ics.texera.workflow.common.operators.LogicalOp
import edu.uci.ics.texera.workflow.common.operators.source.SourceOperatorDescriptor
import edu.uci.ics.texera.workflow.common.tuple.schema.{OperatorSchemaInfo, Schema}
import edu.uci.ics.texera.workflow.operators.sink.SinkOpDesc
import org.jgrapht.graph.DirectedAcyclicGraph

import scala.collection.convert.ImplicitConversions.`collection AsScalaIterable`
import scala.collection.mutable.ArrayBuffer
import scala.collection.{JavaConverters, mutable}

case class BreakpointInfo(operatorID: String, breakpoint: Breakpoint)

object LogicalPlan {

  private def toJgraphtDAG(
      operatorList: List[LogicalOp],
      links: List[LogicalLink]
  ): DirectedAcyclicGraph[OperatorIdentity, LogicalLink] = {
    val workflowDag =
      new DirectedAcyclicGraph[OperatorIdentity, LogicalLink](classOf[LogicalLink])
    operatorList.foreach(op => workflowDag.addVertex(op.operatorIdentifier))
    links.foreach(l =>
      workflowDag.addEdge(
        l.origin.operatorId,
        l.destination.operatorId,
        l
      )
    )
    workflowDag
  }

  def apply(
      pojo: LogicalPlanPojo,
      ctx: WorkflowContext
  ): LogicalPlan = {
    LogicalPlan(ctx, pojo.operators, pojo.links, pojo.breakpoints)
  }

}

case class LogicalPlan(
    context: WorkflowContext,
    operators: List[LogicalOp],
    links: List[LogicalLink],
    breakpoints: List[BreakpointInfo],
    inputSchemaMap: Map[OperatorIdentity, List[Option[Schema]]] = Map.empty
) extends LazyLogging {

  private lazy val operatorMap: Map[OperatorIdentity, LogicalOp] =
    operators.map(op => (op.operatorIdentifier, op)).toMap

  lazy val jgraphtDag: DirectedAcyclicGraph[OperatorIdentity, LogicalLink] =
    LogicalPlan.toJgraphtDAG(operators, links)

  lazy val outputSchemaMap: Map[String, List[Schema]] =
    operatorMap.values
      .map(o => {
        val inputSchemas: Array[Schema] =
          if (!operatorMap(o.operatorIdentifier).isInstanceOf[SourceOperatorDescriptor])
            inputSchemaMap(o.operatorIdentifier).map(s => s.get).toArray
          else Array()
        val outputSchemas = o.getOutputSchemas(inputSchemas).toList
        (o.operatorIdentifier.id, outputSchemas)
      })
      .toMap

  def getOperator(opId: String): LogicalOp = operatorMap(OperatorIdentity(opId))
  def getOperator(opId: OperatorIdentity): LogicalOp = operatorMap(opId)

  def getSourceOperatorIds: List[OperatorIdentity] =
    operatorMap.keys.filter(op => jgraphtDag.inDegreeOf(op) == 0).toList

  def getTerminalOperatorIds: List[OperatorIdentity] =
    operatorMap.keys
      .filter(op => jgraphtDag.outDegreeOf(op) == 0)
      .toList

  def getAncestorOpIds(opId: OperatorIdentity): Set[OperatorIdentity] = {
    JavaConverters.asScalaSet(jgraphtDag.getAncestors(opId)).toSet
  }

  def getUpstreamOps(opId: OperatorIdentity): List[LogicalOp] = {
    jgraphtDag
      .incomingEdgesOf(opId)
      .map(e => operatorMap(e.origin.operatorId))
      .toList
  }

  // returns a new logical plan with the given operator added
  def addOperator(operatorDescriptor: LogicalOp): LogicalPlan = {
    // TODO: fix schema for the new operator
    this.copy(context, operators :+ operatorDescriptor, links, breakpoints)
  }

  def removeOperator(opId: OperatorIdentity): LogicalPlan = {
    this.copy(
      context,
      operators.filter(o => o.operatorIdentifier != opId),
      links.filter(l => l.origin.operatorId != opId && l.destination.operatorId != opId),
      breakpoints.filter(b => OperatorIdentity(b.operatorID) != opId),
      inputSchemaMap.filter({
        case (operatorId, _) => operatorId != opId
      })
    )
  }

  // returns a new logical plan with the given edge added
  def addEdge(
      from: OperatorIdentity,
      to: OperatorIdentity,
      fromPort: Int = 0,
      toPort: Int = 0
  ): LogicalPlan = {
    val newLink = LogicalLink(OperatorPort(from, fromPort), OperatorPort(to, toPort))
    val newLinks = links :+ newLink
    this.copy(context, operators, newLinks, breakpoints)
  }

  // returns a new logical plan with the given edge removed
  def removeEdge(
      from: OperatorIdentity,
      to: OperatorIdentity,
      fromPort: Int = 0,
      toPort: Int = 0
  ): LogicalPlan = {
    val linkToRemove = LogicalLink(OperatorPort(from, fromPort), OperatorPort(to, toPort))
    val newLinks = links.filter(l => l != linkToRemove)
    this.copy(context, operators, newLinks, breakpoints)
  }

  def getDownstream(opId: OperatorIdentity): List[LogicalOp] = {
    val downstream = new mutable.MutableList[LogicalOp]
    jgraphtDag
      .outgoingEdgesOf(opId)
      .forEach(e => downstream += operatorMap(e.destination.operatorId))
    downstream.toList
  }

  def getDownstreamEdges(opId: OperatorIdentity): List[LogicalLink] = {
    links.filter(l => l.origin.operatorId == opId)
  }

  def opSchemaInfo(opId: OperatorIdentity): OperatorSchemaInfo = {
    val op = operatorMap(opId)
    val inputSchemas: Array[Schema] =
      if (!op.isInstanceOf[SourceOperatorDescriptor])
        inputSchemaMap(op.operatorIdentifier).map(s => s.get).toArray
      else Array()
    val outputSchemas = outputSchemaMap(op.operatorIdentifier.id).toArray
    OperatorSchemaInfo(inputSchemas, outputSchemas)
  }

  def propagateWorkflowSchema(
      errorList: Option[ArrayBuffer[(OperatorIdentity, Throwable)]]
  ): LogicalPlan = {

    operators.foreach(operator => {
      if (operator.context == null) {
        operator.setContext(context)
      }
    })

    // a map from an operator to the list of its input schema
    val inputSchemaMap =
      new mutable.HashMap[OperatorIdentity, mutable.MutableList[Option[Schema]]]()
        .withDefault(opId =>
          mutable.MutableList
            .fill(operatorMap(opId).operatorInfo.inputPorts.size)(Option.empty)
        )
    // propagate output schema following topological order
    val topologicalOrderIterator = jgraphtDag.iterator()
    topologicalOrderIterator.forEachRemaining(opId => {
      val op = getOperator(opId)
      // infer output schema of this operator based on its input schema
      val outputSchemas: Option[Array[Schema]] = {
        // call to "getOutputSchema" might cause exceptions, wrap in try/catch and return empty schema
        try {
          if (op.isInstanceOf[SourceOperatorDescriptor]) {
            // op is a source operator, ask for it output schema
            Option.apply(op.getOutputSchemas(Array()))
          } else if (
            !inputSchemaMap.contains(op.operatorIdentifier) || inputSchemaMap(op.operatorIdentifier)
              .exists(s => s.isEmpty)
          ) {
            // op does not have input, or any of the op's input's output schema is null
            // then this op's output schema cannot be inferred as well
            Option.empty
          } else {
            // op's input schema is complete, try to infer its output schema
            // if inference failed, print an exception message, but still continue the process
            Option.apply(
              op.getOutputSchemas(inputSchemaMap(op.operatorIdentifier).map(s => s.get).toArray)
            )
          }
        } catch {
          case e: Throwable =>
            logger.error("got error", e)
            errorList match {
              case Some(list) => list.append((opId, e))
              case None       =>
            }

            Option.empty
        }
      }
      // exception: if op is a source operator, use its output schema as input schema for autocomplete
      if (op.isInstanceOf[SourceOperatorDescriptor]) {
        inputSchemaMap.update(
          op.operatorIdentifier,
          mutable.MutableList(outputSchemas.map(s => s(0)))
        )
      }

      if (!op.isInstanceOf[SinkOpDesc] && outputSchemas.nonEmpty) {
        Verify.verify(outputSchemas.get.length == op.operatorInfo.outputPorts.length)
      }

      // update input schema of all outgoing links
      val outLinks = links.filter(link => link.origin.operatorId == op.operatorIdentifier)
      outLinks.foreach(link => {
        val dest = operatorMap(link.destination.operatorId)
        // get the input schema list, should be pre-populated with size equals to num of ports
        val destInputSchemas = inputSchemaMap(dest.operatorIdentifier)
        // put the schema into the ordinal corresponding to the port
        val schemaOnPort =
          outputSchemas.flatMap(schemas => schemas.toList.lift(link.origin.portOrdinal))
        destInputSchemas(link.destination.portOrdinal) = schemaOnPort
        inputSchemaMap.update(dest.operatorIdentifier, destInputSchemas)
      })
    })

    this.copy(
      context,
      operators,
      links,
      breakpoints,
      inputSchemaMap
        .filter({
          case (_: OperatorIdentity, schemas: mutable.MutableList[Option[Schema]]) =>
            !(schemas.exists(s => s.isEmpty) || schemas.isEmpty)
        })
        .map({ // we need to convert to immutable data structures
          case (opId: OperatorIdentity, schemas: mutable.MutableList[Option[Schema]]) =>
            (opId, schemas.toList)
        })
        .toMap
    )
  }

}
