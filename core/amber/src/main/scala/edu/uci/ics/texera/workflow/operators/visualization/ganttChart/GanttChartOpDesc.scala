package edu.uci.ics.texera.workflow.operators.visualization.ganttChart

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
    "start": {
      "enum": ["timestamp"]
    },
    "finish": {
      "enum": ["timestamp"]
    }
  }
}
""")
class GanttChartOpDesc extends VisualizationOperator with PythonOperatorDescriptor {

  @JsonProperty(value = "start", required = true)
  @JsonSchemaTitle("Start Attribute")
  @JsonPropertyDescription("The start timestamp of the task")
  @AutofillAttributeName
  val start: String = ""

  @JsonProperty(value = "end", required = true)
  @JsonSchemaTitle("End Attribute")
  @JsonPropertyDescription("The end timestamp of the task")
  @AutofillAttributeName
  val end: String = ""

  @JsonProperty(value = "task", required = true)
  @JsonSchemaTitle("Task Attribute")
  @JsonPropertyDescription("The attribute of the tasks")
  @AutofillAttributeName
  val task: String = ""

  @JsonProperty(value = "color", required = false)
  @JsonSchemaTitle("Color Attribute")
  @JsonPropertyDescription("The attribute to color tasks")
  @AutofillAttributeName
  val color: String = ""

  override def getOutputSchema(schemas: Array[Schema]): Schema = {
    Schema.builder().add(new Attribute("html-content", AttributeType.STRING)).build()
  }

  override def operatorInfo: OperatorInfo =
    OperatorInfo(
      "Gantt Chart",
      "A Gantt chart is a type of bar chart that illustrates a project schedule. The chart lists the tasks to be performed on the vertical axis, and time intervals on the horizontal axis. The width of the horizontal bars in the graph shows the duration of each activity.",
      OperatorGroupConstants.VISUALIZATION_GROUP,
      inputPorts = List(InputPort()),
      outputPorts = List(OutputPort())
    )

  def manipulateTable(): String = {
    val optionalFilterTable = if (color.nonEmpty) s"&(table['$color'].notnull())" else ""
    s"""
       |        table = table[(table["$start"].notnull())&(table["$end"].notnull())&(table["$end"].notnull())$optionalFilterTable].copy()
       |""".stripMargin
  }

  def createPlotlyFigure(): String = {
    val colorSetting = if (color.nonEmpty) s", color='$color'" else ""

    s"""
        |        fig = px.timeline(table, x_start='$start', x_end='$end', y='$task' $colorSetting)
        |        fig.update_yaxes(autorange='reversed')
        |""".stripMargin

  }

  override def generatePythonCode(): String = {
    val finalCode = s"""
                        |from pytexera import *
                        |
                        |import plotly.express as px
                        |import plotly.graph_objects as go
                        |import plotly.io
                        |import numpy as np
                        |
                        |class ProcessTableOperator(UDFTableOperator):
                        |    def render_error(self, error_msg):
                        |        return '''<h1>Gantt Chart is not available.</h1>
                        |                  <p>Reason: {} </p>
                        |               '''.format(error_msg)
                        |
                        |    @overrides
                        |    def process_table(self, table: Table, port: int) -> Iterator[Optional[TableLike]]:
                        |        if table.empty:
                        |           yield {'html-content': self.render_error("Input table is empty.")}
                        |           return
                        |        ${manipulateTable()}
                        |        if table.empty:
                        |           yield {'html-content': self.render_error("One or more of your input columns have all missing values")}
                        |           return
                        |        ${createPlotlyFigure()}
                        |        # convert fig to html content
                        |        html = plotly.io.to_html(fig, include_plotlyjs='cdn', auto_play=False)
                        |        yield {'html-content': html}
                        |""".stripMargin
    finalCode
  }

  // make the chart type to html visualization so it can be recognized by both backend and frontend.
  override def chartType(): String = VisualizationConstants.HTML_VIZ
}
