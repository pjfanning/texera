package edu.uci.ics.texera.service.resource

import edu.uci.ics.amber.core.storage.StorageConfig
import edu.uci.ics.texera.dao.SqlServer
import edu.uci.ics.texera.dao.SqlServer.withTransaction
import edu.uci.ics.texera.dao.jooq.generated.tables.daos.WorkflowComputingUnitDao
import edu.uci.ics.texera.dao.jooq.generated.tables.pojos.WorkflowComputingUnit
import edu.uci.ics.texera.service.resource.WorkflowComputingUnitManagingResource.{WorkflowComputingUnitCreationParams, WorkflowPodTerminationParams, context}
import io.kubernetes.client.openapi.models.V1Pod
import jakarta.ws.rs._
import jakarta.ws.rs.core.{MediaType, Response}
import org.jooq.DSLContext
import org.jooq.types.UInteger
import edu.uci.ics.texera.service.util.KubernetesClientService
import io.kubernetes.client.proto.V1.Pod

import java.sql.Timestamp

object WorkflowComputingUnitManagingResource {

  private lazy val context: DSLContext = SqlServer.getInstance(StorageConfig.jdbcUrl, StorageConfig.jdbcUsername, StorageConfig.jdbcPassword).createDSLContext()
  case class WorkflowComputingUnitCreationParams(uid: UInteger) // TODO: add unit type

  case class WorkflowPodTerminationParams(cuid: UInteger)
}

@Produces(Array(MediaType.APPLICATION_JSON))
@Path("/workflowpod")
class WorkflowComputingUnitManagingResource {
  /**
   * Create a new pod for the given workflow wid and workflow content
   *
   * @param param the parameters
   * @return the created pod
   */
  @POST
  @Consumes(Array(MediaType.APPLICATION_JSON))
  @Produces(Array(MediaType.APPLICATION_JSON))
  @Path("/create")
  def createWorkflowComputingUnit(
                 param: WorkflowComputingUnitCreationParams
               ): Response = {
    var newPod: V1Pod = null
    try {
      newPod = new KubernetesClientService().createPod(param.uid.intValue())
    } catch {
      case e: Exception => return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage).build()
    }

    // Set uid, wid, name, pod_uid, creation_time manually
    // terminate_time is set on pod termination
    val computingUnit: WorkflowComputingUnit = new WorkflowComputingUnit()
    computingUnit.setUid(param.uid)
    computingUnit.setName(newPod.getMetadata.getName)
    computingUnit.setCreationTime(Timestamp.from(newPod.getMetadata.getCreationTimestamp.toInstant))


    // Insert the new pod into the database within a transaction
    withTransaction(context) { ctx =>
      val wcDao = new WorkflowComputingUnitDao(ctx.configuration())
      wcDao.insert(computingUnit)
    }

    // If everything succeeds, return the new SQL pod as the response entity
    Response.ok(computingUnit).build()
  }


  /**
    * List all pods created by current user
    *
    * @return
    */
  @GET
  @Path("")
  @Produces(Array(MediaType.APPLICATION_JSON))
  def listComputingUnits(): java.util.List[WorkflowComputingUnit] = {
    withTransaction(context) { ctx =>
      val cuDao = new WorkflowComputingUnitDao(ctx.configuration())
      var units: java.util.List[WorkflowComputingUnit] = null
      units = cuDao.findAll()

      units.removeIf((pod: WorkflowComputingUnit) => pod.getTerminateTime != null)
      units
    }
  }


  /**
    * Terminate the workflow's pod
    * @param param the parameters
    *
    * @return request response
    */
  @POST
  @Consumes(Array(MediaType.APPLICATION_JSON))
  @Produces(Array(MediaType.APPLICATION_JSON))
  @Path("/terminate")
  def terminateComputingUnit(
                    param: WorkflowPodTerminationParams
                  ): Response = {
    new KubernetesClientService().deletePod()
    withTransaction(context) { ctx =>
      val cuDao = new WorkflowComputingUnitDao(ctx.configuration())
      val units = cuDao.fetchByCuid(param.cuid)
      units.forEach(unit =>
        unit.setTerminateTime(new Timestamp(System.currentTimeMillis()))
      )
      cuDao.update(units)
      Response.ok(s"Successfully terminated compute unit with id ${param.cuid}").build()
    }
  }
}
