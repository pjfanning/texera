package edu.uci.ics.texera.workflow.operators.machineLearning.Score_Loop

import com.fasterxml.jackson.annotation.{JsonProperty, JsonPropertyDescription}
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaTitle
import edu.uci.ics.amber.engine.common.workflow.{InputPort, OutputPort}
import edu.uci.ics.texera.workflow.common.metadata.annotations.AutofillAttributeName
import edu.uci.ics.texera.workflow.common.metadata.{OperatorGroupConstants, OperatorInfo}
import edu.uci.ics.texera.workflow.common.operators.PythonOperatorDescriptor
import edu.uci.ics.texera.workflow.common.tuple.schema.{Attribute, AttributeType, Schema}

class Scorer_LoopOpDesc extends PythonOperatorDescriptor {
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
  var scorer: Scorer_LoopFunction = _

  override def operatorInfo: OperatorInfo =
    OperatorInfo(
      "Scorer_Loop",
      "Scorer for machine learning classifier",
      OperatorGroupConstants.ML_GROUP,
      inputPorts = List(InputPort()),
      outputPorts = List(OutputPort())
    )

  override def generatePythonCode(): String = {
    val finalcode =
      s"""
         |from pytexera import *
         |import pandas as pd
         |import numpy as np
         |import pickle
         |from sklearn.metrics import accuracy_score
         |
         |
         |class ProcessTableOperator(UDFTableOperator):
         |
         |    @overrides
         |    def process_table(self, table: Table, port: int) -> Iterator[Optional[TableLike]]:
         |        acc= []
         |        y_test = table["$actualValue"]
         |        y_p_list = table["$predictValue"][0]
         |        para = table["para"][0]
         |        model = table["model"][0]
         |        for i in range(len(y_p_list)):
         |            y_p = y_p_list[i]
         |            scorer = "$scorer"
         |            if scorer == "Accuracy":
         |                accuracy = accuracy_score(y_test, y_p)
         |                acc.append(accuracy)
         |        data = dict()
         |        data["model"]=model
         |        data["Accuracy"]=acc
         |        data["para"] = para
         |        df = pd.DataFrame(data)
         |        yield df
         |""".stripMargin
    finalcode
  }

  override def getOutputSchema(schemas: Array[Schema]): Schema = {
    val outputSchemaBuilder = Schema.newBuilder
    outputSchemaBuilder.add(new Attribute(scorer.toString, AttributeType.DOUBLE))
    outputSchemaBuilder.add(new Attribute("model", AttributeType.BINARY))
    outputSchemaBuilder.add(new Attribute("para", AttributeType.STRING)).build

  }

}