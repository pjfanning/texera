package edu.uci.ics.texera.web.resource.dashboard.user.workflow

import com.fasterxml.jackson.annotation.{JsonProperty, JsonTypeInfo}
import com.fasterxml.jackson.databind.node.ObjectNode
import edu.uci.ics.amber.engine.common.Utils.objectMapper
import edu.uci.ics.amber.engine.common.workflow.{InputPort, PortIdentity}
import edu.uci.ics.texera.web.model.websocket.request.LogicalPlanPojo
import edu.uci.ics.texera.web.resource.dashboard.user.workflow.WorkflowConversionUtil.getLinkId
import edu.uci.ics.texera.workflow.common.metadata.{OperatorMetadata, OperatorMetadataGenerator}
import edu.uci.ics.texera.workflow.common.operators.{LogicalOp, PortDescription}
import edu.uci.ics.texera.workflow.common.storage.FileResolver
import edu.uci.ics.texera.workflow.common.workflow.{LogicalLink, LogicalPlan, PartitionInfo}
import edu.uci.ics.texera.workflow.operators.projection.{AttributeUnit, ProjectionOpDesc}
import edu.uci.ics.texera.workflow.operators.source.scan.csv.CSVScanSourceOpDesc

import java.io.{File, PrintWriter}
import java.util.UUID
import scala.jdk.CollectionConverters._
import scala.util.Random

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

//@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "operatorType")
case class OperatorPredicate(
                              @JsonProperty("operatorID") operatorID: String,
                              @JsonProperty("operatorType") operatorType: String,
                              @JsonProperty("operatorVersion") operatorVersion: String,
                              @JsonProperty("operatorProperties") operatorProperties: ObjectNode, // Changed to ObjectNode
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
                          )

object WorkflowConversionUtil {

  def extractOperatorUUIDFromLogicalOp(input: String): String = {
    input.split("-", 2).last
  }

  def getOperatorPredicateId(operatorType: String, uuid: String): String = {
    operatorType + "-" + "operator" + "-" + uuid
  }

  def getLinkId(uuid: String): String = {
    "link" + "-" + uuid
  }

  def getPortId(portId: Int, isInput: Boolean): String = {
    if (isInput)
      "input" + "-" + portId
    else
      "output" + "-" + portId
  }

  def convertInputPortsToPortDescriptions(operatorMetadata: OperatorMetadata): List[PortDescription] = {
    operatorMetadata.additionalMetadata.inputPorts.map(port => {
      PortDescription(
        portID = getPortId(port.id.id, isInput = true),
        displayName = port.displayName,
        allowMultiInputs = port.allowMultiLinks,
        isDynamicPort = operatorMetadata.additionalMetadata.dynamicInputPorts,
        partitionRequirement = null,
        dependencies = port.dependencies.toList.map(id => id.id)
      )
    })
  }

  def convertOutputPortsToPortDescriptions(operatorMetadata: OperatorMetadata): List[PortDescription] = {
    operatorMetadata.additionalMetadata.outputPorts.map(port => {
      PortDescription(
        portID = getPortId(port.id.id, isInput = false),
        displayName = port.displayName,
        allowMultiInputs = false,
        isDynamicPort = operatorMetadata.additionalMetadata.dynamicOutputPorts,
        partitionRequirement = null,
      )
    })
  }

  def convertToOperatorPredicate(logicalOp: LogicalOp): OperatorPredicate = {
    val operatorClass = logicalOp.getClass
    val operatorMetadata = OperatorMetadataGenerator.generateOperatorMetadata(operatorClass)

    val propertiesNode = objectMapper.valueToTree[ObjectNode](logicalOp)
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

    val logicalOpUUID = extractOperatorUUIDFromLogicalOp(logicalOp.operatorIdentifier.id)
    OperatorPredicate(
      operatorID = getOperatorPredicateId(operatorMetadata.operatorType, logicalOpUUID),
      operatorType = operatorMetadata.operatorType,
      operatorVersion = logicalOp.operatorVersion,
      operatorProperties = filteredProperties,
      inputPorts = convertInputPortsToPortDescriptions(operatorMetadata),
      outputPorts = convertOutputPortsToPortDescriptions(operatorMetadata),
      dynamicInputPorts = dynamicInputPorts,
      dynamicOutputPorts = dynamicOutputPorts,
      customDisplayName = Some(operatorMetadata.additionalMetadata.userFriendlyName)
    )
  }

  def convertToOperatorLink(logicalLink: LogicalLink): OperatorLink = {
    OperatorLink(
      linkID = getLinkId(UUID.randomUUID().toString),
      source = LogicalPort(logicalLink.fromOpId.toString, getPortId(logicalLink.fromPortId.id, isInput = false)),
      target = LogicalPort(logicalLink.toOpId.toString, getPortId(logicalLink.toPortId.id, isInput = true))
    )
  }

  def createWorkflowContent(logicalPlanPojo: LogicalPlanPojo): WorkflowContent = {
    // Define the ranges for x and y coordinates
    val xRange = 0 to 1000
    val yRange = 0 to 1000

    val operatorPredicates = logicalPlanPojo.operators.map(WorkflowConversionUtil.convertToOperatorPredicate)

    // Generate random positions for each operator
    val operatorPositions = operatorPredicates.map { op =>
      op.operatorID -> Point(
        x = Random.between(xRange.start, xRange.end),
        y = Random.between(yRange.start, yRange.end)
      )
    }.toMap

    val operatorLinks = logicalPlanPojo.links.map(WorkflowConversionUtil.convertToOperatorLink)

    WorkflowContent(
      operators = operatorPredicates,
      operatorPositions = operatorPositions,
      links = operatorLinks,
      commentBoxes = List.empty,
      settings = Map(
        "dataTransferBatchSize" -> 400
      )
    )
  }
}

object WorkflowEditionUtil {

  def addOperator(content: WorkflowContent, logicalOp: LogicalOp, nextTo: String = ""): WorkflowContent = {
    val newOperators = content.operators :+ WorkflowConversionUtil.convertToOperatorPredicate(logicalOp)
    val newOperatorId = logicalOp.operatorIdentifier.toString

    val newPosition = if (nextTo.nonEmpty && content.operatorPositions.contains(nextTo)) {
      val referencePosition = content.operatorPositions(nextTo)
      Point(referencePosition.x + 120, referencePosition.y + Random.between(-50, 50))
    } else {
      Point(100, 100)
    }

    val newOperatorPositions = content.operatorPositions + (newOperatorId -> newPosition)

    content.copy(operators = newOperators, operatorPositions = newOperatorPositions)
  }

  def addLink(
               content: WorkflowContent,
               fromOpId: String,
               fromPortId: String,
               toOpId: String,
               toPortId: String
             ): WorkflowContent = {
    val newLink = OperatorLink(
      linkID = getLinkId(UUID.randomUUID().toString),
      source = LogicalPort(
        fromOpId, fromPortId
      ),
      target = LogicalPort(
        toOpId, toPortId
      )
    )

    val newLinks = content.links :+ newLink

    content.copy(links = newLinks)
  }

  def getCsvScanOpDesc(
                        fileName: String,
                        header: Boolean,
                      ): CSVScanSourceOpDesc = {
    val csvHeaderlessOp = new CSVScanSourceOpDesc()
    csvHeaderlessOp.fileName = Some(fileName)
    csvHeaderlessOp.customDelimiter = Some(",")
    csvHeaderlessOp.hasHeader = header
    csvHeaderlessOp.setFileUri(FileResolver.resolve(fileName))
    csvHeaderlessOp
  }

  private def getProjectionOpDesc(
                                   attributeNames: List[String],
                                   isDrop: Boolean = false
                                 ): ProjectionOpDesc = {
    val projectionOpDesc = new ProjectionOpDesc()
    projectionOpDesc.attributes = attributeNames.map(name => new AttributeUnit(name, ""))
    projectionOpDesc.isDrop = isDrop
    projectionOpDesc
  }

  private def getTestLogicalPlan: LogicalPlanPojo = {
    val localCsvFilePath = "/shengqun@uci.edu/ICS80-Assignment1/v6/clean_tweets.csv"
    val csvSourceOp = getCsvScanOpDesc(localCsvFilePath, header = true)
    val projectionOpDesc = getProjectionOpDesc(List("tweet_id", "create_at_month"))
    LogicalPlanPojo(
      operators = List(csvSourceOp, projectionOpDesc),
      links = List(
        LogicalLink(
          csvSourceOp.operatorIdentifier,
          PortIdentity(0),
          projectionOpDesc.operatorIdentifier,
          PortIdentity(0)
        ),
      ),
      opsToViewResult = List(),
      opsToReuseResult = List()
    )
  }

  def main(args: Array[String]): Unit = {
    val workflowJson =
      """
         {
        "operators": [
          {
            "operatorID": "CSVFileScan-operator-ce9d47f5-2574-4749-a333-8dc30d4c7071",
            "operatorType": "CSVFileScan",
            "operatorVersion": "fe684b5e5120c6a24077422336e82c44a24f3c14",
            "operatorProperties": {
              "fileEncoding": "UTF_8",
              "customDelimiter": ",",
              "hasHeader": true,
              "fileName": "/shengqun@uci.edu/ICS80-Assignment1/v6/clean_tweets.csv"
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
          },
          {
            "operatorID": "Projection-operator-020b75de-ff72-4013-bb88-6280a4b46232",
            "operatorType": "Projection",
            "operatorVersion": "25faefd4bf57f6fc1b0b99384eded40f593332c0",
            "operatorProperties": {
              "isDrop": false,
              "attributes": [
                {
                  "alias": "",
                  "originalAttribute": "tweet_id"
                },
                {
                  "alias": "",
                  "originalAttribute": "create_at_month"
                }
              ]
            },
            "inputPorts": [
              {
                "portID": "input-0",
                "displayName": "",
                "allowMultiInputs": false,
                "isDynamicPort": false,
                "dependencies": []
              }
            ],
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
            "customDisplayName": "Projection",
            "dynamicInputPorts": false,
            "dynamicOutputPorts": false
          }
        ],
        "operatorPositions": {
          "CSVFileScan-operator-ce9d47f5-2574-4749-a333-8dc30d4c7071": {
            "x": 404,
            "y": 171
          },
          "Projection-operator-020b75de-ff72-4013-bb88-6280a4b46232": {
            "x": 554,
            "y": 170
          }
        },
        "links": [
          {
            "linkID": "link-512467b8-6b0d-40ba-8f75-fa97d76d8694",
            "source": {
              "operatorID": "CSVFileScan-operator-ce9d47f5-2574-4749-a333-8dc30d4c7071",
              "portID": "output-0"
            },
            "target": {
              "operatorID": "Projection-operator-020b75de-ff72-4013-bb88-6280a4b46232",
              "portID": "input-0"
            }
          }
        ],
        "commentBoxes": [],
        "settings": {
          "dataTransferBatchSize": 400
        }
      }
      """

    val workflowContent = objectMapper.readValue(workflowJson, classOf[WorkflowContent])

    val testLogicalPlan = getTestLogicalPlan
    val artificialWorkflowContent = WorkflowConversionUtil.createWorkflowContent(testLogicalPlan)

    // Serialize artificialWorkflowContent to JSON string
    val serializedJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(artificialWorkflowContent)

    // Define output file path
    val outputFile = new File("artificialWorkflowContent.json")

    // Write JSON string to file
    val writer = new PrintWriter(outputFile)
    try {
      writer.write(serializedJson)
      println(s"Workflow content exported successfully to ${outputFile.getAbsolutePath}")
    } finally {
      writer.close()
    }
  }
}