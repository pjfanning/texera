package edu.uci.ics.texera.workflow.common.operators

import com.fasterxml.jackson.annotation.{JsonIgnore, JsonProperty, JsonPropertyDescription}
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaTitle
import edu.uci.ics.amber.engine.common.workflow.{InputPort, OutputPort, PortIdentity}
import edu.uci.ics.texera.workflow.common.metadata.annotations.{AutofillAttributeName, AutofillAttributeNameList}
import edu.uci.ics.texera.workflow.common.metadata.{OperatorGroupConstants, OperatorInfo}
import edu.uci.ics.texera.workflow.common.tuple.schema.{Attribute, AttributeType, Schema}

abstract class SklearnMLOperatorDescriptorV3 extends PythonOperatorDescriptor{
  @JsonIgnore
  var model = ""

  @JsonIgnore
  var name = ""

  var parameterTuningFlag: Boolean

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

  private var paramMap =Map[String, Array[Any]]()
  def addParamMap():Map[String, Array[Any]]
  def initParamMap(): Unit = {
    this.paramMap = addParamMap()
  }

  def paramFromCustom():String = {
    paramMap.map { case (_, array) =>
      s"""
         |        ${array(0)} = np.array([\"${array(1)}\"])
         |"""
    }.mkString
  }

  def paramFromTuning(): String= {
    paramMap.map { case (_, array) =>
      s"""
         |        ${array(0)} = table[\"${array(2)}\"].values
         |"""
    }.mkString
  }
  def trainingModel(): String = {
    val listName = paramMap.head._2(0)
    val trainingName = model.split(" ").last
    s"""
       |      for i in range($listName.shape[0]):
       |        model = $trainingName(${paramMap.map { case (key, array) =>s"$key=${array(3)}(${array(0)}[i])"}.mkString(",")})
       |        model.fit(X_train, y_train)
       |
       |        para_str = "${paramMap.map { case (key, _) =>s"$key = '{}'"}.mkString(",")}".format(${paramMap.map { case (_, array) =>s"${array(0)}[i]"}.mkString(",")})
       |        model_str = pickle.dumps(model)
       |        model_list.append(model_str)
       |        para_list.append(para_str)
       |        features_list.append(features)
       |""".stripMargin
  }

  override def generatePythonCode(): String = {
    initParamMap()
    var truthy = "False"
    if (parameterTuningFlag) truthy = "True"
    val listFeatures = selectedFeatures.map(feature => s""""$feature"""").mkString(",")
    val finalCode =
      s"""
         |from pytexera import *
         |
         |import pandas as pd
         |import numpy as np
         |import pickle
         |$model
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
         |      parameter_table = table
         |      y_train = dataset["$groundTruthAttribute"]
         |      X_train = dataset[features]
         |
         |      if not ($truthy):
         |        ${paramFromCustom()}
         |
         |      if ($truthy):
         |        ${paramFromTuning()}
         |
         |      ${trainingModel()}
         |
         |      data = dict({})
         |      data["Model"]= model_list
         |      data["Parameters"] = para_list
         |      data["Features"]= features_list
         |
         |      df = pd.DataFrame(data)
         |      if ($truthy):
         |        df["Iteration"]= parameter_table["Iteration"]
         |      yield df
         |""".stripMargin
    finalCode
  }

  override def operatorInfo: OperatorInfo =
    OperatorInfo(
      name+" V3",
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

    override def getOutputSchema(schemas: Array[Schema]): Schema = {
      val outputSchemaBuilder = Schema.builder()
      if (parameterTuningFlag) outputSchemaBuilder.add(new Attribute("Iteration", AttributeType.INTEGER))
      outputSchemaBuilder.add(new Attribute("Model", AttributeType.BINARY))
      outputSchemaBuilder.add(new Attribute("Parameters", AttributeType.BINARY))
      outputSchemaBuilder.add(new Attribute("Features", AttributeType.BINARY)).build
    }
}
