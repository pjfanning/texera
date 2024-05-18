package edu.uci.ics.texera.workflow.operators.machineLearning.AbstractClass

import com.fasterxml.jackson.annotation.{JsonIgnore, JsonProperty, JsonPropertyDescription}
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaTitle
import edu.uci.ics.amber.engine.common.workflow.{InputPort, OutputPort, PortIdentity}
import edu.uci.ics.texera.workflow.common.metadata.annotations.{AutofillAttributeName, AutofillAttributeNameList}
import edu.uci.ics.texera.workflow.common.metadata.{OperatorGroupConstants, OperatorInfo}
import edu.uci.ics.texera.workflow.common.operators.PythonOperatorDescriptor
import edu.uci.ics.texera.workflow.common.tuple.schema.{Attribute, AttributeType, Schema}

trait AbstractEnumClass {
  def getName(): String
  def getType(): String
}

abstract class SklearnMLOperatorDescriptor[T <: AbstractEnumClass] extends PythonOperatorDescriptor {
  @JsonIgnore
  def getImportStatements():String

  @JsonIgnore
  def getOperatorInfo():String

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

  private def numberStatements(paraList:List[HyperParameters[T]]) : String= {
    for (ele<-paraList){
      if (ele.parametersSource == ParametersSource.workflow){
        return s"""table[\"${ele.attribute}\"].values.shape[0]"""
      }else{
        return "1"
      }
    }
    ""
  }

  def writeParameterStatements(paraList:List[HyperParameters[T]]): String =  {
    var s =""
    for  (ele<-paraList){
      if (ele.parametersSource == ParametersSource.workflow){
        s = s +String.format("%s = %s(table['%s'].values[i]),",ele.parameter.getName() ,ele.parameter.getType(),ele.attribute )
      }
      else {
        s = s +String.format("%s = %s ('%s'),",ele.parameter.getName() ,ele.parameter.getType(),ele.value)
      }
    }
    s
  }

  def writeParameterString(paraList:List[HyperParameters[T]]): String =  {
    var s1 =""
    var s2 = ""
    for  (ele<-paraList){
      if (ele.parametersSource == ParametersSource.workflow){
        s1 = s1 +String.format("%s = {},",ele.parameter.getName())
        s2 = s2 +String.format("%s(table['%s'].values[i]),",ele.parameter.getType(),ele.attribute )

      }
      else {
        s1 = s1 +String.format("%s = {},",ele.parameter.getName())
        s2 = s2 +String.format("%s ('%s'),",ele.parameter.getType(),ele.value)
      }
    }
    String.format("\"%s\".format(%s)",s1,s2)
  }

  override def generatePythonCode(): String = {
    val listFeatures = selectedFeatures.map(feature => s""""$feature"""").mkString(",")
    val trainingName = getImportStatements().split(" ").last
    val finalcode =
      s"""
         |from pytexera import *
         |
         |import pandas as pd
         |import numpy as np
         |${getImportStatements()}
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
         |      y_train = dataset["$groundTruthAttribute"]
         |      X_train = dataset[features]
         |      loop_times = ${numberStatements(paraList)}
         |
         |
         |      for i in range(loop_times):
         |        model = ${trainingName}(${writeParameterStatements(paraList)})
         |        model.fit(X_train, y_train)
         |
         |        para_str = ${writeParameterString(paraList)}
         |        model_str = pickle.dumps(model)
         |        model_dict = {}
         |        model_dict["Model"] = model_str
         |        model_dict["Features"] = features
         |        model_dict["Parameters"] = para_str
         |        model_list.append(model_dict)
         |        para_list.append(para_str)
         |
         |      data = dict({})
         |      data["Model"]= model_list
         |      data["Parameters"] =para_list
         |
         |      df = pd.DataFrame(data)
         |      if "Iteration" in df.columns:
         |        df["Iteration"]= table["Iteration"]
         |      yield df
         |
         |""".stripMargin
    finalcode
  }

  override def operatorInfo: OperatorInfo = {
    val name = getOperatorInfo()
    OperatorInfo(
      name+" Training list",
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
    outputSchemaBuilder.add(new Attribute("Model", AttributeType.BINARY))
    outputSchemaBuilder.add(new Attribute("Parameters", AttributeType.STRING)).build
  }
}
