package edu.uci.ics.texera.workflow.common.workflow

import edu.uci.ics.amber.engine.architecture.scheduling.CostBasedRegionPlanGenerator
import edu.uci.ics.texera.workflow.common.WorkflowContext
import edu.uci.ics.texera.workflow.common.storage.OpResultStorage
import edu.uci.ics.texera.workflow.common.workflow.WorkflowParser.{parseWorkflowFile, renderInputPhysicalPlanToFile, renderRegionPlanToFile}

import java.io.BufferedWriter
import java.nio.file.{Files, Path, Paths, StandardOpenOption}
import scala.collection.convert.ImplicitConversions.`collection AsScalaIterable`
import scala.jdk.CollectionConverters.IteratorHasAsScala

case class ExperimentResult(
                             cost: Double,
                             searchTime: Double,
                             searchFinished: Boolean,
                             numStatesExplored: Int
                           )

object PastaMatSizeOptimizationExperimentRunner extends App {

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

//  val inputPath = "/Users/xzliu/Downloads/KNIME workflows parsing/ALL_KNIME_WORKFLOWS_CONVERTED_WITH_BLOCKING"
//  val inputDirectory = Paths.get(inputPath)
//  if (Files.exists(inputDirectory) && Files.isDirectory(inputDirectory)) {
//    // List all files in the directory and call parseWorkflowFile for each file
//    Files.list(inputDirectory).iterator().asScala.foreach { filePath =>
//      runExperimentsOnSingleFile(
//        filePath,
//        Paths.get("/Users/xzliu/Downloads/KNIME workflows parsing/ALL_PASTA_EXPERIMENTS"),
//        Files.newBufferedWriter(Paths.get("/Users/xzliu/Downloads/KNIME workflows parsing/pasta_results.csv"), StandardOpenOption.CREATE, StandardOpenOption.APPEND)
//      )
//    }
//  }

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
        val mustMaterializeSize = physicalPlan.nonMaterializedBlockingAndDependeeLinks.map(link => physicalPlan.dag.getEdgeWeight(link)).sum
        val statsList = List(
          "workflowName" -> workflowName,
          "numOperators" -> numOperators,
          "numLinks" -> numLinks,
          "numBlockingLinks" -> numBlockingLinks,
          "numNonBlockingLinks" -> numNonBlockingLinks,
          "maxDegrees" -> maxDegrees,
          "avgDegrees" -> avgDegrees,
          "maxInDegrees" -> maxInDegrees,
          "avgInDegrees" -> avgInDegrees,
          "maxOutDegrees" -> maxOutDegrees,
          "avgOutDegrees" -> avgOutDegrees,
          "numChains" -> numChains,
          "maxChainSize" -> maxChainSize,
          "avgChainSize" -> avgChainSize,
          "numUndirectedCycles" -> numUndirectedCycles,
          "numBridges" -> numBridges,
          "numCleanEdges" -> numCleanEdges,
          "isDAG" -> isDAG,
          "bottomUpSeedSchedulability" -> bottomUpSeedSchedulability,
          "hasMatSizeOnPorts" -> hasMatSizeOnPorts,
          "mustMaterializeSize" -> mustMaterializeSize
        )
        val stats = statsList.map { case (_, result) => s""""${result.toString.replace("\"", "\"\"")}""""}.mkString(",")
        resultCSVWriter.write(stats + ",")
        resultCSVWriter.flush()
        if (!bottomUpSeedSchedulability && hasMatSizeOnPorts) {
          println(s"Running experiments on $inputPath")
          val baseline = pasta.baselineMethod()
          println("baseline finished")

          val topDownGreedy = pasta.topDownSearch(globalSearch = false)
          println("topDownGreedy finished")

          val bottomUpGreedy = pasta.bottomUpSearch(globalSearch = false)
          println("bottomUpGreedy finished")

          val topDownGlobal = pasta.topDownSearch()
          println("topDownGlobal finished")

          val bottomUpGlobal = pasta.bottomUpSearch()
          println("bottomUpGlobal finished")

          val pastaBest = Set(topDownGreedy, bottomUpGreedy, topDownGlobal, bottomUpGlobal).minBy(res => res.cost)
          println(s"pastaBest finished with cost ${pastaBest.cost}")

          val topDownGreedyNoOptimization = pasta.topDownSearch(globalSearch = false, oChains = false, oCleanEdges = false, oEarlyStop = false)
          println("topDownGreedyNoOptimization finished")

          val topDownGreedyOChains = pasta.topDownSearch(globalSearch = false, oCleanEdges = false, oEarlyStop = false)
          println("topDownGreedyOChains finished")

          val topDownGreedyOCleanEdges = pasta.topDownSearch(globalSearch = false, oChains = false, oEarlyStop = false)
          println("topDownGreedyOCleanEdges finished")

          val topDownGreedyOEarlyStop = pasta.topDownSearch(globalSearch = false, oChains = false, oCleanEdges = false)
          println("topDownGreedyOEarlyStop finished")

          val bottomUpGreedyNoOptimization = pasta.bottomUpSearch(globalSearch = false, oChains = false, oCleanEdges = false, oEarlyStop = false)
          println("bottomUpGreedyNoOptimization finished")

          val bottomUpGreedyOChains = pasta.bottomUpSearch(globalSearch = false, oCleanEdges = false, oEarlyStop = false)
          println("bottomUpGreedyOChains finished")

          val bottomUpGreedyOCleanEdges = pasta.bottomUpSearch(globalSearch = false, oChains = false, oEarlyStop = false)
          println("bottomUpGreedyOCleanEdges finished")

          val bottomUpGreedyOEarlyStop = pasta.bottomUpSearch(globalSearch = false, oChains = false, oCleanEdges = false)
          println("bottomUpGreedyOEarlyStop finished")

          val topDownGlobalNoOptimization = pasta.topDownSearch(oChains = false, oCleanEdges = false, oEarlyStop = false)
          println("topDownGlobalNoOptimization finished")

          val topDownGlobalOChains = pasta.topDownSearch(oCleanEdges = false, oEarlyStop = false)
          println("topDownGlobalOChains finished")

          val topDownGlobalOCleanEdges = pasta.topDownSearch(oChains = false, oEarlyStop = false)
          println("topDownGlobalOCleanEdges finished")

          val topDownGlobalOEarlyStop = pasta.topDownSearch(oChains = false, oCleanEdges = false)
          println("topDownGlobalOEarlyStop finished")

          val bottomUpGlobalNoOptimization = pasta.bottomUpSearch(oChains = false, oCleanEdges = false, oEarlyStop = false)
          println("bottomUpGlobalNoOptimization finished")

          val bottomUpGlobalOChains = pasta.bottomUpSearch(oCleanEdges = false, oEarlyStop = false)
          println("bottomUpGlobalOChains finished")

          val bottomUpGlobalOCleanEdges = pasta.bottomUpSearch(oChains = false, oEarlyStop = false)
          println("bottomUpGlobalOCleanEdges finished")

          val bottomUpGlobalOEarlyStop = pasta.bottomUpSearch(oChains = false, oCleanEdges = false)
          println("bottomUpGlobalOEarlyStop finished")
          val resultList = List(
            "baseline" -> baseline,
            "topDownGreedy" -> topDownGreedy,
            "bottomUpGreedy" -> bottomUpGreedy,
            "topDownGlobal" -> topDownGlobal,
            "bottomUpGlobal" -> bottomUpGlobal,
            "pastaBest" -> pastaBest,
            "topDownGreedyNoOptimization" -> topDownGreedyNoOptimization,
            "topDownGreedyOChains" -> topDownGreedyOChains,
            "topDownGreedyOCleanEdges" -> topDownGreedyOCleanEdges,
            "topDownGreedyOEarlyStop" -> topDownGreedyOEarlyStop,
            "bottomUpGreedyNoOptimization" -> bottomUpGreedyNoOptimization,
            "bottomUpGreedyOChains" -> bottomUpGreedyOChains,
            "bottomUpGreedyOCleanEdges" -> bottomUpGreedyOCleanEdges,
            "bottomUpGreedyOEarlyStop" -> bottomUpGreedyOEarlyStop,
            "topDownGlobalNoOptimization" -> topDownGlobalNoOptimization,
            "topDownGlobalOChains" -> topDownGlobalOChains,
            "topDownGlobalOCleanEdges" -> topDownGlobalOCleanEdges,
            "topDownGlobalOEarlyStop" -> topDownGlobalOEarlyStop,
            "bottomUpGlobalNoOptimization" -> bottomUpGlobalNoOptimization,
            "bottomUpGlobalOChains" -> bottomUpGlobalOChains,
            "bottomUpGlobalOCleanEdges" -> bottomUpGlobalOCleanEdges,
            "bottomUpGlobalOEarlyStop" -> bottomUpGlobalOEarlyStop
          )
          val results = resultList.map { case (_, result) => s""""${new ExperimentResult(cost=result.cost, searchTime = result.searchTime, searchFinished = result.searchFinished, numStatesExplored = result.numStatesExplored).toString.replace("\"", "\"\"")}""""}.mkString(",")
          resultCSVWriter.write(results + "\n")
          resultCSVWriter.flush()
          if (!Files.exists(planOutputDirectory)) Files.createDirectory(planOutputDirectory)
          val outputDirectory = planOutputDirectory.resolve(workflowName)
          if (!Files.exists(outputDirectory)) Files.createDirectory(outputDirectory)
          renderInputPhysicalPlanToFile(physicalPlan, outputDirectory.resolve("inputPhysicalPlan.png").toString)
          resultList.foreach {
            case (experimentName, result) => renderRegionPlanToFile(physicalPlan = physicalPlan, matEdges = result.state, imageOutputPath = outputDirectory.resolve(s"$experimentName.png").toString)
          }
        } else {
          resultCSVWriter.write(",,,,,,,,,,,,,,,,,,,,," + "\n")
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

  def runExperimentsOnDirectory(): Unit = {
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
              if (physicalPlan.links.forall(link => physicalPlan.dag.getEdgeWeight(link) == 1.0))
                println(s"$filePath does not have mat size info to run Pasta, skipping.")
              else if (physicalPlan.operators.size > 100 || physicalPlan.links.size > 100) {
                println(s"$filePath is too large, skipping.")
              }
              else {
                if (physicalPlan.operators.size <= 200 && physicalPlan.links.size <= 200)
                  renderInputPhysicalPlanToFile(physicalPlan, inputPlanImageOutputPath.toString)
                val pastaResultPlanImageOutputPath = outputDirectory.resolve(filePath.getFileName.toString + "_output_region_plan_pasta.png")
                val mustMaterializeSize = physicalPlan.nonMaterializedBlockingAndDependeeLinks.map(link => physicalPlan.dag.getEdgeWeight(link)).sum
                val pastaResult = pasta.runPasta()
                val pastaAdjustedCost = pastaResult.cost - mustMaterializeSize
                println(s"Result of Pasta on $filePath: ${pastaResult.cost}, adjusted: $pastaAdjustedCost")
                if (physicalPlan.operators.size <= 200 && physicalPlan.links.size <= 200)
                  renderRegionPlanToFile(physicalPlan = physicalPlan, matEdges = pastaResult.state, imageOutputPath = pastaResultPlanImageOutputPath.toString)
                val baselineResultPlanImageOutputPath = outputDirectory.resolve(filePath.getFileName.toString + "_output_region_plan_baseline.png")
                val baselineResult = pasta.baselineMethod()
                val baselineAdjustedCost = baselineResult.cost - mustMaterializeSize
                println(s"Result of baseline on $filePath: ${baselineResult.cost}, adjusted: $baselineAdjustedCost")
                if (physicalPlan.operators.size <= 200 && physicalPlan.links.size <= 200)
                  renderRegionPlanToFile(physicalPlan = physicalPlan, matEdges = baselineResult.state, imageOutputPath = baselineResultPlanImageOutputPath.toString)
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
  }
}
