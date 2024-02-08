package edu.uci.ics.texera.workflow.operators.visualization.Scatter3DChart

import com.fasterxml.jackson.annotation.{JsonProperty, JsonPropertyDescription}
import com.kjetland.jackson.jsonSchema.annotations.{JsonSchemaInject, JsonSchemaTitle}
import edu.uci.ics.texera.workflow.common.metadata.annotations.AutofillAttributeName
import edu.uci.ics.texera.workflow.common.metadata.{OperatorGroupConstants, OperatorInfo}
import edu.uci.ics.texera.workflow.common.operators.PythonOperatorDescriptor
import edu.uci.ics.texera.workflow.common.tuple.schema.{Attribute, AttributeType, Schema}
import edu.uci.ics.amber.engine.common.workflow.{InputPort, OutputPort}
import edu.uci.ics.texera.workflow.operators.visualization.{
  VisualizationConstants,
  VisualizationOperator
}


class Scatter3dChartOpDesc extends VisualizationOperator with PythonOperatorDescriptor {

    @JsonProperty(value = "title", required = true)
    @JsonSchemaTitle("Title")
    @JsonPropertyDescription("chart title")
    var title: String = ""

    @JsonProperty(value = "x", required = true)
    @JsonSchemaTitle("Value X")
    @JsonPropertyDescription("the value x")
    @AutofillAttributeName
    var x: String = ""

    @JsonProperty(value = "y", required = true)
    @JsonSchemaTitle("Value Y")
    @JsonPropertyDescription("the value y")
    @AutofillAttributeName
    var y: String = ""

    @JsonProperty(value = "z", required = true)
    @JsonSchemaTitle("Value Z")
    @JsonPropertyDescription("the value z")
    @AutofillAttributeName
    var z: String = ""

    @JsonProperty(value = "category", required = true)
    @JsonSchemaTitle("category")
    @JsonPropertyDescription("category")
    @AutofillAttributeName
    var c: String = ""



    override def getOutputSchema(schemas: Array[Schema]): Schema = {
      Schema.newBuilder.add(new Attribute("html-content", AttributeType.STRING)).build
    }

    override def operatorInfo: OperatorInfo =
      OperatorInfo(
        "Scatter3dChart",
        "Visualize data in a Scatter3d Chart",
        OperatorGroupConstants.VISUALIZATION_GROUP,
        inputPorts = List(InputPort()),
        outputPorts = List(OutputPort())
      )

    //  def manipulateTable(): String = {
    //    assert(value.nonEmpty)
    //    s"""
    //       |        table.dropna(subset = ['$value', '$name'], inplace = True) #remove missing values
    //       |""".stripMargin
    //  }

    private def createPlotlyFigure(): String = {
      assert(x.nonEmpty)
      assert(y.nonEmpty)
      assert(z.nonEmpty)
      s"""
         |        fig = go.Figure()
         |        i = 0
         |        for label in table["$c"].unique():
         |            i=i+1
         |            subset = table[table['$c'] == label]
         |            scatter = go.Scatter3d(
         |            x=subset["$x"],
         |            y=subset["$y"],
         |            z=subset["$z"],
         |            mode='markers',
         |            marker=dict(size=3, color=i),
         |            name=label
         |            )
         |            fig.add_trace(scatter)
         |        fig.update_traces(marker=dict(size=5, opacity=0.8))
         |        fig.update_layout(
         |            title='$title',
         |            scene=dict(
         |                xaxis_title='X:$x',
         |                yaxis_title='Y:$y',
         |                zaxis_title='Z:$z'
         |            ),
         |             margin=dict(t=40, b=30, l=10, r=10)
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
         |import pandas as pd
         |import numpy as np
         |
         |class ProcessTableOperator(UDFTableOperator):
         |    def render_error(self, error_msg):
         |        return '''<h1>Chart is not available.</h1>
         |                  <p>Reason is: {} </p>
         |               '''.format(error_msg)
         |
         |    @overrides
         |    def process_table(self, table: Table, port: int) -> Iterator[Optional[TableLike]]:
         |        original_table = table
         |        if table.empty:
         |           yield {'html-content': self.render_error("input table is empty.")}
         |           return
         |        if table.empty:
         |           yield {'html-content': self.render_error("value column contains only non-positive numbers.")}
         |           return
         |        ${createPlotlyFigure()}
         |        # convert fig to html content
         |        html = plotly.io.to_html(fig, include_plotlyjs='cdn', auto_play=False)
         |        yield {'html-content': html}
         |
         |""".stripMargin
    finalcode
  }

  // make the chart type to html visualization so it can be recognized by both backend and frontend.
  override def chartType(): String = VisualizationConstants.HTML_VIZ
  }
