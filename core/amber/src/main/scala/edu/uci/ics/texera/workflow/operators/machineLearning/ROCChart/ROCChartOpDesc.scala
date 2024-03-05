package edu.uci.ics.texera.workflow.operators.machineLearning.ROCChart

import com.fasterxml.jackson.annotation.{JsonProperty, JsonPropertyDescription}
import com.kjetland.jackson.jsonSchema.annotations.{JsonSchemaInject, JsonSchemaTitle}
import edu.uci.ics.amber.engine.common.workflow.{InputPort, OutputPort, PortIdentity}
import edu.uci.ics.texera.workflow.common.metadata.annotations.AutofillAttributeName
import edu.uci.ics.texera.workflow.common.metadata.{OperatorGroupConstants, OperatorInfo}
import edu.uci.ics.texera.workflow.common.operators.PythonOperatorDescriptor
import edu.uci.ics.texera.workflow.common.tuple.schema.{Attribute, AttributeType, Schema}
import edu.uci.ics.texera.workflow.operators.visualization.{VisualizationConstants, VisualizationOperator}

@JsonSchemaInject(json =
  """
{
  "attributeTypeRules": {
    "title": "string"
  }
}
""")
class ROCChartOpDesc extends VisualizationOperator with PythonOperatorDescriptor {
  @JsonProperty(value = "title", required = true, defaultValue = "ROC Chart")
  @JsonSchemaTitle("Plot Title")
  @JsonPropertyDescription("The value for the plot title")
  var title: String = "ROC Chart"

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
      "ROC Chart",
      "Visualize ROC in a ROC Chart",
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
         |from sklearn import metrics
         |from sklearn.preprocessing import label_binarize
         |from sklearn.preprocessing import LabelEncoder
         |import plotly.express as px
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
         |            y_true = groundTruthTable['$actualValueColumn'].values
         |            y_pred = predictValueTable['y_pred'][0]
         |            y_prob = predictValueTable['y_prob'][0][0]
         |            labels = predictValueTable['y_prob'][0][1]
         |
         |            print(y_true, y_prob, y_prob, labels)
         |
         |            label_encoder = LabelEncoder()
         |            y = label_encoder.fit_transform(y_true)
         |
         |            y_bin = label_binarize(y_true, classes=labels)
         |            n_classes = y_bin.shape[1]
         |
         |            fpr = dict()
         |            tpr = dict()
         |            roc_auc = dict()
         |
         |            for i in range(n_classes):
         |                fpr[i], tpr[i], _ = metrics.roc_curve(y_bin[:, i], y_prob[:, i])
         |                roc_auc[i] = metrics.auc(fpr[i], tpr[i])
         |
         |            fig = go.Figure()
         |
         |            colors = px.colors.qualitative.Plotly[:n_classes]
         |            for i, color in zip(range(n_classes), colors):
         |                fig.add_trace(go.Scatter(x=fpr[i], y=tpr[i], mode='lines',
         |                    name=f'{labels[i]} (area = {roc_auc[i]:0.2f})',
         |                    line=dict(color=color)))
         |
         |            fig.add_trace(go.Scatter(x=[0, 1], y=[0, 1], mode='lines', name='Chance', line=dict(color='navy', dash='dash')))
         |
         |            fig.update_layout(
         |                  title='$title',
         |                  xaxis_title='False Positive Rate',
         |                  yaxis_title='True Positive Rate',
         |                  xaxis=dict(scaleanchor="x", scaleratio=1),
         |                  yaxis=dict(constrain="domain"),
         |                  margin=dict(l=20, r=20, t=40, b=20))
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
