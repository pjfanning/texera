package edu.uci.ics.texera.workflow.operators.machineLearning.Scorer

import com.fasterxml.jackson.annotation.{JsonProperty, JsonPropertyDescription}
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
  @JsonPropertyDescription("Specify the attribute general by the model")
  @AutofillAttributeName
  var predictValue: String = ""

  @JsonProperty(required = true)
  @JsonSchemaTitle("scorer")
  @JsonPropertyDescription("multiple score functions")
  var scorer: ScorerFunction = _

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
         |import pandas as pd
         |import numpy as np
         |
         |class ProcessTableOperator(UDFTableOperator):
         |   @overrides
         |   def process_table(self, table: Table, port: int) -> Iterator[Optional[TableLike]]:
         |       y_true = table['$actualValue']
         |       y_pred = table['$predictValue']
         |
         |       scorer = "$scorer"
         |       if scorer == "Accuracy":
         |          correct_predictions = accuracy_score(y_true, y_pred)
         |
         |       yield {"Accuracy":correct_predictions}
         |
         |""".stripMargin
    finalcode
  }

  override def getOutputSchema(schemas: Array[Schema]): Schema = {
    Schema.newBuilder.add(new Attribute("Accuracy", AttributeType.DOUBLE)).build
  }

}