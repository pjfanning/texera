package edu.uci.ics.texera.workflow.operators.machineLearning.KNNtrainer

import com.fasterxml.jackson.annotation.{JsonProperty, JsonPropertyDescription}
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaTitle
import edu.uci.ics.amber.engine.common.workflow.{InputPort, OutputPort, PortIdentity}
import edu.uci.ics.texera.workflow.common.metadata.annotations.{AutofillAttributeName, AutofillAttributeNameOnPort1}
import edu.uci.ics.texera.workflow.common.metadata.{OperatorGroupConstants, OperatorInfo}
import edu.uci.ics.texera.workflow.common.operators.PythonOperatorDescriptor
import edu.uci.ics.texera.workflow.common.tuple.schema.{Attribute, AttributeType, Schema}

class KNNtrainerOpDesc extends PythonOperatorDescriptor {
    @JsonProperty(required = true)
    @JsonSchemaTitle("K")
    @JsonPropertyDescription("Specify how many nearest neighbours")
    var k: Int = Int.box(1)

    @JsonProperty(value = "loop_k", required = false, defaultValue = "k")
    @JsonSchemaTitle("If use loop")
    @JsonPropertyDescription("K from Loop")
    @AutofillAttributeNameOnPort1
    var loop_k: String = ""

    @JsonProperty(defaultValue = "false")
    @JsonSchemaTitle("Loop K")
    @JsonPropertyDescription("Do you need to use Loop K")
    var is_loop: Boolean = Boolean.box(false)

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
        inputPorts = List(
          InputPort(
            PortIdentity(0),
            displayName = "dataset",
            allowMultiLinks = true,
            dependencies = List(PortIdentity(1))
          ),
          InputPort(PortIdentity(1), displayName = "loop", allowMultiLinks = true),
        ),
        outputPorts = List(OutputPort())
      )

    override def generatePythonCode(): String = {
      var truthy = ""
      if (is_loop) truthy = "True"
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
           |    global k
           |
           |    if port == 1:
           |      print("port0")
           |      print(table)
           |      if ($truthy):
           |        k = table['$loop_k'][1]
           |
           |    if port == 0:
           |      print("port1")
           |      print(table)
           |      if not ($truthy):
           |        k = $k
           |      y_train = table["$label"]
           |      X_train = table.drop(["$label"], axis=1)
           |      knn = KNeighborsClassifier(n_neighbors=k+1)
           |      knn.fit(X_train, y_train)
           |      s = pickle.dumps(knn)
           |      yield {"model":s}
           |
           |    print(f'table{table}')
           |    print(f'k{k}')
           |
           |""".stripMargin
      finalcode
    }

}
