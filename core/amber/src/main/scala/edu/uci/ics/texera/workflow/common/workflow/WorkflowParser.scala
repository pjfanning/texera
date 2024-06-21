package edu.uci.ics.texera.workflow.common.workflow

import edu.uci.ics.amber.engine.architecture.controller.WorkflowScheduler
import edu.uci.ics.amber.engine.architecture.deploysemantics.PhysicalOp
import edu.uci.ics.amber.engine.architecture.deploysemantics.layer.OpExecInitInfo
import edu.uci.ics.amber.engine.common.virtualidentity.{ExecutionIdentity, OperatorIdentity, PhysicalOpIdentity, WorkflowIdentity}
import edu.uci.ics.amber.engine.common.workflow.{InputPort, OutputPort, PhysicalLink, PortIdentity}
import edu.uci.ics.texera.workflow.common.WorkflowContext
import edu.uci.ics.texera.workflow.common.storage.OpResultStorage
import edu.uci.ics.texera.workflow.common.tuple.schema.Schema
import io.circe.{Json, ParsingFailure, parser}
import org.jgrapht.graph.DirectedAcyclicGraph
import org.jgrapht.util.SupplierUtil

import scala.io.Source
import scala.util.Try

object WorkflowParser extends App {

  parseWorkflowFile("/Users/xzliu/Downloads/(test) credit score model.json")

  def parseWorkflowFile(filePath: String) = {
    val fileContent = try {
      Source.fromFile(filePath).getLines.mkString
    } catch {
      case e: Exception =>
        e.printStackTrace()
        ""
    } finally {
      Source.fromFile(filePath).close()
    }
    val json: Either[ParsingFailure, Json] = parser.parse(fileContent)
    // Handle the parsing result
    json match {
      case Right(doc) =>
        println("JSON parsed successfully!")
        val operators = doc.hcursor.downField("operators").values match {
          case Some(operatorsJson) =>
           operatorsJson.toList.flatMap {
            operatorJson => for {
              operatorID <- operatorJson.hcursor.get[String]("operatorID").toOption
              inputPorts = operatorJson.hcursor.downField("inputPorts").values.getOrElse(Vector()).map(portJson => {
                val portIdStr = portJson.hcursor.get[String]("portID").getOrElse("unknown")
                val portId = PortIdentity(extractId(portIdStr))
                val displayName = portJson.hcursor.get[String]("portID").getOrElse("")
                InputPort(portId, displayName)
              })

              outputPorts = operatorJson.hcursor.downField("outputPorts").values.getOrElse(Vector()).map(portJson => {
                val portIdStr = portJson.hcursor.get[String]("portID").getOrElse("unknown")
                val portId = PortIdentity(extractId(portIdStr))
                val displayName = portJson.hcursor.get[String]("portID").getOrElse("")
                OutputPort(portId, displayName)
              })
            } yield PhysicalOp(
              id = PhysicalOpIdentity.apply(OperatorIdentity.apply(operatorID), "main"),
              workflowId = WorkflowIdentity.defaultInstance,
              executionId = ExecutionIdentity.defaultInstance,
              opExecInitInfo = OpExecInitInfo.apply("", "python")
            ).withInputPorts(inputPorts.toList).withOutputPorts(outputPorts.toList)
          }.toSet
          case None=> Set[PhysicalOp]()
        }
        val opIdentityMap = operators.map(op => op.id.logicalOpId.id -> op.id).toMap

        var links = doc.hcursor.downField("links").values match {
          case Some(linksJson) =>
            linksJson.toList.flatMap { linkJson =>
              for {
                fromOpId <- opIdentityMap.get(linkJson.hcursor.downField("source").get[String]("operatorID").getOrElse(""))
                toOpId <- opIdentityMap.get(linkJson.hcursor.downField("target").get[String]("operatorID").getOrElse(""))
              } yield {
                val fromPortId = PortIdentity(extractId(linkJson.hcursor.downField("source").get[String]("portID").getOrElse("unknown")))
                val toPortId = PortIdentity(extractId(linkJson.hcursor.downField("target").get[String]("portID").getOrElse("unknown")))
                PhysicalLink(fromOpId, fromPortId, toOpId, toPortId)
              }
            }.toSet
          case None => Set[PhysicalLink]()

        }

        val jgraphtDag = new DirectedAcyclicGraph[PhysicalOpIdentity, PhysicalLink](
          null, // vertexSupplier
          SupplierUtil.createSupplier(classOf[PhysicalLink]), // edgeSupplier
          false, // weighted
          true // allowMultipleEdges
        )
        operators.foreach(op => jgraphtDag.addVertex(op.id))
        links.foreach(l => {
          try {
            jgraphtDag.addEdge(l.fromOpId, l.toOpId, l)
          } catch {
            case _: Exception => {
              links = links - l
              println(s"Removed link $l to keep the input physical plan acyclic.")
            }
          }
        })

        val physicalPlan = PhysicalPlan(operators = operators, links = links)
        val workflowScheduler: WorkflowScheduler = new WorkflowScheduler(new WorkflowContext(), new OpResultStorage())
        workflowScheduler.updateSchedule(physicalPlan)
        val schedule = workflowScheduler.schedule
        println(schedule.toList)
      case Left(error) =>
        println("Failed to parse JSON:")
        println(error)
    }
  }

  def extractId(portIdStr: String): Int = {
    // Using regular expression to extract numbers
    "\\d+".r.findFirstIn(portIdStr).getOrElse("-1").toInt
  }
}
