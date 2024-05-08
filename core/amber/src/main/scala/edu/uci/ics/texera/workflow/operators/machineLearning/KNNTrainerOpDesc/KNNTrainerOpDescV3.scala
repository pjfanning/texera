package edu.uci.ics.texera.workflow.operators.machineLearning.KNNTrainerOpDesc

import com.fasterxml.jackson.annotation.{JsonProperty, JsonPropertyDescription}
import com.kjetland.jackson.jsonSchema.annotations.{JsonSchemaInject, JsonSchemaString, JsonSchemaTitle}
import edu.uci.ics.amber.engine.common.workflow.{InputPort, OutputPort, PortIdentity}
import edu.uci.ics.texera.workflow.common.metadata.annotations.{AutofillAttributeName, AutofillAttributeNameList, AutofillAttributeNameOnPort1, HideAnnotation}
import edu.uci.ics.texera.workflow.common.metadata.{OperatorGroupConstants, OperatorInfo}
import edu.uci.ics.texera.workflow.common.operators.{SklearnMLOperatorDescriptorV2, SklearnMLOperatorDescriptorV3}

class KNNTrainerOpDescV3 extends SklearnMLOperatorDescriptorV3{
  model = "from sklearn.neighbors import KNeighborsClassifier"
  name = "K-nearest Neighbors"
  
  def addParamMap(): Map[String, Array[Any]] = {
    Map("n_neighbors" -> Array("k_list",k,loopK,"int"))
  }


  @JsonProperty(defaultValue = "false", required = false)
  @JsonSchemaTitle("Using Hyper Parameter Training")
  @JsonSchemaInject(json = """{"toggleHidden" : ["loopK"]}""")
  @JsonPropertyDescription("Tune the parameter K")
  override var parameterTuningFlag: Boolean = false

  @JsonProperty(required = true, defaultValue = "3")
  @JsonSchemaTitle("Custom K")
  @JsonPropertyDescription("Specify the number of nearest neighbours")
  @JsonSchemaInject(
    strings = Array(
      new JsonSchemaString(path = HideAnnotation.hideTarget, value = "parameterTuningFlag"),
      new JsonSchemaString(path = HideAnnotation.hideType, value = HideAnnotation.Type.equals),
      new JsonSchemaString(path = HideAnnotation.hideExpectedValue, value = "true")
    )
  )
  var k: Int = Int.box(1)

  @JsonProperty(required = false, value = "loopK")
  @JsonSchemaTitle("Optimise k from loop")
  @JsonPropertyDescription("Specify which attribute indicates the value of K")
  @JsonSchemaInject(
    strings = Array(
      new JsonSchemaString(path = HideAnnotation.hideTarget, value = "parameterTuningFlag"),
      new JsonSchemaString(path = HideAnnotation.hideType, value = HideAnnotation.Type.equals),
      new JsonSchemaString(path = HideAnnotation.hideExpectedValue, value = "false")
    )
  )
  @AutofillAttributeNameOnPort1
  var loopK: String = ""


}
