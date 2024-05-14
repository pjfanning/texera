package edu.uci.ics.texera.workflow.operators.machineLearning.Scorer

import com.fasterxml.jackson.annotation.{JsonProperty, JsonPropertyDescription}
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaTitle
import edu.uci.ics.amber.engine.common.workflow.{InputPort, OutputPort}
import edu.uci.ics.texera.workflow.common.metadata.annotations.AutofillAttributeName
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
  @AutofillAttributeName
  var predictValueColumn: String = ""

  @JsonProperty(required = true)
  @JsonSchemaTitle("Scorer Functions")
  @JsonPropertyDescription("Select multiple score functions")
  var scorers: List[ScorerFunction] = List()
  override def operatorInfo: OperatorInfo =
    OperatorInfo(
      "Classification Scorer",
      "Scorer for machine learning classifier",
      OperatorGroupConstants.MODEL_VISUALIZATION_GROUP,
      inputPorts = List(InputPort()),
      outputPorts = List(OutputPort())
    )
  override def getOutputSchema(schemas: Array[Schema]): Schema = {
    val outputSchemaBuilder = Schema.builder()
    val inputSchema = schemas(0)
    outputSchemaBuilder.add(new Attribute("Label", AttributeType.STRING))
    scorers
      .map(scorer => getEachScorerName(scorer))
      .foreach(scorer => {
        outputSchemaBuilder.add(new Attribute(scorer, AttributeType.DOUBLE))
      })
    if (inputSchema.containsAttribute("para")){
      outputSchemaBuilder.add(inputSchema)
      outputSchemaBuilder.removeIfExists("para")
      outputSchemaBuilder.add(new Attribute("para", AttributeType.STRING))

    }


    outputSchemaBuilder.build()
  }

  private def getEachScorerName(scorer: ScorerFunction): String = {
    // Directly return the name of the scorer using the getName() method
    scorer.getName()
  }
  private def getSelectedScorers(): String = {
    // Return a string of scorers using the getEachScorerName() method
    scorers.map(scorer => getEachScorerName(scorer)).mkString("'", "','", "'")
  }

  override def generatePythonCode(): String = {
    val finalCode =
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
         |    label_cm = [None] * len(label)
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
         |        label_cm[i] = [label[i], tp, fp, fn, tn, f1[0], precision[0], recall[0]]
         |
         |    return label_cm
         |
         |
         |class ProcessTableOperator(UDFTableOperator):
         |
         |    @overrides
         |    def process_table(self, table: Table, port: int) -> Iterator[Optional[TableLike]]:
         |            result = dict()
         |            if table.shape[0]>1:
         |              y_true = table['$actualValueColumn']
         |              y_pred = table['$predictValueColumn']
         |
         |            else:
         |              y_true = table['$actualValueColumn'][0]
         |              y_pred = table['$predictValueColumn'][0]
         |            labels = list(set(y_true))
         |            labels.append('Overall')
         |
         |            scorer_list = [${getSelectedScorers()}]
         |
         |            for scorer in scorer_list:
         |              result[scorer] = [ None ] * len(labels)
         |
         |            for scorer in scorer_list:
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
         |            label_show = []
         |
         |            for item in labels:
         |              if type(item) != str:
         |                 label_show.append('class_' + str(item))
         |              else:
         |                 label_show.append(item)
         |
         |            result['Label'] = label_show
         |
         |            result_df = pd.DataFrame(result)
         |
         |            row_diff = len(result_df) - len(table) # make two table have same row number
         |            if row_diff > 0:
         |              fill_data = {col: [table[col][0]] * row_diff for col in table.columns}
         |              fill_df = pd.DataFrame(fill_data)
         |              table = pd.concat([table, fill_df], ignore_index=True)
         |            if "para" in table.columns:
         |              para_str_series = pd.Series(table['para'].tolist())
         |              table = table.drop(['para'], axis=1)
         |              table['para'] = para_str_series
         |              result_df = pd.concat([result_df, table], axis=1)
         |
         |            if "Iteration" in result_df.columns:
         |              result_df['Iteration'] = result_df['Iteration'].astype(int)
         |
         |            yield result_df
         |
         |""".stripMargin
    finalCode
  }

}