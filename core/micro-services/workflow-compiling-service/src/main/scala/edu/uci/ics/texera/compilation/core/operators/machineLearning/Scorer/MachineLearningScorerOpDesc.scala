package edu.uci.ics.texera.compilation.core.operators.machineLearning.Scorer

import com.fasterxml.jackson.annotation.{JsonProperty, JsonPropertyDescription}
import com.kjetland.jackson.jsonSchema.annotations.{JsonSchemaInject, JsonSchemaString, JsonSchemaTitle}
import edu.uci.ics.amber.core.tuple.{Attribute, AttributeType, Schema}
import edu.uci.ics.amber.workflow.{InputPort, OutputPort}
import edu.uci.ics.texera.compilation.core.common.metadata.{OperatorGroupConstants, OperatorInfo}
import edu.uci.ics.texera.compilation.core.common.metadata.annotations.{AutofillAttributeName, HideAnnotation}
import edu.uci.ics.texera.compilation.core.operators.PythonOperatorDescriptor

class MachineLearningScorerOpDesc extends PythonOperatorDescriptor {
  @JsonProperty(required = true, defaultValue = "false")
  @JsonSchemaTitle("Regression")
  @JsonPropertyDescription(
    "Choose to solve a regression task"
  )
  var isRegression: Boolean = false

  @JsonProperty(required = true)
  @JsonSchemaTitle("Actual Value")
  @JsonPropertyDescription("Specify the label attribute")
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
      "Machine Learning Scorer",
      "Scorer for machine learning models",
      OperatorGroupConstants.MACHINE_LEARNING_GENERAL_GROUP,
      inputPorts = List(InputPort()),
      outputPorts = List(OutputPort())
    )
  override def getOutputSchema(schemas: Array[Schema]): Schema = {
    val outputSchemaBuilder = Schema.builder()
    if (!isRegression) {
      outputSchemaBuilder.add(new Attribute("Class", AttributeType.STRING))
    }

    val metrics = if (isRegression) {
      regressionMetrics.map(_.getName())
    } else {
      classificationMetrics.map(_.getName())
    }
    metrics.foreach(metricName => {
      outputSchemaBuilder.add(new Attribute(metricName, AttributeType.DOUBLE))
    })

    outputSchemaBuilder.build()
  }

//  private def getClassificationScorerName(scorer: classificationMetricsFnc): String = {
//    // Directly return the name of the scorer using the getName() method
//    scorer.getName()
//  }
//  private def getRegressionScorerName(scorer: regressionMetricsFnc): String = {
//    // Directly return the name of the scorer using the getName() method
//    scorer.getName()
//  }

  private def getMetricName(metric: Any): String =
    metric match {
      case m: regressionMetricsFnc     => m.getName()
      case m: classificationMetricsFnc => m.getName()
      case _                           => throw new IllegalArgumentException("Unknown metric type")
    }

  private def getSelectedMetrics(): String = {
    // Return a string of metrics using the getEachScorerName() method
    val metric = if (isRegression) regressionMetrics else classificationMetrics
    metric.map(metric => getMetricName(metric)).mkString("'", "','", "'")
  }

  override def generatePythonCode(): String = {
    val isRegressionStr = if (isRegression) "True" else "False"
    val finalcode =
      s"""
         |from pytexera import *
         |import pandas as pd
         |from sklearn.metrics import accuracy_score, precision_score, recall_score, f1_score, mean_squared_error, root_mean_squared_error, mean_absolute_error, r2_score
         |
         |def classification_metrics(y_true, y_pred, metric_list, labels):
         |  if 'Accuracy' in metric_list:
         |    labels.insert(0, 'Overall')
         |  result = {metric: [None] * len(labels) for metric in metric_list}
         |  metrics_func = {'Precision Score': precision_score, 'Recall Score': recall_score, 'F1 Score': f1_score}
         |
         |  for metric in metric_list:
         |    prediction = None
         |    if metric == 'Accuracy':
         |      result['Accuracy'][0] = accuracy_score(y_true, y_pred)
         |    else:
         |      for i, label in enumerate(labels):
         |        if label != 'Overall':
         |          prediction = metrics_func[metric](y_true, y_pred, average=None, labels=[label])
         |          result[metric][i] = prediction[0]
         |
         |  # if the label is not a string, convert it to string
         |  labels = ['class_' + str(label) if type(label) != str else label for label in labels]
         |  result['Class'] = labels
         |  result_df = pd.DataFrame(result)
         |
         |  return result_df
         |
         |
         |def regression_metrics(y_true, y_pred, metric_list):
         |  result = dict()
         |  metrics_func = {'MSE': mean_squared_error, 'RMSE': root_mean_squared_error, 'MAE': mean_absolute_error, 'R2': r2_score}
         |  for metric in metric_list:
         |    result[metric] = metrics_func[metric](y_true, y_pred)
         |
         |  result_df = pd.DataFrame(result, index=[0])
         |
         |  return result_df
         |
         |class ProcessTableOperator(UDFTableOperator):
         |
         |    @overrides
         |    def process_table(self, table: Table, port: int) -> Iterator[Optional[TableLike]]:
         |      y_true = table['$actualValueColumn']
         |      y_pred = table['$predictValueColumn']
         |
         |      metric_list = [${getSelectedMetrics()}]
         |
         |      if $isRegressionStr:
         |        result = regression_metrics(y_true, y_pred, metric_list)
         |      else:
         |        # calculate the number of unique labels
         |        labels = list(set(y_true))
         |        # align the type of y_true and y_pred(str)
         |        y_true_str = y_true.astype(str)
         |        result = classification_metrics(y_true_str, y_pred, metric_list, labels)
         |
         |      yield result
         |""".stripMargin
    finalcode
  }

}
