package edu.uci.ics.texera.workflow.common.workflow

import com.typesafe.scalalogging.LazyLogging
import edu.uci.ics.amber.engine.architecture.deploysemantics.PhysicalLink
import edu.uci.ics.amber.engine.common.virtualidentity.{OperatorIdentity, PhysicalOpIdentity}
import edu.uci.ics.texera.workflow.common.WorkflowContext
import edu.uci.ics.texera.workflow.common.storage.OpResultStorage
import edu.uci.ics.texera.workflow.common.tuple.schema.{OperatorSchemaInfo, Schema}
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
    // get the actual Op from the physical plan. the operators on the link and that on the physical plan
    // are different due to partial rewrite
    val fromOp = physicalPlan.getOperator(physicalLink.id.from)
    val fromOutputPortIdx = fromOp.getPortIdxForOutputLinkId(physicalLink.id)

    // get the actual Op from the physical plan. the operators on the link and that on the physical plan
    // are different due to partial rewrite
    val toOp = physicalPlan.getOperator(physicalLink.id.to)
    val toInputPortIdx = toOp.getPortIdxForInputLinkId(physicalLink.id)

    val storage = opResultStorage.getOrCreate(
      key = s"${fromOp.id.logicalOpId}_port$fromOutputPortIdx",
      mode = OpResultStorage.defaultStorageMode)
    fromOp.resultStorage = Some(storage)

    val materializationReader = new CacheSourceOpDesc(storage)
    materializationReader.setContext(context)
    val matReaderOutputSchema = materializationReader.getOutputSchemas(Array())
    val matReaderOp =
      materializationReader.getPhysicalOp(
        context.executionId,
        OperatorSchemaInfo(Array(), matReaderOutputSchema)
      )

    // create 2 links for materialization
    val readerToDestLink = PhysicalLink(matReaderOp, 0, toOp, toInputPortIdx)

    // add the pair to the map for later adding edges between 2 regions.
    writerReaderPairs(fromOp.id) = matReaderOp.id

    physicalPlan
      .removeEdge(physicalLink)
      .addOperator(matReaderOp)
      .addEdge(readerToDestLink)
      .setOperatorUnblockPort(toOp.id, toInputPortIdx)
      .populatePartitioningOnLinks()

  }

}
