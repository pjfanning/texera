package edu.uci.ics.texera.workflow.operators.machineLearning.OneTraining

import com.fasterxml.jackson.annotation.{JsonProperty, JsonPropertyDescription}
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaTitle
import edu.uci.ics.amber.engine.common.workflow.{InputPort, OutputPort, PortIdentity}
import edu.uci.ics.texera.workflow.common.metadata.annotations.{AutofillAttributeName, AutofillAttributeNameList}
import edu.uci.ics.texera.workflow.common.metadata.{OperatorGroupConstants, OperatorInfo}
import edu.uci.ics.texera.workflow.common.operators.PythonOperatorDescriptor
import edu.uci.ics.texera.workflow.common.tuple.schema.{Attribute, AttributeType, Schema}

class TrainingOpDesc extends PythonOperatorDescriptor {
  @JsonProperty(required = true)
  @JsonSchemaTitle("Parameter Setting")
  var trainingList: List[TrainingType] = _

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

  override def operatorInfo: OperatorInfo =
    OperatorInfo(
      "Training Model",
      "Training Machine learning model(scikit-learn)",
      OperatorGroupConstants.MODEL_TRAINING_GROUP,
      inputPorts = List(
        InputPort(
          PortIdentity(0),
          displayName = "dataset",
          allowMultiLinks = true
        ),
      ),
      outputPorts = List(OutputPort())
    )

  override def getOutputSchema(schemas: Array[Schema]): Schema = {
    val outputSchemaBuilder = Schema.builder()
    outputSchemaBuilder.add(new Attribute("Model", AttributeType.BINARY))
    outputSchemaBuilder.add(new Attribute("Parameters", AttributeType.STRING))
    outputSchemaBuilder.add(new Attribute("Features", AttributeType.BINARY)).build()
  }

  override def generatePythonCode(): String = {
    val listFeatures = selectedFeatures.map(feature => s""""$feature"""").mkString(",")
    val listTraining = trainingList.map(feature => s""""$feature"""").mkString(",")
    val finalCode =
      s"""
         |from pytexera import *
         |
         |import pandas as pd
         |from sklearn.neighbors import KNeighborsClassifier
         |from sklearn.svm import SVC
         |
         |class ProcessTableOperator(UDFTableOperator):
         |
         |  @overrides
         |  def process_table(self, table: Table, port: int) -> Iterator[Optional[TableLike]]:
         |    global dataset
         |    model_list = []
         |    modelName_list =[]
         |    para_list = []
         |    features_list =[]
         |    trainingList = [$listTraining]
         |    features = [$listFeatures]
         |
         |    if port == 0:
         |      dataset = table
         |      y_train = dataset["$groundTruthAttribute"]
         |      X_train = dataset[features]
         |
         |      if 'knn' in trainingList:
         |        model = KNeighborsClassifier()
         |        model.fit(X_train, y_train)
         |        features_list.append(features)
         |        model_list.append(model)
         |        para_list.append(" ")
         |        modelName_list.append("KNN")
         |
         |      if 'svm' in trainingList:
         |        model = SVC()
         |        model.fit(X_train, y_train)
         |        features_list.append(features)
         |        model_list.append(model)
         |        para_list.append(" ")
         |        modelName_list.append("SVM")
         |
         |      data = dict({})
         |      data["Model"]= model_list
         |      data["Parameters"]= modelName_list
         |      data["Features"] = features_list
         |
         |      df = pd.DataFrame(data)
         |      yield df
         |
         |""".stripMargin
    finalCode
  }
}
