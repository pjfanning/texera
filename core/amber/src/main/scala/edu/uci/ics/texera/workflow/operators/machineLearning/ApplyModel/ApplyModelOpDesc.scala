package edu.uci.ics.texera.workflow.operators.machineLearning.ApplyModel

import com.fasterxml.jackson.annotation.{JsonProperty, JsonPropertyDescription}
import com.google.common.base.Preconditions
import com.kjetland.jackson.jsonSchema.annotations.{
  JsonSchemaInject,
  JsonSchemaString,
  JsonSchemaTitle
}
import edu.uci.ics.amber.engine.common.workflow.{InputPort, OutputPort, PortIdentity}
import edu.uci.ics.texera.workflow.common.metadata.annotations.{
  AutofillAttributeName,
  HideAnnotation
}
import edu.uci.ics.texera.workflow.common.metadata.{OperatorGroupConstants, OperatorInfo}
import edu.uci.ics.texera.workflow.common.operators.PythonOperatorDescriptor
import edu.uci.ics.texera.workflow.common.tuple.schema.{Attribute, AttributeType, Schema}

class ApplyModelOpDesc extends PythonOperatorDescriptor {

  @JsonProperty(required = true, defaultValue = "y_prediction")
  @JsonSchemaTitle("Predict Column")
  @JsonPropertyDescription("Specify the name of the predicted data column")
  var yPred: String = ""

  @JsonProperty(defaultValue = "false")
  @JsonSchemaTitle("Predict Probability For Each Class")
  @JsonSchemaInject(json = """{"toggleHidden" : ["yProb"]}""")
  @JsonPropertyDescription(
    "Choose to calculate the probabilities of one dataset belongs to each class"
  )
  var isProb: Boolean = false

  @JsonProperty(value = "yProb", required = false, defaultValue = "y_probability")
  @JsonSchemaTitle("Probability Column")
  @JsonPropertyDescription("Specify the name of the predicted probability column")
  @JsonSchemaInject(
    strings = Array(
      new JsonSchemaString(path = HideAnnotation.hideTarget, value = "isProb"),
      new JsonSchemaString(path = HideAnnotation.hideType, value = HideAnnotation.Type.equals),
      new JsonSchemaString(path = HideAnnotation.hideExpectedValue, value = "false")
    )
  )
  var yProb: String = "y_prob"

  @JsonProperty(value = "is_ground_truth", defaultValue = "false")
  @JsonSchemaTitle("Ground Truth In Datasets")
  @JsonSchemaInject(json = """{"toggleHidden" : ["yTrue"]}""")
  @JsonPropertyDescription("Choose to pass the ground truth value through this operator")
  var isGroundTruth: Boolean = false

  @JsonProperty(value = "yTrue", required = false)
  @JsonSchemaTitle("Ground Truth Label")
  @JsonPropertyDescription("Specify the name of label column")
  @JsonSchemaInject(
    strings = Array(
      new JsonSchemaString(path = HideAnnotation.hideTarget, value = "isGroundTruth"),
      new JsonSchemaString(path = HideAnnotation.hideType, value = HideAnnotation.Type.equals),
      new JsonSchemaString(path = HideAnnotation.hideExpectedValue, value = "false")
    )
  )
  @AutofillAttributeName
  var yTrue: String = ""

  override def operatorInfo: OperatorInfo =
    OperatorInfo(
      "Apply Model",
      "Apply Machine learning model(scikit-learn)",
      OperatorGroupConstants.MODELEAPPLY_GROUP,
      inputPorts = List(
        InputPort(
          PortIdentity(0),
          displayName = "dataset",
          allowMultiLinks = true
        ),
        InputPort(
          PortIdentity(1),
          displayName = "model",
          allowMultiLinks = true,
          dependencies = List(PortIdentity(0))
        )
      ),
      outputPorts = List(OutputPort())
    )

  override def getOutputSchema(schemas: Array[Schema]): Schema = {
    Preconditions.checkArgument(schemas.length == 2)

    val outputSchemaBuilder = Schema.builder()
    val inputSchema = schemas(1)
    outputSchemaBuilder.add(inputSchema)
    if (isGroundTruth) outputSchemaBuilder.add(new Attribute(yTrue, AttributeType.BINARY))
    if (isProb) outputSchemaBuilder.add(new Attribute(yProb, AttributeType.BINARY))
    outputSchemaBuilder.add(new Attribute(yPred, AttributeType.BINARY)).build

  }

  override def generatePythonCode(): String = {
    var flagProb = "False"
    if (isProb) flagProb = "True"
    var flagGroundTruth = "False"
    if (isGroundTruth) flagGroundTruth = "True"
    val finalCode =
      s"""
         |from pytexera import *
         |import pandas as pd
         |import numpy as np
         |import pickle
         |
         |global model_config
         |class ApplyModelOperator(UDFTableOperator):
         |
         |  @overrides
         |  def process_table(self, table: Table, port: int) -> Iterator[Optional[TableLike]]:
         |    global dataset
         |    global model_config
         |    if port == 0:
         |      dataset = table
         |
         |    if port ==1:
         |      y_pred = []
         |      if $flagProb:
         |        y_prob = []
         |      if $flagGroundTruth:
         |        y_true = []
         |      model_config = table
         |      for i in range(model_config.shape[0]):
         |        feature = model_config["Features"][i]
         |        x_test = dataset[feature]
         |        model = pickle.loads(model_config["Model"][i])
         |        y_predict = model.predict(x_test)
         |        y_pred.append(y_predict)
         |        if $flagGroundTruth:
         |            y_true.append(dataset["$yTrue"])
         |        if $flagProb:
         |            y_proba = model.predict_proba(x_test)
         |            y_prob.append([y_proba,model.classes_])
         |      result = dict()
         |      result['$yPred'] = y_pred
         |      if $flagProb:
         |        result['$yProb'] = y_prob
         |      if $flagGroundTruth:
         |        result['$yTrue'] = y_true
         |      result_df  = pd.DataFrame(result)
         |      result_df["Model"] = model_config["Model"]
         |      result_df["Parameters"] =  model_config["Parameters"]
         |      result_df["Features"] = model_config["Features"]
         |      if "Iteration" in model_config.columns:
         |        result_df["Iteration"] = model_config["Iteration"]
         |
         |      yield result_df
         |
         |""".stripMargin
    finalCode
  }

}