package edu.uci.ics.texera.web.resource.dashboard.hub

import edu.uci.ics.texera.web.SqlServer
import edu.uci.ics.texera.web.model.jooq.generated.Tables._
import org.jooq.{Record, Result}

import javax.annotation.security.RolesAllowed
import javax.ws.rs._
import javax.ws.rs.core.MediaType

@Produces(Array(MediaType.APPLICATION_JSON))
@RolesAllowed(Array("REGULAR", "ADMIN"))
@Path("/workflow")
class HubWorkflowResource {
  final private lazy val context = SqlServer.createDSLContext()

  @GET
  @Path("/user-workflow-ids")
  def retrieveWorkflows(): Result[Record] = {
    context
      .select()
      .from(WORKFLOW)
      .fetch()
  }
}
