package edu.uci.ics.texera.workflow.operators.machineLearning.NNTrainer

import com.fasterxml.jackson.annotation.{JsonProperty, JsonPropertyDescription}
import com.google.common.base.Preconditions
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaTitle
import edu.uci.ics.amber.engine.common.workflow.{InputPort, OutputPort, PortIdentity}
import edu.uci.ics.texera.workflow.common.metadata.{OperatorGroupConstants, OperatorInfo}
import edu.uci.ics.texera.workflow.common.operators.PythonOperatorDescriptor
import edu.uci.ics.texera.workflow.common.tuple.schema.{Attribute, AttributeType, Schema}
import edu.uci.ics.texera.workflow.common.metadata.annotations.{AutofillAttributeName, AutofillAttributeNameList}


class NNTrainerOpDesc extends PythonOperatorDescriptor {



  @JsonProperty(required = true)
  @JsonSchemaTitle("Label Column")
  @JsonPropertyDescription("Label")
  @AutofillAttributeName
  var label: String = ""

  @JsonProperty(required = true,defaultValue = "100")
  @JsonSchemaTitle("Epochs")
  @JsonPropertyDescription("Specify number of epochs for training the model")
  var epochs: Int = 100

  @JsonProperty(required = true)
  @JsonSchemaTitle("Learning Rate")
  @JsonPropertyDescription("Specify the learning rate")
  val learningRate: Float = Float.box(1.0f)

  @JsonProperty(required = true)
  @JsonSchemaTitle("Optimizer")
  @JsonPropertyDescription("Choose the optimizer for training")
  var optimizer: OptimizerFunction = OptimizerFunction.Adam

  @JsonProperty(required = true)
  @JsonSchemaTitle("Loss Function")
  @JsonPropertyDescription("Choose the loss function for training")
  var lossFunction: LossFunction = LossFunction.CrossEntropyLoss


  override def operatorInfo: OperatorInfo =
    OperatorInfo(
      "Neural Network Trainer",
      "Train a Neural Network model (PyTorch)",
      OperatorGroupConstants.MACHINE_LEARNING_GROUP,
      inputPorts = List(
        InputPort(
          PortIdentity(0),
          displayName = "datasets",
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
    val outputSchemaBuilder = Schema.builder()
    outputSchemaBuilder.add(new Attribute("name", AttributeType.STRING))
    outputSchemaBuilder.add(new Attribute("label", AttributeType.STRING))
    outputSchemaBuilder.add(new Attribute("weights", AttributeType.BINARY))
    outputSchemaBuilder.add(new Attribute("html", AttributeType.STRING)).build()

  }
  override def generatePythonCode(): String = {

    val finalCode =
      s"""
         |
         |import pandas as pd
         |from sklearn.model_selection import train_test_split
         |import torch
         |import torch.nn as nn
         |import torch.optim as optim
         |from pytexera import *
         |from sklearn.metrics import accuracy_score
         |global dataset
         |from pytexera.Model_repo import *
         |import plotly.graph_objects as go
         |import plotly.io
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
         |            label = "$label"
         |            X =dataset.drop([label],axis=1).values
         |            y = dataset[label].values
         |            X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)
         |            X_train_tensor = torch.tensor(X_train, dtype=torch.float32)
         |            y_train_tensor = torch.tensor(y_train, dtype=torch.long)
         |            X_test_tensor = torch.tensor(X_test, dtype=torch.float32)
         |            y_test_tensor = torch.tensor(y_test, dtype=torch.long)
         |            input_size = X_train.shape[1]
         |            output_size = len(set(y_train))
         |            model_name = table["name"].values[0]
         |            model = eval(model_name)
         |            model = model(input_size, output_size)
         |            print(model)
         |            criterion = nn.$lossFunction()
         |            optimizer = optim.$optimizer(model.parameters(), lr=$learningRate)
         |            num_epochs = $epochs
         |            list_train_loss=[]
         |            list_test_loss=[]
         |            for epoch in range(num_epochs):
         |                outputs = model(X_train_tensor)
         |                loss = criterion(outputs, y_train_tensor)
         |                optimizer.zero_grad()
         |                loss.backward()
         |                optimizer.step()
         |                list_train_loss.append(loss.item())
         |
         |                with torch.no_grad():
         |                    outputs = model(X_test_tensor)
         |                    loss2 = criterion(outputs, y_test_tensor)
         |                    list_test_loss.append(loss2.item())
         |                    #print(f'Epoch [{epoch + 1}/{num_epochs}], train_Loss: {loss.item():.4f},test_Loss: {loss2.item():.4f}')
         |            _, predicted = torch.max(outputs.data, 1)
         |            accuracy = accuracy_score(y_test_tensor.numpy(), predicted.numpy())
         |            print(f'Accuracy on test set: {accuracy:.2f}')
         |            weights = model.state_dict()
         |            fig = go.Figure()
         |            z = list(range(len(list_train_loss)))
         |            fig.add_trace(go.Scatter(x=z, y=list_train_loss, mode='lines', name='Training Loss'))
         |            fig.add_trace(go.Scatter(x=z, y=list_test_loss, mode='lines', name='Testing Loss'))
         |            fig.update_layout(title='Learning Curve',xaxis_title='Epoch',yaxis_title='Loss')
         |            html = plotly.io.to_html(fig, include_plotlyjs="cdn", auto_play=False)
         |            table ={"weights":weights,"html":html,"name":model_name,"label":label}
         |            yield table
         |
         |""".stripMargin
    finalCode
  }

}