package edu.uci.ics.amber.operator.visualization.rangeSlider

import com.fasterxml.jackson.annotation.{JsonProperty, JsonPropertyDescription}
import com.kjetland.jackson.jsonSchema.annotations.{JsonSchemaInject, JsonSchemaTitle}
import edu.uci.ics.amber.core.tuple.{Attribute, AttributeType, Schema}
import edu.uci.ics.amber.operator.PythonOperatorDescriptor
import edu.uci.ics.amber.workflow.OutputPort.OutputMode
import edu.uci.ics.amber.workflow.{InputPort, OutputPort}
import edu.uci.ics.amber.operator.metadata.{OperatorGroupConstants, OperatorInfo}
import edu.uci.ics.amber.operator.metadata.annotations.AutofillAttributeName

// type constraint: value can only be numeric
@JsonSchemaInject(json = """
{
  "attributeTypeRules": {
    "value": {
      "enum": ["integer", "long", "double"]
    }
  }
}
""")
class RangeSliderOpDesc extends PythonOperatorDescriptor {
  @JsonProperty(value = "value", required = true)
  @JsonSchemaTitle("Value Column")
  @JsonPropertyDescription("the value associated with the y-axis")
  @AutofillAttributeName var value: String = ""

  @JsonProperty(value = "range", required = true)
  @JsonSchemaTitle("Range Column")
  @JsonPropertyDescription("the name of the column to represent the range (x-axis)")
  @AutofillAttributeName var range: String = ""

  @JsonProperty(
    value = "Handle Duplicate Method",
    required = true,
    defaultValue = "mean"
  )
  @AutofillAttributeName var duplicateType: RangeSliderHandleDuplicateFunction = _


  override def getOutputSchema(schemas: Array[Schema]): Schema = {
    Schema.builder().add(new Attribute("html-content", AttributeType.STRING)).build()
  }

  override def operatorInfo: OperatorInfo =
    OperatorInfo(
      "RangeSlider",
      "Visualize data in a Range Slider",
      OperatorGroupConstants.VISUALIZATION_GROUP,
      inputPorts = List(InputPort()),
      outputPorts = List(OutputPort(mode = OutputMode.SINGLE_SNAPSHOT))
    )

  def manipulateTable(): String = {
    assert(value.nonEmpty)
    assert(range.nonEmpty)
    s"""
       |        table = table.dropna(subset=['$range', '$value'])
       |        functionType = '${duplicateType.getFunctionType}'
       |        if functionType == "mean":
       |          table = table.groupby('$range')['$value'].mean().reset_index() #get mean of values
       |        elif functionType == "sum":
       |          table = table.groupby('$range')['$value'].sum().reset_index() #get sum of values
       |""".stripMargin
  }

  def createPlotlyFigure(): String = {
    s"""
       |        # Create figure
       |        fig = go.Figure()
       |
       |        fig.add_trace(
       |            go.Scatter(x=list(table['$range']), y=list(table['$value'])))
       |
       |        # Add range slider
       |        fig.update_layout(
       |            xaxis=dict(
       |                rangeselector=dict(
       |                    buttons=list([
       |                        dict(count=1,
       |                            label="1m",
       |                            step="month",
       |                            stepmode="backward"),
       |                        dict(count=6,
       |                            label="6m",
       |                            step="month",
       |                            stepmode="backward"),
       |                        dict(count=1,
       |                            label="YTD",
       |                            step="year",
       |                            stepmode="todate"),
       |                        dict(count=1,
       |                            label="1y",
       |                            step="year",
       |                            stepmode="backward"),
       |                        dict(step="all")
       |                    ])
       |                ),
       |                rangeslider=dict(
       |                    visible=True
       |                ),
       |                type="date"
       |            )
       |        )
       |""".stripMargin
  }

  override def generatePythonCode(): String = {
    val finalcode =
      s"""
         |from pytexera import *
         |
         |import plotly.express as px
         |import plotly.graph_objects as go
         |import plotly.io
         |import numpy as np
         |
         |class ProcessTableOperator(UDFTableOperator):
         |    def render_error(self, error_msg):
         |        return '''<h1>RangeChart is not available.</h1>
         |                  <p>Reason is: {} </p>
         |               '''.format(error_msg)
         |
         |    @overrides
         |    def process_table(self, table: Table, port: int) -> Iterator[Optional[TableLike]]:
         |        original_table = table
         |        ${manipulateTable()}
         |        if table.empty:
         |           yield {'html-content': self.render_error("input table is empty.")}
         |           return
         |        ${createPlotlyFigure()}
         |        # convert fig to html content
         |        html = plotly.io.to_html(fig, include_plotlyjs='cdn', auto_play=False)
         |        yield {'html-content': html}
         |
         |""".stripMargin
    finalcode
  }

}