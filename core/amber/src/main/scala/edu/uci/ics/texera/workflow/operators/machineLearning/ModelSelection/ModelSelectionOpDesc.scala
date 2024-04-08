package edu.uci.ics.texera.workflow.operators.machineLearning.ModelSelection

import com.fasterxml.jackson.annotation.{JsonProperty, JsonPropertyDescription}
import com.kjetland.jackson.jsonSchema.annotations.{JsonSchemaInject, JsonSchemaTitle}
import edu.uci.ics.amber.engine.common.workflow.{InputPort, OutputPort}
import edu.uci.ics.texera.workflow.common.metadata.{OperatorGroupConstants, OperatorInfo}
import edu.uci.ics.texera.workflow.common.metadata.annotations.AutofillAttributeName
import edu.uci.ics.texera.workflow.common.operators.PythonOperatorDescriptor
import edu.uci.ics.texera.workflow.common.tuple.schema.Schema


@JsonSchemaInject(json = """
{
  "attributeTypeRules": {
    "evaluation": {
        "enum": ["integer", "long", "double"]
      }
    }
}
""")
class ModelSelectionOpDesc extends PythonOperatorDescriptor{
  @JsonProperty(required = true,defaultValue = "Accuracy")
  @JsonSchemaTitle("Metric")
  @JsonPropertyDescription("Choose the metric to optimize")
  @AutofillAttributeName
  var evaluation: String = ""

  override def getOutputSchema(schemas: Array[Schema]): Schema = {
    val outputSchemaBuilder = Schema.newBuilder
    val inputSchema = schemas(0)
    outputSchemaBuilder.add(inputSchema)
    outputSchemaBuilder.build
  }
  override def operatorInfo: OperatorInfo =
    OperatorInfo(
      "Model Selection",
      "Select the best models based on a chosen metric",
      OperatorGroupConstants.ML_GROUP,
      inputPorts = List(InputPort()),
      outputPorts = List(OutputPort())
    )

  override def generatePythonCode(): String = {
    val finalcode =
      s"""
         |from pytexera import *
         |
         |class ProcessTableOperator(UDFTableOperator):
         |  def process_table(self, table: Table, port: int) -> Iterator[Optional[TableLike]]:
         |    keys_list = list(table.keys())
         |    table = table[table["Label"]=="Overall"]
         |    if "$evaluation" in ("MSE","MAE","RMSE"):
         |      best_result = table["$evaluation"].min()
         |    else:
         |      best_result = table["$evaluation"].max()
         |    data = table[table["$evaluation"]==best_result]
         |    yield data
         |
         |""".stripMargin
    finalcode
  }
}