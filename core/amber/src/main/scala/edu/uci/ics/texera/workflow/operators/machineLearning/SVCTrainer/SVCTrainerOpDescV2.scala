package edu.uci.ics.texera.workflow.operators.machineLearning.SVCTrainer

import com.fasterxml.jackson.annotation.{JsonProperty, JsonPropertyDescription}
import com.kjetland.jackson.jsonSchema.annotations.{JsonSchemaBool, JsonSchemaInject, JsonSchemaString, JsonSchemaTitle}
import edu.uci.ics.amber.engine.common.workflow.{InputPort, OutputPort, PortIdentity}
import edu.uci.ics.texera.workflow.common.metadata.{OperatorGroupConstants, OperatorInfo}
import edu.uci.ics.texera.workflow.common.metadata.annotations.{AutofillAttributeName, AutofillAttributeNameList, AutofillAttributeNameOnPort1, HideAnnotation}
import edu.uci.ics.texera.workflow.common.operators.SklearnMLOperatorDescriptorV2

class SVCTrainerOpDescV2 extends SklearnMLOperatorDescriptorV2{
  @JsonProperty(defaultValue = "false")
  @JsonSchemaTitle("Get Parameters From Workflow")
  @JsonSchemaInject(json = """{"toggleHidden" : ["loopC","loopKernal","loopGamma","loopCoef"]}""")
  @JsonPropertyDescription("Tune the parameter")
  override var parameterTuningFlag: Boolean = false

  @JsonProperty(required = true)
  @JsonSchemaTitle("Ground Truth Attribute Column")
  @JsonPropertyDescription("Ground truth attribute column")
  @AutofillAttributeName
  override var groundTruthAttribute: String = ""

  @JsonProperty(value = "Selected Features", required = true)
  @JsonSchemaTitle("Selected Features")
  @JsonPropertyDescription("Features used to train the model")
  @AutofillAttributeNameList
  override var selectedFeatures: List[String] = _

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
  var loopCoef: String = ""

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
  var kernal: KernalFunction = KernalFunction.linear

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
  var coef: Float = Float.box(1.0f)

  override def operatorInfo: OperatorInfo =
    OperatorInfo(
      "SVC Trainer V2",
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
  override def addImportMap(): Map[String, String] = {
    val importMap = Map("sklearn.svm" -> "SVC")
    importMap
  }

  override def addParamMap(): Map[String, Array[Any]] = {
    var paramMap = Map(
      "C" -> Array("c_list",c,loopC,"float"),
    )
    paramMap += ("kernel" -> Array("kernal_list",kernal,loopKernal,"str"))
    paramMap += ("gamma" -> Array("gamma_list",gamma,loopGamma,"float"))
    paramMap += ("coef0" -> Array("coef_list",coef,loopCoef,"float"))
    paramMap
  }

}
