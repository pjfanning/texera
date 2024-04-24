package edu.uci.ics.texera.workflow.common.operators
import com.fasterxml.jackson.annotation.{JsonProperty, JsonPropertyDescription}
import com.kjetland.jackson.jsonSchema.annotations.{JsonSchemaInject, JsonSchemaTitle}
import edu.uci.ics.texera.workflow.common.metadata.OperatorInfo
import edu.uci.ics.texera.workflow.common.tuple.schema.{Attribute, AttributeType, Schema}

trait SklearnMLOperatorDescriptor extends PythonOperatorDescriptor{
  var parameterTuningFlag: Boolean
  var groundTruthAttribute: String
  var selectedFeatures: List[String]

  def addImportMap():Map[String, String]
  def addParamMap():Map[String, Array[Any]]

  def importPackage(): String ={
    val importMap = addImportMap()
    val importLines = importMap.map { case (key, value) =>
      s"from $key import $value"
    }.mkString("\n")

    s"""
       |$importLines
       |""".stripMargin
  }

  def paramFromCustom():String = {
    val paramMap = addParamMap()
    val paramLines = paramMap.map { case (key, array) =>
      val listName = array(0)
      val attributeName = array(1)
      s"""
         |        $listName = np.array([$attributeName])
         |"""
    }.mkString

    s"""
       |$paramLines
       |"""
  }

  def paramFromTuning(): String= {
    val paramMap = addParamMap()
    val paramLines = paramMap.map { case (key, array) =>
      val listName = array(0)
      val attributeName = array(2)
      s"""
         |        $listName = table[\"$attributeName\"].values
         |"""
    }.mkString

    s"""
       |$paramLines
       |"""
  }
  def trainingModel(): String = {
    val paramMap = addParamMap()
    val importMap = addImportMap()
    s"""
       |      for i in range(k_list.shape[0]):
       |        k_value = int(k_list[i])
       |        model = KNeighborsClassifier(n_neighbors=int(k_list[i]))
       |        model.fit(X_train, y_train)
       |
       |        para_str = "K = '{}'".format(k_value)
       |        model_str = pickle.dumps(model)
       |        model_list.append(model_str)
       |        para_list.append(para_str)
       |        features_list.append(features)
       |""".stripMargin
  }

  override def getOutputSchema(schemas: Array[Schema]): Schema = {
    val outputSchemaBuilder = Schema.builder()
    if (parameterTuningFlag) outputSchemaBuilder.add(new Attribute("Iteration", AttributeType.INTEGER))
    outputSchemaBuilder.add(new Attribute("Model", AttributeType.BINARY))
    outputSchemaBuilder.add(new Attribute("Parameters", AttributeType.BINARY))
    outputSchemaBuilder.add(new Attribute("Features", AttributeType.BINARY)).build
  }

  def injectDataToOuputPort(): String = {
    s"""
       |      data = dict({})
       |      data["Model"]= model_list
       |      data["Parameters"] = para_list
       |      data["Features"]= features_list
       |""".stripMargin
  }

  override def generatePythonCode(): String = {
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
         |${importPackage()}
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
         |      ${injectDataToOuputPort()}
         |
         |      df = pd.DataFrame(data)
         |      if ($truthy):
         |        df["Iteration"]= parameter_table["Iteration"]
         |      yield df
         |""".stripMargin
    finalCode
  }

}
