//package edu.uci.ics.texera.workflow.common.workflow
//
//import edu.uci.ics.amber.engine.architecture.deploysemantics.PhysicalOp
//import edu.uci.ics.amber.engine.architecture.deploysemantics.layer.OpExecInitInfo
//import edu.uci.ics.amber.engine.common.virtualidentity.{ExecutionIdentity, OperatorIdentity, WorkflowIdentity}
//import edu.uci.ics.amber.engine.common.workflow.{InputPort, OutputPort, PhysicalLink, PortIdentity}
//
//import java.io.{BufferedReader, FileReader, IOException}
//import scala.collection.mutable
//import scala.jdk.CollectionConverters._
//import org.jgrapht.Graph
//import org.jgrapht.graph.DirectedAcyclicGraph
//import org.jgrapht.nio.dot.DOTImporter
//import org.jgrapht.util.SupplierUtil
//import org.jgrapht.nio.Attribute
//
//import scala.collection.convert.ImplicitConversions.`set asScala`
//
//object DotFileParser {
//
//  case class DotFileVertex(id: Int, label: String) {
//    override def toString: String = s"$id [$label]"
//  }
//
//  def parseDotFile(filePath: String): PhysicalPlan = {
//    val graph: Graph[PhysicalOp, PhysicalLink] = new DirectedAcyclicGraph(classOf[PhysicalLink])
//
//    val weights = mutable.HashMap[PhysicalLink, Double]()
//
//    val importer = new DOTImporter[PhysicalOp, PhysicalLink]
//    importer.setVertexWithAttributesFactory((label, attributes) =>
//      PhysicalOp
//        .oneToOnePhysicalOp(
//          WorkflowIdentity.defaultInstance,
//          ExecutionIdentity.defaultInstance,
//          OperatorIdentity.apply(attributes.get("label").toString + label),
//          OpExecInitInfo("", "python")
//        )
//        .withInputPorts(List(InputPort(PortIdentity())))
//        .withOutputPorts(List(OutputPort(PortIdentity())))
//    )
//
//    importer.setEdgeWithAttributesFactory((sourceVertex, targetVertex, attribtues)=> {
//
//    })
//
//    importer.addEdgeAttributeConsumer((dotFileEdge, attribute) => {
//      val labels = attribute.toString.split(";")
//      val matSize = labels(0).split(":")(1).toDouble
//      val isBlocking = labels(1).split(":")(1).contains("True")
////      dotFileEdge.getFirst.fromOpId.logicalOpId
////      weights(dotFileEdge.getFirst) = matSize
//    })
//
//    try {
//      importer.importGraph(graph, new FileReader(filePath))
//    } catch {
//      case e: IOException => e.printStackTrace()
//    }
//
//    new PhysicalPlan(operators = graph.vertexSet().toSet, links = graph.edgeSet().toSet)
//
////    val dualDAG = new DirectedAcyclicGraph[Int, DualEdge](SupplierUtil.createIntegerSupplier(), () => new DualEdge, true)
//
//
////    graph.vertexSet().asScala.foreach(dotFileVertex => {
////      dualDAG.addVertex(dotFileVertex.id)
////    })
////
////    graph.edgeSet().asScala.foreach { initialDualEdge =>
////      val upstreamVertex = graph.getEdgeSource(initialDualEdge)
////      val downstreamVertex = graph.getEdgeTarget(initialDualEdge)
////      dualDAG.addEdge(upstreamVertex.id, downstreamVertex.id, new DualEdge(initialDualEdge.isBlkOrMat()))
////      dualDAG.setEdgeWeight(upstreamVertex.id, downstreamVertex.id, weights(initialDualEdge))
////    }
////
////    dualDAG
//  }
//
//  def main(args: Array[String]): Unit = {
//    parseDotFile("/Users/xzliu/IdeaProjects/KnimeParser/graph/Justknimeit-S2C24.dot")
//  }
//}
