package edu.uci.ics.texera.workflow.operators.machineLearning.KNNtrainer

import com.fasterxml.jackson.annotation.{JsonProperty, JsonPropertyDescription}
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaTitle
import edu.uci.ics.amber.engine.common.workflow.{InputPort, OutputPort}
import edu.uci.ics.texera.workflow.common.metadata.annotations.AutofillAttributeName
import edu.uci.ics.texera.workflow.common.metadata.{OperatorGroupConstants, OperatorInfo}
import edu.uci.ics.texera.workflow.common.operators.PythonOperatorDescriptor
import edu.uci.ics.texera.workflow.common.tuple.schema.{Attribute, AttributeType, Schema}




class KNNtrainerOpDesc extends PythonOperatorDescriptor {


    @JsonProperty(required = true)
    @JsonSchemaTitle("K")
    @JsonPropertyDescription("Specify how many nearest neighbours")
    var k: Int = Int.box(1)

    @JsonProperty(required = true)
    @JsonSchemaTitle("label Column")
    @JsonPropertyDescription("Specify the attribute to be predicted")
    @AutofillAttributeName
    var label: String = ""







  override def getOutputSchema(schemas: Array[Schema]): Schema = {
    Schema.newBuilder.add(new Attribute("model", AttributeType.BINARY)).build
  }


    override def operatorInfo: OperatorInfo =
      OperatorInfo(
        "KNNtrainer",
        "Train a KNN classifier",
        OperatorGroupConstants.ML_GROUP,
        inputPorts = List(InputPort()),
        outputPorts = List(OutputPort())
      )

    //  def manipulateTable(): String = {
    //    assert(value.nonEmpty)
    //    s"""
    //       |        table.dropna(subset = ['$value', '$name'], inplace = True) #remove missing values
    //       |""".stripMargin
    //  }





  override def generatePythonCode(): String = {
    val finalcode =
      s"""
         |from pytexera import *
         |
         |import pandas as pd
         |import numpy as np
         |from sklearn.neighbors import KNeighborsClassifier
         |import pickle
         |
         |
         |class ProcessTableOperator(UDFTableOperator):
         |
         |  @overrides
         |  def process_table(self, table: Table, port: int) -> Iterator[Optional[TableLike]]:
         |    k =$k
         |
         |    y_train = table["$label"]
         |    X_train = table.drop(["$label"], axis=1)
         |
         |    knn = KNeighborsClassifier(n_neighbors=k)
         |    knn.fit(X_train, y_train)
         |    s =  pickle.dumps(knn)
         |    yield {"model":s}
         |
         |""".stripMargin
    finalcode
  }

  }
