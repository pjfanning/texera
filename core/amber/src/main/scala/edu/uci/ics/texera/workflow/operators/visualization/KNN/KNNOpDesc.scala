package edu.uci.ics.texera.workflow.operators.visualization.KNN

import com.fasterxml.jackson.annotation.{JsonProperty, JsonPropertyDescription}
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaTitle
import edu.uci.ics.amber.engine.common.workflow.{InputPort, OutputPort}
import edu.uci.ics.texera.workflow.common.metadata.annotations.AutofillAttributeName
import edu.uci.ics.texera.workflow.common.metadata.{OperatorGroupConstants, OperatorInfo}
import edu.uci.ics.texera.workflow.common.operators.PythonOperatorDescriptor
import edu.uci.ics.texera.workflow.common.tuple.schema.{Attribute, AttributeType, Schema}
import edu.uci.ics.texera.workflow.operators.visualization.{VisualizationConstants, VisualizationOperator}
import scala.jdk.CollectionConverters.IterableHasAsJava

class KNNOpDesc extends PythonOperatorDescriptor {


    @JsonProperty(required = true)
    @JsonSchemaTitle("K")
    @JsonPropertyDescription("Specify how many nearest neighbours")
    var k: Int = Int.box(1)

    @JsonProperty
    @JsonSchemaTitle("name of the predict column(s)")
    @JsonPropertyDescription(
      "Name of the newly added output columns that the UDF will produce, if any"
    )
    var outputColumns: List[Attribute] = List()






  override def getOutputSchema(schemas: Array[Schema]): Schema = {
    //    Preconditions.checkArgument(schemas.length == 1)
    val inputSchema = schemas(0)
    val outputSchemaBuilder = Schema.newBuilder
    // keep the same schema from input
    outputSchemaBuilder.add(inputSchema)
    // for any pythonUDFType, it can add custom output columns (attributes).


        for (column <- outputColumns) {
          if (inputSchema.containsAttribute(column.getName))
            throw new RuntimeException("Column name " + column.getName + " already exists!")
        }
      outputSchemaBuilder.add(outputColumns.asJava).build
    }


    override def operatorInfo: OperatorInfo =
      OperatorInfo(
        "KNN",
        "KNN classification",
        OperatorGroupConstants.VISUALIZATION_GROUP,
        inputPorts = List(InputPort()),
        outputPorts = List(OutputPort())
      )

    //  def manipulateTable(): String = {
    //    assert(value.nonEmpty)
    //    s"""
    //       |        table.dropna(subset = ['$value', '$name'], inplace = True) #remove missing values
    //       |""".stripMargin
    //  }





  override def generatePythonCode(): String = {
    val finalcode =
      s"""
         |from pytexera import *
         |
         |import pandas as pd
         |import numpy as np
         |from sklearn.neighbors import KNeighborsClassifier
         |import pickle
         |
         |
         |class ProcessTableOperator(UDFTableOperator):
         |
         |  @overrides
         |  def process_table(self, table: Table, port: int) -> Iterator[Optional[TableLike]]:
         |    k =3
         |
         |    y_train = table["variety"]
         |    X_train = table.drop(["variety"], axis=1)
         |
         |    knn = KNeighborsClassifier(n_neighbors=k)
         |    knn.fit(X_train, y_train)
         |    p = knn.predict(X_train)
         |    table["y_pred"]= p
         |    yield table
         |
         |""".stripMargin
    finalcode
  }

  }
