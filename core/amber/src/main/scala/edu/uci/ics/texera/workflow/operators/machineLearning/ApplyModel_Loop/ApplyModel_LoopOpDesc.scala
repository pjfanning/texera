package edu.uci.ics.texera.workflow.operators.machineLearning.ApplyModel_Loop

import com.fasterxml.jackson.annotation.{JsonProperty, JsonPropertyDescription}
import com.google.common.base.Preconditions
import com.kjetland.jackson.jsonSchema.annotations.{JsonSchemaInject, JsonSchemaString, JsonSchemaTitle}
import edu.uci.ics.amber.engine.common.workflow.{InputPort, OutputPort, PortIdentity}
import edu.uci.ics.texera.workflow.common.metadata.annotations.{AutofillAttributeName, AutofillAttributeNameOnPort1, HideAnnotation}
import edu.uci.ics.texera.workflow.common.metadata.{OperatorGroupConstants, OperatorInfo}
import edu.uci.ics.texera.workflow.common.operators.PythonOperatorDescriptor
import edu.uci.ics.texera.workflow.common.tuple.schema.{Attribute, AttributeType, Schema}

import scala.jdk.CollectionConverters.IterableHasAsJava

class ApplyModel_LoopOpDesc extends PythonOperatorDescriptor {


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

  override def operatorInfo: OperatorInfo =
    OperatorInfo(
      "Apply Models loops",
      "Apply Machine learning classifiers (scikit-learn)",
      OperatorGroupConstants.ML_GROUP,
      inputPorts = List(
        InputPort(
          PortIdentity(0),
          displayName = "dataset",
          allowMultiLinks = true,
          dependencies = List(PortIdentity(1))
        ),
        InputPort(PortIdentity(1), displayName = "model", allowMultiLinks = true)
      ),
      outputPorts = List(OutputPort())
    )

  override def getOutputSchema(schemas: Array[Schema]): Schema = {
    Preconditions.checkArgument(schemas.length == 2)

    val outputSchemaBuilder = Schema.newBuilder
    outputSchemaBuilder.add(new Attribute("para", AttributeType.BINARY))
    outputSchemaBuilder.add(new Attribute("features", AttributeType.BINARY))
    outputSchemaBuilder.add(new Attribute("model", AttributeType.BINARY))
    if (is_prob)  outputSchemaBuilder.add(new Attribute(y_prob, AttributeType.BINARY))
    outputSchemaBuilder.add(new Attribute(y_pred, AttributeType.BINARY)).build


  }



  override def generatePythonCode(): String = {
    var flag_prob = "False"
    if (is_prob)  flag_prob = "True"
    val finalCode =
      s"""
         |from pytexera import *
         |import pandas as pd
         |import numpy as np
         |import pickle
         |
         |global s
         |class ApplyModelOperator(UDFTableOperator):
         |
         |  @overrides
         |  def process_table(self, table: Table, port: int) -> Iterator[Optional[TableLike]]:
         |    global s
         |    if port == 1:
         |      s = table
         |
         |    if port ==0:
         |      y_pred = []
         |      if $flag_prob:
         |        y_prob = []
         |      for i in range(s.shape[0]):
         |        f = s["features"][i]
         |        x_test = table[f]
         |        model = pickle.loads(s["model"][i])
         |        y_predict = model.predict(x_test)
         |        y_pred.append(y_predict)
         |        if $flag_prob:
         |            y_proba = model.predict_proba(x_test)
         |            y_prob.append(y_proba)
         |      result = dict()
         |      result['$y_pred'] = y_pred
         |      if $flag_prob:
         |        result['$y_prob'] = y_prob
         |      res  = pd.DataFrame(result)
         |      res["model"] = s["model"]
         |      res["para"] =  s["para"]
         |      res["features"] = s["features"]
         |
         |
         |      yield res
         |
         |""".stripMargin
    finalCode
  }

}