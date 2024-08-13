package edu.uci.ics.texera.web.resource

import com.typesafe.scalalogging.LazyLogging
import edu.uci.ics.amber.engine.common.virtualidentity.WorkflowIdentity
import edu.uci.ics.texera.Utils
import edu.uci.ics.texera.web.auth.SessionUser
import edu.uci.ics.texera.web.model.http.response.SchemaPropagationResponse
import edu.uci.ics.texera.web.model.websocket.request.LogicalPlanPojo
import edu.uci.ics.texera.web.service.WorkflowEditingService.staticCheckWorkflow
import edu.uci.ics.texera.workflow.common.WorkflowContext
import edu.uci.ics.texera.workflow.common.tuple.schema.Attribute
import edu.uci.ics.texera.workflow.common.workflow.{LogicalPlan, PhysicalPlan}
import io.dropwizard.auth.Auth
import org.jooq.types.UInteger

import javax.annotation.security.RolesAllowed
import javax.ws.rs._
import javax.ws.rs.core.MediaType
import scala.collection.immutable.Map

case class WorkflowEditingResponse(
   operatorIdToInputSchemas: Map[String, List[Option[List[Attribute]]]],
   operatorIdToError: Map[String, String]
                                      )

@Consumes(Array(MediaType.APPLICATION_JSON))
@Produces(Array(MediaType.APPLICATION_JSON))
@Path("/workflow/editing")
class WorkflowEditingResource extends LazyLogging {

  @POST
  @Path("/{wid}")
  @RolesAllowed(Array("REGULAR", "ADMIN"))
  def editWorkflow(
      workflowStr: String,
      @PathParam("wid") wid: UInteger,
      @Auth sessionUser: SessionUser
  ): WorkflowEditingResponse = {

    val logicalPlanPojo = Utils.objectMapper.readValue(workflowStr, classOf[LogicalPlanPojo])

    val context = new WorkflowContext(
      userId = Option(sessionUser.getUser.getUid),
      workflowId = WorkflowIdentity(wid.toString.toLong)
    )

    val (operatorIdToInputSchemasMapping, operatorIdToError) = staticCheckWorkflow(logicalPlanPojo, context)

    WorkflowEditingResponse(
      operatorIdToInputSchemasMapping.map {
        case (logicalOpId, schemas) =>
          logicalOpId.id -> schemas.map(_.map(_.getAttributes))
      },
      operatorIdToError.map {
        case (logicalOpId, error) =>
          logicalOpId.id -> error.toString
      }
    )
  }
}
