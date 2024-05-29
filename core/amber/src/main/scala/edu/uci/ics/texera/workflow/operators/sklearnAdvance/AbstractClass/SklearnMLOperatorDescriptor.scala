package edu.uci.ics.texera.workflow.operators.sklearnAdvance.AbstractClass

import com.fasterxml.jackson.annotation.{JsonIgnore, JsonProperty, JsonPropertyDescription}
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaTitle
import edu.uci.ics.amber.engine.common.workflow.{InputPort, OutputPort, PortIdentity}
import edu.uci.ics.texera.workflow.common.metadata.annotations.{
  AutofillAttributeName,
  AutofillAttributeNameList
}
import edu.uci.ics.texera.workflow.common.metadata.{OperatorGroupConstants, OperatorInfo}
import edu.uci.ics.texera.workflow.common.operators.PythonOperatorDescriptor
import edu.uci.ics.texera.workflow.common.tuple.schema.{Attribute, AttributeType, Schema}

trait EnumClass {
  def getName(): String
  def getType(): String
}

abstract class SklearnMLOperatorDescriptor[T <: EnumClass] extends PythonOperatorDescriptor {
  @JsonIgnore
  def getImportStatements(): String

  @JsonIgnore
  def getOperatorInfo(): String

  @JsonProperty(required = true)
  @JsonSchemaTitle("Parameter Setting")
  var paraList: List[HyperParameters[T]] = List()

  @JsonProperty(required = true)
  @JsonSchemaTitle("Ground Truth Attribute Column")
  @JsonPropertyDescription("Ground truth attribute column")
  @AutofillAttributeName
  var groundTruthAttribute: String = ""

  @JsonProperty(value = "Selected Features", required = true)
  @JsonSchemaTitle("Selected Features")
  @JsonPropertyDescription("Features used to train the model")
  @AutofillAttributeNameList
  var selectedFeatures: List[String] = _

  private def getLoopTimes(paraList: List[HyperParameters[T]]): String = {
    for (ele <- paraList) {
      if (ele.parametersSource) {
        return s"""table[\"${ele.attribute}\"].values.shape[0]"""
      }
    }
    "1"
  }

  def getParameter(paraList: List[HyperParameters[T]]): List[String] = {
    var str1 = ""; var str2 = ""; var str3 = ""
    for (ele <- paraList) {
      if (ele.parametersSource) {
        str1 = str1 + String.format("%s = {},", ele.parameter.getName())
        str2 =
          str2 + String.format("%s(table['%s'].values[i]),", ele.parameter.getType(), ele.attribute)
        str3 = str3 + String.format(
          "%s = %s(table['%s'].values[i]),",
          ele.parameter.getName(),
          ele.parameter.getType(),
          ele.attribute
        )
      } else {
        str1 = str1 + String.format("%s = {},", ele.parameter.getName())
        str2 = str2 + String.format("%s ('%s'),", ele.parameter.getType(), ele.value)
        str3 = str3 + String.format(
          "%s = %s ('%s'),",
          ele.parameter.getName(),
          ele.parameter.getType(),
          ele.value
        )
      }
    }
    List(String.format("\"%s\".format(%s)", str1, str2), str3)
  }

  override def generatePythonCode(): String = {
    val listFeatures = selectedFeatures.map(feature => s""""$feature"""").mkString(",")
    val trainingName = getImportStatements().split(" ").last
    val stringList = getParameter(paraList)
    val trainingParam = stringList(1)
    val paramString = stringList(0)
    val finalCode =
      s"""
         |from pytexera import *
         |
         |import pandas as pd
         |${getImportStatements()}
         |
         |class ProcessTableOperator(UDFTableOperator):
         |
         |  @overrides
         |  def process_table(self, table: Table, port: int) -> Iterator[Optional[TableLike]]:
         |    global dataset
         |    model_list = []
         |    para_list = []
         |    features_list =[]
         |    features = [$listFeatures]
         |
         |    if port == 0:
         |      dataset = table
         |
         |    if port == 1:
         |      y_train = dataset["$groundTruthAttribute"]
         |      X_train = dataset[features]
         |      loop_times = ${getLoopTimes(paraList)}
         |
         |      for i in range(loop_times):
         |        model = ${trainingName}(${trainingParam})
         |        model.fit(X_train, y_train)
         |
         |        para_str = ${paramString}
         |        para_list.append(para_str)
         |        features_list.append(features)
         |        model_list.append(model)
         |
         |      data = dict({})
         |      data["Model"]= model_list
         |      data["Parameters"] =para_list
         |      data["Features"] =features_list
         |
         |      df = pd.DataFrame(data)
         |      yield df
         |
         |""".stripMargin
    finalCode
  }

  override def operatorInfo: OperatorInfo = {
    val name = getOperatorInfo()
    OperatorInfo(
      name,
      "Sklearn " + name + " Operator",
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
  }

  override def getOutputSchema(schemas: Array[Schema]): Schema = {
    val outputSchemaBuilder = Schema.builder()
    outputSchemaBuilder.add(new Attribute("Parameters", AttributeType.STRING))
    outputSchemaBuilder.add(new Attribute("Model", AttributeType.BINARY))
    outputSchemaBuilder.add(new Attribute("Features", AttributeType.BINARY)).build()
  }
}
