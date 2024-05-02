package edu.uci.ics.texera.workflow.operators.machineLearning.SVCTrainer
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
class SVCTrainerOpDescOld extends PythonOperatorDescriptor {
  @JsonProperty(defaultValue = "false")
  @JsonSchemaTitle("Get Parameters From Workflow")
  @JsonSchemaInject(json = """{"toggleHidden" : ["loopC","loopKernal","loopGamma","loopCoef"]}""")
  @JsonPropertyDescription("Tune the parameter")
  var isLoop: Boolean = false

  @JsonProperty(value = "Selected Features", required = true)
  @JsonSchemaTitle("Selected Features")
  @JsonPropertyDescription("Features used to train the model")
  @AutofillAttributeNameList
  var selectedFeatures: List[String] = _

  @JsonProperty(required = true)
  @JsonSchemaTitle("label Column")
  @JsonPropertyDescription("Label")
  @AutofillAttributeName
  var label: String = ""

  @JsonProperty(required = false, defaultValue = "1")
  @JsonSchemaTitle("Custom C")
  @JsonPropertyDescription("Specify the value of 'c'")
  @JsonSchemaInject(
    strings = Array(
      new JsonSchemaString(path = HideAnnotation.hideTarget, value = "isLoop"),
      new JsonSchemaString(path = HideAnnotation.hideType, value = HideAnnotation.Type.equals),
      new JsonSchemaString(path = HideAnnotation.hideExpectedValue, value = "true")
    )
  )
  val c: Float = Float.box(1.0f)

  @JsonProperty(value = "loopC", required = false)
  @JsonSchemaTitle("Optimise C From Loop")
  @JsonPropertyDescription("Specify which attribute is 'c'")
  @JsonSchemaInject(
    strings = Array(
      new JsonSchemaString(path = HideAnnotation.hideTarget, value = "isLoop"),
      new JsonSchemaString(path = HideAnnotation.hideType, value = HideAnnotation.Type.equals),
      new JsonSchemaString(path = HideAnnotation.hideExpectedValue, value = "false")
    )
  )
  @AutofillAttributeNameOnPort1
  var loopC: String = ""

  @JsonProperty(value = "loopKernal", required = false)
  @JsonSchemaTitle("Optimise kernal From Loop")
  @JsonPropertyDescription("Specify which attribute is 'kernal'")
  @JsonSchemaInject(
    strings = Array(
      new JsonSchemaString(path = HideAnnotation.hideTarget, value = "isLoop"),
      new JsonSchemaString(path = HideAnnotation.hideType, value = HideAnnotation.Type.equals),
      new JsonSchemaString(path = HideAnnotation.hideExpectedValue, value = "false")
    )
  )
  @AutofillAttributeNameOnPort1
  var loopKernal: String = ""

  @JsonProperty(value = "loopGamma", required = false)
  @JsonSchemaTitle("Optimise Gamma From Loop")
  @JsonPropertyDescription("Specify which attribute is 'gamma'")
  @JsonSchemaInject(
    strings = Array(
      new JsonSchemaString(path = HideAnnotation.hideTarget, value = "isLoop"),
      new JsonSchemaString(path = HideAnnotation.hideType, value = HideAnnotation.Type.equals),
      new JsonSchemaString(path = HideAnnotation.hideExpectedValue, value = "false")
    )
  )
  @AutofillAttributeNameOnPort1
  var loopGamma: String = ""

  @JsonProperty(value = "loopCoef", required = false)
  @JsonSchemaTitle("Optimise Coef From Loop")
  @JsonPropertyDescription("Specify which attribute is 'coef'")
  @JsonSchemaInject(
    strings = Array(
      new JsonSchemaString(path = HideAnnotation.hideTarget, value = "isLoop"),
      new JsonSchemaString(path = HideAnnotation.hideType, value = HideAnnotation.Type.equals),
      new JsonSchemaString(path = HideAnnotation.hideExpectedValue, value = "false")
    )
  )
  @AutofillAttributeNameOnPort1
  val loopCoef: String = ""

  @JsonProperty(required = false)
  @JsonSchemaTitle("Kernal Function")
  @JsonPropertyDescription("Multiple kernal functions")
  @JsonSchemaInject(
    strings = Array(
      new JsonSchemaString(path = HideAnnotation.hideTarget, value = "isLoop"),
      new JsonSchemaString(path = HideAnnotation.hideType, value = HideAnnotation.Type.equals),
      new JsonSchemaString(path = HideAnnotation.hideExpectedValue, value = "true")
    )
  )
  var kernal: KernelFunction = KernelFunction.linear

  @JsonProperty(value = "Gamma For SVC", defaultValue = "0.1")
  @JsonSchemaTitle("Gamma For SVC")
  @JsonPropertyDescription("Gamma for SVC")
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

  @JsonProperty(value = "Coef for SVC", required = false, defaultValue = "1")
  @JsonSchemaTitle("Coef For SVC")
  @JsonPropertyDescription("Coef for SVC")
  @JsonSchemaInject(
    strings = Array(
      new JsonSchemaString(path = HideAnnotation.hideTarget, value = "kernal"),
      new JsonSchemaString(path = HideAnnotation.hideType, value = HideAnnotation.Type.regex),
      new JsonSchemaString(
        path = HideAnnotation.hideExpectedValue,
        value = "^linear$|^sigmoid$|^rbf$"
      )
    ),
    bools = Array(
      new JsonSchemaBool(path = HideAnnotation.hideOnNull, value = true)
    )
  )
  val coef: Float = Float.box(1.0f)

  override def getOutputSchema(schemas: Array[Schema]): Schema = {
    val outputSchemaBuilder = Schema.builder()
    if (isLoop) outputSchemaBuilder.add(new Attribute("Iteration", AttributeType.INTEGER))
    outputSchemaBuilder.add(new Attribute("Model", AttributeType.BINARY))
    outputSchemaBuilder.add(new Attribute("Parameters", AttributeType.BINARY))
    outputSchemaBuilder.add(new Attribute("Features", AttributeType.BINARY)).build
  }
  override def operatorInfo: OperatorInfo =
    OperatorInfo(
      "SVC Trainer Old",
      "Train a SVM classifier",
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

  def buildParaStr():String = {
    s"""
       |        params = {'kernal_value': kernal_list[i], 'c_value': c_list[i], 'gamma_value': gamma_list[i],'coef_value':coef_list[i]}
       |        if kernal_value == 'linear':
       |          del params['gamma_value']
       |        if kernal_value in ['linear', 'sigmoid']:
       |          del params['coef_value']
       |        para_str = ";".join(["{} = {}".format(key, value) for key, value in params.items()])
       |""".stripMargin
  }

  override def generatePythonCode(): String = {
    var truthy = "False"
    if (isLoop) truthy = "True"
    assert(selectedFeatures.nonEmpty)
    val listFeatures = selectedFeatures.map(feature => s""""$feature"""").mkString(",")
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
         |      if not ($truthy):
         |        c_list = np.array([$c])
         |        kernal_list = np.array(["$kernal"])
         |        gamma_list = np.array([$gamma])
         |        coef_list = np.array([$coef])
         |
         |      if ($truthy):
         |        c_list = table["$loopC"].values
         |        kernal_list = table["$loopKernal"].values
         |        gamma_list = table["$loopGamma"].values
         |        coef_list = table["$loopCoef"].values
         |
         |      X_train = dataset[features]
         |      y_train = dataset["$label"]
         |
         |      for i in range(c_list.shape[0]):
         |        c_value = c_list[i]
         |        svc = SVC(kernel=kernal_list[i],C=float(c_list[i]),gamma=gamma_list[i],coef0=float(coef_list[i]),probability=True)
         |        svc.fit(X_train, y_train)
         |
         |        ${buildParaStr()}
         |        model_str = pickle.dumps(svc)
         |        model_list.append(model_str)
         |        para_list.append(para_str)
         |        features_list.append(features)
         |
         |      data = dict()
         |      data["Model"]= model_list
         |      data["Parameters"] = para_list
         |      data["Features"]= features_list
         |
         |      df = pd.DataFrame(data)
         |      if ($truthy):
         |        df["Iteration"]= table["Iteration"]
         |
         |      yield df
         |
         |""".stripMargin
    finalcode
  }
}