package edu.uci.ics.texera.workflow.operators.machineLearning.ConfusionMatrixChart

import com.fasterxml.jackson.annotation.{JsonProperty, JsonPropertyDescription}
import com.kjetland.jackson.jsonSchema.annotations.{JsonSchemaInject, JsonSchemaTitle}
import edu.uci.ics.amber.engine.common.workflow.{InputPort, OutputPort}
import edu.uci.ics.texera.workflow.common.metadata.annotations.{
  AutofillAttributeName,
  AutofillAttributeNameOnPort1
}
import edu.uci.ics.texera.workflow.common.metadata.{OperatorGroupConstants, OperatorInfo}
import edu.uci.ics.texera.workflow.common.operators.PythonOperatorDescriptor
import edu.uci.ics.texera.workflow.common.tuple.schema.{Attribute, AttributeType, Schema}
import edu.uci.ics.texera.workflow.operators.visualization.{
  VisualizationConstants,
  VisualizationOperator
}

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
  @JsonPropertyDescription("The title of the plot figure")
  var title: String = "Confusion Matrix Chart"

  @JsonProperty(required = true)
  @JsonSchemaTitle("Ground Truth")
  @JsonPropertyDescription("Specify the ground truth label column")
  @AutofillAttributeName
  var actualValueColumn: String = ""

  @JsonProperty(required = true,defaultValue = "y_prediction")
  @JsonSchemaTitle("Predicted Value")
  @JsonPropertyDescription("Specify the attribute predicted by the model")
  @AutofillAttributeNameOnPort1
  var predictValueColumn: String = ""

  override def getOutputSchema(schemas: Array[Schema]): Schema = {
    Schema.builder().add(new Attribute("html-content", AttributeType.STRING)).build
  }

  override def operatorInfo: OperatorInfo =
    OperatorInfo(
      "Confusion Matrix Chart",
      "Visualize confusion matrix in a Confusion Matrix Chart",
      OperatorGroupConstants.MODEL_VISUALIZATION_GROUP,
      inputPorts = List(
        InputPort()
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
         |from sklearn.metrics import confusion_matrix
         |
         |class ProcessTableOperator(UDFTableOperator):
         |    @overrides
         |    def process_table(self, table: Table, port: int) -> Iterator[Optional[TableLike]]:
         |        global predict_value_table
         |
         |        if table.empty:
         |            yield {"html-content": self.render_error("input table is empty.")}
         |            return
         |
         |        predict_value_table = table
         |
         |        y_true = predict_value_table["$actualValueColumn"][0]
         |        y_pred = predict_value_table["$predictValueColumn"][0]
         |
         |        column_set = set(y_true)
         |        labels = list(column_set)
         |
         |        prediction = confusion_matrix(y_true, y_pred, labels=labels)
         |
         |        text = [[str(value) for value in row] for row in prediction]
         |
         |        fig = go.Figure(
         |            data=go.Heatmap(
         |                z=prediction,
         |                x=labels,
         |                y=labels,
         |                text=text,
         |                texttemplate="%{text}",
         |                hoverongaps=False,
         |                colorscale="Viridis",
         |                showscale=True,
         |            )
         |        )
         |
         |        fig.update_layout(
         |            title="$title", xaxis_title="Predicted Label", yaxis_title="True Label"
         |        )
         |
         |        # convert fig to html content
         |        html = plotly.io.to_html(fig, include_plotlyjs="cdn", auto_play=False)
         |        yield {"html-content": html}
         |
         |
         |""".stripMargin
    finalcode
  }

  // make the chart type to html visualization so it can be recognized by both backend and frontend.
  override def chartType(): String = VisualizationConstants.HTML_VIZ
}