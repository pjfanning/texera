package edu.uci.ics.texera.workflow.common.operators
import com.fasterxml.jackson.annotation.{JsonProperty, JsonPropertyDescription}
import com.kjetland.jackson.jsonSchema.annotations.{JsonSchemaInject, JsonSchemaTitle}
import edu.uci.ics.texera.workflow.common.metadata.OperatorInfo
import edu.uci.ics.texera.workflow.common.tuple.schema.{Attribute, AttributeType, Schema}

trait MLOperatorDescriptor extends PythonOperatorDescriptor{
  override def getOutputSchema(schemas: Array[Schema]): Schema = {
    val outputSchemaBuilder = Schema.newBuilder
    if (isLoop) outputSchemaBuilder.add(new Attribute("Iteration", AttributeType.INTEGER))
    outputSchemaBuilder.add(new Attribute("model", AttributeType.BINARY))
    outputSchemaBuilder.add(new Attribute("para", AttributeType.BINARY))
    outputSchemaBuilder.add(new Attribute("features", AttributeType.BINARY)).build
  }

  var isLoop: Boolean
  var label: String
  var selectedFeatures: List[String]

  def importPackage(): String

  def trainingModelCustom(): String

  def trainingModelOptimization(): String

  override def generatePythonCode(): String = {
    var truthy = "False"
    if (isLoop) truthy = "True"
    val list_features = selectedFeatures.map(feature => s""""$feature"""").mkString(",")
    val finalcode =
      s"""
         |from pytexera import *
         |
         |import pandas as pd
         |import numpy as np
         |import pickle
         |${importPackage()}
         |
         |class ProcessTableOperator(UDFTableOperator):
         |
         |  @overrides
         |  def process_table(self, table: Table, port: int) -> Iterator[Optional[TableLike]]:
         |    global dataset
         |    model_list = []
         |    para_list = []
         |    features_list = []
         |    features = [$list_features]
         |
         |    if port == 0:
         |      dataset = table
         |
         |      if not ($truthy):
         |        ${trainingModelCustom()}
         |        model_list.append(model_str)
         |        para_list.append(para_str)
         |        features_list.append(features)
         |
         |      if ($truthy):
         |        ${trainingModelOptimization()}
         |          model_list.append(model_str)
         |          para_list.append(para_str)
         |          features_list.append(features)
         |
         |      data = dict({})
         |      data["model"]= model_list
         |      data["para"] = para_list
         |      data["features"]= features_list
         |
         |      df = pd.DataFrame(data)
         |      if ($truthy):
         |        df["Iteration"]= param["Iteration"]
         |      yield df
         |
         |""".stripMargin
    finalcode
  }
}
