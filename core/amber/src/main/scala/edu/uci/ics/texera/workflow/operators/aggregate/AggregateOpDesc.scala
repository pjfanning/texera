package edu.uci.ics.texera.workflow.operators.aggregate

import com.fasterxml.jackson.annotation.{JsonProperty, JsonPropertyDescription}
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaTitle
import edu.uci.ics.amber.engine.architecture.deploysemantics.layer.OpExecInitInfo
import edu.uci.ics.amber.engine.common.model.{PhysicalOp, PhysicalPlan, SchemaPropagationFunc}
import edu.uci.ics.amber.engine.common.model.tuple.Schema
import edu.uci.ics.amber.engine.common.virtualidentity.{ExecutionIdentity, PhysicalOpIdentity, WorkflowIdentity}
import edu.uci.ics.amber.engine.common.workflow.{InputPort, OutputPort, PhysicalLink, PortIdentity}
import edu.uci.ics.texera.workflow.common.metadata.annotations.AutofillAttributeNameList
import edu.uci.ics.texera.workflow.common.metadata.{OperatorGroupConstants, OperatorInfo}
import edu.uci.ics.texera.workflow.common.operators.LogicalOp
import edu.uci.ics.texera.workflow.common.workflow.HashPartition

import javax.validation.constraints.{NotNull, Size}

class AggregateOpDesc extends LogicalOp {
  @JsonProperty(value = "aggregations", required = true)
  @JsonPropertyDescription("multiple aggregation functions")
  @NotNull(message = "aggregation cannot be null")
  @Size(min = 1, message = "aggregations cannot be empty")
  var aggregations: List[AggregationOperation] = List()

  @JsonProperty("groupByKeys")
  @JsonSchemaTitle("Group By Keys")
  @JsonPropertyDescription("group by columns")
  @AutofillAttributeNameList
  var groupByKeys: List[String] = List()

  override def getPhysicalPlan(
                                workflowId: WorkflowIdentity,
                                executionId: ExecutionIdentity
                              ): PhysicalPlan = {
    //    if (aggregations.isEmpty) {
    ////      throw new UnsupportedOperationException("Aggregation Functions Cannot be Empty")
    //      throw new RuntimeException("11222")
    //    }

    // TODO: this is supposed to be blocking but due to limitations of materialization naming on the logical operator
    // we are keeping it not annotated as blocking.
    val outputPort = OutputPort(PortIdentity(internal = true))
    val partialPhysicalOp =
      PhysicalOp
        .oneToOnePhysicalOp(
          PhysicalOpIdentity(operatorIdentifier, "localAgg"),
          workflowId,
          executionId,
          OpExecInitInfo((_, _) => new AggregateOpExec(aggregations, groupByKeys))
        )
        .withIsOneToManyOp(true)
        .withInputPorts(List(InputPort(PortIdentity())))
        .withOutputPorts(List(outputPort))
        .withPropagateSchema(
          SchemaPropagationFunc(inputSchemas =>
            Map(
              PortIdentity(internal = true) -> getOutputSchema(
                operatorInfo.inputPorts.map(port => inputSchemas(port.id)).toArray
              )
            )
          )
        )

    val inputPort = InputPort(PortIdentity(0, internal = true))

    val finalOutputPort = OutputPort(PortIdentity(0), blocking = true)

    val finalPhysicalOp = PhysicalOp
      .oneToOnePhysicalOp(
        PhysicalOpIdentity(operatorIdentifier, "globalAgg"),
        workflowId,
        executionId,
        OpExecInitInfo((_, _) =>
          new AggregateOpExec(aggregations.map(aggr => aggr.getFinal), groupByKeys)
        )
      )
      .withParallelizable(false)
      .withIsOneToManyOp(true)
      .withInputPorts(List(inputPort))
      .withOutputPorts(List(finalOutputPort))
      .withPropagateSchema(
        SchemaPropagationFunc(inputSchemas =>
          Map(operatorInfo.outputPorts.head.id -> {
            inputSchemas(PortIdentity(internal = true))
          })
        )
      )
      .withPartitionRequirement(List(Option(HashPartition(groupByKeys))))
      .withDerivePartition(_ => HashPartition(groupByKeys))

    var plan = PhysicalPlan(
      operators = Set(partialPhysicalOp, finalPhysicalOp),
      links = Set(
        PhysicalLink(partialPhysicalOp.id, outputPort.id, finalPhysicalOp.id, inputPort.id)
      )
    )
    plan.operators.foreach(op => plan = plan.setOperator(op.withIsOneToManyOp(true)))
    plan
  }

  override def operatorInfo: OperatorInfo =
    OperatorInfo(
      "Aggregate",
      "Calculate different types of aggregation values",
      OperatorGroupConstants.AGGREGATE_GROUP,
      inputPorts = List(
        InputPort(PortIdentity())
      ),
      outputPorts = List(
        OutputPort(PortIdentity())
      )
    )

  override def getOutputSchema(schemas: Array[Schema]): Schema = {
    if (
      aggregations.exists(agg => agg.resultAttribute == null || agg.resultAttribute.trim.isEmpty)
    ) {
      return null
    }
    if (groupByKeys == null) groupByKeys = List()
    Schema
      .builder()
      .add(
        Schema
          .builder()
          .add(groupByKeys.map(key => schemas(0).getAttribute(key)): _*)
          .build()
      )
      .add(
        aggregations.map(agg =>
          agg.getAggregationAttribute(schemas(0).getAttribute(agg.attribute).getType)
        )
      )
      .build()
  }
}
