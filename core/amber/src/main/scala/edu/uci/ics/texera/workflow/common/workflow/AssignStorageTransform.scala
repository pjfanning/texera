package edu.uci.ics.texera.workflow.common.workflow

import edu.uci.ics.amber.engine.common.virtualidentity.OperatorIdentity
import edu.uci.ics.texera.Utils.objectMapper
import edu.uci.ics.texera.web.service.ExecutionsMetadataPersistService
import edu.uci.ics.texera.workflow.common.storage.OpResultStorage
import edu.uci.ics.texera.workflow.operators.visualization.VisualizationConstants

object AssignStorageTransform {

  def transform(opsToViewResult: List[String],
                        opResultStorage: OpResultStorage,
                        logicalPlan: LogicalPlan,
                               ): LogicalPlan = {
    // create a JSON object that holds pointers to the workflow's results in Mongo
    // TODO in the future, will extract this logic from here when we need pointers to the stats storage
    val resultsJSON = objectMapper.createObjectNode()
    val sinksPointers = objectMapper.createArrayNode()
    // assign storage to texera-managed sinks before generating exec config
    // for any terminal operator, attach a sink storage
    (logicalPlan.getTerminalOperatorIds ++ opsToViewResult.map(idString => OperatorIdentity(idString))).toSet.foreach {
      opId: OperatorIdentity =>
        val logicalOp = logicalPlan.getOperator(opId)
        logicalOp.outputPortsInfo.indices.foreach{
          portIdx =>
            val port = logicalOp.outputPortsInfo(portIdx)
            // due to the size limit of single document in mongoDB (16MB)
            // for sinks visualizing HTMLs which could possibly be large in size, we always use the memory storage.
            val storageType = {
              if (port.outputDescriptor.chartType.contains(VisualizationConstants.HTML_VIZ)) OpResultStorage.MEMORY
              else OpResultStorage.defaultStorageMode
            }
            val storage = opResultStorage.getOrCreate(
              logicalOp.getContext.executionId + "_",
              s"${opId}_port$portIdx",
              storageType
            )
            storage.setSchema(logicalPlan.getOpOutputSchemas(opId)(portIdx))
            port.storage = Some(storage)
            // add the sink collection name to the JSON array of sinks
            sinksPointers.add(logicalOp.getContext.executionId + "_" + opId)
        }
    }
    // update execution entry in MySQL to have pointers to the mongo collections
    resultsJSON.set("results", sinksPointers)
    ExecutionsMetadataPersistService.updateExistingExecutionVolumePointers(
      logicalPlan.context.executionId,
      resultsJSON.toString
    )
    logicalPlan
  }

}
