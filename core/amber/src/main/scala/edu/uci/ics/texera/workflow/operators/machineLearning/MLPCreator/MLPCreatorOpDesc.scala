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
//  @JsonProperty(required = false)
//  @JsonSchemaTitle("Layer")
//  @JsonPropertyDescription("Select multiple score functions")
//  var layers: List[LayersOperation] = List()

  @JsonProperty(required = true)
  @JsonSchemaTitle("Hidden Sizes for Layers")
  @JsonPropertyDescription("Hidden size of each linear layer and split with ',' ")
  var layersList: String = "32,256,128"

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
    outputSchemaBuilder.add(new Attribute("model", AttributeType.BINARY)).build()
  }
  override def asSource(): Boolean = true

//  private def getLayers(): Unit= {
//    layers.map(layer => layer.size.toString).mkString("'", "','", "'")
//  }
  override def generatePythonCode(): String = {
    val finalCode =
      s"""
         |from pytexera import *
         |import pandas as pd
         |import pickle
         |import torch
         |import torch.nn as nn
         |from torchrl.modules import MLP
         |
         |
         |class GenerateOperator(UDFSourceOperator):
         |    @overrides
         |    def produce(self) -> Iterator[Union[TupleLike, TableLike, None]]:
         |        result = dict()
         |        model = MLP(out_features=1, num_cells=[$layersList], activation_class=nn.$activationFunction)
         |        print(model)
         |        serialized_model = pickle.dumps(model)
         |
         |        result['model'] = serialized_model
         |        df = pd.DataFrame(result, index=[0])
         |
         |        yield df
         |
         |
         |""".stripMargin
    finalCode
  }

}