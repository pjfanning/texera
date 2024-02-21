package edu.uci.ics.texera.workflow.operators.machineLearning.Scorer

import com.fasterxml.jackson.annotation.{JsonIgnore, JsonProperty, JsonPropertyDescription}
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaTitle
import edu.uci.ics.amber.engine.common.workflow.{InputPort, OutputPort}
import edu.uci.ics.texera.workflow.common.metadata.{OperatorGroupConstants, OperatorInfo}
import edu.uci.ics.texera.workflow.common.metadata.annotations.AutofillAttributeName
import edu.uci.ics.texera.workflow.common.operators.PythonOperatorDescriptor
import edu.uci.ics.texera.workflow.common.tuple.schema.{Attribute, AttributeType, Schema}

class ScorerOpDesc extends PythonOperatorDescriptor {
  @JsonProperty(required = true)
  @JsonSchemaTitle("Actual Value")
  @JsonPropertyDescription("Specify the attribute to be predicted")
  @AutofillAttributeName
  var actualValue: String = ""

  @JsonProperty(required = true)
  @JsonSchemaTitle("Predicted Value")
  @JsonPropertyDescription("Specify the attribute generated by the model")
  @AutofillAttributeName
  var predictValue: String = ""

  @JsonProperty(required = true)
  @JsonSchemaTitle("Scorer Functions")
  @JsonPropertyDescription("Select multiple score functions")
  var scorers: List[ScorerFunction] = List()

  override def operatorInfo: OperatorInfo =
    OperatorInfo(
      "Scorer",
      "Scorer for machine learning model",
      OperatorGroupConstants.ML_GROUP,
      inputPorts = List(InputPort()),
      outputPorts = List(OutputPort())
    )

  override def generatePythonCode(): String = {
    val finalcode =
      s"""
         |from pytexera import *
         |
         |from sklearn.metrics import accuracy_score
         |from sklearn.metrics import precision_score
         |from sklearn.metrics import confusion_matrix
         |import pandas as pd
         |import json
         |import pickle
         |
         |class ProcessTableOperator(UDFTableOperator):
         |   @overrides
         |   def process_table(self, table: Table, port: int) -> Iterator[Optional[TableLike]]:
         |       y_true = table['$actualValue']
         |       y_pred = table['$predictValue']
         |
         |       scorerList = [${getSelectedScorers()}]
         |       # print(scorerList)
         |       result = dict()
         |
         |       for scorer in scorerList:
         |           prediction = None
         |           if scorer == 'Accuracy':
         |               prediction = accuracy_score(y_true, y_pred)
         |               result['Accuracy'] = prediction
         |               # print('Accuracy', prediction)
         |           elif scorer == 'Precision Score':
         |               prediction = precision_score(y_true, y_pred, average = 'macro')
         |               result['Precision Score'] = prediction
         |               # print('Precision Score', prediction)
         |           elif scorer == 'Confusion Matrix':
         |               column_set = set(y_true)
         |               labels = list(column_set)
         |               prediction = confusion_matrix(y_true, y_pred, labels = labels)
         |               # print('''Confusion Matrix
         |               # ''', prediction)
         |               prediction = json.dumps(prediction.tolist(), indent = 4)
         |               result['Confusion Matrix'] = prediction
         |
         |       df = pd.DataFrame(result, index=[0])
         |       yield df
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
        } else {
          outputSchemaBuilder.add(new Attribute(scorer, AttributeType.DOUBLE))
        }
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