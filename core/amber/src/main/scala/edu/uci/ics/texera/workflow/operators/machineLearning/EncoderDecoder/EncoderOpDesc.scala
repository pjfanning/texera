package edu.uci.ics.texera.workflow.operators.machineLearning.EncoderDecoder

import com.fasterxml.jackson.annotation.{JsonProperty, JsonPropertyDescription}
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaTitle
import edu.uci.ics.amber.engine.common.workflow.{InputPort, OutputPort}
import edu.uci.ics.texera.workflow.common.metadata.{OperatorGroupConstants, OperatorInfo}
import edu.uci.ics.texera.workflow.common.metadata.annotations.AutofillAttributeNameList
import edu.uci.ics.texera.workflow.common.operators.PythonOperatorDescriptor
import edu.uci.ics.texera.workflow.common.tuple.schema.{Attribute, AttributeType, Schema}

class EncoderOpDesc extends PythonOperatorDescriptor{

  @JsonProperty(value = "Selected Features", required = true)
  @JsonSchemaTitle("Selected Features")
  @JsonPropertyDescription("Features used to train the model")
  @AutofillAttributeNameList
  var selectedColumns: List[String] = _
  override def operatorInfo: OperatorInfo = OperatorInfo(
    "Label Encoder",
    "Convert category to number",
    OperatorGroupConstants.PREPROCESSING_GROUP,
    inputPorts = List(InputPort()),
    outputPorts = List(OutputPort())
  )

  override def getOutputSchema(schemas: Array[Schema]): Schema = {
    val outputSchemaBuilder = Schema.builder();
    val inputSchema = schemas(0)
    outputSchemaBuilder.add(inputSchema)
    selectedColumns.foreach(col => {
      outputSchemaBuilder.removeIfExists(col)
      outputSchemaBuilder.add(new Attribute(col, AttributeType.INTEGER))
    })
    outputSchemaBuilder.build()
  }
  override def generatePythonCode(): String = {
    val selected_columns = selectedColumns.map(col => s""""$col"""").mkString(",")
    val finalcode =
      s"""
         |from pytexera import *
         |
         |import pandas as pd
         |import numpy as np
         |from sklearn.preprocessing import OneHotEncoder
         |from sklearn.preprocessing import LabelEncoder
         |import pickle
         |
         |
         |class ProcessTableOperator(UDFTableOperator):
         |
         |  @overrides
         |  def process_table(self, table: Table, port: int) -> Iterator[Optional[TableLike]]:
         |    dataset = table
         |    columns = [$selected_columns]
         |
         |    result= dict()
         |    result['column_name'] = []
         |    result['encoder'] = []
         |
         |    for column in columns:
         |      le = LabelEncoder()
         |      if dataset[column].isnull().any():
         |        mode_value = dataset[column].mode()[0]
         |        dataset[column].fillna(mode_value, inplace=True)
         |        #print(f"Column {column} has missing values, fill with mode value {mode_value}")
         |
         |      dataset[column] = le.fit_transform(dataset[column])
         |      binary_encoder = pickle.dumps(le)
         |      result['column_name'].append(column)
         |      result['encoder'].append(binary_encoder)
         |
         |    #print(result)
         |    yield dataset
         |
         |
         |""".stripMargin
    finalcode
  }

}
