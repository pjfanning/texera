package edu.uci.ics.texera.web.resource.dashboard.user.workflow

import com.fasterxml.jackson.annotation.{JsonProperty, JsonTypeInfo}
import com.fasterxml.jackson.databind.node.ObjectNode
import edu.uci.ics.amber.engine.common.Utils.objectMapper
import edu.uci.ics.amber.engine.common.workflow.PortIdentity
import edu.uci.ics.texera.workflow.common.metadata.OperatorMetadataGenerator
import edu.uci.ics.texera.workflow.common.operators.{LogicalOp, PortDescription}
import edu.uci.ics.texera.workflow.common.workflow.{LogicalLink, PartitionInfo}

import java.util.UUID
import scala.jdk.CollectionConverters._
import scala.util.Random

case class Point(x: Int, y: Int)

case class Comment(
                    content: String,
                    creationTime: String,
                    creatorName: String,
                    creatorID: Int
                  )

case class CommentBox(
                       commentBoxID: String,
                       comments: List[Comment],
                       commentBoxPosition: Point
                     )

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "operatorType")
case class OperatorPredicate(
                              operatorID: String,
                              operatorType: String,
                              operatorVersion: String,
                              operatorProperties: ObjectNode, // Changed to ObjectNode
                              inputPorts: List[PortDescription],
                              outputPorts: List[PortDescription],
                              dynamicInputPorts: Boolean = false,
                              dynamicOutputPorts: Boolean = false,
                              showAdvanced: Boolean = false,
                              isDisabled: Boolean = false,
                              viewResult: Boolean = false,
                              markedForReuse: Boolean = false,
                              customDisplayName: Option[String] = None
                            )

case class LogicalPort(
                        operatorID: String,
                        portID: String
                      )

case class OperatorLink(
                         linkID: String,
                         source: LogicalPort,
                         target: LogicalPort
                       )

case class WorkflowContent(
                            @JsonProperty("operators") operators: List[OperatorPredicate],
                            @JsonProperty("operatorPositions") operatorPositions: Map[String, Point],
                            @JsonProperty("links") links: List[OperatorLink],
                            @JsonProperty("commentBoxes") commentBoxes: List[CommentBox],
                            @JsonProperty("settings") settings: Map[String, Any]
                          )

object WorkflowConversionUtil {

  /**
    * Converts a LogicalOp to OperatorPredicate by extracting properties and metadata.
    *
    * @param logicalOp the LogicalOp to convert.
    * @return the corresponding OperatorPredicate.
    */
  def convertToOperatorPredicate(logicalOp: LogicalOp): OperatorPredicate = {
    // Retrieve operator metadata for the class
    val operatorClass = logicalOp.getClass
    val metadata = OperatorMetadataGenerator.operatorTypeMap.getOrElse(
      operatorClass,
      throw new RuntimeException(s"Metadata not found for class: ${operatorClass.getSimpleName}")
    )

    val operatorMetadata = OperatorMetadataGenerator.generateOperatorMetadata(operatorClass)

    // Extract operator properties using the JSON schema
    val propertiesNode = objectMapper.valueToTree[ObjectNode](logicalOp)
    val definedProperties = operatorMetadata.jsonSchema
      .get("properties")
      .fieldNames()
      .asScala
      .toSet

    // Create a new ObjectNode for filtered properties
    val filteredProperties = objectMapper.createObjectNode()
    propertiesNode.fieldNames().asScala.foreach { fieldName =>
      if (definedProperties.contains(fieldName)) {
        filteredProperties.set(fieldName, propertiesNode.get(fieldName))
      }
    }

    // Extract dynamic input/output ports from metadata
    val dynamicInputPorts = operatorMetadata.additionalMetadata.dynamicInputPorts
    val dynamicOutputPorts = operatorMetadata.additionalMetadata.dynamicOutputPorts

    OperatorPredicate(
      operatorID = logicalOp.operatorIdentifier.toString,
      operatorType = logicalOp.getClass.getSimpleName,
      operatorVersion = logicalOp.operatorVersion,
      operatorProperties = filteredProperties, // Use the filtered properties ObjectNode
      inputPorts = logicalOp.inputPorts,
      outputPorts = logicalOp.outputPorts,
      dynamicInputPorts = dynamicInputPorts,
      dynamicOutputPorts = dynamicOutputPorts
    )
  }

  /**
    * Converts a LogicalLink to OperatorLink with a generated UUID for the link ID.
    *
    * @param logicalLink the LogicalLink to convert.
    * @return the corresponding OperatorLink.
    */
  def convertToOperatorLink(logicalLink: LogicalLink): OperatorLink = {
    OperatorLink(
      linkID = UUID.randomUUID().toString,
      source = LogicalPort(logicalLink.fromOpId.toString, s"port-${logicalLink.fromPortId.id}"),
      target = LogicalPort(logicalLink.toOpId.toString, s"port-${logicalLink.toPortId.id}")
    )
  }
}

object WorkflowContentUtil {

  /**
    * Constructs a WorkflowContent from lists of LogicalOps and LogicalLinks.
    *
    * @param operators the list of LogicalOp.
    * @param links the list of LogicalLink.
    * @return the constructed WorkflowContent.
    */
  def createWorkflowContent(operators: List[LogicalOp], links: List[LogicalLink]): WorkflowContent = {
    val operatorPredicates = operators.map(WorkflowConversionUtil.convertToOperatorPredicate)
    val operatorPositions = operatorPredicates.map(op => op.operatorID -> Point(100, 100)).toMap
    val operatorLinks = links.map(WorkflowConversionUtil.convertToOperatorLink)

    WorkflowContent(
      operators = operatorPredicates,
      operatorPositions = operatorPositions,
      links = operatorLinks,
      commentBoxes = List.empty,
      settings = Map.empty
    )
  }
}

object WorkflowEditionUtil {

  /**
    * Adds a LogicalOp to the WorkflowContent and updates the position.
    *
    * @param content   the current WorkflowContent.
    * @param logicalOp the LogicalOp to be added.
    * @param nextTo    the operator ID to place the new operator next to (optional).
    * @return the updated WorkflowContent.
    */
  def addOperator(content: WorkflowContent, logicalOp: LogicalOp, nextTo: String = ""): WorkflowContent = {
    val newOperators = content.operators :+ WorkflowConversionUtil.convertToOperatorPredicate(logicalOp)
    val newOperatorId = logicalOp.operatorIdentifier.toString

    // Determine the new operator's position
    val newPosition = if (nextTo.nonEmpty && content.operatorPositions.contains(nextTo)) {
      val referencePosition = content.operatorPositions(nextTo)
      // Place it slightly to the right and below the referenced operator
      Point(referencePosition.x + 120, referencePosition.y + Random.between(-50, 50))
    } else {
      // Default position if `nextTo` is not provided
      Point(100, 100)
    }

    val newOperatorPositions = content.operatorPositions + (newOperatorId -> newPosition)

    content.copy(operators = newOperators, operatorPositions = newOperatorPositions)
  }

  /**
    * Adds a link between two LogicalOps in the WorkflowContent.
    *
    * @param content  the current WorkflowContent.
    * @param fromOp   the source LogicalOp.
    * @param fromPort the source PortIdentity.
    * @param toOp     the target LogicalOp.
    * @param toPort   the target PortIdentity.
    * @return the updated WorkflowContent.
    */
  def addLink(
               content: WorkflowContent,
               fromOp: LogicalOp,
               fromPort: PortIdentity,
               toOp: LogicalOp,
               toPort: PortIdentity
             ): WorkflowContent = {
    val newLink = WorkflowConversionUtil.convertToOperatorLink(
      LogicalLink(
        fromOpId = fromOp.operatorIdentifier,
        fromPortId = fromPort,
        toOpId = toOp.operatorIdentifier,
        toPortId = toPort
      )
    )

    val newLinks = content.links :+ newLink

    content.copy(links = newLinks)
  }

  def main(args: Array[String]): Unit = {
    val workflowJson =
      """
          {
            "operators": [
              {
                "operatorID": "CSVFileScan-operator-0fb45eb2-3117-45a2-8245-420e569cc57a",
                "operatorType": "CSVFileScan",
                "operatorVersion": "fe684b5e5120c6a24077422336e82c44a24f3c14",
                "operatorProperties": {
                  "fileEncoding": "UTF_8",
                  "customDelimiter": ",",
                  "hasHeader": true,
                  "fileName": "/bob@test.com/test dataset/v3/FileResolver.scala"
                },
                "inputPorts": [],
                "outputPorts": [
                  {
                    "portID": "output-0",
                    "displayName": "",
                    "allowMultiInputs": false,
                    "isDynamicPort": false
                  }
                ],
                "showAdvanced": false,
                "isDisabled": false,
                "customDisplayName": "CSV File Scan",
                "dynamicInputPorts": false,
                "dynamicOutputPorts": false
              }
            ],
            "operatorPositions": {
              "CSVFileScan-operator-0fb45eb2-3117-45a2-8245-420e569cc57a": {
                "x": 364,
                "y": 220
              }
            },
            "links": [],
            "commentBoxes": [],
            "settings": {
              "dataTransferBatchSize": 400
            }
          }
        """

    val workflowContent = objectMapper.readValue(workflowJson, classOf[WorkflowContent])
    println("Deserialized WorkflowContent:")
    println(workflowContent)
  }
}