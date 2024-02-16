package edu.uci.ics.texera.workflow.operators.machineLearning.KNNtrainer

import com.fasterxml.jackson.annotation.{JsonProperty, JsonPropertyDescription}
import com.kjetland.jackson.jsonSchema.annotations.{JsonSchemaInject, JsonSchemaString, JsonSchemaTitle}
import edu.uci.ics.amber.engine.common.workflow.{InputPort, OutputPort, PortIdentity}
import edu.uci.ics.texera.workflow.common.metadata.annotations.{AutofillAttributeName, AutofillAttributeNameOnPort1, HideAnnotation}
import edu.uci.ics.texera.workflow.common.metadata.{OperatorGroupConstants, OperatorInfo}
import edu.uci.ics.texera.workflow.common.operators.PythonOperatorDescriptor
import edu.uci.ics.texera.workflow.common.tuple.schema.{Attribute, AttributeType, Schema}

class KNNtrainerOpDesc extends PythonOperatorDescriptor {
    @JsonProperty(defaultValue = "false")
    @JsonSchemaTitle("Using optimized K")
    @JsonSchemaInject(json = """{"toggleHidden" : ["loop_k"]}""")
    var is_loop: Boolean = false

    @JsonProperty(required = true)
    @JsonSchemaTitle("label Column")
    @AutofillAttributeName
    var label: String = ""

    @JsonProperty(required = true,defaultValue = "3")
    @JsonSchemaTitle("Custom K")
    @JsonPropertyDescription("Specify the nearest neighbours")
    @JsonSchemaInject(
      strings = Array(
        new JsonSchemaString(path = HideAnnotation.hideTarget, value = "is_loop"),
        new JsonSchemaString(path = HideAnnotation.hideType, value = HideAnnotation.Type.equals),
        new JsonSchemaString(path = HideAnnotation.hideExpectedValue, value = "true")
      )
    )
    var k: Int = Int.box(1)

    @JsonProperty(value = "loop_k", required = false)
    @JsonSchemaTitle("Optimise k from loop")
    @JsonPropertyDescription("Specify how many nearest neighbours")
    @JsonSchemaInject(
      strings = Array(
        new JsonSchemaString(path = HideAnnotation.hideTarget, value = "is_loop"),
        new JsonSchemaString(path = HideAnnotation.hideType, value = HideAnnotation.Type.equals),
        new JsonSchemaString(path = HideAnnotation.hideExpectedValue, value = "false")
      )
    )
    @AutofillAttributeNameOnPort1
    var loop_k: String = ""

    override def getOutputSchema(schemas: Array[Schema]): Schema = {
      Schema.newBuilder.add(new Attribute("model", AttributeType.BINARY)).build
    }


    override def operatorInfo: OperatorInfo =
      OperatorInfo(
        "KNNTrainer",
        "Train a KNN classifier",
        OperatorGroupConstants.ML_GROUP,
        inputPorts = List(
          InputPort(
            PortIdentity(0),
            displayName = "dataset",
            allowMultiLinks = true,
            dependencies = List(PortIdentity(1))
          ),
          InputPort(PortIdentity(1), displayName = "optimization", allowMultiLinks = true),
        ),
        outputPorts = List(OutputPort())
      )

    override def generatePythonCode(): String = {
      var truthy = "False"
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
           |      if ($truthy):
           |        k = table['$loop_k'][1]
           |
           |    if port == 0:
           |      if not ($truthy):
           |        k = $k
           |        print(f'k={k}')
           |      y_train = table["$label"]
           |      X_train = table.drop(["$label"], axis=1)
           |      knn = KNeighborsClassifier(n_neighbors=k+1)
           |      knn.fit(X_train, y_train)
           |      s = pickle.dumps(knn)
           |      yield {"model":s}
           |
           |
           |""".stripMargin
      finalcode
    }

}
