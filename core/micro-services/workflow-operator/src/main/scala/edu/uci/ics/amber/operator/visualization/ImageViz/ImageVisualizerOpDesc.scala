package edu.uci.ics.amber.operator.visualization.ImageViz

import com.fasterxml.jackson.annotation.{JsonProperty, JsonPropertyDescription}
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaTitle
import edu.uci.ics.amber.core.tuple.{Attribute, AttributeType, Schema}
import edu.uci.ics.amber.operator.PythonOperatorDescriptor
import edu.uci.ics.amber.operator.common.annotations.AutofillAttributeName
import edu.uci.ics.amber.operator.common.metadata.{OperatorGroupConstants, OperatorInfo}
import edu.uci.ics.amber.workflow.{InputPort, OutputPort}
import edu.uci.ics.amber.operator.visualization.{VisualizationConstants, VisualizationOperator}

class ImageVisualizerOpDesc extends VisualizationOperator with PythonOperatorDescriptor {

  @JsonProperty(required = true)
  @JsonSchemaTitle("image content column")
  @JsonPropertyDescription("The Binary data of the Image")
  @AutofillAttributeName
  var binaryContent: String = _

  override def getOutputSchema(schemas: Array[Schema]): Schema = {
    Schema.builder().add(new Attribute("html-content", AttributeType.STRING)).build()
  }

  override def operatorInfo: OperatorInfo =
    OperatorInfo(
      "Image Visualizer",
      "visualize image content",
      OperatorGroupConstants.VISUALIZATION_GROUP,
      inputPorts = List(InputPort()),
      outputPorts = List(OutputPort())
    )

  def createBinaryData(): String = {
    assert(binaryContent.nonEmpty)
    s"""
       |        binary_image_data = tuple_['$binaryContent']
       |""".stripMargin
  }

  override def generatePythonCode(): String = {
    val finalCode = s"""
                       |from pytexera import *
                       |import base64
                       |from io import BytesIO
                       |
                       |class ProcessTupleOperator(UDFOperatorV2):
                       |    images_html = []
                       |
                       |    def render_error(self, error_msg):
                       |        return f'<h1>Image is not available.</h1><p>Reason: {error_msg}</p>'
                       |
                       |    def encode_image_to_html(self, binary_image_data):
                       |        try:
                       |            encoded_image_data = base64.b64encode(binary_image_data)
                       |            encoded_image_str = encoded_image_data.decode("utf-8")
                       |            html = f'<img src="data:image;base64,{encoded_image_str}" alt="Image" style="max-width: 100vw; max-height: 90vh; width: auto; height: auto;">'
                       |            return html
                       |        except Exception as e:
                       |            return self.render_error("Binary input is not valid")
                       |
                       |    @overrides
                       |    def process_tuple(self, tuple_: Tuple, port: int) -> Iterator[Optional[TupleLike]]:
                       |        ${createBinaryData()}
                       |        self.images_html.append(self.encode_image_to_html(binary_image_data))
                       |        yield
                       |
                       |    @overrides
                       |    def on_finish(self, port: int) -> Iterator[Optional[TupleLike]]:
                       |        all_images_html = "<div>" + "".join(self.images_html) + "</div>"
                       |        yield {"html-content": all_images_html}
                       |""".stripMargin
    finalCode
  }

  // make the chart type to html visualization so it can be recognized by both backend and frontend.
  override def chartType(): String = VisualizationConstants.HTML_VIZ
}
