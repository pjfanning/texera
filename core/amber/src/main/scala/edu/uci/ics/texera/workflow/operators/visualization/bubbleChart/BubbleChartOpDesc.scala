package edu.uci.ics.texera.workflow.operators.visualization.bubbleChart

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyDescription
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaTitle
import edu.uci.ics.texera.workflow.common.metadata.{OperatorGroupConstants, OperatorInfo}
import edu.uci.ics.texera.workflow.common.metadata.annotations.AutofillAttributeName
import edu.uci.ics.texera.workflow.common.operators.PythonOperatorDescriptor
import edu.uci.ics.texera.workflow.common.tuple.schema.{Attribute, AttributeType, Schema}
import edu.uci.ics.amber.engine.common.workflow.{InputPort, OutputPort}
import edu.uci.ics.texera.workflow.operators.visualization.{
  VisualizationConstants,
  VisualizationOperator
}

/**
  * Visualization Operator to visualize results as a Bubble Chart
  * User specifies 2 attributes to use for the x, y labels.
  * Size of bubbles can be determined via third attribute.
  * Bubbles can be colored using a fourth attribute.
  */

// type can be numerical only
class BubbleChartOpDesc extends VisualizationOperator with PythonOperatorDescriptor {

  @JsonProperty(value = "title", required = true, defaultValue = "Bubble Chart")
  @JsonSchemaTitle("Chart Title")
  @JsonPropertyDescription("Add a title to your visualization")
  val title: String = ""

  @JsonProperty(value = "xValue", required = true)
  @JsonSchemaTitle("X-axis Attribute")
  @JsonPropertyDescription("The attribute for the x-axis")
  @AutofillAttributeName
  val xValue: String = ""

  @JsonProperty(value = "yValue", required = true)
  @JsonSchemaTitle("Y-axis Attribute")
  @JsonPropertyDescription("The attribute for the y-axis")
  @AutofillAttributeName
  val yValue: String = ""

  @JsonProperty(value = "zValue", required = true)
  @JsonSchemaTitle("Size Attribute")
  @JsonPropertyDescription("The attribute to determine bubble size")
  @AutofillAttributeName
  val zValue: String = ""

  @JsonProperty(value = "enableColor", defaultValue = "false")
  @JsonSchemaTitle("Enable Coloring")
  @JsonPropertyDescription("Give bubbles colors")
  val enableColoring: Boolean = false

  @JsonProperty(value = "colorCategory", required = true)
  @JsonSchemaTitle("Color Attribute")
  @JsonPropertyDescription("The attribute to color bubbles")
  @AutofillAttributeName
  private val colorCategory: String = ""

  override def chartType: String = VisualizationConstants.HTML_VIZ

  override def getOutputSchema(schemas: Array[Schema]): Schema = {
    Schema.builder().add(new Attribute("html-content", AttributeType.STRING)).build()
  }

  override def operatorInfo: OperatorInfo =
    OperatorInfo(
      "Bubble Chart",
      "a 3D Scatter Plot; Bubbles are graphed using x and y labels, and their sizes determined by a z-value.",
      OperatorGroupConstants.VISUALIZATION_GROUP,
      inputPorts = List(InputPort()),
      outputPorts = List(OutputPort())
    )

  def manipulateTable(): String = {
    assert(xValue.nonEmpty && yValue.nonEmpty && zValue.nonEmpty)
    s"""
       |        # drops rows with missing values pertaining to relevant columns
       |        table.dropna(subset=['$xValue', '$yValue', '$zValue'], inplace = True)
       |
       |""".stripMargin
  }

  def createPlotlyFigure(): String = {
    assert(xValue.nonEmpty && yValue.nonEmpty && zValue.nonEmpty)
    s"""
       |        if '$enableColoring' == 'true':
       |            fig = go.Figure(px.scatter(table, x='$xValue', y='$yValue', size='$zValue', size_max=100, title='$title', color='$colorCategory'))
       |        else:
       |            fig = go.Figure(px.scatter(table, x='$xValue', y='$yValue', size='$zValue', size_max=100, title='$title'))
       |""".stripMargin
  }

  override def generatePythonCode(): String = {
    val finalCode = s"""
        |from pytexera import *
        |
        |import plotly.express as px
        |import plotly.graph_objects as go
        |import plotly.io
        |import numpy as np
        |
        |
        |class ProcessTableOperator(UDFTableOperator):
        |
        |    def render_error(self, error_msg):
        |        return '''<h1>TreeMap is not available.</h1>
        |                  <p>Reasons are: {} </p>
        |               '''.format(error_msg)
        |
        |    @overrides
        |    def process_table(self, table: Table, port: int) -> Iterator[Optional[TableLike]]:
        |        if table.empty:
        |            yield {'html-content': self.render_error("Input table is empty.")}
        |            return
        |        ${manipulateTable()}
        |        ${createPlotlyFigure()}
        |        if table.empty:
        |            yield {'html-content': self.render_error("No valid rows left (every row has at least 1 missing value).")}
        |            return
        |        html = plotly.io.to_html(fig, include_plotlyjs = 'cdn', auto_play = False)
        |        yield {'html-content':html}
        |""".stripMargin
    finalCode
  }
}
