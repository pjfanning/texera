package edu.uci.ics.texera.workflow.operators.machineLearning.ApplyModel_Loop

import com.fasterxml.jackson.annotation.{JsonProperty, JsonPropertyDescription}
import com.google.common.base.Preconditions
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaTitle
import edu.uci.ics.amber.engine.common.workflow.{InputPort, OutputPort, PortIdentity}
import edu.uci.ics.texera.workflow.common.metadata.annotations.AutofillAttributeName
import edu.uci.ics.texera.workflow.common.metadata.{OperatorGroupConstants, OperatorInfo}
import edu.uci.ics.texera.workflow.common.operators.PythonOperatorDescriptor
import edu.uci.ics.texera.workflow.common.tuple.schema.{Attribute, AttributeType, Schema}

import scala.jdk.CollectionConverters.IterableHasAsJava

class ApplyModel_LoopOpDesc extends PythonOperatorDescriptor {


  @JsonProperty(required = true, defaultValue = "y_pred")
  @JsonSchemaTitle("Predict Column")
  @JsonPropertyDescription("Specify the table name of the predict data")
  var y_pred: String = ""

  override def operatorInfo: OperatorInfo =
    OperatorInfo(
      "Apply Models loops",
      "Apply Machine learning classifiers (scikit-learn)",
      OperatorGroupConstants.ML_GROUP,
      inputPorts = List(
        InputPort(
          PortIdentity(0),
          displayName = "dataset",
          allowMultiLinks = true,
          dependencies = List(PortIdentity(1))
        ),
        InputPort(PortIdentity(1), displayName = "model", allowMultiLinks = true)
      ),
      outputPorts = List(OutputPort())
    )

  override def getOutputSchema(schemas: Array[Schema]): Schema = {
    Preconditions.checkArgument(schemas.length == 2)
    val inputSchema = schemas(0)
    val outputSchemaBuilder = Schema.newBuilder
    outputSchemaBuilder.add(inputSchema)
    outputSchemaBuilder.add(new Attribute("para", AttributeType.BINARY))
    outputSchemaBuilder.add(new Attribute("features", AttributeType.BINARY))
    outputSchemaBuilder.add(new Attribute(y_pred, AttributeType.BINARY))
    outputSchemaBuilder.add(new Attribute("model", AttributeType.BINARY)).build
  }



  override def generatePythonCode(): String = {
    val finalCode =
      s"""
         |from pytexera import *
         |import pandas as pd
         |import numpy as np
         |import pickle
         |
         |global s
         |class ApplyModelOperator(UDFTableOperator):
         |
         |  @overrides
         |  def process_table(self, table: Table, port: int) -> Iterator[Optional[TableLike]]:
         |    global s
         |    if port == 1:
         |      s = table
         |
         |    if port ==0:
         |      f = s["features"][0]
         |      x_test = table[f]
         |      y_pred = []
         |      for i in range(s.shape[0]):
         |        model = pickle.loads(s["model"][i])
         |        y_predict = model.predict(x_test)
         |        y_pred.append(y_predict)
         |      y_p = [None]*table.shape[0]
         |      y_p[0] = y_pred
         |      table['$y_pred'] = y_p
         |      y_p[0] = s["model"]
         |      table["model"] = y_p
         |      y_p[0] = s["para"]
         |      table["para"] = y_p
         |      y_p[0] = s["features"][0]
         |      table["features"] = y_p
         |
         |      yield table
         |
         |""".stripMargin
    finalCode
  }

}