package edu.uci.ics.texera.workflow.operators.machineLearning.SVCtrainer_Loop
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
import edu.uci.ics.texera.workflow.operators.machineLearning.SVCtrainer_Loop.KernalFunction
class SVCtrainerOpDesc_Loop extends PythonOperatorDescriptor {

  @JsonProperty(defaultValue = "false")
  @JsonSchemaTitle("Get parameters from workflow")
  @JsonSchemaInject(json = """{"toggleHidden" : ["loop_c","loop_kernal","loop_gamma","loop_coef"]}""")
  var is_loop: Boolean = false

  @JsonProperty(value = "Selected Features", required = true)
  @JsonSchemaTitle("Selected Features")
  @JsonPropertyDescription("Features used to train the model")
  @AutofillAttributeNameList
  var selectedFeatures: List[String] = _

  @JsonProperty(required = true)
  @JsonSchemaTitle("label Column")
  @AutofillAttributeName
  var label: String = ""

  @JsonProperty(required = false, defaultValue = "1")
  @JsonSchemaTitle("Custom C")
  @JsonPropertyDescription("Specify the value of 'c'")
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
  @JsonPropertyDescription("Specify which attribute is 'c'")
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
  @JsonPropertyDescription("Specify which attribute is 'kernal'")
  @JsonSchemaInject(
    strings = Array(
      new JsonSchemaString(path = HideAnnotation.hideTarget, value = "is_loop"),
      new JsonSchemaString(path = HideAnnotation.hideType, value = HideAnnotation.Type.equals),
      new JsonSchemaString(path = HideAnnotation.hideExpectedValue, value = "false")
    )
  )
  @AutofillAttributeNameOnPort1
  var loop_kernal: String = ""

  @JsonProperty(value = "loop_gamma", required = false)
  @JsonSchemaTitle("Optimise gamma from loop")
  @JsonPropertyDescription("Specify which attribute is 'gamma'")
  @JsonSchemaInject(
    strings = Array(
      new JsonSchemaString(path = HideAnnotation.hideTarget, value = "is_loop"),
      new JsonSchemaString(path = HideAnnotation.hideType, value = HideAnnotation.Type.equals),
      new JsonSchemaString(path = HideAnnotation.hideExpectedValue, value = "false")
    )
  )
  @AutofillAttributeNameOnPort1
  var loop_gamma: String = ""

  @JsonProperty(value = "loop_coef", required = false)
  @JsonSchemaTitle("Optimise coef from loop")
  @JsonPropertyDescription("Specify which attribute is 'coef'")
  @JsonSchemaInject(
    strings = Array(
      new JsonSchemaString(path = HideAnnotation.hideTarget, value = "is_loop"),
      new JsonSchemaString(path = HideAnnotation.hideType, value = HideAnnotation.Type.equals),
      new JsonSchemaString(path = HideAnnotation.hideExpectedValue, value = "false")
    )
  )
  @AutofillAttributeNameOnPort1
  val loop_coef: String = ""

  @JsonProperty(value = "Kernal Function", required = false,defaultValue ="linear")
  @JsonSchemaTitle("Kernal Function")
  @JsonPropertyDescription("multiple kernal functions")
  @JsonSchemaInject(
    strings = Array(
      new JsonSchemaString(path = HideAnnotation.hideTarget, value = "is_loop"),
      new JsonSchemaString(path = HideAnnotation.hideType, value = HideAnnotation.Type.equals),
      new JsonSchemaString(path = HideAnnotation.hideExpectedValue, value = "true")
    )
  )
  var kernal: KernalFunction = KernalFunction.linear

  @JsonProperty(value = "gamma for SVC")
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

  @JsonProperty(value="coef for SVC",required = false, defaultValue = "1")
  @JsonSchemaTitle("coef for SVC")
  @JsonPropertyDescription("coef for SVC")
  @JsonSchemaInject(
    strings = Array(
      new JsonSchemaString(path = HideAnnotation.hideTarget, value = "kernal"),
      new JsonSchemaString(path = HideAnnotation.hideType, value = HideAnnotation.Type.regex),
      new JsonSchemaString(path = HideAnnotation.hideExpectedValue, value = "^linear$|^sigmoid$|^rbf$")
    ),
    bools = Array(
      new JsonSchemaBool(path = HideAnnotation.hideOnNull, value = true)
    )
  )
  val coef: Float = Float.box(1.0f)

  override def getOutputSchema(schemas: Array[Schema]): Schema = {
    val outputSchemaBuilder = Schema.newBuilder
    outputSchemaBuilder.add(new Attribute("model", AttributeType.BINARY))
    outputSchemaBuilder.add(new Attribute("para", AttributeType.BINARY))
    outputSchemaBuilder.add(new Attribute("features", AttributeType.BINARY)).build
  }
  override def operatorInfo: OperatorInfo =
    OperatorInfo(
      "SVC Trainer Loop",
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
    val list_features = selectedFeatures.map(feature => s""""$feature"""").mkString(",")
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
         |      print(table)
         |      if ($truthy):
         |        para = table
         |
         |    if port == 0:
         |      if not ($truthy):
         |        c_list = np.array([$c])
         |        kernal_list = np.array(["$kernal"])
         |        kernal_type = "$kernal"
         |        if kernal_type in ['poly', 'rbf', 'sigmoid']:
         |          gamma_list = np.array([$gamma])
         |        if kernal_type in ['poly']:
         |          coef_list = np.array([$coef])
         |
         |      if ($truthy):
         |        c_list = para["$loop_c"]
         |        kernal_list = para["$loop_kernal"]
         |        gamma_list = para["$loop_gamma"]
         |        coef_list = para["$loop_coef"]
         |      y_train = table["$label"]
         |      features = [$list_features]
         |      X_train = table[features]
         |      model_list = []
         |      para_list = []
         |      for i in range(c_list.shape[0]):
         |        c_value= c_list[i]
         |        kernal_value  = kernal_list[i]
         |        if kernal_value in ['poly']:
         |          gamma_value = gamma_list[i]
         |          coef_value = coef_list[i]
         |          para_str = "kernal_value = '{}';c_value= {};gamma_value= {};coef_value= {}".format(kernal_value,c_value,gamma_value,coef_value)
         |          model = SVC(kernel=kernal_value,C=float(c_value),gamma=gamma_value,coef0=float(coef_value),probability=True)
         |        elif kernal_value in ['sigmoid','rbf']:
         |          gamma_value = gamma_list[i]
         |          para_str = "kernal_value = '{}';c_value= {};gamma_value= {}".format(kernal_value,c_value,gamma_value)
         |          model = SVC(kernel=kernal_value,C=float(c_value),gamma=gamma_value,probability=True)
         |        elif kernal_value in ['linear']:
         |          para_str = "kernal_value = '{}';c_value= {}".format(kernal_value,c_value)
         |          model = SVC(kernel=kernal_value,C=float(c_value),probability=True)
         |        model.fit(X_train, y_train)
         |        model_p = pickle.dumps(model)
         |        model_list.append(model_p)
         |        para_list.append(para_str)
         |
         |      data = dict()
         |      data["model"]= model_list
         |      data["para"] = para_list
         |      data["features"]= [features]*c_list.shape[0]
         |
         |      df = pd.DataFrame(data)
         |      yield df
         |
         |
         |""".stripMargin
    finalcode
  }
}