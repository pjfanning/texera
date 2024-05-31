package edu.uci.ics.texera.workflow.operators.visualization.sankeyDiagram

import com.fasterxml.jackson.annotation.{JsonProperty, JsonPropertyDescription}
import com.kjetland.jackson.jsonSchema.annotations.{JsonSchemaInject, JsonSchemaTitle}
import edu.uci.ics.texera.workflow.common.metadata.annotations.AutofillAttributeName
import edu.uci.ics.texera.workflow.common.metadata.{OperatorGroupConstants, OperatorInfo}
import edu.uci.ics.texera.workflow.common.operators.PythonOperatorDescriptor
import edu.uci.ics.texera.workflow.common.tuple.schema.{Attribute, AttributeType, Schema}
import edu.uci.ics.amber.engine.common.workflow.{InputPort, OutputPort}
import edu.uci.ics.texera.workflow.operators.visualization.{
  VisualizationConstants,
  VisualizationOperator
}

@JsonSchemaInject(json = """
{
  "attributeTypeRules": {
    "value": {
      "enum": ["integer", "long", "double"]
    }
  }
}
""")
class SankeyDiagramOpDesc extends VisualizationOperator with PythonOperatorDescriptor {

  @JsonProperty(defaultValue = "Sankey Diagram Visual")
  @JsonSchemaTitle("Title")
  @JsonPropertyDescription("Add a title to your visualization")
  var title: String = ""

  @JsonProperty(value = "source", required = true)
  @JsonSchemaTitle("Source")
  @JsonPropertyDescription("Data column for the source")
  @AutofillAttributeName var source: String = ""

  @JsonProperty(value = "target", required = true)
  @JsonSchemaTitle("Target")
  @JsonPropertyDescription("Data column for the target")
  @AutofillAttributeName var target: String = ""

  @JsonProperty(value = "value", required = true)
  @JsonSchemaTitle("Value")
  @JsonPropertyDescription("Data column for the value")
  @AutofillAttributeName var value: String = ""

  // @JsonProperty(defaultValue = 15)
  // @JsonSchemaTitle("Pad")
  // @JsonPropertyDescription("Pad")
  // var pad: Int = 0

  // @JsonProperty(defaultValue = 20)
  // @JsonSchemaTitle("Thickness")
  // @JsonPropertyDescription("Thickness")
  // var thickness: Integer = 0 

  // @JsonProperty(defaultValue = "blue")
  // @JsonSchemaTitle("NodeColor")
  // @JsonPropertyDescription("Node Color")
  // var color: String = ""

  override def getOutputSchema(schemas: Array[Schema]): Schema = {
    Schema.builder().add(new Attribute("html-content", AttributeType.STRING)).build()
  }

  override def operatorInfo: OperatorInfo =
    OperatorInfo(
      "Sankey Diagram",
      "Visualize data in a Sankey Diagram",
      OperatorGroupConstants.VISUALIZATION_GROUP,
      inputPorts = List(InputPort()),
      outputPorts = List(OutputPort())
    )

  def manipulateTable(): String = {
    s"""
       |        table = table.dropna() #remove missing values
       |""".stripMargin
  }

  override def generatePythonCode(): String = {

    val finalCode =
      s"""
         |from pytexera import *
         |
         |import plotly.express as px
         |import pandas as pd
         |import plotly.graph_objects as go
         |import plotly.io
         |import json
         |import pickle
         |import plotly
         |
         |class ProcessTableOperator(UDFTableOperator):
         |
         |    # Generate custom error message as html string
         |    def render_error(self, error_msg) -> str:
         |        return '''<h1>Sankey diagram is not available.</h1>
         |                  <p>Reason is: {} </p>
         |               '''.format(error_msg)
         |
         |    @overrides
         |    def process_table(self, table: Table, port: int) -> Iterator[Optional[TableLike]]:
         |         if table.empty:
         |           yield {'html-content': self.render_error("input table is empty.")}
         |           return
         |         if '$source' == '$target':
         |           yield {'html-content': self.render_error("Source and Target columns have the same value.")}
         |           return
         |         ${manipulateTable()}
         |         fig = go.Figure(data=[go.Sankey(
         |           node = dict(
         |             pad=15,
         |             thickness=20,
         |             line=dict(color="black", width=0.5),
         |             label=list(set(table['$source'].tolist() + table['target'].tolist())),
         |             color="blue"
         |           ),
         |           link=dict(
         |             source=[table['$source'].tolist().index(i) for i in table['$source']],
         |             target=[table['$target'].tolist().index(i) for i in table['$target']],
         |             value=table['$value'].tolist()
         |           ))])
         |         fig.update_layout(title_text='$title', font_size=10)
         |         # convert fig to html content
         |         html = plotly.io.to_html(fig, include_plotlyjs='cdn', auto_play=False)
         |        yield {'html-content': html}
         |        """.stripMargin
    finalCode
  }
  // make the chart type to html visualization so it can be recognized by both backend and frontend.
  override def chartType(): String = VisualizationConstants.HTML_VIZ
}

