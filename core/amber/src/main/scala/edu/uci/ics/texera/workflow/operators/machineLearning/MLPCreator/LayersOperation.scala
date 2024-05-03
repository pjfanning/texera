package edu.uci.ics.texera.workflow.operators.machineLearning.MLPCreator

import com.fasterxml.jackson.annotation.{JsonIgnore, JsonProperty, JsonPropertyDescription}
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaTitle

class LayersOperation {
  @JsonProperty(required = true)
  @JsonSchemaTitle("Hidden Layer Neurons")
  @JsonPropertyDescription("Hidden size of each layer")
  var size: Int = 0

//  @JsonIgnore
//  def getSize(): String = {
//    size.toString
//  }
}
