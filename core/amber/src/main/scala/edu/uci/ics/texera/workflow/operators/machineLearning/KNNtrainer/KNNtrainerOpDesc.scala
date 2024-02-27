package edu.uci.ics.texera.workflow.operators.machineLearning.KNNtrainer

import com.fasterxml.jackson.annotation.{JsonProperty, JsonPropertyDescription}
import com.kjetland.jackson.jsonSchema.annotations.{JsonSchemaInject, JsonSchemaString, JsonSchemaTitle}
import edu.uci.ics.amber.engine.common.workflow.{InputPort, OutputPort, PortIdentity}
import edu.uci.ics.texera.workflow.common.metadata.annotations.{AutofillAttributeName, AutofillAttributeNameList, AutofillAttributeNameOnPort1, HideAnnotation}
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
    @JsonPropertyDescription("Specify the number of nearest neighbours")
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
    @JsonPropertyDescription("Specify which attribute indicates the value of K")
    @JsonSchemaInject(
      strings = Array(
        new JsonSchemaString(path = HideAnnotation.hideTarget, value = "is_loop"),
        new JsonSchemaString(path = HideAnnotation.hideType, value = HideAnnotation.Type.equals),
        new JsonSchemaString(path = HideAnnotation.hideExpectedValue, value = "false")
      )
    )
    @AutofillAttributeNameOnPort1
    var loop_k: String = ""

    @JsonProperty(value = "Selected Features", required = true)
    @JsonSchemaTitle("Selected Features")
    @JsonPropertyDescription("Features used to train the model")
    @AutofillAttributeNameList
    var selectedFeatures: List[String] = _

    override def getOutputSchema(schemas: Array[Schema]): Schema = {
      val outputSchemaBuilder = Schema.newBuilder
      outputSchemaBuilder.add(new Attribute("model", AttributeType.BINARY))
      outputSchemaBuilder.add(new Attribute("para", AttributeType.BINARY))
      outputSchemaBuilder.add(new Attribute("features", AttributeType.BINARY)).build
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
          InputPort(PortIdentity(1), displayName = "parameter", allowMultiLinks = true),
        ),
        outputPorts = List(OutputPort())
      )

    override def generatePythonCode(): String = {
      var truthy = "False"
      if (is_loop) truthy = "True"
      val list_features = selectedFeatures.map(feature => s""""$feature"""").mkString(",")
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
           |    global param
           |    model_list = []
           |    para_list = []
           |    features_list = []
           |    features = [$list_features]
           |
           |    if port == 1:
           |      if ($truthy):
           |        param = table
           |
           |    if port == 0:
           |      y_train = table["$label"]
           |      X_train = table[features]
           |      if not ($truthy):
           |        k = $k
           |        knn = KNeighborsClassifier(n_neighbors=k)
           |        knn.fit(X_train, y_train)
           |        para_str = "K = '{}'".format(k)
           |        model_str = pickle.dumps(knn)
           |        model_list.append(model_str)
           |        para_list.append(para_str)
           |        features_list.append(features)
           |
           |      if ($truthy):
           |        k = param["$loop_k"].values
           |        for i in k:
           |          k = int(i)
           |          knn = KNeighborsClassifier(n_neighbors=k)
           |          knn.fit(X_train, y_train)
           |          para_str = "K = '{}'".format(k)
           |          model_str = pickle.dumps(knn)
           |          model_list.append(model_str)
           |          para_list.append(para_str)
           |          features_list.append(features)
           |
           |      data = dict({})
           |      data["model"]= model_list
           |      # print(model_list)
           |      data["para"] = para_list
           |      # print(para_list)
           |      data["features"]= features_list
           |      # print(features_list)
           |
           |      df = pd.DataFrame(data)
           |      yield df
           |
           |""".stripMargin
      finalcode
    }

}
