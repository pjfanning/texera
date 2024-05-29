package edu.uci.ics.texera.workflow.operators.machineLearning.KNNTrainerOpDesc.old

import com.fasterxml.jackson.annotation.{JsonProperty, JsonPropertyDescription}
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaTitle
import edu.uci.ics.amber.engine.common.workflow.{InputPort, OutputPort, PortIdentity}
import edu.uci.ics.texera.workflow.common.metadata.annotations.{AutofillAttributeName, AutofillAttributeNameList}
import edu.uci.ics.texera.workflow.common.metadata.{OperatorGroupConstants, OperatorInfo}
import edu.uci.ics.texera.workflow.common.operators.PythonOperatorDescriptor
import edu.uci.ics.texera.workflow.common.tuple.schema.{Attribute, AttributeType, Schema}

class KNNTrainerOpDesc extends PythonOperatorDescriptor {
  @JsonProperty(required = true)
  @JsonSchemaTitle("Parameter Setting")
  var paraList: List[HyperP] = List()

  @JsonProperty(required = true)
  @JsonSchemaTitle("Label Column")
  @JsonPropertyDescription("Label")
  @AutofillAttributeName
  var label: String = ""

  @JsonProperty(value = "Selected Features", required = true)
  @JsonSchemaTitle("Selected Features")
  @JsonPropertyDescription("Features used to train the model")
  @AutofillAttributeNameList
  var selectedFeatures: List[String] = _

  override def getOutputSchema(schemas: Array[Schema]): Schema = {
    val outputSchemaBuilder = Schema.builder()
    outputSchemaBuilder.add(new Attribute("Model", AttributeType.BINARY))
    outputSchemaBuilder.add(new Attribute("Features", AttributeType.BINARY))
    outputSchemaBuilder.add(new Attribute("Parameters", AttributeType.STRING)).build
  }

  override def operatorInfo: OperatorInfo =
    OperatorInfo(
      "KNN Trainer",
      "Train a KNN classifier",
      OperatorGroupConstants.MACHINE_LEARNING_GROUP,
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



  private def numberStatements(paraList:List[HyperP]) : String= {
    for (ele<-paraList){
      if (ele.source == Source.workflow){
        return s"""table[\"${ele.attribute}\"].values.shape[0]"""
      }else{
        return "1"
      }
    }
    ""
  }



  def writeParameterStatements(paraList:List[HyperP]): String =  {
    var s =""
    for  (ele<-paraList){
      if (ele.source == Source.workflow){
        s = s +String.format("%s = %s(table['%s'].values[i]),",ele.parameter.getName() ,ele.parameter.getType(),ele.attribute )
      }
      else {
        s = s +String.format("%s = %s ('%s'),",ele.parameter.getName() ,ele.parameter.getType(),ele.value)
      }
    }
    s
  }

  def writeParameterString(paraList:List[HyperP]): String =  {
    var s1 =""
    var s2 = ""
    for  (ele<-paraList){
      if (ele.source == Source.workflow){
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



  override def generatePythonCode() = {
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
         |    global dataset
         |    model_list = []
         |    para_list = []
         |    features_list = []
         |    features = [$list_features]
         |
         |    if port == 0:
         |      dataset = table
         |
         |    if port == 1:
         |      y_train = dataset["$label"]
         |      X_train = dataset[features]
         |      n = ${numberStatements(paraList)}
         |
         |
         |      for i in range(n):
         |        model = KNeighborsClassifier(${writeParameterStatements(paraList)})
         |        model.fit(X_train, y_train)
         |
         |        para_str = ${writeParameterString(paraList)}
         |        para_list.append(para_str)
         |        features_list.append(features)
         |
         |      data = dict({})
         |      data["Model"]= model
         |      data["Parameters"] =para_list
         |      data["Features"] = features_list
         |
         |      df = pd.DataFrame(data)
         |      if "Iteration" in df.columns:
         |        df["Iteration"]= table["Iteration"]
         |      yield df
         |
         |""".stripMargin
    finalcode
  }

}