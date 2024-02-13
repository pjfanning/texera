package edu.uci.ics.texera.workflow.operators.loop

import edu.uci.ics.amber.engine.architecture.deploysemantics.PhysicalOp
import edu.uci.ics.amber.engine.architecture.deploysemantics.layer.OpExecInitInfo
import edu.uci.ics.amber.engine.common.virtualidentity.{ExecutionIdentity, WorkflowIdentity}
import edu.uci.ics.amber.engine.common.workflow.{InputPort, OutputPort}
import edu.uci.ics.texera.workflow.common.metadata.{OperatorGroupConstants, OperatorInfo}
import edu.uci.ics.texera.workflow.common.operators.LogicalOp
import edu.uci.ics.texera.workflow.common.operators.source.SourceOperatorDescriptor
import edu.uci.ics.texera.workflow.common.tuple.schema.{Attribute, Schema}


class AttributeUnit {
    def getOriginalAttribute: String = ???
}

class GeneratorOpDesc extends SourceOperatorDescriptor {

  var attributes: List[AttributeUnit] = List()
  override def getPhysicalOp(
      workflowId: WorkflowIdentity,
      executionId: ExecutionIdentity
  ): PhysicalOp = PhysicalOp
      .oneToOnePhysicalOp(
        workflowId,
        executionId,
        operatorIdentifier,
        OpExecInitInfo((_, _, _) => new LoopEndOpExec())
      )
      .withInputPorts(operatorInfo.inputPorts, inputPortToSchemaMapping)
      .withOutputPorts(operatorInfo.outputPorts, outputPortToSchemaMapping)
      .withSuggestedWorkerNum(1)

  override def operatorInfo: OperatorInfo =
    OperatorInfo(
      "Generator",
      "Generator",
      OperatorGroupConstants.CONTROL_GROUP,
      inputPorts = List.empty,
      outputPorts = List(OutputPort()),
      supportReconfiguration = true
    )

  override def sourceSchema(): Schema = Schema.newBuilder.add(
      attributes
        .map(attribute =>
          new Attribute(
            attribute.getAlias,
            schemas(0).getAttribute(attribute.getOriginalAttribute).getType
          )
        )
        .asJava
    )
    .build()
}
