package edu.uci.ics.texera.workflow.operators.machineLearning.RegressionScorer

import com.fasterxml.jackson.annotation.{JsonProperty, JsonPropertyDescription}
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaTitle
import edu.uci.ics.amber.engine.common.workflow.{InputPort, OutputPort}
import edu.uci.ics.texera.workflow.common.metadata.{OperatorGroupConstants, OperatorInfo}
import edu.uci.ics.texera.workflow.common.metadata.annotations.AutofillAttributeName
import edu.uci.ics.texera.workflow.common.operators.PythonOperatorDescriptor
import edu.uci.ics.texera.workflow.common.tuple.schema.{Attribute, AttributeType, Schema}

class RegressionScorerOpDesc extends PythonOperatorDescriptor {
  @JsonProperty(required = true)
  @JsonSchemaTitle("Actual Value")
  @JsonPropertyDescription("Specify the label column")
  @AutofillAttributeName
  var actualValueColumn: String = ""

  @JsonProperty(required = true)
  @JsonSchemaTitle("Predicted Value")
  @JsonPropertyDescription("Specify the attribute generated by the model")
  @AutofillAttributeName
  var predictValueColumn: String = ""

  @JsonProperty(required = true)
  @JsonSchemaTitle("Scorer Functions")
  @JsonPropertyDescription("Select multiple score functions")
  var scorers: List[RegressionScorerFunction] = List()

  override def operatorInfo: OperatorInfo =
    OperatorInfo(
      "Regression Scorer",
      "Scorer for machine learning regression",
      OperatorGroupConstants.MODEL_PERFORMANCE_GROUP,
      inputPorts = List(InputPort()),
      outputPorts = List(OutputPort())
    )

  override def getOutputSchema(schemas: Array[Schema]): Schema = {
    val outputSchemaBuilder = Schema.builder()
    val inputSchema = schemas(0)
    outputSchemaBuilder.add(new Attribute("Label", AttributeType.STRING))
    scorers
      .map(scorer => getEachScorerName(scorer))
      .foreach(scorer => {
        outputSchemaBuilder.add(new Attribute(scorer, AttributeType.DOUBLE))
      })
    if(inputSchema.containsAttribute("Parameters")){
    outputSchemaBuilder.add(inputSchema)}
    outputSchemaBuilder.build()
  }
  private def getEachScorerName(scorer: RegressionScorerFunction): String = {
    // Directly return the name of the scorer using the getName() method
    scorer.getName()
  }

  private def getSelectedScorers(): String = {
    // Return a string of scorers using the getEachScorerName() method
    scorers.map(scorer => getEachScorerName(scorer)).mkString("'", "','", "'")
  }

  override def generatePythonCode(): String = {
    val finalcode =
      s"""
         |from pytexera import *
         |import pandas as pd
         |import numpy as np
         |from sklearn.metrics import mean_squared_error
         |from sklearn.metrics import root_mean_squared_error
         |from sklearn.metrics import mean_absolute_error
         |from sklearn.metrics import r2_score
         |
         |
         |class ProcessTableOperator(UDFTableOperator):
         |
         |    @overrides
         |    def process_table(self, table: Table, port: int) -> Iterator[Optional[TableLike]]:
         |      result = dict()
         |      length = 1
         |      if isinstance(table['$actualValueColumn'][0], pd.Series):
         |        y_true_list = table['$actualValueColumn']
         |        y_pred_list = table['$predictValueColumn']
         |        length = len(table['$actualValueColumn'])
         |        #print(y_pred_list)
         |
         |      else:
         |        y_true_list = [table['$actualValueColumn']]
         |        y_pred_list = [table['$predictValueColumn']]
         |
         |      scorer_list = [${getSelectedScorers()}]
         |
         |      for scorer in scorer_list:
         |        result[scorer] = [None] * length
         |
         |      for i in range(length):
         |        y_true = y_true_list[i]
         |        y_pred = y_pred_list[i]
         |
         |        for scorer in scorer_list:
         |          if scorer == "MSE":
         |            result["MSE"][i] = mean_squared_error(y_true, y_pred)
         |          elif scorer == "RMSE":
         |            result["RMSE"][i] = root_mean_squared_error(y_true, y_pred)
         |          elif scorer == "MAE":
         |            result["MAE"][i] = mean_absolute_error(y_true, y_pred)
         |          elif scorer == "R2":
         |            result["R2"][i] = r2_score(y_true, y_pred)
         |
         |      #print('result: ', result)
         |      # convert list/dict to dataframe
         |      label = ['Overall']* length
         |      label_df = pd.DataFrame(label, columns=['Label'])
         |      #print('label_df: ', label_df)
         |      result_df = pd.DataFrame(result)
         |
         |      result_df = pd.DataFrame(result)
         |      if "Parameters" in table.columns:
         |        result_df = pd.concat([label_df,result_df, table], axis=1)
         |      else:
         |        result_df = pd.concat([label_df,result_df], axis=1)
         |
         |
         |
         |      if "Iteration" in result_df.columns:
         |        result_df['Iteration'] = result_df['Iteration'].astype(int)
         |
         |      yield result_df
         |
         |""".stripMargin
    finalcode
  }

}
