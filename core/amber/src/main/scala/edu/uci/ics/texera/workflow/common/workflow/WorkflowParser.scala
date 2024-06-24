package edu.uci.ics.texera.workflow.common.workflow

import com.mxgraph.layout.hierarchical.mxHierarchicalLayout
import com.mxgraph.model.mxICell
import com.mxgraph.util.{mxCellRenderer, mxConstants}
import com.mxgraph.view.mxStylesheet
import edu.uci.ics.amber.engine.architecture.deploysemantics.PhysicalOp
import edu.uci.ics.amber.engine.architecture.deploysemantics.layer.OpExecInitInfo
import edu.uci.ics.amber.engine.architecture.scheduling.CostBasedRegionPlanGenerator
import edu.uci.ics.amber.engine.common.virtualidentity.{ExecutionIdentity, OperatorIdentity, PhysicalOpIdentity, WorkflowIdentity}
import edu.uci.ics.amber.engine.common.workflow.{InputPort, OutputPort, PhysicalLink, PortIdentity}
import edu.uci.ics.texera.workflow.common.WorkflowContext
import edu.uci.ics.texera.workflow.common.storage.OpResultStorage
import io.circe.{Json, ParsingFailure, parser}
import org.jgrapht.ext.JGraphXAdapter
import org.jgrapht.graph.DirectedAcyclicGraph
import org.jgrapht.util.SupplierUtil

import java.awt._
import java.awt.image.BufferedImage
import java.io.File
import java.nio.file.{Files, Paths}
import java.text.DecimalFormat
import java.util.{Map => JMap}
import javax.imageio.ImageIO
import javax.swing._
import scala.collection.mutable
import scala.io.Source
import scala.jdk.CollectionConverters.{CollectionHasAsScala, IteratorHasAsScala}

object WorkflowParser extends App {

  val inputPath = "/Users/xzliu/Downloads/KNIME workflows parsing/ALL_KNIME_WORKFLOWS_CONVERTED_WITH_BLOCKING"
  val inputDirectory = Paths.get(inputPath)
  val outputPath = "/Users/xzliu/Downloads/KNIME workflows parsing/ALL_KNIME_WORKFLOWS_RENDERED"
  val outputDirectory = Paths.get(outputPath)
  if (!Files.exists(outputDirectory)) Files.createDirectory(outputDirectory)

  if (Files.exists(inputDirectory) && Files.isDirectory(inputDirectory)) {
    // List all files in the directory and call parseWorkflowFile for each file
    Files.list(inputDirectory).iterator().asScala.foreach { filePath =>
      try {
        if (Files.isRegularFile(filePath)) {
          val physicalPlan = parseWorkflowFile(filePath.toString)
          val pasta = new CostBasedRegionPlanGenerator(new WorkflowContext(), physicalPlan, new OpResultStorage(), costFunction = "MATERIALIZATION_SIZES")
          if (!pasta.getNaiveSchedulability()) {
            println(s"$filePath needs Pasta.")
            val inputPlanImageOutputPath = outputDirectory.resolve(filePath.getFileName.toString + "_input_physical_plan.png")
            if (physicalPlan.links.forall(link=>physicalPlan.dag.getEdgeWeight(link) == 1.0))
              println(s"$filePath does not have mat size info to run Pasta, skipping.")
            else if (physicalPlan.operators.size > 100 || physicalPlan.links.size > 100) {
              println(s"$filePath is too large, skipping.")
            }
            else {
//              if (physicalPlan.operators.size <= 200 && physicalPlan.links.size <= 200)
//                renderInputPhysicalPlanToFile(physicalPlan, inputPlanImageOutputPath.toString)
              val pastaResultPlanImageOutputPath = outputDirectory.resolve(filePath.getFileName.toString + "_output_region_plan_pasta.png")
              val mustMaterializeSize = physicalPlan.getNonMaterializedBlockingAndDependeeLinks.map(link=>physicalPlan.dag.getEdgeWeight(link)).sum
              val pastaResult = pasta.runPasta()
              val pastaAdjustedCost = pastaResult.cost - mustMaterializeSize
              println(s"Result of Pasta on $filePath: ${pastaResult.cost}, adjusted: $pastaAdjustedCost")
//              if (physicalPlan.operators.size <= 200 && physicalPlan.links.size <= 200)
//                renderRegionPlanToFile(physicalPlan = physicalPlan, matEdges = pastaResult.state, imageOutputPath = pastaResultPlanImageOutputPath.toString)
              val baselineResultPlanImageOutputPath = outputDirectory.resolve(filePath.getFileName.toString + "_output_region_plan_baseline.png")
              val baselineResult = pasta.runBaselineMethod()
              val baselineAdjustedCost = baselineResult.cost - mustMaterializeSize
              println(s"Result of baseline on $filePath: ${baselineResult.cost}, adjusted: $baselineAdjustedCost")
//              if (physicalPlan.operators.size <= 200 && physicalPlan.links.size <= 200)
//                renderRegionPlanToFile(physicalPlan = physicalPlan, matEdges = baselineResult.state, imageOutputPath = baselineResultPlanImageOutputPath.toString)
            }
          }
        }
      } catch {
        case error: Exception => println(error)
      }
    }
  } else {
    println(s"The path $inputPath is not a valid directory.")
  }

  def parseWorkflowFile(filePath: String): PhysicalPlan = {
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
        val portMatSizeMap: mutable.Map[(PhysicalOpIdentity, PortIdentity), Double] = mutable.Map.empty
        val operators = doc.hcursor.downField("operators").values match {
          case Some(operatorsJson) =>
           operatorsJson.toList.flatMap {
            operatorJson => for {
              operatorIdStr <- operatorJson.hcursor.get[String]("operatorID").toOption
              operatorID = PhysicalOpIdentity.apply(OperatorIdentity.apply(operatorIdStr), "main")
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
                val blocking: Boolean = portJson.hcursor.get[Boolean]("isBlocking").getOrElse(false)
                val matSize: Double = portJson.hcursor.get[Double]("materializationSize").getOrElse(1.0)
                portMatSizeMap += (operatorID, portId) -> matSize
                OutputPort(portId, displayName, blocking)
              })
            } yield PhysicalOp(
              id = operatorID,
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
          true, // weighted
          true // allowMultipleEdges
        )
        operators.foreach(op => jgraphtDag.addVertex(op.id))
        val operatorsMap = operators.map(op => op.id -> op).toMap
        links.foreach(l => {
          try {
            val fromOp = operatorsMap(l.fromOpId)
            val toOp = operatorsMap(l.toOpId)
            if (!fromOp.outputPorts.contains(l.fromPortId) || !toOp.inputPorts.contains(l.toPortId)) {
              links = links - l
              println(s"Removed invalid link $l as it connects to a non-existing port.")
            } else jgraphtDag.addEdge(l.fromOpId, l.toOpId, l)
          } catch {
            case _: Exception => {
              links = links - l
              println(s"Removed link $l to keep the input physical plan acyclic.")
            }
          }
        })

        val physicalPlan = PhysicalPlan(operators = operators, links = links)

        physicalPlan.links.foreach(link => {
          val matSize = portMatSizeMap((link.fromOpId, link.fromPortId))
          physicalPlan.dag.setEdgeWeight(link, matSize)
        })

        physicalPlan
//        val workflowScheduler: WorkflowScheduler = new WorkflowScheduler(new WorkflowContext(), new OpResultStorage(), costFunction = "MATERIALIZATION_SIZES")
//        workflowScheduler.updateSchedule(physicalPlan)
//        val schedule = workflowScheduler.schedule
//        println(schedule.toList.size)
      case Left(error) =>
        println("Failed to parse JSON:")
        throw error
    }
  }

  private def extractId(portIdStr: String): Int = {
    // Using regular expression to extract numbers
    "\\d+".r.findFirstIn(portIdStr).getOrElse("-1").toInt
  }

  def renderInputPhysicalPlanToFile(physicalPlan: PhysicalPlan, imageOutputPath: String): Unit = {
    val graphAdapter = getInputPhysicalPlanGraphAdapter(physicalPlan = physicalPlan)
    renderDAGToFile(imageOutputPath, graphAdapter)
  }

  def renderRegionPlanToFile(physicalPlan: PhysicalPlan, matEdges: Set[PhysicalLink], imageOutputPath:String): Unit = {
    val graphAdapter = getRegionPlanGraphAdapter(physicalPlan = physicalPlan, matEdges = matEdges)
    renderDAGToFile(imageOutputPath, graphAdapter)
  }

  private def renderDAGToFile(outputPath: String, graphAdapter: JGraphXAdapter[PhysicalOpIdentity, PhysicalLink]): Unit = {
    try {
    val image: BufferedImage = mxCellRenderer.createBufferedImage(graphAdapter, null, 10, Color.WHITE, true, null)
    val imgFile = new File(outputPath)
      ImageIO.write(image, "PNG", imgFile)
    } catch {
      case e: Exception => println(e)
    }
  }

  private def getRegionPlanGraphAdapter(physicalPlan: PhysicalPlan, matEdges: Set[PhysicalLink]): JGraphXAdapter[PhysicalOpIdentity, PhysicalLink] = {
    def getMxStylesheet(graphAdapter: JGraphXAdapter[PhysicalOpIdentity, PhysicalLink]): mxStylesheet = {
      val stylesheet = graphAdapter.getStylesheet
      val edgeStyle: JMap[String, Object] = stylesheet.getDefaultEdgeStyle
      val vertexStyle: JMap[String, Object] = stylesheet.getDefaultVertexStyle
      edgeStyle.put(mxConstants.STYLE_FONTSIZE, java.lang.Integer.valueOf(7))
      edgeStyle.put(mxConstants.STYLE_FONTFAMILY, "Arial")
      edgeStyle.put(mxConstants.STYLE_ENDARROW, mxConstants.ARROW_BLOCK)
      edgeStyle.put(mxConstants.STYLE_ENDSIZE, java.lang.Integer.valueOf(2))
      edgeStyle.put(mxConstants.STYLE_STROKEWIDTH, java.lang.Integer.valueOf(1))
      edgeStyle.put(mxConstants.STYLE_STROKE_OPACITY, java.lang.Integer.valueOf(50))
      vertexStyle.put(mxConstants.STYLE_FONTFAMILY, "Arial")
      vertexStyle.put(mxConstants.STYLE_ROUNDED, java.lang.Boolean.TRUE)
      stylesheet.setDefaultVertexStyle(vertexStyle) // Set the default style for vertices
      stylesheet.setDefaultEdgeStyle(edgeStyle) // Set the default style for edges
      stylesheet
    }

    val blockingEdgeColor = "strokeColor=#b22812"
    val matEdgeColor = "strokeColor=#cccc00"
    val graphAdapter = new JGraphXAdapter[PhysicalOpIdentity, PhysicalLink](physicalPlan.dag)
    val layout = new mxHierarchicalLayout(graphAdapter, SwingConstants.WEST)
    layout.execute(graphAdapter.getDefaultParent)
    val edgeToCellMap: JMap[PhysicalLink, mxICell] = graphAdapter.getEdgeToCellMap
    val stylesheet = getMxStylesheet(graphAdapter)

    graphAdapter.setStylesheet(stylesheet)

    val df = new DecimalFormat("#.#")

    for (edge <- physicalPlan.dag.edgeSet.asScala) {
      if (matEdges.contains(edge)) {
        edgeToCellMap.get(edge).setStyle(matEdgeColor)
      }
      if (physicalPlan.getNonMaterializedBlockingAndDependeeLinks.contains(edge)) {
        edgeToCellMap.get(edge).setStyle(blockingEdgeColor)
      }
      edgeToCellMap.get(edge).setValue(df.format(physicalPlan.dag.getEdgeWeight(edge)))
    }
    graphAdapter
  }

  private def getInputPhysicalPlanGraphAdapter(physicalPlan: PhysicalPlan): JGraphXAdapter[PhysicalOpIdentity, PhysicalLink] = {
    def getMxStylesheet(graphAdapter: JGraphXAdapter[PhysicalOpIdentity, PhysicalLink]): mxStylesheet = {
      val stylesheet = graphAdapter.getStylesheet
      val edgeStyle: JMap[String, Object] = stylesheet.getDefaultEdgeStyle
      val vertexStyle: JMap[String, Object] = stylesheet.getDefaultVertexStyle
      edgeStyle.put(mxConstants.STYLE_FONTSIZE, java.lang.Integer.valueOf(7))
      edgeStyle.put(mxConstants.STYLE_FONTFAMILY, "Arial")
      edgeStyle.put(mxConstants.STYLE_ENDARROW, mxConstants.ARROW_BLOCK)
      edgeStyle.put(mxConstants.STYLE_ENDSIZE, java.lang.Integer.valueOf(5))
      edgeStyle.put(mxConstants.STYLE_STROKEWIDTH, java.lang.Integer.valueOf(4))
      edgeStyle.put(mxConstants.STYLE_STROKE_OPACITY, java.lang.Integer.valueOf(50))
      edgeStyle.put(mxConstants.STYLE_STROKECOLOR, "#000000")
      vertexStyle.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_ELLIPSE)
      vertexStyle.put(mxConstants.STYLE_PERIMETER, mxConstants.PERIMETER_ELLIPSE)
      vertexStyle.put(mxConstants.STYLE_FONTSIZE, java.lang.Integer.valueOf(7))
      vertexStyle.put(mxConstants.STYLE_FONTFAMILY, "Arial")
      vertexStyle.put(mxConstants.STYLE_ROUNDED, java.lang.Boolean.TRUE)
      vertexStyle.put(mxConstants.STYLE_FILLCOLOR, "#FFFFFF")
      vertexStyle.put(mxConstants.STYLE_STROKECOLOR, "#000000") // Black color for vertex borders
      vertexStyle.put(mxConstants.STYLE_STROKEWIDTH, java.lang.Integer.valueOf(3)) // Increase the line width as needed
      stylesheet.setDefaultVertexStyle(vertexStyle) // Set the default style for vertices
      stylesheet.setDefaultEdgeStyle(edgeStyle) // Set the default style for edges
      stylesheet
    }

    val strokeColor = "strokeColor=#b22812"
    val graphAdapter = new JGraphXAdapter[PhysicalOpIdentity, PhysicalLink](physicalPlan.dag)
    val layout = new mxHierarchicalLayout(graphAdapter, SwingConstants.WEST)
    layout.execute(graphAdapter.getDefaultParent)
    val edgeToCellMap: JMap[PhysicalLink, mxICell] = graphAdapter.getEdgeToCellMap
    val stylesheet = getMxStylesheet(graphAdapter)

    graphAdapter.setStylesheet(stylesheet)

    val vertexDiameter = 30 // Set the diameter for the vertex
    graphAdapter.getModel.beginUpdate()
    try {
      for (vertex <- graphAdapter.getChildVertices(graphAdapter.getDefaultParent)) {
        val geometry = graphAdapter.getModel.getGeometry(vertex)
        geometry.setWidth(vertexDiameter)
        geometry.setHeight(vertexDiameter)
      }
    } finally {
      graphAdapter.getModel.endUpdate()
    }

    for (vertex <- physicalPlan.dag.vertexSet.asScala) {
      graphAdapter.getVertexToCellMap.get(vertex).setValue("")
    }

    for (edge <- physicalPlan.dag.edgeSet.asScala) {
      if (physicalPlan.getNonMaterializedBlockingAndDependeeLinks.contains(edge)) {
        edgeToCellMap.get(edge).setStyle(strokeColor)
      }
      edgeToCellMap.get(edge).setValue("")
    }
    graphAdapter
  }
}
