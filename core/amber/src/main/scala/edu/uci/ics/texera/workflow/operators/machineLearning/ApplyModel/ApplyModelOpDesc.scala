package edu.uci.ics.texera.workflow.operators.machineLearning.ApplyModel

import com.fasterxml.jackson.annotation.{JsonProperty, JsonPropertyDescription}
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaTitle
import edu.uci.ics.amber.engine.common.workflow.{InputPort, OutputPort, PortIdentity}
import edu.uci.ics.texera.workflow.common.metadata.annotations.AutofillAttributeName
import edu.uci.ics.texera.workflow.common.metadata.{OperatorGroupConstants, OperatorInfo}
import edu.uci.ics.texera.workflow.common.operators.PythonOperatorDescriptor
import edu.uci.ics.texera.workflow.common.tuple.schema.{Attribute, Schema}

import scala.jdk.CollectionConverters.IterableHasAsJava
import com.google.common.base.Preconditions

class ApplyModelOpDesc extends PythonOperatorDescriptor {

  @JsonProperty(required = true)
  @JsonSchemaTitle("label Column")
  @JsonPropertyDescription("Specify the attribute to be predicted")
  @AutofillAttributeName
  var label: String = ""

  @JsonProperty
  @JsonSchemaTitle("Extra output column(s)")
  @JsonPropertyDescription(
    "Name of the newly added output columns that the UDF will produce, if any"
  )
  var outputColumns: List[Attribute] = List()

  override def getOutputSchema(schemas: Array[Schema]): Schema = {
    Preconditions.checkArgument(schemas.length == 2)
    val inputSchema = schemas(0)
    val outputSchemaBuilder = Schema.newBuilder
    // keep the same schema from input
    outputSchemaBuilder.add(inputSchema)
    // for any pythonUDFType, it can add custom output columns (attributes).

    for (column <- outputColumns) {
      if (inputSchema.containsAttribute(column.getName))
        throw new RuntimeException("Column name " + column.getName + " already exists!")
    }
    outputSchemaBuilder.add(outputColumns.asJava).build
  }
  
  @JsonProperty(required = true, defaultValue = "y_pred")
  @JsonSchemaTitle("Predict Column")
  @JsonPropertyDescription("Specify the table name of the predict data")
  var y_pred: String = ""

  override def operatorInfo: OperatorInfo =
    OperatorInfo(
      "Apply Models",
      "Apply Machine learning model (scikit-learn)",
      OperatorGroupConstants.ML_GROUP,
      inputPorts = List(
        InputPort(
          PortIdentity(0),
          displayName = "tuples",
          allowMultiLinks = true,
          dependencies = List(PortIdentity(1))
        ),
        InputPort(PortIdentity(1), displayName = "model", allowMultiLinks = true)
      ),
      outputPorts = List(OutputPort())
    )

<<<<<<< HEAD
=======
  override def getOutputSchema(schemas: Array[Schema]): Schema = {
    Preconditions.checkArgument(schemas.length == 2)
    val inputSchema = schemas(0)
    val outputSchemaBuilder = Schema.newBuilder
    outputSchemaBuilder.add(inputSchema)
    var outputColumns: List[Attribute] = getPredictTableName(inputSchema)
    for (column <- outputColumns) {
      if (inputSchema.containsAttribute(column.getName))
        throw new RuntimeException("Column name " + column.getName + " already exists!")
    }
    outputSchemaBuilder.add(outputColumns.asJava).build
  }

  private def getPredictTableName(inputSchema: Schema): List[Attribute] = {
    val attrType = inputSchema.getAttribute(label).getType
    val y_pred_list: List[Attribute] = List(new Attribute(y_pred, attrType))
    y_pred_list
  }

>>>>>>> 77264604659e28e76ca534f39e9572aa122a4555
  override def generatePythonCode(): String = {
    val finalCode =
      s"""
         |from pytexera import *
         |
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
         |      s = table["model"].values[0]
         |
<<<<<<< HEAD
         |    if port == 0:
         |      #print("port0")
         |      #print(table)
=======
         |    if port ==0:
>>>>>>> 77264604659e28e76ca534f39e9572aa122a4555
         |      y_test = table["$label"]
         |      X_test = table.drop(["$label"], axis=1)
         |      model = pickle.loads(s)
         |      y_predict = model.predict(X_test)
         |      table["$y_pred"] = y_predict
         |      yield table
         |
         |""".stripMargin
    finalCode
  }
}
