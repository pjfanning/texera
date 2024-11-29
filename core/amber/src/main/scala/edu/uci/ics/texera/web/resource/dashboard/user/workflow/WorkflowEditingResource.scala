package edu.uci.ics.texera.web.resource.dashboard.user.workflow

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.node.ObjectNode
import com.typesafe.scalalogging.LazyLogging
import edu.uci.ics.amber.engine.common.Utils.objectMapper
import edu.uci.ics.amber.operator.PortDescription
import edu.uci.ics.amber.operator.metadata.OperatorMetadataGenerator
import edu.uci.ics.texera.web.auth.SessionUser
import edu.uci.ics.texera.web.resource.dashboard.user.workflow.WorkflowEditingResource.{
  AddOpRequest,
  LinkDescriptor,
  WorkflowContent
}
import io.dropwizard.auth.Auth

import java.util.UUID
import javax.annotation.security.RolesAllowed
import javax.ws.rs.core.MediaType
import javax.ws.rs.{POST, Path, Produces}
import scala.jdk.CollectionConverters.IteratorHasAsScala
import scala.util.Random

object WorkflowEditingResource {

  case class Point(
      @JsonProperty("x") x: Int,
      @JsonProperty("y") y: Int
  )

  case class Comment(
      @JsonProperty("content") content: String,
      @JsonProperty("creationTime") creationTime: String,
      @JsonProperty("creatorName") creatorName: String,
      @JsonProperty("creatorID") creatorID: Int
  )

  case class CommentBox(
      @JsonProperty("commentBoxID") commentBoxID: String,
      @JsonProperty("comments") comments: List[Comment],
      @JsonProperty("commentBoxPosition") commentBoxPosition: Point
  )

  case class OperatorPredicate(
      @JsonProperty("operatorID") operatorID: String,
      @JsonProperty("operatorType") operatorType: String,
      @JsonProperty("operatorVersion") operatorVersion: String,
      @JsonProperty("operatorProperties") operatorProperties: ObjectNode,
      @JsonProperty("inputPorts") inputPorts: List[PortDescription],
      @JsonProperty("outputPorts") outputPorts: List[PortDescription],
      @JsonProperty("dynamicInputPorts") dynamicInputPorts: Boolean = false,
      @JsonProperty("dynamicOutputPorts") dynamicOutputPorts: Boolean = false,
      @JsonProperty("showAdvanced") showAdvanced: Boolean = false,
      @JsonProperty("isDisabled") isDisabled: Boolean = false,
      @JsonProperty("viewResult") viewResult: Boolean = false,
      @JsonProperty("markedForReuse") markedForReuse: Boolean = false,
      @JsonProperty("customDisplayName") customDisplayName: Option[String] = None
  )

  case class LogicalPort(
      @JsonProperty("operatorID") operatorID: String,
      @JsonProperty("portID") portID: String
  )

  case class OperatorLink(
      @JsonProperty("linkID") linkID: String,
      @JsonProperty("source") source: LogicalPort,
      @JsonProperty("target") target: LogicalPort
  )

  case class WorkflowContent(
      @JsonProperty("operators") operators: List[OperatorPredicate],
      @JsonProperty("operatorPositions") operatorPositions: Map[String, Point],
      @JsonProperty("links") links: List[OperatorLink],
      @JsonProperty("commentBoxes") commentBoxes: List[CommentBox],
      @JsonProperty("settings") settings: Map[String, Int]
  ) {

    def addOperator(
        operatorType: String,
        properties: ObjectNode,
        nextToOpId: String = ""
    ): (WorkflowContent, OperatorPredicate) = {
      val newOperator = convertToOperatorPredicate(operatorType, properties)
      val newOperators = operators :+ newOperator
      val newOperatorId = newOperator.operatorID

      val newPosition = if (nextToOpId.nonEmpty && operatorPositions.contains(nextToOpId)) {
        val referencePosition = operatorPositions(nextToOpId)
        Point(referencePosition.x + 120, referencePosition.y + Random.between(-50, 50))
      } else {
        Point(100, 100)
      }

      val newOperatorPositions = operatorPositions + (newOperatorId -> newPosition)

      (this.copy(operators = newOperators, operatorPositions = newOperatorPositions), newOperator)
    }

    def addLink(
        fromOpId: String,
        fromPortId: String,
        toOpId: String,
        toPortId: String
    ): (WorkflowContent, OperatorLink) = {
      val newLink = OperatorLink(
        linkID = getLinkId(UUID.randomUUID().toString),
        source = LogicalPort(fromOpId, fromPortId),
        target = LogicalPort(toOpId, toPortId)
      )

      val newLinks = links :+ newLink

      (this.copy(links = newLinks), newLink)
    }
  }

  def getLinkId(uuid: String): String = {
    "link" + "-" + uuid
  }

  def convertToOperatorPredicate(
      operatorType: String,
      propertiesNode: ObjectNode
  ): OperatorPredicate = {
    val operatorMetadata = OperatorMetadataGenerator.generateOperatorMetadata(operatorType)

    val definedProperties = operatorMetadata.jsonSchema
      .get("properties")
      .fieldNames()
      .asScala
      .filter(name => name != "dummyPropertyList")
      .toSet

    val filteredProperties = objectMapper.createObjectNode()
    propertiesNode.fieldNames().asScala.foreach { fieldName =>
      if (definedProperties.contains(fieldName)) {
        filteredProperties.set(fieldName, propertiesNode.get(fieldName))
      }
    }

    val dynamicInputPorts = operatorMetadata.additionalMetadata.dynamicInputPorts
    val dynamicOutputPorts = operatorMetadata.additionalMetadata.dynamicOutputPorts

    OperatorPredicate(
      operatorID = operatorType + "-" + UUID.randomUUID().toString,
      operatorType = operatorMetadata.operatorType,
      operatorVersion = operatorMetadata.operatorVersion,
      operatorProperties = filteredProperties,
      inputPorts = List(),
      outputPorts = List(),
      dynamicInputPorts = dynamicInputPorts,
      dynamicOutputPorts = dynamicOutputPorts,
      customDisplayName = Some(operatorMetadata.additionalMetadata.userFriendlyName)
    )
  }

  case class LinkDescriptor(
      sourceOpId: String,
      sourcePortId: String,
      targetPortId: String
  )

  case class AddOpRequest(
      workflowContent: WorkflowContent,
      operatorType: String,
      operatorProperties: ObjectNode,
      links: List[LinkDescriptor]
  )
}

@Produces(Array(MediaType.APPLICATION_JSON))
@RolesAllowed(Array("REGULAR", "ADMIN"))
@Path("/workflow-editing")
class WorkflowEditingResource extends LazyLogging {

  @POST
  @Produces(Array(MediaType.APPLICATION_JSON))
  @Path("/add-operator-and-links")
  def addOpAndLinks(
      request: AddOpRequest,
      @Auth sessionUser: SessionUser
  ): WorkflowContent = {

    // Validate and retrieve the source operators
    val srcOps = request.links.map(desc =>
      request.workflowContent.operators
        .find(op => op.operatorID.equals(desc.sourceOpId))
        .getOrElse(
          throw new RuntimeException(s"Given source op '${desc.sourceOpId}' doesn't exist")
        )
    )

    // First add the operator, update workflowContent
    val (workflowAfterAddOperator, newOp) = request.workflowContent.addOperator(
      request.operatorType,
      request.operatorProperties,
      srcOps.head.operatorID
    )

    // Add links for each source operator descriptor, updating workflowContent in each step
    val workflowAfterLinks =
      request.links.zipWithIndex.foldLeft(workflowAfterAddOperator) {
        case (updatedWorkflow, (srcOpDesc, i)) =>
          val (workflowWithLink, _) = updatedWorkflow.addLink(
            srcOpDesc.sourceOpId,
            srcOpDesc.sourcePortId,
            newOp.operatorID,
            srcOpDesc.targetPortId
          )
          workflowWithLink
      }

    // Return the fully updated WorkflowContent
    workflowAfterLinks
  }
}
