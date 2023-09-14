package edu.uci.ics.texera.workflow.operators.visualization.dumbbellPlot

import com.fasterxml.jackson.annotation.{JsonProperty, JsonPropertyDescription}
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaTitle
import edu.uci.ics.texera.workflow.common.metadata.{InputPort, OperatorGroupConstants, OperatorInfo, OutputPort}
import edu.uci.ics.texera.workflow.common.metadata.annotations.AutofillAttributeName
import edu.uci.ics.texera.workflow.common.operators.PythonOperatorDescriptor
import edu.uci.ics.texera.workflow.common.tuple.schema.{Attribute, AttributeType, OperatorSchemaInfo, Schema}
import edu.uci.ics.texera.workflow.operators.visualization.{VisualizationConstants, VisualizationOperator}

class DumbbellPlotOpDesc extends VisualizationOperator with PythonOperatorDescriptor {

  @JsonProperty(value = "endPointsColumnName", required = true)
  @JsonSchemaTitle("Endpoints Column Name")
  @JsonPropertyDescription("the name of the endpoints column")
  @AutofillAttributeName
  var endPointsColumnName: String = ""

  @JsonProperty(value = "startPointValue", required = true)
  @JsonSchemaTitle("Start Point Value")
  @JsonPropertyDescription("the value of the start point")
  @AutofillAttributeName
  var startPointValue: String = ""

  @JsonProperty(value = "endPointValue", required = true)
  @JsonSchemaTitle("End Point Value")
  @JsonPropertyDescription("the value of the end point")
  @AutofillAttributeName
  var endPointValue: String = ""

  // TODO: add restriction on this attribute
  @JsonProperty(value = "endPointsMagnitudeColumnName", required = true)
  @JsonSchemaTitle("Endpoints Magnitude Column Name")
  @JsonPropertyDescription("the name of the column that indicates the measurement at endpoints")
  @AutofillAttributeName
  var endPointsMagnitudeColumnName: String = ""

  @JsonProperty(value = "entityColumnName", required = true)
  @JsonSchemaTitle("Entity Column Name")
  @JsonPropertyDescription("the column name of the entity being compared")
  @AutofillAttributeName
  var entityColumnName: String = ""


  override def getOutputSchema(schemas: Array[Schema]): Schema = {
    Schema.newBuilder.add(new Attribute("html-content", AttributeType.STRING)).build
  }

  override def operatorInfo: OperatorInfo =
    OperatorInfo(
      "DumbbellPlot",
      "Visualize data in a Dumbbell Plots. A dumbbell plots (also known as a lollipop chart) is typically used to compare two distinct values or time points for the same entity.",
      OperatorGroupConstants.VISUALIZATION_GROUP,
      inputPorts = List(InputPort()),
      outputPorts = List(OutputPort())
    )

  override def numWorkers() = 1

  override def generatePythonCode(operatorSchemaInfo: OperatorSchemaInfo): String = ???

  override def chartType(): String = VisualizationConstants.HTML_VIZ
}
