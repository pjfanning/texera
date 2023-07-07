package edu.uci.ics.texera.workflow.operators.visualization.barChart

import com.fasterxml.jackson.annotation.{JsonIgnore, JsonProperty, JsonPropertyDescription}
import edu.uci.ics.amber.engine.architecture.deploysemantics.layer.OpExecConfig
import edu.uci.ics.texera.workflow.common.metadata.annotations.{AutofillAttributeName, AutofillAttributeNameList}
import edu.uci.ics.texera.workflow.common.metadata.{InputPort, OperatorGroupConstants, OperatorInfo, OutputPort}
import edu.uci.ics.texera.workflow.common.tuple.schema.{OperatorSchemaInfo, Schema}
import edu.uci.ics.texera.workflow.operators.aggregate.{AggregationFunction, AggregationOperation, SpecializedAggregateOpDesc}
import edu.uci.ics.texera.workflow.operators.visualization.{VisualizationConstants, VisualizationOperator}

import java.util.Collections.singletonList
import scala.jdk.CollectionConverters.asScalaBuffer

/**
  * Supports a bar chart with internal aggregation for one name column (label, x-axis) and multiple data columns (y-axis).
  * If no data column is provided, the count of each label is returned; else the aggregated sum over each data column,
  * grouped by each label is returned.
  */
class BarChartOpDesc extends VisualizationOperator {
  @JsonProperty(value = "name column", required = true)
  @JsonPropertyDescription("column of name (for x-axis)")
  @AutofillAttributeName var nameColumn: String = _

  @JsonProperty(value = "data column(s)", required = false)
  @JsonPropertyDescription("column(s) of data (for y-axis)")
  @AutofillAttributeNameList var dataColumns: List[String] = _

  override def chartType: String = VisualizationConstants.BAR

  def noDataCol: Boolean = dataColumns == null || dataColumns.isEmpty

  def resultAttributeNames: List[String] = if (noDataCol) List("count") else dataColumns

  @JsonIgnore
  lazy val aggOperator: SpecializedAggregateOpDesc = {
    val aggOperator = new SpecializedAggregateOpDesc()
    aggOperator.context = this.context
    aggOperator.operatorID = this.operatorID
    if (noDataCol) {
      val aggOperation = new AggregationOperation()
      aggOperation.aggFunction = AggregationFunction.COUNT
      aggOperation.attribute = nameColumn
      aggOperation.resultAttribute = resultAttributeNames.head
      aggOperator.aggregations = List(aggOperation)
      aggOperator.groupByKeys = List(nameColumn)
    } else {
      val aggOperations = dataColumns.map(dataCol => {
        val aggOperation = new AggregationOperation()
        aggOperation.aggFunction = AggregationFunction.SUM
        aggOperation.attribute = dataCol
        aggOperation.resultAttribute = dataCol
        aggOperation
      })
      aggOperator.aggregations = aggOperations
      aggOperator.groupByKeys = List(nameColumn)
    }
    aggOperator
  }

  override def operatorExecutorMultiLayer(operatorSchemaInfo: OperatorSchemaInfo) = {
    if (nameColumn == null || nameColumn == "") {
      throw new RuntimeException("bar chart: name column is null or empty")
    }

    aggOperator.aggregateOperatorExecutor(
      OperatorSchemaInfo(
        operatorSchemaInfo.inputSchemas,
        Array(aggOperator.getOutputSchema(operatorSchemaInfo.inputSchemas))
      )
    )
  }

  override def operatorInfo: OperatorInfo =
    OperatorInfo(
      "Bar Chart",
      "View the result in bar chart",
      OperatorGroupConstants.VISUALIZATION_GROUP,
      asScalaBuffer(singletonList(InputPort(""))).toList,
      asScalaBuffer(singletonList(OutputPort(""))).toList
    )

  override def getOutputSchema(schemas: Array[Schema]): Schema = {
    aggOperator.getOutputSchema(schemas)
  }

  override def operatorExecutor(operatorSchemaInfo: OperatorSchemaInfo): OpExecConfig = {
    throw new UnsupportedOperationException("multi layer operators use operatorExecutorMultiLayer")
  }
}
