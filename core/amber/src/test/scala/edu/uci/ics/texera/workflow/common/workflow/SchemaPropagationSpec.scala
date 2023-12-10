package edu.uci.ics.texera.workflow.common.workflow

import edu.uci.ics.amber.engine.architecture.deploysemantics.layer.OpExecConfig
import edu.uci.ics.texera.workflow.common.WorkflowContext
import edu.uci.ics.texera.workflow.common.metadata.{InputPort, OperatorInfo, OutputPort}
import edu.uci.ics.texera.workflow.common.operators.LogicalOp
import edu.uci.ics.texera.workflow.common.operators.source.SourceOperatorDescriptor
import edu.uci.ics.texera.workflow.common.tuple.schema.{AttributeType, OperatorSchemaInfo, Schema}
import edu.uci.ics.texera.workflow.operators.sink.SinkOpDesc
import org.apache.arrow.util.Preconditions
import org.scalatest.BeforeAndAfter
import org.scalatest.flatspec.AnyFlatSpec

class SchemaPropagationSpec extends AnyFlatSpec with BeforeAndAfter {

  private abstract class TempTestSourceOpDesc extends SourceOperatorDescriptor {
    override def operatorExecutor(
        executionId: Long,
        operatorSchemaInfo: OperatorSchemaInfo
    ): OpExecConfig = ???
    override def operatorInfo: OperatorInfo =
      OperatorInfo("", "", "", List(InputPort()), List(OutputPort()))
  }
  private class TempTestSinkOpDesc extends SinkOpDesc {
    override def operatorExecutor(
        executionId: Long,
        operatorSchemaInfo: OperatorSchemaInfo
    ): OpExecConfig = ???
    override def operatorInfo: OperatorInfo =
      OperatorInfo("", "", "", List(InputPort()), List(OutputPort()))
    override def getOutputSchema(schemas: Array[Schema]): Schema = {
      Preconditions.checkArgument(schemas.length == 1)
      schemas(0)
    }
  }

  it should "propagate workflow schema with multiple input and output ports" in {
    // build the following workflow DAG:
    // trainingData ---\                 /----> mlVizSink
    // testingData  ----> mlTrainingOp--<
    // inferenceData ---------------------> mlInferenceOp --> inferenceSink

    val dataSchema = Schema.newBuilder().add("dataCol", AttributeType.INTEGER).build()
    val trainingScan = new TempTestSourceOpDesc() {
      override def sourceSchema(): Schema = dataSchema
    }
    trainingScan.operatorId = "trainingScan"

    val testingScan = new TempTestSourceOpDesc() {
      override def sourceSchema(): Schema = dataSchema
    }
    testingScan.operatorId = "testingScan"

    val inferenceScan = new TempTestSourceOpDesc() {
      override def sourceSchema(): Schema = dataSchema
    }
    inferenceScan.operatorId = "inferenceScan"

    val mlModelSchema = Schema.newBuilder().add("model", AttributeType.STRING).build()
    val mlVizSchema = Schema.newBuilder().add("visualization", AttributeType.STRING).build()

    val mlTrainingOp = new LogicalOp() {
      override def operatorExecutor(
          executionId: Long,
          operatorSchemaInfo: OperatorSchemaInfo
      ): OpExecConfig = ???

      override def operatorInfo: OperatorInfo =
        OperatorInfo(
          "",
          "",
          "",
          List(InputPort("training"), InputPort("testing")),
          List(OutputPort("visualization"), OutputPort("model"))
        )

      override def getOutputSchema(schemas: Array[Schema]): Schema = ???

      override def getOutputSchemas(schemas: Array[Schema]): Array[Schema] = {
        Preconditions.checkArgument(schemas.length == 2)
        Preconditions.checkArgument(schemas.distinct.length == 1)
        Array(mlVizSchema, mlModelSchema)
      }
    }
    mlTrainingOp.operatorId = "mlTrainingOp"

    val mlInferOp = new LogicalOp() {
      override def operatorExecutor(
          executionId: Long,
          operatorSchemaInfo: OperatorSchemaInfo
      ): OpExecConfig = ???

      override def operatorInfo: OperatorInfo =
        OperatorInfo(
          "",
          "",
          "",
          List(InputPort("model"), InputPort("data")),
          List(OutputPort("data"))
        )

      override def getOutputSchema(schemas: Array[Schema]): Schema = ???

      override def getOutputSchemas(schemas: Array[Schema]): Array[Schema] = {
        Preconditions.checkArgument(schemas.length == 2)
        Array(schemas(1))
      }
    }
    mlInferOp.operatorId = "mlInferOp"

    val mlVizSink = new TempTestSinkOpDesc
    mlVizSink.operatorId = "mlVizSink"

    val inferenceSink = new TempTestSinkOpDesc
    inferenceSink.operatorId = "inferenceSink"

    val operators = List(
      trainingScan,
      testingScan,
      inferenceScan,
      mlTrainingOp,
      mlInferOp,
      mlVizSink,
      inferenceSink
    )

    val links = List(
      LogicalLink(
        OperatorPort(trainingScan.operatorId),
        OperatorPort(mlTrainingOp.operatorId, 0)
      ),
      LogicalLink(
        OperatorPort(testingScan.operatorId),
        OperatorPort(mlTrainingOp.operatorId, 1)
      ),
      LogicalLink(
        OperatorPort(inferenceScan.operatorId),
        OperatorPort(mlInferOp.operatorId, 1)
      ),
      LogicalLink(
        OperatorPort(mlTrainingOp.operatorId, 0),
        OperatorPort(mlVizSink.operatorId)
      ),
      LogicalLink(
        OperatorPort(mlTrainingOp.operatorId, 1),
        OperatorPort(mlInferOp.operatorId, 0)
      ),
      LogicalLink(
        OperatorPort(mlInferOp.operatorId, 0),
        OperatorPort(inferenceSink.operatorId, 0)
      )
    )

    val ctx = new WorkflowContext()
    val logicalPlan = LogicalPlan(ctx, operators, links, List())
    val schemaResult = logicalPlan.propagateWorkflowSchema(None).inputSchemaMap

    assert(schemaResult(mlTrainingOp.operatorIdentifier).head.get.equals(dataSchema))
    assert(schemaResult(mlTrainingOp.operatorIdentifier)(1).get.equals(dataSchema))

    assert(schemaResult(mlInferOp.operatorIdentifier).head.get.equals(mlModelSchema))
    assert(schemaResult(mlInferOp.operatorIdentifier)(1).get.equals(dataSchema))

    assert(schemaResult(mlVizSink.operatorIdentifier).head.get.equals(mlVizSchema))
    assert(schemaResult(inferenceSink.operatorIdentifier).head.get.equals(dataSchema))

  }

}
