package edu.uci.ics.texera.workflow.common.workflow

import com.typesafe.scalalogging.LazyLogging
import edu.uci.ics.amber.engine.architecture.deploysemantics.PhysicalLink
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
      physicalLink: PhysicalLink,
      writerReaderPairs: mutable.HashMap[PhysicalOpIdentity, PhysicalOpIdentity]
  ): PhysicalPlan = {

    val fromOp = physicalLink.fromOp
    val fromOutputPortIdx = fromOp.outputToOrdinalMapping(physicalLink.id)._2

    val toOp = physicalLink.toOp

    val toInputPortIdx =
      physicalPlan.getOperator(toOp.id).inputToOrdinalMapping(physicalLink.id)._2


    val materializationWriter = new ProgressiveSinkOpDesc()
    materializationWriter.setContext(context)

    val fromOpIdInputSchema: Array[Schema] =
      if (
        !logicalPlan
          .getOperator(OperatorIdentity(fromOp.id.logicalOpId.id))
          .isInstanceOf[SourceOperatorDescriptor]
      )
        logicalPlan
          .inputSchemaMap(
            logicalPlan.getOperator(fromOp.id.logicalOpId.id).operatorIdentifier
          )
          .map(s => s.get)
          .toArray
      else Array()
    val matWriterInputSchema = logicalPlan
      .getOperator(fromOp.id.logicalOpId.id)
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
    val readerToDestLink = PhysicalLink(matReaderOpExecConfig, 0, toOp, toInputPortIdx, part = null)
    val sourceToWriterLink =
      PhysicalLink(fromOp, fromOutputPortIdx, matWriterOpExecConfig, 0, part = null)

    // add the pair to the map for later adding edges between 2 regions.
    writerReaderPairs(matWriterOpExecConfig.id) = matReaderOpExecConfig.id

    physicalPlan
      .addOperator(matWriterOpExecConfig)
      .addOperator(matReaderOpExecConfig)
      .addEdge(
        fromOp,
        fromOutputPortIdx,
        matWriterOpExecConfig,
        0
      )
      .addEdge(
        matReaderOpExecConfig,
        0,
        toOp,
        toInputPortIdx
      )
      .removeEdge(physicalLink)
      .setOperator(
        toOp.copy(
          // update the input mapping by replacing the original link with the new link from materialization.
          inputToOrdinalMapping =
            toOp.inputToOrdinalMapping - physicalLink.id + (readerToDestLink.id -> (readerToDestLink, toInputPortIdx)),
          // the dest operator's input port is not blocking anymore.
          blockingInputs = toOp.blockingInputs.filter(e => e != toInputPortIdx)
        )
      )
      .setOperator(
        fromOp.copy(
          // update the output mapping by replacing the original link with the new link to materialization.
          outputToOrdinalMapping =
            fromOp.outputToOrdinalMapping - physicalLink.id + (sourceToWriterLink.id -> (sourceToWriterLink, fromOutputPortIdx))
        )
      ).enforcePartition()
  }

}
