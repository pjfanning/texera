package edu.uci.ics.texera.workflow.common.workflow

import com.typesafe.scalalogging.LazyLogging
import edu.uci.ics.amber.engine.common.virtualidentity.{
  OperatorIdentity,
  PhysicalLinkIdentity,
  PhysicalOpIdentity
}
import edu.uci.ics.texera.workflow.common.WorkflowContext
import edu.uci.ics.texera.workflow.common.operators.source.SourceOperatorDescriptor
import edu.uci.ics.texera.workflow.common.storage.OpResultStorage
import edu.uci.ics.texera.workflow.common.tuple.schema.{OperatorSchemaInfo, Schema}
import edu.uci.ics.texera.workflow.operators.sink.managed.ProgressiveSinkOpDesc
import edu.uci.ics.texera.workflow.operators.source.cache.CacheSourceOpDesc

import scala.collection.mutable

class MaterializationRewriter(
    val context: WorkflowContext,
    val opResultStorage: OpResultStorage
) extends LazyLogging {

  def addMaterializationToLink(
      physicalPlan: PhysicalPlan,
      logicalPlan: LogicalPlan,
      physicalLink: PhysicalLinkIdentity,
      writerReaderPairs: mutable.HashMap[PhysicalOpIdentity, PhysicalOpIdentity]
  ): PhysicalPlan = {

    val fromPhysicalOpId = physicalLink.from
    val fromPhysicalOp = physicalPlan.getOperator(fromPhysicalOpId)
    val fromOutputPortIdx = fromPhysicalOp.outputToOrdinalMapping(physicalLink)
    val fromOutputPortName = fromPhysicalOp.outputPorts(fromOutputPortIdx).displayName
    val toPhysicalOpId = physicalLink.to
    val toPhysicalOp = physicalPlan.getOperator(toPhysicalOpId)
    val toInputPortIdx =
      physicalPlan.getOperator(toPhysicalOpId).inputToOrdinalMapping(physicalLink)
    val toInputPortName = toPhysicalOp.inputPorts(toInputPortIdx).displayName

    val materializationWriter = new ProgressiveSinkOpDesc()
    materializationWriter.setContext(context)

    val fromOpIdInputSchema: Array[Schema] =
      if (
        !logicalPlan
          .getOperator(OperatorIdentity(fromPhysicalOpId.logicalOpId.id))
          .isInstanceOf[SourceOperatorDescriptor]
      )
        logicalPlan
          .inputSchemaMap(
            logicalPlan.getOperator(fromPhysicalOpId.logicalOpId.id).operatorIdentifier
          )
          .map(s => s.get)
          .toArray
      else Array()
    val matWriterInputSchema = logicalPlan
      .getOperator(fromPhysicalOpId.logicalOpId.id)
      .getOutputSchemas(
        fromOpIdInputSchema
      )(fromOutputPortIdx)
    val matWriterOutputSchema =
      materializationWriter.getOutputSchemas(Array(matWriterInputSchema))(0)
    materializationWriter.setStorage(
      opResultStorage.create(
        key = materializationWriter.operatorIdentifier,
        mode = OpResultStorage.defaultStorageMode
      )
    )
    opResultStorage.get(materializationWriter.operatorIdentifier).setSchema(matWriterOutputSchema)
    val matWriterOpExecConfig =
      materializationWriter.getPhysicalOp(
        context.executionId,
        OperatorSchemaInfo(Array(matWriterInputSchema), Array(matWriterOutputSchema))
      )

    val materializationReader = new CacheSourceOpDesc(
      materializationWriter.operatorIdentifier,
      opResultStorage: OpResultStorage
    )
    materializationReader.setContext(context)
    materializationReader.schema = materializationWriter.getStorage.getSchema
    val matReaderOutputSchema = materializationReader.getOutputSchemas(Array())
    val matReaderOpExecConfig =
      materializationReader.getPhysicalOp(
        context.executionId,
        OperatorSchemaInfo(Array(), matReaderOutputSchema)
      )

    // create 2 links for materialization
    val readerToDestLink = PhysicalLinkIdentity(matReaderOpExecConfig.id, 0, toPhysicalOpId, toInputPortIdx)
    val sourceToWriterLink =
      PhysicalLinkIdentity(fromPhysicalOpId, fromOutputPortIdx, matWriterOpExecConfig.id, 0)
    // add the pair to the map for later adding edges between 2 regions.
    writerReaderPairs(matWriterOpExecConfig.id) = matReaderOpExecConfig.id

    physicalPlan
      .addOperator(matWriterOpExecConfig)
      .addOperator(matReaderOpExecConfig)
      .addEdge(
        fromPhysicalOpId,
        fromOutputPortIdx,
        matWriterOpExecConfig.id,
        0
      )
      .addEdge(
        matReaderOpExecConfig.id,
        0,
        toPhysicalOpId,
        toInputPortIdx
      )
      .removeEdge(physicalLink)
      .setOperator(
        toPhysicalOp.copy(
          // update the input mapping by replacing the original link with the new link from materialization.
          inputToOrdinalMapping =
            toPhysicalOp.inputToOrdinalMapping - physicalLink + (readerToDestLink -> toInputPortIdx),
          // the dest operator's input port is not blocking anymore.
          blockingInputs = toPhysicalOp.blockingInputs.filter(e => e != toInputPortIdx)
        )
      )
      .setOperator(
        fromPhysicalOp.copy(
          // update the output mapping by replacing the original link with the new link to materialization.
          outputToOrdinalMapping =
            fromPhysicalOp.outputToOrdinalMapping - physicalLink + (sourceToWriterLink -> fromOutputPortIdx)
        )
      )
  }

}
