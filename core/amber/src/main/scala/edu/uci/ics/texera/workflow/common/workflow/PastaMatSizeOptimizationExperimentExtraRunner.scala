package edu.uci.ics.texera.workflow.common.workflow

import edu.uci.ics.amber.engine.architecture.scheduling.CostBasedRegionPlanGenerator
import edu.uci.ics.texera.workflow.common.WorkflowContext
import edu.uci.ics.texera.workflow.common.storage.OpResultStorage
import edu.uci.ics.texera.workflow.common.workflow.WorkflowParser.{parseWorkflowFile, renderInputPhysicalPlanToFile, renderRegionPlanToFile}
import scala.collection.convert.ImplicitConversions.`collection AsScalaIterable`
import scala.jdk.CollectionConverters.IteratorHasAsScala
import java.io.BufferedWriter
import java.nio.file.{Files, Path, Paths, StandardOpenOption}

object PastaMatSizeOptimizationExperimentExtraRunner extends App {

  if (args.length != 3) {
    println("Usage: WorkflowExperimentApp <input_file> <output_directory> <results_file>")
    System.exit(1)
  }

  val inputFilePath = args(0)
  val outputPath = args(1)
  val resultsFilePath = args(2)

  val inputFile = Paths.get(inputFilePath)
  if (Files.exists(inputFile) && Files.isRegularFile(inputFile)) {
    val resultsFile = Paths.get(resultsFilePath)
    val bufferedWriter = Files.newBufferedWriter(resultsFile, StandardOpenOption.CREATE, StandardOpenOption.APPEND)
    try {
      runExperimentsOnSingleFile(
        inputFile,
        Paths.get(outputPath),
        bufferedWriter
      )
    } catch {
      case e: Exception => throw e
    } finally {
      bufferedWriter.close()
    }
  } else {
    println(s"Input file $inputFilePath does not exist or is not a regular file.")
  }

  def runExperimentsOnSingleFile(inputPath: Path, planOutputDirectory: Path, resultCSVWriter: BufferedWriter): Unit = {
    try {
      if (Files.isRegularFile(inputPath)) {
        println(s"Starting experiments on $inputPath")
        val parts = inputPath.getFileName.toString.split("\\.")
        val workflowName = if (parts.length > 1) parts.dropRight(1).mkString(".") else inputPath.getFileName.toString
        val physicalPlan = parseWorkflowFile(filePath = inputPath.toString)
        val numOperators = physicalPlan.dag.vertexSet().size()
        val numLinks = physicalPlan.dag.edgeSet().size()
        val numBlockingLinks = physicalPlan.nonMaterializedBlockingAndDependeeLinks.size
        val numNonBlockingLinks = numLinks - numBlockingLinks
        val vertexSet = physicalPlan.dag.vertexSet().toSet
        val maxDegrees = vertexSet.map(opId=>physicalPlan.dag.degreeOf(opId)).max
        val avgDegrees = vertexSet.map(opId=>physicalPlan.dag.degreeOf(opId)).sum * 1.0 / vertexSet.size
        val maxInDegrees = vertexSet.map(opId=>physicalPlan.dag.inDegreeOf(opId)).max
        val avgInDegrees = vertexSet.map(opId=>physicalPlan.dag.inDegreeOf(opId)).sum * 1.0 / vertexSet.size
        val maxOutDegrees = vertexSet.map(opId=>physicalPlan.dag.outDegreeOf(opId)).max
        val avgOutDegrees = vertexSet.map(opId=>physicalPlan.dag.outDegreeOf(opId)).sum * 1.0 / vertexSet.size
        val numChains = physicalPlan.maxChains.size
        val maxChainSize = if (numChains > 0) physicalPlan.maxChains.map(_.size).max else 1
        val avgChainSize = if (numChains > 0) physicalPlan.maxChains.map(_.size).sum * 1.0 / numChains else 0.0
        val numUndirectedCycles = physicalPlan.allUndirectedCycles match {
          case Some(allCycles) => allCycles.size
          case None => 1001
        }
        val numBridges = physicalPlan.getBridges.size
        val numCleanEdges = physicalPlan.getCleanEdges.size
        val isDAG = numUndirectedCycles > 0
        val pasta = new CostBasedRegionPlanGenerator(new WorkflowContext(), physicalPlan, new OpResultStorage(), costFunction = "MATERIALIZATION_SIZES")
        val bottomUpSeedSchedulability = pasta.getNaiveSchedulability()
        val hasMatSizeOnPorts = !physicalPlan.links.forall(link => physicalPlan.dag.getEdgeWeight(link) == 1.0)
        val hasControlBlocks = physicalPlan.operators.exists(op=>op.id.layerName.toLowerCase().contains("switch") || op.id.layerName.toLowerCase().contains("loop"))
        val statsList = List(
          "workflowName" -> workflowName,
          "bottomUpSeedSchedulability" -> bottomUpSeedSchedulability,
          "hasMatSizeOnPorts" -> hasMatSizeOnPorts,
          "hasControlBlocks"->hasControlBlocks
        )
        val stats = statsList.map { case (_, result) => s""""${result.toString.replace("\"", "\"\"")}""""}.mkString(",")
        if (!bottomUpSeedSchedulability && hasMatSizeOnPorts) {
          println(s"Running experiments on $inputPath")
          val allMat = pasta.allMatMethod
          println(s"$workflowName: baseline finished")

          val resultList = List("allMat" -> allMat)
          val results = resultList.map { case (_, result) => s""""${ExperimentResult(cost=result.cost, searchTime = result.searchTime, searchFinished = result.searchFinished, numStatesExplored = result.numStatesExplored).toString.replace("\"", "\"\"")}""""}.mkString(",")
          resultCSVWriter.write(stats + ",")
          resultCSVWriter.write(results + "\n")
          resultCSVWriter.flush()
        } else {
          resultCSVWriter.write(stats + ",")
          resultCSVWriter.write("," + "\n")
          resultCSVWriter.flush()
        }
        println(s"Finished $inputPath")
      }
      else {
        println(inputPath)
      }
    } catch {
      case error: Exception => throw error
    }
  }
}
