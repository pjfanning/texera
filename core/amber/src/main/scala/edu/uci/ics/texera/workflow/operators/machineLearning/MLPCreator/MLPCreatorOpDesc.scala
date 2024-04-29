package edu.uci.ics.texera.workflow.operators.machineLearning.MLPCreator


import com.fasterxml.jackson.annotation.{JsonProperty, JsonPropertyDescription}
import com.google.common.base.Preconditions
import com.kjetland.jackson.jsonSchema.annotations.{JsonSchemaInject, JsonSchemaInt, JsonSchemaString, JsonSchemaTitle}
import edu.uci.ics.amber.engine.common.workflow.{InputPort, OutputPort, PortIdentity}
import edu.uci.ics.texera.workflow.common.metadata.annotations.{AutofillAttributeName, HideAnnotation, UIWidget}
import edu.uci.ics.texera.workflow.common.metadata.{OperatorGroupConstants, OperatorInfo}
import edu.uci.ics.texera.workflow.common.operators.PythonOperatorDescriptor
import edu.uci.ics.texera.workflow.common.tuple.schema.{Attribute, AttributeType, Schema}
import org.jooq.True


class MLPCreatorOpDesc extends PythonOperatorDescriptor {

/*
  @JsonProperty(required = true)
  @JsonSchemaTitle("Layers")
  @JsonPropertyDescription("Number of linear layers")
  var layersNumber: Int = Int.box(1)

  @JsonProperty(required = true)
  @JsonSchemaTitle("Hidden Size")
  @JsonPropertyDescription("Hidden size of each linear layer")
  var hiddenSize: Int = Int.box(128)

*/
  @JsonProperty(required = true)
  @JsonSchemaTitle("Hidden Sizes for Layers")
  @JsonPropertyDescription("Hidden size of each linear layer and split with ',' ")
  var layersList: String = "32,256,128"

  @JsonProperty(required = true)
  @JsonSchemaTitle("Scorer Functions")
  @JsonPropertyDescription("Select multiple score functions")
  var scorers: List[Int] = List()

  @JsonProperty(required = true)
  @JsonSchemaTitle("Activation Function")
  @JsonPropertyDescription("Choose the function for activation layers between linear layers ")
  var activationFunction: ActivationFunction =ActivationFunction.ReLU

  override def operatorInfo: OperatorInfo =
    OperatorInfo(
      "MLP Generator",
      "Generate a MLP model (Pytorch)",
      OperatorGroupConstants.MACHINE_LEARNING_GROUP,
      inputPorts = List(),
      outputPorts = List(OutputPort())
    )

  override def getOutputSchema(schemas: Array[Schema]): Schema = {

    val outputSchemaBuilder = Schema.builder()
    outputSchemaBuilder.add(new Attribute("name", AttributeType.STRING)).build()


  }
  override def asSource(): Boolean = true
  override def generatePythonCode(): String = {
    //val layers = Array.fill(layersNumber)(hiddenSize)
    //val listLayers = layers.mkString(",")
    val finalCode =
      s"""
         |from pytexera import *
         |import torch
         |import torch.nn as nn
         |import os
         |
         |class GenerateOperator(UDFSourceOperator):
         |
         |     @overrides
         |     def produce(self) -> Iterator[Union[TupleLike, TableLike, None]]:
         |        define_code = \"\"\"
         |import torch.nn as nn
         |class MLP(nn.Module):
         |    def __init__(self, input_size, output_size):
         |        super(MLP, self).__init__()
         |        self.fc = []
         |        layers = [$layersList]
         |        self.fc1 = nn.Linear(input_size, layers[0])
         |        self.activate = nn.$activationFunction()
         |        for i in range(len(layers)-1):
         |            self.fc.append(nn.Linear(layers[i], layers[i+1]))
         |            if i !=len(layers)-1:
         |                self.fc.append(self.activate)
         |            self.all_layers = nn.Sequential(*self.fc)
         |        self.fc2 = nn.Linear(layers[-1], output_size)
         |
         |    def forward(self, x):
         |        x = x.reshape(x.shape[0], -1)
         |        out = self.fc1(x)
         |        out = self.activate(out)
         |        out = self.all_layers(out)
         |        out = self.fc2(out)
         |        return out
         |\"\"\"
         |        file_path = r'./src/main/python/pytexera/Model_repo.py'
         |        with open(file_path, 'w') as file:
         |            file.write(define_code)
         |
         |
         |        table = {}
         |        table["name"] = "MLP"
         |        yield table
         |
         |
         |""".stripMargin
    finalCode
  }

}