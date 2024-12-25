package edu.uci.ics.texera.service.resource

import edu.uci.ics.amber.core.storage.StorageConfig
import edu.uci.ics.texera.dao.SqlServer
import edu.uci.ics.texera.dao.SqlServer.withTransaction
import edu.uci.ics.texera.dao.jooq.generated.tables.daos.WorkflowComputingUnitDao
import edu.uci.ics.texera.dao.jooq.generated.tables.pojos.WorkflowComputingUnit
import edu.uci.ics.texera.service.resource.WorkflowComputingUnitManagingResource.{
  WorkflowComputingUnitCreationParams,
  WorkflowComputingUnitTerminationParams,
  context
}
import edu.uci.ics.texera.service.util.KubernetesClientService

import io.kubernetes.client.openapi.models.V1Pod
import jakarta.ws.rs._
import jakarta.ws.rs.core.{MediaType, Response}
import org.jooq.DSLContext
import org.jooq.types.UInteger

import java.net.URI
import java.sql.Timestamp

object WorkflowComputingUnitManagingResource {

  private lazy val context: DSLContext = SqlServer
    .getInstance(StorageConfig.jdbcUrl, StorageConfig.jdbcUsername, StorageConfig.jdbcPassword)
    .createDSLContext()

  case class WorkflowComputingUnitCreationParams(uid: UInteger, name: String) // TODO: add unit type if needed

  case class WorkflowComputingUnitTerminationParams(uri: String, name: String)
}

@Produces(Array(MediaType.APPLICATION_JSON))
@Path("/computing-unit")
class WorkflowComputingUnitManagingResource {

  /**
    * Create a new pod for the given user ID.
    *
    * @param param The parameters containing the user ID.
    * @return The created pod or an error response.
    */
  @POST
  @Consumes(Array(MediaType.APPLICATION_JSON))
  @Produces(Array(MediaType.APPLICATION_JSON))
  @Path("/create")
  def createWorkflowComputingUnit(param: WorkflowComputingUnitCreationParams): Response = {
    var computingUnit: WorkflowComputingUnit = null
    var newPod: V1Pod = null

    try {
      withTransaction(context) { ctx =>
        val wcDao = new WorkflowComputingUnitDao(ctx.configuration())

        // Create a new database entry and insert to generate cuid
        computingUnit = new WorkflowComputingUnit()
        computingUnit.setUid(param.uid)
        computingUnit.setName(param.name)
        computingUnit.setCreationTime(new Timestamp(System.currentTimeMillis()))
        wcDao.insert(computingUnit)

        // Retrieve the generated cuid
        val cuid = ctx.lastID().intValue()

        // Create the pod with the generated cuid
        newPod = KubernetesClientService.createPod(cuid.intValue())

        // Update the computing unit with the pod name and creation time
        computingUnit.setName(newPod.getMetadata.getName)
        computingUnit.setCreationTime(
          Timestamp.from(newPod.getMetadata.getCreationTimestamp.toInstant)
        )

        wcDao.update(computingUnit)
      }
    }
    Response.ok(computingUnit).build()
  }

  /**
    * List all computing units created by the current user.
    *
    * @return A list of computing units that are not terminated.
    */
  @GET
  @Path("")
  @Produces(Array(MediaType.APPLICATION_JSON))
  def listComputingUnits(): java.util.List[WorkflowComputingUnit] = {
    withTransaction(context) { ctx =>
      val cuDao = new WorkflowComputingUnitDao(ctx.configuration())
      cuDao.findAll()
    }
  }

  /**
    * Terminate the computing unit's pod based on the pod URI.
    *
    * @param param The parameters containing the pod URI.
    * @return A response indicating success or failure.
    */
  @POST
  @Consumes(Array(MediaType.APPLICATION_JSON))
  @Produces(Array(MediaType.APPLICATION_JSON))
  @Path("/terminate")
  def terminateComputingUnit(param: WorkflowComputingUnitTerminationParams): Response = {
      // Attempt to delete the pod using the provided URI
    val podURI = URI.create(param.uri)
    KubernetesClientService.deletePod(podURI)

      // If successful, update the database
      withTransaction(context) { ctx =>
        val cuDao = new WorkflowComputingUnitDao(ctx.configuration())
        val cuid = KubernetesClientService.parseCUIDFromURI(podURI)
        val units = cuDao.fetchByCuid(UInteger.valueOf(cuid))

        units.forEach(unit => unit.setTerminateTime(new Timestamp(System.currentTimeMillis())))
        cuDao.update(units)
      }

      Response.ok(s"Successfully terminated compute unit with URI $podURI").build()

  }
}
