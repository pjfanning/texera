package edu.uci.ics.texera.workflow.operators.machineLearning.Score_Loop

import com.fasterxml.jackson.annotation.{JsonProperty, JsonPropertyDescription}
import com.kjetland.jackson.jsonSchema.annotations.{JsonSchemaInject, JsonSchemaString, JsonSchemaTitle}
import edu.uci.ics.amber.engine.common.workflow.{InputPort, OutputPort, PortIdentity}
import edu.uci.ics.texera.workflow.common.metadata.annotations.{AutofillAttributeName, AutofillAttributeNameOnPort1, HideAnnotation}
import edu.uci.ics.texera.workflow.common.metadata.{OperatorGroupConstants, OperatorInfo}
import edu.uci.ics.texera.workflow.common.operators.PythonOperatorDescriptor
import edu.uci.ics.texera.workflow.common.tuple.schema.{Attribute, AttributeType, Schema}

class Scorer_LoopOpDesc extends PythonOperatorDescriptor {
  @JsonProperty(required = true)
  @JsonSchemaTitle("Actual Value")
  @JsonPropertyDescription("Specify the label column")
  @AutofillAttributeName
  var actualValueColumn: String = ""

  @JsonProperty(required = true)
  @JsonSchemaTitle("Predicted Value")
  @JsonPropertyDescription("Specify the attribute generated by the model")
  @AutofillAttributeNameOnPort1
  var predictValue: String = ""

//  @JsonProperty(defaultValue = "false")
//  @JsonSchemaTitle("Using probability?")
//  @JsonSchemaInject(json = """{"toggleHidden" : ["probabilityValue"]}""")
//  var is_prob: Boolean = _
//
//  @JsonProperty()
//  @JsonSchemaTitle("Probability Column")
//  @JsonPropertyDescription("Specify the name of the predicted probability")
//  @JsonSchemaInject(
//    strings = Array(
//      new JsonSchemaString(path = HideAnnotation.hideTarget, value = "is_prob"),
//      new JsonSchemaString(path = HideAnnotation.hideType, value = HideAnnotation.Type.equals),
//      new JsonSchemaString(path = HideAnnotation.hideExpectedValue, value = "false")
//    )
//  )
//  @AutofillAttributeNameOnPort1
//  var probabilityValue: String = ""

  // 我要怎麼知道前一個operator有選y_prob這個選項呢？
  // 如果我在這裡加 可能user前面沒選y_prob 但這裡有選擇probabilityValue會有問題

  @JsonProperty(required = true)
  @JsonSchemaTitle("Scorer Functions")
  @JsonPropertyDescription("Select multiple score functions")
  var scorers: List[Scorer_LoopFunction] = List()
  override def operatorInfo: OperatorInfo =
    OperatorInfo(
      "Scorer_Loop",
      "Scorer for machine learning classifier",
      OperatorGroupConstants.ML_GROUP,
      inputPorts = List(
        InputPort(
        PortIdentity(0),
        displayName = "GroundTruth",
        ),
        InputPort(
          PortIdentity(1),
          displayName = "PredictValue",
          dependencies = List(PortIdentity(0)))),
      outputPorts = List(OutputPort())
    )

  override def generatePythonCode(): String = {
    val finalcode =
      s"""
         |from pytexera import *
         |import pandas as pd
         |import numpy as np
         |from sklearn.metrics import accuracy_score
         |from sklearn.metrics import precision_score
         |from sklearn.metrics import confusion_matrix, ConfusionMatrixDisplay
         |from sklearn.metrics import recall_score
         |from sklearn.metrics import f1_score
         |import plotly.express as px
         |import plotly.graph_objects as go
         |import plotly.io
         |
         |import json
         |
         |def drawConfusionMatrixImage(prediction, labels):
         |
         |  text = [[str(value) for value in row] for row in prediction]
         |
         |  fig = go.Figure(data=go.Heatmap(
         |    z=prediction,
         |    x=labels,
         |    y=labels,
         |    text=text,
         |    texttemplate="%{text}",
         |    hoverongaps=False,
         |    colorscale='Viridis',
         |    showscale=True))
         |
         |  fig.update_layout(
         |    title='Confusion Matrix',
         |    xaxis_title="Predicted Label",
         |    yaxis_title="True Label")
         |
         |  html = plotly.io.to_html(fig, include_plotlyjs='cdn', auto_play=False)
         |
         |  return html
         |
         |
         |class ProcessTableOperator(UDFTableOperator):
         |
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
         |            predictValueTable = table
         |            print(predictValueTable.columns)
         |
         |            bestAccuracy = 0
         |            bestPredict = None
         |            bestLabels = None
         |
         |            if 'y_prob' in predictValueTable.columns:
         |              print("'y_prob' in predictValueTable.columns")
         |            else:
         |              print("'y_prob' not in predictValueTable.columns")
         |
         |            optimizationTimes = len(predictValueTable['model'])
         |            y_true = groundTruthTable['$actualValueColumn']
         |
         |            scorerList = [${getSelectedScorers()}]
         |            result = dict()
         |
         |            for scorer in scorerList:
         |              result[ scorer ] = [ None ] * optimizationTimes
         |
         |            for i in range (optimizationTimes):
         |              for scorer in scorerList:
         |                y_pred = predictValueTable['$predictValue'][i] # list of predicted values by model i

         |                prediction = None
         |                if scorer == 'Accuracy':
         |                  prediction = accuracy_score(y_true, y_pred)
         |                  result['Accuracy'][i]=prediction
         |                elif scorer == 'Precision Score':
         |                  prediction = precision_score(y_true, y_pred, average = 'macro')
         |                  result['Precision Score'][i]=prediction
         |                elif scorer == 'Recall Score':
         |                  prediction = recall_score(y_true, y_pred, average = 'macro')
         |                  result['Recall Score'][i]=prediction
         |                elif scorer == 'F1 Score':
         |                  prediction = f1_score(y_true, y_pred, average = 'macro')
         |                  result['F1 Score'][i]=prediction
         |                elif scorer == 'Confusion Matrix':
         |                  column_set = set(y_true)
         |                  labels = list(column_set)
         |                  prediction = confusion_matrix(y_true, y_pred, labels = labels)
         |                  prediction_json = json.dumps(prediction.tolist(), indent = 4)
         |                  result['Confusion Matrix'][i] = prediction_json
         |
         |                  prediction_accuracy = accuracy_score(y_true, y_pred)
         |                  if bestAccuracy < prediction_accuracy:
         |                    bestAccuracy = prediction_accuracy
         |                    bestPredict = prediction
         |                    bestLabels = labels
         |
         |            if  bestPredict and bestLabels:
         |              html = drawConfusionMatrixImage(bestPredict, bestLabels)
         |              result['Best Confusion Matrix Chart'] = html
         |
         |            result['model'] = predictValueTable['model'].tolist()
         |            result['para'] = predictValueTable['para'].tolist()
         |
         |            df = pd.DataFrame(result)
         |            yield df
         |
         |""".stripMargin
    finalcode
  }
  override def getOutputSchema(schemas: Array[Schema]): Schema = {
    val outputSchemaBuilder = Schema.newBuilder
    scorers.map(scorer => getEachScorerName(scorer)).foreach(scorer =>
    {
      if (scorer == "Confusion Matrix") {
        outputSchemaBuilder.add(new Attribute(scorer, AttributeType.STRING))
        outputSchemaBuilder.add(new Attribute("Best Confusion Matrix Chart", AttributeType.STRING))
      } else {
        outputSchemaBuilder.add(new Attribute(scorer, AttributeType.DOUBLE))
      }
    }
    )
    outputSchemaBuilder.add(new Attribute("para", AttributeType.STRING))
    outputSchemaBuilder.add(new Attribute("model", AttributeType.BINARY)).build
  }


  private def getEachScorerName(scorer: Scorer_LoopFunction): String = {
    // Directly return the name of the scorer using the getName() method
    scorer.getName()
  }
  private def getSelectedScorers(): String = {
    // Return a string of scorers using the getEachScorerName() method
    scorers.map(scorer => getEachScorerName(scorer)).mkString("'", "','", "'")
  }


}