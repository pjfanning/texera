package edu.uci.ics.texera.workflow.operators.machineLearning.KNNTrainerOpDesc_old

import com.fasterxml.jackson.annotation.{JsonProperty, JsonPropertyDescription}
import com.kjetland.jackson.jsonSchema.annotations.{JsonSchemaInject, JsonSchemaString, JsonSchemaTitle}
import edu.uci.ics.amber.engine.common.workflow.{InputPort, OutputPort, PortIdentity}
import edu.uci.ics.texera.workflow.common.metadata.annotations.{AutofillAttributeName, AutofillAttributeNameList, AutofillAttributeNameOnPort1, HideAnnotation}
import edu.uci.ics.texera.workflow.common.metadata.{OperatorGroupConstants, OperatorInfo}
import edu.uci.ics.texera.workflow.common.operators.PythonOperatorDescriptor
import edu.uci.ics.texera.workflow.common.tuple.schema.{Attribute, AttributeType, Schema}

class KNNTrainerRegressionOpDescOld extends PythonOperatorDescriptor {
  @JsonProperty(defaultValue = "false")
  @JsonSchemaTitle("Using optimized K")
  @JsonSchemaInject(json = """{"toggleHidden" : ["loopK"]}""")
  @JsonPropertyDescription("Tune the parameter")
  var isLoop: Boolean = false

  @JsonProperty(required = true)
  @JsonSchemaTitle("Label Column")
  @JsonPropertyDescription("Label")
  @AutofillAttributeName
  var label: String = ""

  @JsonProperty(required = true, defaultValue = "3")
  @JsonSchemaTitle("Custom K")
  @JsonPropertyDescription("Specify the number of nearest neighbours")
  @JsonSchemaInject(
    strings = Array(
      new JsonSchemaString(path = HideAnnotation.hideTarget, value = "isLoop"),
      new JsonSchemaString(path = HideAnnotation.hideType, value = HideAnnotation.Type.equals),
      new JsonSchemaString(path = HideAnnotation.hideExpectedValue, value = "true")
    )
  )
  var k: Int = Int.box(1)

  @JsonProperty(value = "loopK", required = false)
  @JsonSchemaTitle("Optimise k from loop")
  @JsonPropertyDescription("Specify which attribute indicates the value of K")
  @JsonSchemaInject(
    strings = Array(
      new JsonSchemaString(path = HideAnnotation.hideTarget, value = "isLoop"),
      new JsonSchemaString(path = HideAnnotation.hideType, value = HideAnnotation.Type.equals),
      new JsonSchemaString(path = HideAnnotation.hideExpectedValue, value = "false")
    )
  )
  @AutofillAttributeNameOnPort1
  var loopK: String = ""

  @JsonProperty(value = "Selected Features", required = true)
  @JsonSchemaTitle("Selected Features")
  @JsonPropertyDescription("Features used to train the model")
  @AutofillAttributeNameList
  var selectedFeatures: List[String] = _

  override def getOutputSchema(schemas: Array[Schema]): Schema = {
    val outputSchemaBuilder = Schema.builder()
    if (isLoop) outputSchemaBuilder.add(new Attribute("Iteration", AttributeType.INTEGER))
    outputSchemaBuilder.add(new Attribute("Model", AttributeType.BINARY))
    outputSchemaBuilder.add(new Attribute("Parameters", AttributeType.BINARY))
    outputSchemaBuilder.add(new Attribute("Features", AttributeType.BINARY)).build
  }

  override def operatorInfo: OperatorInfo =
    OperatorInfo(
      "KNN Trainer Regression",
      "Train a KNN regression",
      OperatorGroupConstants.MODEL_TRAINING_GROUP,
      inputPorts = List(
        InputPort(
          PortIdentity(0),
          displayName = "dataset",
          allowMultiLinks = true
        ),
        InputPort(
          PortIdentity(1),
          displayName = "parameter",
          allowMultiLinks = true,
          dependencies = List(PortIdentity(0))
        )
      ),
      outputPorts = List(OutputPort())
    )

  override def generatePythonCode(): String = {
    var truthy = "False"
    if (isLoop) truthy = "True"
    val listFeatures = selectedFeatures.map(feature => s""""$feature"""").mkString(",")
    val finalCode =
      s"""
         |from pytexera import *
         |
         |import pandas as pd
         |import numpy as np
         |from sklearn.neighbors import KNeighborsRegressor
         |import pickle
         |
         |
         |class ProcessTableOperator(UDFTableOperator):
         |
         |  @overrides
         |  def process_table(self, table: Table, port: int) -> Iterator[Optional[TableLike]]:
         |    global dataset
         |    model_list = []
         |    para_list = []
         |    features_list = []
         |    features = [$listFeatures]
         |
         |    if port == 0:
         |      dataset = table
         |
         |    if port == 1:
         |      param = table
         |      table = dataset
         |      y_train = table["$label"]
         |      X_train = table[features]
         |      if not ($truthy):
         |        k = $k
         |        knn = KNeighborsRegressor(n_neighbors=k)
         |        knn.fit(X_train, y_train)
         |        para_str = "K = '{}'".format(k)
         |        model_str = pickle.dumps(knn)
         |        model_list.append(model_str)
         |        para_list.append(para_str)
         |        features_list.append(features)
         |
         |      if ($truthy):
         |        param = param.head(1)
         |        k = param["$loopK"].values
         |        for i in k:
         |          k = int(i)
         |          knn = KNeighborsRegressor(n_neighbors=k)
         |          knn.fit(X_train, y_train)
         |          para_str = "K = '{}'".format(k)
         |          model_str = pickle.dumps(knn)
         |          model_list.append(model_str)
         |          para_list.append(para_str)
         |          features_list.append(features)
         |
         |      data = dict({})
         |      data["Model"]= model_list
         |      data["Parameters"] = para_list
         |      data["Features"]= features_list
         |
         |      df = pd.DataFrame(data)
         |      if ($truthy):
         |        df["Iteration"]= param["Iteration"]
         |      yield df
         |
         |""".stripMargin
    finalCode
  }

}