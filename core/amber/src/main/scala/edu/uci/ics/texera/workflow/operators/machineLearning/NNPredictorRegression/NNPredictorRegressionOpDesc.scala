package edu.uci.ics.texera.workflow.operators.machineLearning.NNPredictorRegression

import com.fasterxml.jackson.annotation.{JsonProperty, JsonPropertyDescription}
import com.google.common.base.Preconditions
import com.kjetland.jackson.jsonSchema.annotations.{JsonSchemaInject, JsonSchemaString, JsonSchemaTitle}
import edu.uci.ics.amber.engine.common.workflow.{InputPort, OutputPort, PortIdentity}
import edu.uci.ics.texera.workflow.common.metadata.annotations.{AutofillAttributeName, HideAnnotation}
import edu.uci.ics.texera.workflow.common.metadata.{OperatorGroupConstants, OperatorInfo}
import edu.uci.ics.texera.workflow.common.operators.PythonOperatorDescriptor
import edu.uci.ics.texera.workflow.common.tuple.schema.{Attribute, AttributeType, Schema}

class NNPredictorRegressionOpDesc extends PythonOperatorDescriptor {

  @JsonProperty(required = true, defaultValue = "y_pred")
  @JsonSchemaTitle("Predict Column")
  @JsonPropertyDescription("Specify the name of the predicted data column")
  var yPred: String = ""

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
      "Neural Network Predictor (Regression)",
      "Make prediction with Neural Network model (PyTorch)",
      OperatorGroupConstants.MACHINE_LEARNING_GROUP,
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
    val inputSchema = schemas(0)
    outputSchemaBuilder.add(inputSchema)
    outputSchemaBuilder.add(new Attribute(yPred, AttributeType.DOUBLE))
    outputSchemaBuilder.build()
  }


  override def generatePythonCode(): String = {
    var flagGroundTruth = "False"
    if (isGroundTruth) flagGroundTruth = "True"
      val finalCode =
        s"""
           |from pytexera import *
           |import pandas as pd
           |import pickle
           |import torch
           |from sklearn.metrics import r2_score
           |
           |class ProcessTableOperator(UDFTableOperator):
           |
           |     @overrides
           |     def process_table(self, table: Table, port: int) -> Iterator[Optional[TableLike]]:
           |        global dataset
           |        if port == 0:
           |            dataset = table
           |
           |        if port == 1:
           |            if $flagGroundTruth:
           |                label = table["label"].values[0]
           |                X =dataset.drop([label],axis=1).values
           |                y = dataset[label].values
           |                y_test_tensor = torch.tensor(y, dtype=torch.double).reshape([y.shape[0],-1])
           |            else:
           |                X = dataset.values
           |            X_test_tensor = torch.tensor(X, dtype=torch.float32)
           |
           |            model = pickle.loads(table["model"].values[0])
           |            state_dict = table["weights"].values[0]
           |            model.load_state_dict(state_dict)
           |
           |            with torch.no_grad():
           |                outputs = model(X_test_tensor)
           |                predicted = outputs.numpy()
           |                pd_pred = pd.DataFrame(predicted, columns=["y_pred"])
           |                pd_result = pd.concat([pd_pred,dataset],axis=1)
           |                if $flagGroundTruth:
           |                    accuracy = r2_score(y_test_tensor.numpy(), predicted)
           |                    print(f'R2 on test set: {accuracy:.2f}')
           |
           |            yield pd_result
           |""".stripMargin
      finalCode
    }

    
}