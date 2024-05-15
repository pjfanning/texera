package edu.uci.ics.texera.workflow.operators.machineLearning.Standardization
import com.fasterxml.jackson.annotation.{JsonProperty, JsonPropertyDescription}
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaTitle
import edu.uci.ics.amber.engine.common.workflow.{InputPort, OutputPort}
import edu.uci.ics.texera.workflow.common.metadata.{OperatorGroupConstants, OperatorInfo}
import edu.uci.ics.texera.workflow.common.metadata.annotations.AutofillAttributeNameList
import edu.uci.ics.texera.workflow.common.operators.PythonOperatorDescriptor
import edu.uci.ics.texera.workflow.common.tuple.schema.{Attribute, AttributeType, Schema}

class ScalerOpDesc extends PythonOperatorDescriptor{

  @JsonProperty(value = "Selected Features", required = true)
  @JsonSchemaTitle("Selected Features")
  @JsonPropertyDescription("Features used to train the model")
  @AutofillAttributeNameList
  var selectedColumns: List[String] = _
  override def operatorInfo: OperatorInfo = OperatorInfo(
    "Scaler",
    "Standardize numerical features between 0 and 1",
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
      outputSchemaBuilder.add(new Attribute(col, AttributeType.DOUBLE))
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
         |from sklearn.preprocessing import StandardScaler
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
         |    result['scaler'] = []
         |
         |    for column in columns:
         |      ss = StandardScaler()
         |      if dataset[column].isnull().any():
         |        median_value = dataset[column].median()
         |        dataset[column].fillna(median_value, inplace=True)
         |        print(f"Column {column} has missing values, fill with median value {median_value}")
         |
         |      dataset[column] = ss.fit_transform(dataset[[column]])
         |      binary_scaler = pickle.dumps(ss)
         |      result['column_name'].append(column)
         |      result['scaler'].append(binary_scaler)
         |
         |    print(result)
         |    yield dataset
         |
         |
         |""".stripMargin
    finalcode
  }

}
