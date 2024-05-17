package edu.uci.ics.texera.workflow.operators.machineLearning.NNPredictorClassification

import com.fasterxml.jackson.annotation.{JsonProperty, JsonPropertyDescription}
import com.google.common.base.Preconditions
import com.kjetland.jackson.jsonSchema.annotations.{JsonSchemaInject, JsonSchemaString, JsonSchemaTitle}
import edu.uci.ics.amber.engine.common.workflow.{InputPort, OutputPort, PortIdentity}
import edu.uci.ics.texera.workflow.common.metadata.annotations.{AutofillAttributeName, HideAnnotation}
import edu.uci.ics.texera.workflow.common.metadata.{OperatorGroupConstants, OperatorInfo}
import edu.uci.ics.texera.workflow.common.operators.PythonOperatorDescriptor
import edu.uci.ics.texera.workflow.common.tuple.schema.{Attribute, AttributeType, Schema}

class NNPredictorClassificationOpDesc extends PythonOperatorDescriptor {

  @JsonProperty(required = true, defaultValue = "y_pred")
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

  @JsonProperty(value = "yProb", required = false)
  @JsonSchemaTitle("Number of Classes")
  @JsonPropertyDescription("Specify the name of the predicted probability column")
  @JsonSchemaInject(
    strings = Array(
      new JsonSchemaString(path = HideAnnotation.hideTarget, value = "isProb"),
      new JsonSchemaString(path = HideAnnotation.hideType, value = HideAnnotation.Type.equals),
      new JsonSchemaString(path = HideAnnotation.hideExpectedValue, value = "false")
    )
  )
  var classNumber: Int = Int.box(2)

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
      "Neural Network Predictor (Classification)",
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
    outputSchemaBuilder.add(new Attribute(yPred, AttributeType.INTEGER))

    if (isProb) {
      var classList = this.generateClassStrings(classNumber)
      for (className <- classList) {
        outputSchemaBuilder.add(new Attribute(className, AttributeType.DOUBLE))
      }
    }
    outputSchemaBuilder.build()
  }

  private def generateClassStrings(classNumber: Int): List[String] = {
    (0 until classNumber).map(i => s"class$i").toList
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
           |import torch
           |import pickle
           |import torch.nn.functional as F
           |from sklearn.metrics import accuracy_score
           |from sklearn.metrics import r2_score
           |
           |class ProcessTableOperator(UDFTableOperator):
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
           |                y_test_tensor = torch.tensor(y, dtype=torch.long)
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
           |                if $flagProb:
           |                    probability = F.softmax(outputs, dim=1)
           |                    output_numpy = probability.numpy().round(3)
           |                    predicted = outputs
           |                    predicted = torch.argmax(predicted, dim=1)
           |
           |                    cols = ["class{}".format(i) for i in range($classNumber)]
           |                    pd_prob = pd.DataFrame(output_numpy, columns=cols)
           |                    pd_pred = pd.DataFrame(predicted, columns=["$yPred"])
           |                    pd_result = pd.concat([pd_prob, pd_pred, dataset],axis=1)
           |                    if $flagGroundTruth:
           |                        accuracy = accuracy_score(y_test_tensor.numpy(), predicted.numpy())
           |                        print(f'Accuracy on test set: {accuracy:.2f}')
           |                else:
           |                    predicted = outputs
           |                    predicted = torch.argmax(predicted, dim=1)
           |                    pd_pred = pd.DataFrame(predicted.numpy(), columns=["y_pred"])
           |                    pd_result = pd.concat([pd_pred,dataset],axis=1)
           |                    if $flagGroundTruth:
           |                        accuracy = r2_score(y_test_tensor.numpy(), predicted.numpy())
           |                        print(f'R2 on test set: {accuracy:.2f}')
           |
           |            yield pd_result
           |""".stripMargin
      finalCode
    }


}