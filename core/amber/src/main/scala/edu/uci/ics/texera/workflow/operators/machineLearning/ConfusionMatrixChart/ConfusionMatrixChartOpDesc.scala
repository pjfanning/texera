package edu.uci.ics.texera.workflow.operators.machineLearning.ConfusionMatrixChart

import com.fasterxml.jackson.annotation.{JsonProperty, JsonPropertyDescription}
import com.kjetland.jackson.jsonSchema.annotations.{JsonSchemaInject, JsonSchemaTitle}
import edu.uci.ics.amber.engine.common.workflow.{InputPort, OutputPort, PortIdentity}
import edu.uci.ics.texera.workflow.common.metadata.annotations.AutofillAttributeName
import edu.uci.ics.texera.workflow.common.metadata.{OperatorGroupConstants, OperatorInfo}
import edu.uci.ics.texera.workflow.common.operators.PythonOperatorDescriptor
import edu.uci.ics.texera.workflow.common.tuple.schema.{Attribute, AttributeType, Schema}
import edu.uci.ics.texera.workflow.operators.visualization.{VisualizationConstants, VisualizationOperator}

@JsonSchemaInject(json = """
{
  "attributeTypeRules": {
    "title": "string"
  }
}
""")
class ConfusionMatrixChartOpDesc extends VisualizationOperator with PythonOperatorDescriptor {
  @JsonProperty(value = "title", required = true, defaultValue = "Confusion Matrix Chart")
  @JsonSchemaTitle("Plot Title")
  @JsonPropertyDescription("The value for the plot title")
  var title: String = "Confusion Matrix Chart"

  @JsonProperty(required = true)
  @JsonSchemaTitle("Actual Value")
  @JsonPropertyDescription("Specify the label column")
  @AutofillAttributeName
  var actualValueColumn: String = ""

  override def getOutputSchema(schemas: Array[Schema]): Schema = {
    Schema.newBuilder.add(new Attribute("html-content", AttributeType.STRING)).build
  }

  override def operatorInfo: OperatorInfo =
    OperatorInfo(
      "Confusion Matrix Chart",
      "Visualize confusion matrix in a Confusion Matrix Chart",
      OperatorGroupConstants.ML_GROUP,
      inputPorts = List(
        InputPort(
          PortIdentity(0),
          displayName = "GroundTruth",
        ),
        InputPort(
          PortIdentity(1),
          displayName = "PredictValue",
          dependencies = List(PortIdentity(0)))
      ),
      outputPorts = List(OutputPort())
    )

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
         |import json
         |
         |class ProcessTableOperator(UDFTableOperator):
         |    @overrides
         |    def process_table(self, table: Table, port: int) -> Iterator[Optional[TableLike]]:
         |        global groundTruthTable
         |        global predictValueTable
         |
         |        if port == 0:
         |            groundTruthTable = table
         |            print(groundTruthTable.columns)
         |
         |        if port == 1:
         |            if table.empty:
         |                  yield {'html-content': self.render_error("input table is empty.")}
         |                  return
         |
         |            predictValueTable = table
         |            print(predictValueTable.columns)
         |
         |            y_true = groundTruthTable['$actualValueColumn']
         |            y_pred = predictValueTable['y_pred']
         |
         |            column_set = set(y_true)
         |            labels = list(column_set)
         |            prediction = predictValueTable['Confusion Matrix'].apply(json.loads)[0]
         |
         |            print('prediction', prediction)
         |            print('labels', labels)
         |
         |            text = [[str(value) for value in row] for row in prediction]
         |
         |            print('text', text)
         |
         |            fig = go.Figure(data=go.Heatmap(
         |                z=prediction,
         |                x=labels,
         |                y=labels,
         |                text=text,
         |                texttemplate="%{text}",
         |                hoverongaps=False,
         |                colorscale='Viridis',
         |                showscale=True))
         |
         |            fig.update_layout(
         |                title='$title',
         |                xaxis_title="Predicted Label",
         |                yaxis_title="True Label")
         |
         |            # convert fig to html content
         |            html = plotly.io.to_html(fig, include_plotlyjs='cdn', auto_play=False)
         |            yield {'html-content': html}
         |
         |""".stripMargin
    finalcode
  }

  // make the chart type to html visualization so it can be recognized by both backend and frontend.
  override def chartType(): String = VisualizationConstants.HTML_VIZ
}
