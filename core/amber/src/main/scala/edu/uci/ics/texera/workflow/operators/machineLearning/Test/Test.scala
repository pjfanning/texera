package edu.uci.ics.texera.workflow.operators.machineLearning.Test

import com.fasterxml.jackson.annotation.{JsonProperty, JsonPropertyDescription}
import com.kjetland.jackson.jsonSchema.annotations.{JsonSchemaInject, JsonSchemaTitle}
import edu.uci.ics.texera.workflow.common.metadata.OperatorInfo
import edu.uci.ics.texera.workflow.common.operators.{MLOperatorDescriptor, PythonOperatorDescriptor}

class Test extends MLOperatorDescriptor{
  override var isLoop: Boolean = ???
  override var label: String = ???
  override var selectedFeatures: List[String] = ???

  override def importPackage(): String = ???

  override def operatorInfo: OperatorInfo = ???

  override def trainingModelCustom(): String = ???

  override def trainingModelOptimization(): String = ???
}
