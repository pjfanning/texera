package edu.uci.ics.texera.workflow.operators.machineLearning.SVCtrainer

import com.fasterxml.jackson.annotation.{JsonProperty, JsonPropertyDescription}
import com.kjetland.jackson.jsonSchema.annotations.{
  JsonSchemaBool,
  JsonSchemaInject,
  JsonSchemaString,
  JsonSchemaTitle
}
import edu.uci.ics.amber.engine.common.workflow.{InputPort, OutputPort, PortIdentity}
import edu.uci.ics.texera.workflow.common.metadata.annotations.{
  AutofillAttributeName,
  AutofillAttributeNameList,
  AutofillAttributeNameOnPort1,
  HideAnnotation
}
import edu.uci.ics.texera.workflow.common.metadata.{OperatorGroupConstants, OperatorInfo}
import edu.uci.ics.texera.workflow.common.operators.PythonOperatorDescriptor
import edu.uci.ics.texera.workflow.common.tuple.schema.{Attribute, AttributeType, Schema}
import edu.uci.ics.texera.workflow.operators.machineLearning.SVCtrainer.KernalFunction

class SVCtrainerOpDesc extends PythonOperatorDescriptor {
  @JsonProperty(defaultValue = "false")
  @JsonSchemaTitle("Using optimized C")
  @JsonSchemaInject(json = """{"toggleHidden" : ["loop_c","loop_kernal"]}""")
  var is_loop: Boolean = false

  @JsonProperty("Selected Features")
  @JsonSchemaTitle("Selected Features")
  @JsonPropertyDescription("Features used to train the model")
  @AutofillAttributeNameList
  var selectedFeatures: List[String] = _

  @JsonProperty(required = true)
  @JsonSchemaTitle("label Column")
  @AutofillAttributeName
  var label: String = ""

  @JsonProperty(required = true, defaultValue = "1")
  @JsonSchemaTitle("Custom C")
  @JsonPropertyDescription("Specify the number of nearest neighbours")
  @JsonSchemaInject(
    strings = Array(
      new JsonSchemaString(path = HideAnnotation.hideTarget, value = "is_loop"),
      new JsonSchemaString(path = HideAnnotation.hideType, value = HideAnnotation.Type.equals),
      new JsonSchemaString(path = HideAnnotation.hideExpectedValue, value = "true")
    )
  )
  val c: Float = Float.box(1.0f)

  @JsonProperty(value = "loop_c", required = false)
  @JsonSchemaTitle("Optimise c from loop")
  @JsonPropertyDescription("Specify which attribute indicates the value of c")
  @JsonSchemaInject(
    strings = Array(
      new JsonSchemaString(path = HideAnnotation.hideTarget, value = "is_loop"),
      new JsonSchemaString(path = HideAnnotation.hideType, value = HideAnnotation.Type.equals),
      new JsonSchemaString(path = HideAnnotation.hideExpectedValue, value = "false")
    )
  )
  @AutofillAttributeNameOnPort1
  var loop_c: String = ""

  @JsonProperty(value = "loop_kernal", required = false)
  @JsonSchemaTitle("Optimise kernal from loop")
  @JsonPropertyDescription("Specify which attribute indicates the value of kernal")
  @JsonSchemaInject(
    strings = Array(
      new JsonSchemaString(path = HideAnnotation.hideTarget, value = "is_loop"),
      new JsonSchemaString(path = HideAnnotation.hideType, value = HideAnnotation.Type.equals),
      new JsonSchemaString(path = HideAnnotation.hideExpectedValue, value = "false")
    )
  )
  @AutofillAttributeNameOnPort1
  var loop_kernal: String = ""

  @JsonProperty(required = true)
  @JsonSchemaTitle("Kernal Function")
  @JsonPropertyDescription("multiple kernal functions")
  @JsonSchemaInject(
    strings = Array(
      new JsonSchemaString(path = HideAnnotation.hideTarget, value = "is_loop"),
      new JsonSchemaString(path = HideAnnotation.hideType, value = HideAnnotation.Type.equals),
      new JsonSchemaString(path = HideAnnotation.hideExpectedValue, value = "true")
    )
  )
  var kernal: KernalFunction = KernalFunction.rbf

  @JsonProperty(required = true)
  @JsonSchemaTitle("gamma for SVC")
  @JsonPropertyDescription("gamma for SVC")
  @JsonSchemaInject(
    strings = Array(
      new JsonSchemaString(path = HideAnnotation.hideTarget, value = "kernal"),
      new JsonSchemaString(path = HideAnnotation.hideType, value = HideAnnotation.Type.regex),
      new JsonSchemaString(path = HideAnnotation.hideExpectedValue, value = "^linear$")
    ),
    bools = Array(
      new JsonSchemaBool(path = HideAnnotation.hideOnNull, value = true)
    )
  )
  var gamma: String = _

  @JsonProperty(required = true)
  @JsonSchemaTitle("coef for SVC")
  @JsonPropertyDescription("coef for SVC")
  @JsonSchemaInject(
    strings = Array(
      new JsonSchemaString(path = HideAnnotation.hideTarget, value = "kernal"),
      new JsonSchemaString(path = HideAnnotation.hideType, value = HideAnnotation.Type.regex),
      new JsonSchemaString(path = HideAnnotation.hideExpectedValue, value = "^linear$|^sigmod$")
    ),
    bools = Array(
      new JsonSchemaBool(path = HideAnnotation.hideOnNull, value = true)
    )
  )
  var coef: String = _

  override def getOutputSchema(schemas: Array[Schema]): Schema = {
    val outputSchemaBuilder = Schema.newBuilder
    outputSchemaBuilder.add(new Attribute("model", AttributeType.BINARY))
    outputSchemaBuilder.add(new Attribute("para", AttributeType.BINARY))
    outputSchemaBuilder.add(new Attribute("features", AttributeType.BINARY)).build
  }

  override def operatorInfo: OperatorInfo =
    OperatorInfo(
      "SVCTrainer",
      "Train a SVM classifier",
      OperatorGroupConstants.ML_GROUP,
      inputPorts = List(
        InputPort(
          PortIdentity(0),
          displayName = "dataset",
          allowMultiLinks = true,
          dependencies = List(PortIdentity(1))
        ),
        InputPort(PortIdentity(1), displayName = "parameter", allowMultiLinks = true)
      ),
      outputPorts = List(OutputPort())
    )

  override def generatePythonCode(): String = {
    var truthy = "False"
    if (is_loop) truthy = "True"
    assert(selectedFeatures.nonEmpty)
    val list_features = selectedFeatures.map(word => s""""$word"""").mkString(",")
    val finalcode =
      s"""
         |from pytexera import *
         |
         |import pandas as pd
         |import numpy as np
         |from sklearn.svm import SVC
         |import pickle
         |global para
         |
         |class ProcessTableOperator(UDFTableOperator):
         |
         |  @overrides
         |  def process_table(self, table: Table, port: int) -> Iterator[Optional[TableLike]]:
         |    global para
         |
         |    if port == 1:
         |      print("port0")
         |      print(table)
         |      if ($truthy):
         |        para = table
         |
         |    if port == 0:
         |      if not ($truthy):
         |        c = $c
         |        c = [c]
         |        c = pd.DataFrame({"para":c})
         |        c = c["para"]
         |        kernal_list = ["$kernal"]
         |        kernal_list = pd.DataFrame({"para":kernal_list})
         |        kernal_list = kernal_list["para"]
         |
         |      if ($truthy):
         |        c = para["$loop_c"]
         |        kernal_list = para["$loop_kernal"]
         |      y_train = table["$label"]
         |      features = [$list_features]
         |      X_train = table[features]
         |      model_list = []
         |      para_list = []
         |      for i in range(c.shape[0]):
         |        C= c[i]
         |        kernal  = kernal_list[i]
         |
         |        para_str = "kernal = '{}';C= {}".format(kernal,C)
         |        para_list.append(para_str)
         |        model = SVC(kernel=kernal,C=C)
         |        model.fit(X_train, y_train)
         |        model_p = pickle.dumps(model)
         |        model_list.append(model_p)
         |      data = dict()
         |      data["model"]= model_list
         |      data["para"] = para_list
         |      data["features"]= [features]*c.shape[0]
         |
         |      df = pd.DataFrame(data)
         |      yield df
         |
         |
         |""".stripMargin
    finalcode
  }

}