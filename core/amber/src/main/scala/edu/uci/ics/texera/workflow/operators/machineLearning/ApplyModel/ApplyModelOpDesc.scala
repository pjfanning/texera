package edu.uci.ics.texera.workflow.operators.machineLearning.ApplyModel

import com.fasterxml.jackson.annotation.{JsonProperty, JsonPropertyDescription}
import com.google.common.base.Preconditions
import com.kjetland.jackson.jsonSchema.annotations.{JsonSchemaInject, JsonSchemaString, JsonSchemaTitle}
import edu.uci.ics.amber.engine.common.workflow.{InputPort, OutputPort, PortIdentity}
import edu.uci.ics.texera.workflow.common.metadata.annotations.{AutofillAttributeName, AutofillAttributeNameOnPort1, HideAnnotation}
import edu.uci.ics.texera.workflow.common.metadata.{OperatorGroupConstants, OperatorInfo}
import edu.uci.ics.texera.workflow.common.operators.PythonOperatorDescriptor
import edu.uci.ics.texera.workflow.common.tuple.schema.{Attribute, AttributeType, Schema}

import scala.jdk.CollectionConverters.IterableHasAsJava

class ApplyModelOpDesc extends PythonOperatorDescriptor {


  @JsonProperty(required = true, defaultValue = "y_pred")
  @JsonSchemaTitle("Predict Column")
  @JsonPropertyDescription("Specify the name of the predicted data")
  var y_pred: String = ""

  @JsonProperty(defaultValue = "false")
  @JsonSchemaTitle("Predict probability for each class")
  @JsonSchemaInject(json = """{"toggleHidden" : ["y_prob"]}""")
  var is_prob: Boolean = false



  @JsonProperty(value = "y_prob", required = false,defaultValue = "y_prob")
  @JsonSchemaTitle("Probability Column")
  @JsonPropertyDescription("Specify the name of the predicted probability")
  @JsonSchemaInject(
    strings = Array(
      new JsonSchemaString(path = HideAnnotation.hideTarget, value = "is_prob"),
      new JsonSchemaString(path = HideAnnotation.hideType, value = HideAnnotation.Type.equals),
      new JsonSchemaString(path = HideAnnotation.hideExpectedValue, value = "false")
    )
  )
  var y_prob: String = "y_prob"


  @JsonProperty(value = "is_ground_truth",defaultValue = "false")
  @JsonSchemaTitle("Ground truth in datasets")
  @JsonSchemaInject(json = """{"toggleHidden" : ["y_true"]}""")
  var is_ground_truth: Boolean = false


  @JsonProperty(value = "y_true", required = false)
  @JsonSchemaTitle("Ground truth label")
  @JsonPropertyDescription("Specify the label column")
  @JsonSchemaInject(
    strings = Array(
      new JsonSchemaString(path = HideAnnotation.hideTarget, value = "is_ground_truth"),
      new JsonSchemaString(path = HideAnnotation.hideType, value = HideAnnotation.Type.equals),
      new JsonSchemaString(path = HideAnnotation.hideExpectedValue, value = "false")
    )
  )
  @AutofillAttributeName
  var y_true: String = ""

  override def operatorInfo: OperatorInfo =
    OperatorInfo(
      "Apply Model",
      "Apply Machine learning model(scikit-learn)",
      OperatorGroupConstants.ML_GROUP,
      inputPorts = List(
        InputPort(
          PortIdentity(0),
          displayName = "dataset",
          allowMultiLinks = true
        ),
        InputPort(PortIdentity(1), displayName = "model", allowMultiLinks = true,
          dependencies = List(PortIdentity(0)))
      ),
      outputPorts = List(OutputPort())
    )

  override def getOutputSchema(schemas: Array[Schema]): Schema = {
    Preconditions.checkArgument(schemas.length == 2)

    val outputSchemaBuilder = Schema.newBuilder
    val inputSchema = schemas(1)
    outputSchemaBuilder.add(inputSchema)
    if (is_ground_truth)  outputSchemaBuilder.add(new Attribute(y_true, AttributeType.BINARY))
    if (is_prob)  outputSchemaBuilder.add(new Attribute(y_prob, AttributeType.BINARY))
    outputSchemaBuilder.add(new Attribute(y_pred, AttributeType.BINARY)).build


  }



  override def generatePythonCode(): String = {
    var flag_prob = "False"
    if (is_prob)  flag_prob = "True"
    var flag_gt = "False"
    if (is_ground_truth)  flag_gt = "True"
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
         |      if $flag_prob:
         |        y_prob = []
         |      if $flag_gt:
         |        y_true = []
         |      model_config = table
         |      for i in range(model_config.shape[0]):
         |        feature = model_config["features"][i]
         |        x_test = dataset[feature]
         |        model = pickle.loads(model_config["model"][i])
         |        y_predict = model.predict(x_test)
         |        y_pred.append(y_predict)
         |        if $flag_gt:
         |            y_true.append(dataset["$y_true"])
         |        if $flag_prob:
         |            y_proba = model.predict_proba(x_test)
         |            y_prob.append([y_proba,model.classes_])
         |      result = dict()
         |      result['$y_pred'] = y_pred
         |      if $flag_prob:
         |        result['$y_prob'] = y_prob
         |      if $flag_gt:
         |        result['$y_true'] = y_true
         |      result_df  = pd.DataFrame(result)
         |      result_df["model"] = model_config["model"]
         |      result_df["para"] =  model_config["para"]
         |      result_df["features"] = model_config["features"]
         |      if "Iteration" in model_config.columns:
         |        result_df["Iteration"] = model_config["Iteration"]
         |
         |
         |      yield result_df
         |
         |""".stripMargin
    finalCode
  }

}