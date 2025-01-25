package edu.uci.ics.texera.web.resource.dashboard.hub.dataset

import edu.uci.ics.amber.core.storage.StorageConfig
import edu.uci.ics.texera.dao.SqlServer
import edu.uci.ics.texera.dao.jooq.generated.Tables.{DATASET, DATASET_USER_LIKES}
import edu.uci.ics.texera.web.resource.dashboard.hub.workflow.HubWorkflowResource.recordUserActivity
import org.jooq.types.UInteger

import java.util.regex.Pattern
import javax.servlet.http.HttpServletRequest
import javax.ws.rs.core.{Context, MediaType}
import javax.ws.rs.{Consumes, POST, Path}

object HubDatasetResource {
  final private lazy val context = SqlServer
    .getInstance(StorageConfig.jdbcUrl, StorageConfig.jdbcUsername, StorageConfig.jdbcPassword)
    .createDSLContext()
}

class HubDatasetResource {
  final private lazy val context = SqlServer
    .getInstance(StorageConfig.jdbcUrl, StorageConfig.jdbcUsername, StorageConfig.jdbcPassword)
    .createDSLContext()

  @POST
  @Path("/like")
  @Consumes(Array(MediaType.APPLICATION_JSON))
  def likeWorkflow(@Context request: HttpServletRequest, likeRequest: Array[UInteger]): Boolean = {
    if (likeRequest.length != 2) {
      return false
    }

    val datasetId = likeRequest(0)
    val userId = likeRequest(1)

    val existingLike = context
      .selectFrom(DATASET_USER_LIKES)
      .where(
        DATASET_USER_LIKES.UID
          .eq(userId)
          .and(DATASET_USER_LIKES.DID.eq(datasetId))
      )
      .fetchOne()

    if (existingLike == null) {
      context
        .insertInto(WORKFLOW_USER_LIKES)
        .set(WORKFLOW_USER_LIKES.UID, userId)
        .set(WORKFLOW_USER_LIKES.WID, workflowId)
        .execute()

      recordUserActivity(request, userId, workflowId, "like")
      true
    } else {
      false
    }
  }
}
