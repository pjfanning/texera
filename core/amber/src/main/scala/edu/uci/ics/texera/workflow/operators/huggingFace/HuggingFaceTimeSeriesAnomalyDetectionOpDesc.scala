package edu.uci.ics.texera.workflow.operators.huggingFace

import com.fasterxml.jackson.annotation.{JsonProperty, JsonPropertyDescription}
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaTitle
import edu.uci.ics.amber.engine.common.workflow.{InputPort, OutputPort}
import edu.uci.ics.texera.workflow.common.metadata.{OperatorGroupConstants, OperatorInfo}
import edu.uci.ics.texera.workflow.common.metadata.annotations.AutofillAttributeName
import edu.uci.ics.texera.workflow.common.operators.PythonOperatorDescriptor
import edu.uci.ics.texera.workflow.common.tuple.schema.{AttributeType, Schema}

class HuggingFaceTimeSeriesAnomalyDetectionOpDesc extends PythonOperatorDescriptor {
  @JsonProperty(value = "Feature Attribute", required = true)
  @JsonPropertyDescription("column to detect the anomalies")
  @AutofillAttributeName
  var featureAttribute: String = _

  @JsonProperty(value = "Output Anomaly Attribute", required = false, defaultValue = "anomaly")
  @JsonSchemaTitle("Output Anomaly Attribute Name")
  @JsonPropertyDescription("name of the output anomaly column")
  var outputAttribute: String = "anomaly"

  override def generatePythonCode(): String = {
    s"""
       |from pytexera import *
       |import numpy as np
       |import pandas as pd
       |from typing import Iterator, Optional
       |from huggingface_hub import from_pretrained_keras
       |
       |class ProcessTableOperator(UDFTableOperator):
       |
       |    def open(self):
       |        # Load the pre-trained Keras model
       |        self.model = from_pretrained_keras("keras-io/timeseries-anomaly-detection")
       |        self.threshold = None  # Initialize the threshold
       |
       |    @overrides
       |    def process_table(self, table: Table, port: int) -> Iterator[Optional[TableLike]]:
       |        df = pd.DataFrame(table)
       |
       |        # Extract the feature column to analyze
       |        data_points = df['$featureAttribute'].values
       |
       |        # Chop the data into segments of 288, padding the last segment if necessary
       |        # 288 is required by this model
       |        num_segments = len(data_points) // 288 + (1 if len(data_points) % 288 != 0 else 0)
       |        anomalies = np.zeros(len(data_points), dtype=bool)  # Array to store anomaly detection results
       |
       |        for i in range(num_segments):
       |            start_idx = i * 288
       |            end_idx = start_idx + 288
       |            segment = data_points[start_idx:end_idx]
       |            if len(segment) < 288:
       |                segment = np.pad(segment, (0, 288 - len(segment)), 'constant', constant_values=0)
       |            segment = segment.reshape(1, 288, 1)
       |
       |            # Predict using the loaded model
       |            predictions = self.model.predict(segment)
       |            reconstruction_error = np.abs(predictions - segment)
       |
       |            # Dynamically set the threshold if not already set
       |            if self.threshold is None:
       |                self.threshold = np.mean(reconstruction_error) + 1.5 * np.std(reconstruction_error)
       |
       |            # Determine if an anomaly based on the threshold
       |            is_anomaly = np.any(reconstruction_error > self.threshold)
       |            anomalies[start_idx:end_idx] = is_anomaly
       |
       |        # Append the anomaly detection results to the DataFrame
       |        df['$outputAttribute'] = anomalies[:len(data_points)]
       |        yield df
       |
       |""".stripMargin
  }
  override def operatorInfo: OperatorInfo = OperatorInfo(
    "Hugging Face Time Series Anomaly Detection",
    "Detect anomalies in a sequence of time series data using a pre-trained model from Hugging Face",
    OperatorGroupConstants.MACHINE_LEARNING_GROUP,
    inputPorts = List(InputPort()),
    outputPorts = List(OutputPort()),
    supportReconfiguration = true
  )

  override def getOutputSchema(schemas: Array[Schema]): Schema = {
    if (featureAttribute == null)
      return null

    Schema
      .builder()
      .add(schemas(0))
      .add(outputAttribute, AttributeType.BOOLEAN)
      .build()
  }
}
