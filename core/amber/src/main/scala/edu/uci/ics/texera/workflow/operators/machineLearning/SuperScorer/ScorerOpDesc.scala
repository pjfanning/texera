package edu.uci.ics.texera.workflow.operators.machineLearning.SuperScorer

import com.fasterxml.jackson.annotation.{JsonProperty, JsonPropertyDescription}
import com.kjetland.jackson.jsonSchema.annotations.{
  JsonSchemaInject,
  JsonSchemaString,
  JsonSchemaTitle
}
import edu.uci.ics.amber.engine.common.workflow.{InputPort, OutputPort, PortIdentity}
import edu.uci.ics.texera.workflow.common.metadata.annotations.{
  AutofillAttributeName,
  AutofillAttributeNameOnPort1,
  HideAnnotation
}
import edu.uci.ics.texera.workflow.common.metadata.{OperatorGroupConstants, OperatorInfo}
import edu.uci.ics.texera.workflow.common.operators.PythonOperatorDescriptor
import edu.uci.ics.texera.workflow.common.tuple.schema.{Attribute, AttributeType, Schema}

class ScorerOpDesc extends PythonOperatorDescriptor {
  @JsonProperty(required = false, defaultValue = "false")
  @JsonSchemaTitle("Regression")
  @JsonPropertyDescription(
    "Choose to solve a regression task"
  )
  var isRegression: Boolean = false

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

  @JsonProperty(required = false, value = "classificationFlag")
  @JsonSchemaTitle("Scorer Functions")
  @JsonPropertyDescription("Select classification tasks metrics")
  @JsonSchemaInject(
    strings = Array(
      new JsonSchemaString(path = HideAnnotation.hideTarget, value = "isRegression"),
      new JsonSchemaString(path = HideAnnotation.hideType, value = HideAnnotation.Type.equals),
      new JsonSchemaString(path = HideAnnotation.hideExpectedValue, value = "true")
    )
  )
  var classificationMetrics: List[classificationMetricsFnc] = List()

  @JsonProperty(required = false, value = "regressionFlag")
  @JsonSchemaTitle("Scorer Functions")
  @JsonPropertyDescription("Select regression tasks metrics")
  @JsonSchemaInject(
    strings = Array(
      new JsonSchemaString(path = HideAnnotation.hideTarget, value = "isRegression"),
      new JsonSchemaString(path = HideAnnotation.hideType, value = HideAnnotation.Type.equals),
      new JsonSchemaString(path = HideAnnotation.hideExpectedValue, value = "false")
    )
  )
  var regressionMetrics: List[regressionMetricsFnc] = List()

  override def operatorInfo: OperatorInfo =
    OperatorInfo(
      "Scorer",
      "Scorer for machine learning models",
      OperatorGroupConstants.MODEL_PERFORMANCE_GROUP,
      inputPorts = List(InputPort()),
      outputPorts = List(OutputPort())
    )
  override def getOutputSchema(schemas: Array[Schema]): Schema = {
    val outputSchemaBuilder = Schema.builder()
    outputSchemaBuilder.add(new Attribute("Label", AttributeType.STRING))

    if (isRegression) {
      regressionMetrics.foreach(metric => {
        outputSchemaBuilder.add(new Attribute(metric.getName(), AttributeType.DOUBLE))
      })
    } else {
      classificationMetrics.foreach(metric => {
        outputSchemaBuilder.add(new Attribute(metric.getName(), AttributeType.DOUBLE))
      })
    }

    outputSchemaBuilder.build()
  }

  private def getClassificationScorerName(scorer: classificationMetricsFnc): String = {
    // Directly return the name of the scorer using the getName() method
    scorer.getName()
  }
  private def getRegressionScorerName(scorer: regressionMetricsFnc): String = {
    // Directly return the name of the scorer using the getName() method
    scorer.getName()
  }

  private def getEachScorerName(scorer: Any): String =
    scorer match {
      case s: classificationMetricsFnc => getClassificationScorerName(s)
      case s: regressionMetricsFnc     => getRegressionScorerName(s)
      case _                           => throw new IllegalArgumentException("Unknown scorer type")
    }

  private def getSelectedScorers(): String = {
    // Return a string of scorers using the getEachScorerName() method
    var scorers: List[_] = List()
    if (isRegression) scorers = regressionMetrics
    else scorers = classificationMetrics

    scorers.map(scorer => getEachScorerName(scorer)).mkString("'", "','", "'")
  }

  override def generatePythonCode(): String = {
    var is_regression = "False"
    if (isRegression) is_regression = "True"
    val finalcode =
      s"""
         |from pytexera import *
         |import pandas as pd
         |import numpy as np
         |from sklearn.metrics import accuracy_score, precision_score, recall_score, f1_score, mean_squared_error, root_mean_squared_error, mean_absolute_error, r2_score
         |import json
         |
         |
         |def classification_scorers(y_true, y_pred, scorer_list, labels):
         |  result = {scorer: [None] * len(labels) for scorer in scorer_list}
         |  metrics_func = {'Precision Score': precision_score, 'Recall Score': recall_score, 'F1 Score': f1_score}
         |
         |  for scorer in scorer_list:
         |    prediction = None
         |    if scorer == 'Accuracy':
         |      result['Accuracy'][len(labels) - 1] = accuracy_score(y_true, y_pred)
         |    else:
         |      for i, label in enumerate(labels):
         |        if label != 'Overall':
         |          prediction = metrics_func[scorer](y_true, y_pred, average=None, labels=[label])
         |          result[scorer][i] = prediction[0]
         |        else:
         |          result[scorer][i] = metrics_func[scorer](y_true, y_pred, average='macro')
         |
         |  # if the label is not a string, convert it to string
         |  labels = ['class_' + str(label) if type(label) != str else label for label in labels]
         |  result['Label'] = labels
         |  result_df = pd.DataFrame(result)
         |
         |  return result_df
         |
         |
         |def regression_scorers(y_true, y_pred, scorer_list):
         |  result = dict()
         |  for scorer in scorer_list:
         |    if scorer == 'MSE':
         |      result['MSE'] = mean_squared_error(y_true, y_pred)
         |    elif scorer == 'RMSE':
         |      result['RMSE'] = root_mean_squared_error(y_true, y_pred)
         |    elif scorer == "MAE":
         |      result['MAE'] = mean_absolute_error(y_true, y_pred)
         |    elif scorer == 'R2':
         |      result['R2'] = r2_score(y_true, y_pred)
         |
         |  result_df = pd.DataFrame(result, index=[0])
         |  result_df['Label'] = ['Overall']
         |
         |  return result_df
         |
         |class ProcessTableOperator(UDFTableOperator):
         |
         |    @overrides
         |    def process_table(self, table: Table, port: int) -> Iterator[Optional[TableLike]]:
         |      num_of_model = 1
         |      result_df = dict()
         |
         |      if isinstance(table['$predictValueColumn'].iloc[0], np.ndarray):
         |        y_trues = [col.tolist() for col in table['$actualValueColumn']]
         |        y_preds = [col.tolist() for col in table['$predictValueColumn']]
         |        num_of_model = len(y_trues)
         |
         |      else:
         |        y_trues = [table['$actualValueColumn'].tolist()]
         |        y_preds = [table['$predictValueColumn'].tolist()]
         |
         |
         |      scorer_list = [${getSelectedScorers()}]
         |
         |      for i in range(num_of_model):
         |        y_true = y_trues[i]
         |        y_pred = y_preds[i]
         |
         |        if $is_regression:
         |          result = regression_scorers(y_true, y_pred, scorer_list)
         |        else:
         |          # calculate the number of unique labels
         |          labels = list(set(y_true))
         |          labels.append('Overall')
         |          # align the type of y_true and y_pred(str)[this is for apply model]
         |          # y_true_str = y_true.astype(str)
         |          result = classification_scorers(y_true, y_pred, scorer_list, labels)
         |
         |        # concatenate the result of each model
         |        if i == 0:
         |          result_df = result
         |        else:
         |          result_df = pd.concat([result_df, result], ignore_index=True)
         |
         |      yield result_df
         |""".stripMargin
    finalcode
  }

}