package edu.uci.ics.texera.workflow.operators.machineLearning.MLP

import com.fasterxml.jackson.annotation.{JsonProperty, JsonPropertyDescription}
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaTitle
import edu.uci.ics.amber.engine.common.workflow.{InputPort, OutputPort}
import edu.uci.ics.texera.workflow.common.metadata.{OperatorGroupConstants, OperatorInfo}
import edu.uci.ics.texera.workflow.common.metadata.annotations.{AutofillAttributeName, AutofillAttributeNameList}
import edu.uci.ics.texera.workflow.common.operators.PythonOperatorDescriptor
import edu.uci.ics.texera.workflow.common.tuple.schema.{Attribute, AttributeType, Schema}

class MLPOpDesc extends PythonOperatorDescriptor{
  @JsonProperty(required = true)
  @JsonSchemaTitle("Input Features")
  @JsonPropertyDescription("Specify number of input features")
  var inFeatures: Int = 0

  @JsonProperty(required = true)
  @JsonSchemaTitle("Output Features")
  @JsonPropertyDescription("Specify number of input features depending on the task")
  var outFeatures: Int = 1

  @JsonProperty(required = true)
  @JsonSchemaTitle("Hidden Layer Neurons")
  @JsonPropertyDescription("Specify number of neurons in the hidden layer")
  var numNeurons: Int = 1

  @JsonProperty(required = true)
  @JsonSchemaTitle("Neural Network Depth")
  @JsonPropertyDescription("Specify depth for the neural network")
  var depth: Int = 0

  @JsonProperty(required = true)
  @JsonSchemaTitle("Epochs")
  @JsonPropertyDescription("Specify number of epochs for training the model")
  var epochs: Int = 1

  @JsonProperty(value = "Selected Features", required = true)
  @JsonSchemaTitle("Selected Features")
  @JsonPropertyDescription("Features used to train the model")
  @AutofillAttributeNameList
  var selectedFeatures: List[String] = _

  @JsonProperty(required = true)
  @JsonSchemaTitle("Label Column")
  @JsonPropertyDescription("Label")
  @AutofillAttributeName
  var label: String = ""
  override def operatorInfo: OperatorInfo =
    OperatorInfo(
      "MLP Trainer",
      "Train a Multi-Layer Perceptron model",
      OperatorGroupConstants.MACHINE_LEARNING_GROUP,
      inputPorts = List(InputPort()),
      outputPorts = List(OutputPort())
    )

  override def getOutputSchema(schemas: Array[Schema]): Schema = {
    val outputSchemaBuilder = Schema.builder()
    outputSchemaBuilder.add(new Attribute("model", AttributeType.BINARY))
    outputSchemaBuilder.add(new Attribute("features", AttributeType.STRING))
    outputSchemaBuilder.build
  }

  override def generatePythonCode(): String = {
    val list_features = selectedFeatures.map(feature => s""""$feature"""").mkString(",")
    val finalcode =
      s"""
         |from pytexera import *
         |
         |import pandas as pd
         |import numpy as np
         |import pickle
         |import torch
         |import torch.nn as nn
         |from torch.optim import Adam
         |from torch.utils.data import Dataset, DataLoader
         |from torchrl.modules import MLP
         |
         |class CustomDataset(Dataset):
         |  def __init__(self, dataset, feature_cols, label_col):
         |    self.dataset = dataset
         |    self.feature_cols = feature_cols
         |    self.label_col = label_col
         |
         |  def __len__(self):
         |    return len(self.dataset)
         |
         |  def __getitem__(self, idx):
         |    features = torch.tensor(self.dataset[self.feature_cols].iloc[idx].values, dtype=torch.float32)
         |    label = torch.tensor(self.dataset[self.label_col].iloc[idx], dtype=torch.float32)
         |    return features, label
         |
         |
         |class ProcessTableOperator(UDFTableOperator):
         |  @overrides
         |  def process_table(self, table: Table, port: int) -> Iterator[Optional[TableLike]]:
         |
         |    dataset = table
         |    features = [$list_features]
         |
         |    # make a dataloader
         |    custom_dataset = CustomDataset(dataset, features, '$label')
         |    data_loader = DataLoader(custom_dataset, batch_size=32, shuffle=True)
         |
         |    # define the model
         |    model = MLP(in_features=$inFeatures, out_features=$outFeatures, num_cells=$numNeurons, depth=$depth)
         |    optimizer = torch.optim.Adam(model.parameters(), lr=0.01)
         |    criterion = nn.MSELoss()
         |    # criterion = nn.CrossEntropyLoss()
         |
         |    # train the model
         |    model.train()
         |    print(model)
         |
         |    for epoch in range($epochs):
         |      lossItem = 0
         |      cnt = 0
         |      for feature, label in data_loader:
         |        optimizer.zero_grad()
         |        predicted = model(feature)
         |        loss = criterion(predicted, label)
         |        loss.backward()
         |        optimizer.step()
         |
         |        lossItem += loss.item()
         |        cnt += 1
         |      print(f'Epoch: {epoch}, Loss: {lossItem/cnt}')
         |
         |    serialized_model = pickle.dumps(model)
         |
         |    result = dict()
         |    result['model'] = serialized_model
         |    result['features'] = str(features)
         |    df = pd.DataFrame(result, index=[0])
         |
         |    yield df
         |
         |""".stripMargin
    finalcode
  }

}
