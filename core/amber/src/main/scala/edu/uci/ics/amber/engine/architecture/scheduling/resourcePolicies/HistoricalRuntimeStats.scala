package edu.uci.ics.amber.engine.architecture.scheduling.resourcePolicies

import edu.uci.ics.amber.engine.common.virtualidentity.PhysicalOpIdentity
import edu.uci.ics.texera.web.SqlServer
import edu.uci.ics.texera.web.model.jooq.generated.tables.daos.{
  WorkflowDao,
  WorkflowExecutionsDao,
  WorkflowRuntimeStatisticsDao
}
import edu.uci.ics.texera.web.model.jooq.generated.tables.pojos.WorkflowRuntimeStatistics
import org.jooq.DSLContext

import java.io.{BufferedWriter, FileWriter}
import scala.collection.mutable
import scala.jdk.CollectionConverters._

class HistoricalRuntimeStats() {
  lazy val context: DSLContext = SqlServer.createDSLContext()
  val workflowRuntimeStatisticsDao = new WorkflowRuntimeStatisticsDao(context.configuration)
  val workflowDao = new WorkflowDao(context.configuration)
  val workflowExecutionsDao = new WorkflowExecutionsDao(context.configuration())

  def getHistoricalRuntimeStatsByOperatorVersion(
      physicalOpIdentity: PhysicalOpIdentity
  ): mutable.Buffer[WorkflowRuntimeStatistics] = {
    val historicalRuntimeStats =
      workflowRuntimeStatisticsDao.fetchByOperatorId(physicalOpIdentity.logicalOpId.id)

    val file = new BufferedWriter(new FileWriter("history.txt"))

    try {
      file.write(historicalRuntimeStats.size.toString)
      historicalRuntimeStats.forEach { key =>
        file.write(key.getOperatorId)
        file.write(" ")
        file.write(key.getExecutionId.toString)
        file.write(" ")
        file.write(key.getWorkflowId.toString)
        file.write(" ")
        file.newLine()
      }
    } finally {
      file.close()
    }

    if (historicalRuntimeStats.isEmpty)
      mutable.Buffer.empty[WorkflowRuntimeStatistics]
    else {
      val latestExecution = historicalRuntimeStats.asScala.maxBy(_.getExecutionId.intValue())
      val latestVersionId =
        workflowExecutionsDao.fetchByEid(latestExecution.getExecutionId).get(0).getVid

      val executionIdsMatchedLatestVersion =
        workflowExecutionsDao.fetchByVid(latestVersionId).asScala.map(_.getEid.intValue()).toSet

      val ans = historicalRuntimeStats.asScala.filter(record =>
        executionIdsMatchedLatestVersion.contains(record.getExecutionId.intValue())
      )
      ans
    }
  }
}
