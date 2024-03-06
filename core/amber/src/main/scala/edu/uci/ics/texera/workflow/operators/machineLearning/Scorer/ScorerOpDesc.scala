package edu.uci.ics.texera.workflow.operators.machineLearning.Scorer

import com.fasterxml.jackson.annotation.{JsonProperty, JsonPropertyDescription}
import com.kjetland.jackson.jsonSchema.annotations.{JsonSchemaInject, JsonSchemaString, JsonSchemaTitle}
import edu.uci.ics.amber.engine.common.workflow.{InputPort, OutputPort, PortIdentity}
import edu.uci.ics.texera.workflow.common.metadata.annotations.{AutofillAttributeName, AutofillAttributeNameOnPort1, HideAnnotation}
import edu.uci.ics.texera.workflow.common.metadata.{OperatorGroupConstants, OperatorInfo}
import edu.uci.ics.texera.workflow.common.operators.PythonOperatorDescriptor
import edu.uci.ics.texera.workflow.common.tuple.schema.{Attribute, AttributeType, Schema}

class ScorerOpDesc extends PythonOperatorDescriptor {
  @JsonProperty(required = true)
  @JsonSchemaTitle("Actual Value")
  @JsonPropertyDescription("Specify the label column")
  @AutofillAttributeName
  var actualValueColumn: String = ""

  @JsonProperty(required = true)
  @JsonSchemaTitle("Predicted Value")
  @JsonPropertyDescription("Specify the attribute generated by the model")
  @AutofillAttributeNameOnPort1
  var predictValueColumn: String = ""

  @JsonProperty(required = true)
  @JsonSchemaTitle("Scorer Functions")
  @JsonPropertyDescription("Select multiple score functions")
  var scorers: List[ScorerFunction] = List()
  override def operatorInfo: OperatorInfo =
    OperatorInfo(
      "Scorer",
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
         |from sklearn.metrics import confusion_matrix
         |from sklearn.metrics import recall_score
         |from sklearn.metrics import f1_score
         |import json
         |
         |
         |def label_confusion_matrix(y_true, y_pred, label):
         |    labelCM = [None] * len(label)
         |    cm = confusion_matrix(y_true, y_pred, labels = label)
         |
         |    for i in range(len(cm)):
         |        tp = cm[i, i]
         |        fp = np.sum(cm[:, i]) - tp
         |        fn = np.sum(cm[i, :]) - tp
         |        tn = np.sum(cm) - tp - fp - fn
         |        f1 = f1_score(y_true, y_pred, average = None, labels = [label[i]])
         |        precision = precision_score(y_true, y_pred, average = None, labels = [label[i]])
         |        recall = recall_score(y_true, y_pred, average = None, labels = [label[i]])
         |
         |        labelCM[i] = [label[i], tp, fp, fn, tn, f1[0], precision[0], recall[0]]
         |
         |    return labelCM
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
         |
         |        if port == 1:
         |            predictValueTable = table
         |            result = dict()
         |
         |
         |            y_true = groundTruthTable['$actualValueColumn']
         |            y_pred = predictValueTable['$predictValueColumn'][0]
         |            labels = list(set(y_true))
         |
         |            labels.append('Overall')
         |
         |            scorerList = [${getSelectedScorers()}]
         |
         |            for scorer in scorerList:
         |              result[scorer] = [ None ] * len(labels)
         |
         |            result['Label'] = labels
         |            for scorer in scorerList:
         |              prediction = None
         |              if scorer == 'Accuracy':
         |                prediction = accuracy_score(y_true, y_pred)
         |                result['Accuracy'][len(labels) - 1] = prediction
         |
         |              elif scorer == 'Precision Score':
         |                for i in range(len(labels)):
         |                  if labels[i] != 'Overall':
         |                    prediction = precision_score(y_true, y_pred, average = None, labels = [labels[i]])
         |                    result['Precision Score'][i] = prediction[0]
         |                  else:
         |                    result['Precision Score'][i] = precision_score(y_true, y_pred, average = 'macro')
         |
         |              elif scorer == 'Recall Score':
         |                for i in range(len(labels)):
         |                  if labels[i] != 'Overall':
         |                    prediction = recall_score(y_true, y_pred, average = None, labels = [labels[i]])
         |                    result['Recall Score'][i] = prediction[0]
         |                  else:
         |                    result['Recall Score'][i] = recall_score(y_true, y_pred, average = 'macro')
         |
         |              elif scorer == 'F1 Score':
         |                for i in range(len(labels)):
         |                   if labels[i] != 'Overall':
         |                    prediction = f1_score(y_true, y_pred, average = None, labels = [labels[i]])
         |                    result['F1 Score'][i] = prediction[0]
         |                   else:
         |                    result['F1 Score'][i] = f1_score(y_true, y_pred, average = 'macro')
         |
         |            paraStrSeries = pd.Series(predictValueTable['para'].tolist())
         |            predictValueTable = predictValueTable.drop(['para'], axis=1)
         |            predictValueTable['para'] = paraStrSeries
         |
         |            resultDf = pd.DataFrame(result)
         |            df = pd.concat([resultDf, predictValueTable], axis=1)
         |
         |            for column in df.columns:
         |              if pd.api.types.is_numeric_dtype(df[column]):
         |                df[column] = df[column].fillna(df[column][0])
         |              elif pd.api.types.is_string_dtype(df[column]):
         |                df[column] = df[column].fillna('NaN')
         |
         |            if "Iteration" in df.columns:
         |              df['Iteration'] = df['Iteration'].astype(int)
         |
         |            yield df
         |
         |""".stripMargin
    finalcode
  }
  override def getOutputSchema(schemas: Array[Schema]): Schema = {
    val outputSchemaBuilder = Schema.newBuilder
    val inputSchema = schemas(1)
    outputSchemaBuilder.add(inputSchema)
    outputSchemaBuilder.removeIfExists("para")
    outputSchemaBuilder.add(new Attribute("para", AttributeType.STRING))
    outputSchemaBuilder.add(new Attribute("Label", AttributeType.STRING))
    scorers.map(scorer => getEachScorerName(scorer)).foreach(scorer =>
    {
      outputSchemaBuilder.add(new Attribute(scorer, AttributeType.DOUBLE))
    }
    )
    outputSchemaBuilder.build
  }


  private def getEachScorerName(scorer: ScorerFunction): String = {
    // Directly return the name of the scorer using the getName() method
    scorer.getName()
  }
  private def getSelectedScorers(): String = {
    // Return a string of scorers using the getEachScorerName() method
    scorers.map(scorer => getEachScorerName(scorer)).mkString("'", "','", "'")
  }


}