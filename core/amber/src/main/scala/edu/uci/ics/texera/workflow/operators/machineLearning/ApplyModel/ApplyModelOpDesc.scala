package edu.uci.ics.texera.workflow.operators.machineLearning.ApplyModel

import com.fasterxml.jackson.annotation.{JsonProperty, JsonPropertyDescription}
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaTitle
import edu.uci.ics.amber.engine.common.workflow.{InputPort, OutputPort, PortIdentity}
import edu.uci.ics.texera.workflow.common.metadata.annotations.AutofillAttributeName
import edu.uci.ics.texera.workflow.common.metadata.{OperatorGroupConstants, OperatorInfo}
import edu.uci.ics.texera.workflow.common.operators.PythonOperatorDescriptor
import edu.uci.ics.texera.workflow.common.tuple.schema.{Attribute, AttributeType, Schema}
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

  override def generatePythonCode(): String = {
    val finalcode =
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
         |      #print("port1")
         |      #print(table)
         |      s = table["model"].values[0]
         |
         |    if port == 0:
         |      #print("port0")
         |      #print(table)
         |      y_test = table["$label"]
         |      X_test = table.drop(["$label"], axis=1)
         |      #print(s)
         |      model = pickle.loads(s)
         |      y_p = model.predict(X_test)
         |      table["y_pred"] = y_p
         |      yield table
         |
         |""".stripMargin
    finalcode
  }
}
