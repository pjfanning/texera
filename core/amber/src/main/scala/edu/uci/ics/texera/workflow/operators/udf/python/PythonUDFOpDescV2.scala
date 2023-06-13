package edu.uci.ics.texera.workflow.operators.udf.python

import com.fasterxml.jackson.annotation.{JsonProperty, JsonPropertyDescription}
import com.fasterxml.jackson.databind.JsonNode
import com.google.common.base.Preconditions
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaTitle
import edu.uci.ics.amber.engine.architecture.deploysemantics.layer.OpExecConfig
import edu.uci.ics.texera.Utils
import edu.uci.ics.texera.Utils.objectMapper
import edu.uci.ics.texera.workflow.common.metadata.{InputPort, OperatorGroupConstants, OperatorInfo, OutputPort}
import edu.uci.ics.texera.workflow.common.operators.{OperatorDescriptor, StateTransferFunc}
import edu.uci.ics.texera.workflow.common.tuple.schema.{Attribute, AttributeType, OperatorSchemaInfo, Schema}
import edu.uci.ics.texera.workflow.common.workflow.UnknownPartition
import scalaj.http.{Http, HttpOptions}

import java.util.Collections.singletonList
import scala.collection.JavaConverters._
import scala.collection.convert.ImplicitConversions.`iterator asScala`
import scala.util.{Success, Try}

class PythonUDFOpDescV2 extends OperatorDescriptor {
  @JsonProperty(
    required = true,
    defaultValue =
      "# Choose from the following templates:\n" +
        "# \n" +
        "# from pytexera import *\n" +
        "# \n" +
        "# class ProcessTupleOperator(UDFOperatorV2):\n" +
        "#     \n" +
        "#     @overrides\n" +
        "#     def process_tuple(self, tuple_: Tuple, port: int) -> Iterator[Optional[TupleLike]]:\n" +
        "#         yield tuple_\n" +
        "# \n" +
        "# class ProcessBatchOperator(UDFBatchOperator):\n" +
        "#     BATCH_SIZE = 10 # must be a positive integer\n" +
        "# \n" +
        "#     @overrides\n" +
        "#     def process_batch(self, batch: Batch, port: int) -> Iterator[Optional[BatchLike]]:\n" +
        "#         yield batch\n" +
        "# \n" +
        "# class ProcessTableOperator(UDFTableOperator):\n" +
        "# \n" +
        "#     @overrides\n" +
        "#     def process_table(self, table: Table, port: int) -> Iterator[Optional[TableLike]]:\n" +
        "#         yield table\n"
  )
  @JsonSchemaTitle("Python script")
  @JsonPropertyDescription("Input your code here")
  var code: String = ""
  @JsonProperty(required = true)
  @JsonSchemaTitle("Worker count")
  @JsonPropertyDescription("Specify how many parallel workers to lunch")
  var workers: Int = Int.box(1)


  override def operatorExecutor(operatorSchemaInfo: OperatorSchemaInfo) = {
    Preconditions.checkArgument(workers >= 1, "Need at least 1 worker.", Array())
    if (workers > 1)
      OpExecConfig
        .oneToOneLayer(
          operatorIdentifier,
          _ => new PythonUDFOpExecV2(code, operatorSchemaInfo.outputSchemas.head)
        )
        .copy(numWorkers = workers, derivePartition = _ => UnknownPartition(), isOneToManyOp = true)
    else
      OpExecConfig
        .manyToOneLayer(
          operatorIdentifier,
          _ => new PythonUDFOpExecV2(code, operatorSchemaInfo.outputSchemas.head)
        )
        .copy(derivePartition = _ => UnknownPartition(), isOneToManyOp = true)
  }

  override def operatorInfo: OperatorInfo =
    OperatorInfo(
      "Python UDF",
      "User-defined function operator in Python script",
      OperatorGroupConstants.UDF_GROUP,
      asScalaBuffer(singletonList(new InputPort("", true))).toList,
      asScalaBuffer(singletonList(new OutputPort(""))).toList,
      dynamicInputPorts = true,
      dynamicOutputPorts = true,
      supportReconfiguration = true
    )

  case class Request(inputSchema: Schema, code: String)

  case class Result(@JsonProperty("outputSchema") outputSchema: Schema)

  override def getOutputSchema(schemas: Array[Schema]): Schema = {
    Preconditions.checkArgument(schemas.length == 1)
    val inputSchema = schemas(0)
    val json = objectMapper.writeValueAsString(Request(inputSchema, code))


    val output = Http("http://localhost:8000").postData(json)
      .header("Content-Type", "application/json")
      .header("Charset", "UTF-8")
      .option(HttpOptions.readTimeout(10000)).asString

    val result: JsonNode = Utils.objectMapper.readTree(output.body)


    val attributes = result.get("outputSchema")
    val outputSchemaBuilder = Schema.newBuilder


    val mapping = Map(
      "str" -> AttributeType.STRING,
      "int" -> AttributeType.LONG,
      "float" -> AttributeType.DOUBLE,
      "bool" -> AttributeType.BOOLEAN,
      "datetime" -> AttributeType.TIMESTAMP,
      "binary" -> AttributeType.BINARY
    )
    for ((key, value) <- attributes.fields() zip attributes.iterator()) {
      outputSchemaBuilder.add(new Attribute(key.getKey, mapping.apply(key.getValue.textValue())))

    }
    val out = outputSchemaBuilder.build
    println(out)
    out
  }

  override def runtimeReconfiguration(
                                       newOpDesc: OperatorDescriptor,
                                       operatorSchemaInfo: OperatorSchemaInfo
                                     ): Try[(OpExecConfig, Option[StateTransferFunc])] = {
    Success(newOpDesc.operatorExecutor(operatorSchemaInfo), None)
  }
}
