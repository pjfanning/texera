package edu.uci.ics.texera.workflow.operators.visualization.dumbbellPlot

import com.fasterxml.jackson.annotation.{JsonProperty, JsonPropertyDescription}
import com.kjetland.jackson.jsonSchema.annotations.{JsonSchemaInject, JsonSchemaTitle}
import edu.uci.ics.texera.workflow.common.metadata.{InputPort, OperatorGroupConstants, OperatorInfo, OutputPort}
import edu.uci.ics.texera.workflow.common.metadata.annotations.AutofillAttributeName
import edu.uci.ics.texera.workflow.common.operators.PythonOperatorDescriptor
import edu.uci.ics.texera.workflow.common.tuple.schema.{Attribute, AttributeType, OperatorSchemaInfo, Schema}
import edu.uci.ics.texera.workflow.operators.visualization.{VisualizationConstants, VisualizationOperator}

//type constraint: endPointsMagnitudeColumn can only be a numeric column
@JsonSchemaInject(json = """
{
  "attributeTypeRules": {
    "endPointsMagnitudeColumnName": {
      "enum": ["integer", "long", "double"]
    }
  }
}
""")
class DumbbellPlotOpDesc extends VisualizationOperator with PythonOperatorDescriptor {

  @JsonProperty(value = "title", required = false, defaultValue = "DumbbellPlot Visualization")
  @JsonSchemaTitle("Title")
  @JsonPropertyDescription("the title of this dumbbell plots")
  var title: String = "DumbbellPlot Visualization"

  @JsonProperty(value = "endPointsColumnName", required = true)
  @JsonSchemaTitle("Endpoints Column Name")
  @JsonPropertyDescription("the name of the endpoints column")
  @AutofillAttributeName
  var endPointsColumnName: String = ""

  @JsonProperty(value = "startPointValue", required = true)
  @JsonSchemaTitle("Start Point Value")
  @JsonPropertyDescription("the value of the start point")
  @AutofillAttributeName
  var startPointValue: String = ""

  @JsonProperty(value = "endPointValue", required = true)
  @JsonSchemaTitle("End Point Value")
  @JsonPropertyDescription("the value of the end point")
  @AutofillAttributeName
  var endPointValue: String = ""

  @JsonProperty(value = "endPointsMagnitudeColumnName", required = true)
  @JsonSchemaTitle("Endpoints Magnitude Column Name")
  @JsonPropertyDescription("the name of the column that indicates the measurement at endpoints")
  @AutofillAttributeName
  var endPointsMagnitudeColumnName: String = ""

  @JsonProperty(value = "entityColumnName", required = true)
  @JsonSchemaTitle("Entity Column Name")
  @JsonPropertyDescription("the column name of the entity being compared")
  @AutofillAttributeName
  var entityColumnName: String = ""

  @JsonSchemaTitle("Entities")
  @JsonPropertyDescription("the entities to be compared")
  var entities: List[DumbbellPlotEntityUnit] = List()


  override def getOutputSchema(schemas: Array[Schema]): Schema = {
    Schema.newBuilder.add(new Attribute("html-content", AttributeType.STRING)).build
  }

  override def operatorInfo: OperatorInfo =
    OperatorInfo(
      "DumbbellPlot",
      "Visualize data in a Dumbbell Plots. A dumbbell plots (also known as a lollipop chart) is typically used to compare two distinct values or time points for the same entity.",
      OperatorGroupConstants.VISUALIZATION_GROUP,
      inputPorts = List(InputPort()),
      outputPorts = List(OutputPort())
    )

  override def numWorkers() = 1

  def createPlotlyFigure(): String = {
    val entityNames = entities.map(unit => s"'${unit.entityName}'").mkString(", ")
    val endPointValues = startPointValue + ", " + endPointValue

    s"""
     |        entityNames = [${entityNames}]
     |        endPointValues = [${endPointValues}]
     |        filtered_table = table[(table['${entityColumnName}'].isin(entityNames)) &
     |                    (table['${endPointsMagnitudeColumnName}'].isin(endPointValues))]
     |
     |        # Create the dumbbell plot using Plotly
     |        fig = go.Figure()
     |
     |        for entity in entityNames:
     |          entity_data = filtered_table[filtered_table['${entityColumnName}'] == entity]
     |          fig.add_trace(go.Scatter(x=entity_data['${endPointsMagnitudeColumnName}'],
     |                             y=[entity]*2,
     |                             mode='lines+markers',
     |                             name=entity,
     |                             marker=dict(size=10)))
     |
     |          fig.update_layout(title="${title}",
     |                  xaxis_title="${endPointsMagnitudeColumnName}",
     |                  yaxis_title="${entityColumnName}",
     |                  yaxis=dict(categoryorder='array', categoryarray=entityNames))
     |""".stripMargin
  }

  override def generatePythonCode(operatorSchemaInfo: OperatorSchemaInfo): String = {
    s"""
     |from pytexera import *
     |
     |import plotly.express as px
     |import plotly.graph_objects as go
     |import plotly.io
     |import numpy as np
     |
     |class ProcessTableOperator(UDFTableOperator):
     |    def render_error(self, error_msg):
     |        return '''<h1>PieChart is not available.</h1>
     |                  <p>Reason is: {} </p>
     |               '''.format(error_msg)
     |
     |    @overrides
     |    def process_table(self, table: Table, port: int) -> Iterator[Optional[TableLike]]:
     |        if table.empty:
     |           yield {'html-content': self.render_error("input table is empty.")}
     |           return
     |        ${createPlotlyFigure()}
     |        # convert fig to html content
     |        html = plotly.io.to_html(fig, include_plotlyjs='cdn', auto_play=False)
     |        yield {'html-content': html}
     |
     |""".stripMargin
  }

  override def chartType(): String = VisualizationConstants.HTML_VIZ
}
