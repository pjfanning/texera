package edu.uci.ics.texera.workflow.operators.machineLearning.CategoryToNumber

import com.fasterxml.jackson.annotation.{JsonProperty, JsonPropertyDescription}
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaTitle
import edu.uci.ics.amber.engine.common.workflow.{InputPort, OutputPort}
import edu.uci.ics.texera.workflow.common.metadata.annotations.AutofillAttributeName
import edu.uci.ics.texera.workflow.common.metadata.{OperatorGroupConstants, OperatorInfo}
import edu.uci.ics.texera.workflow.common.operators.PythonOperatorDescriptor
import edu.uci.ics.texera.workflow.common.tuple.schema.{Attribute, AttributeType, Schema}
import edu.uci.ics.texera.workflow.operators.machineLearning.Scorer.ScorerFunction


class CategoryToNumberOpDesc extends PythonOperatorDescriptor {
  @JsonProperty(required = true)
  @JsonSchemaTitle("Column")
  @JsonPropertyDescription("Specify the label column")
  var selections: List[AttributeSectionList] = List()

  override def operatorInfo: OperatorInfo =
    OperatorInfo(
      "Category To Number",
      "Convert category to number",
      OperatorGroupConstants.ML_GROUP,
      inputPorts = List(InputPort()),
      outputPorts = List(OutputPort())
    )

  override def getOutputSchema(schemas: Array[Schema]): Schema = {
    val outputSchemaBuilder = Schema.newBuilder
    val inputSchema = schemas(0)
    outputSchemaBuilder.add(inputSchema)
    selections.map(item => outputSchemaBuilder.add(new Attribute(item.attributeName + "_to_number", AttributeType.INTEGER)))
    outputSchemaBuilder.add(new Attribute("label_encoder", AttributeType.BINARY))
    outputSchemaBuilder.build
  }

  private def getSelectedList(): String = {
    // Return a string of scorers using the getEachScorerName() method
    selections.map(item => item.attributeName).mkString("'", "','", "'")
  }

  override def generatePythonCode(): String = {
    val finalcode =
      s"""
         |from pytexera import *
         |import pandas as pd
         |import numpy as np
         |from sklearn.preprocessing import LabelEncoder
         |import json
         |import pickle
         |
         |class ProcessTableOperator(UDFTableOperator):
         |
         |    @overrides
         |    def process_table(self, table: Table, port: int) -> Iterator[Optional[TableLike]]:
         |
         |      attribute_list = [${getSelectedList()}]
         |
         |      label_model = []
         |
         |      for item in attribute_list:
         |        le = LabelEncoder()
         |        le.fit(table[item].values)
         |        result = le.transform(table[item].values)
         |
         |        table[item+'_to_number'] = result
         |        model = {}
         |        model['name'] = item
         |        model['label_encoder'] = le
         |        label_model.append(model)
         |
         |      table["label_encoder"] = pickle.dumps(label_model)
         |
         |      yield table
         |
         |""".stripMargin
    finalcode
  }

}