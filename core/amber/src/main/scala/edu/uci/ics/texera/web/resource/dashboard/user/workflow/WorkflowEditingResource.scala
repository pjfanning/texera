package edu.uci.ics.texera.web.resource.dashboard.user.workflow

import com.fasterxml.jackson.databind.node.ObjectNode
import edu.uci.ics.texera.web.resource.dashboard.user.workflow.WorkflowEditingResource.SourceOperatorDescriptor

import javax.ws.rs.core.MediaType
import javax.ws.rs.{POST, Path, Produces}

object WorkflowEditingResource {
  case class SourceOperatorDescriptor(
      sourceOpId: String,
      sourcePortId: String,
                                     )
}

@Path("/workflow-editing")
class WorkflowEditingResource {

  @POST
  @Produces(Array(MediaType.APPLICATION_JSON))
  @Path("/operator/add")
  def addOperators(
      workflowContent: WorkflowContent,
      properties: ObjectNode,
      operatorToConnect: List[SourceOperatorDescriptor] // whether to connect with a operator
                 ): WorkflowContent = {

  }
}
