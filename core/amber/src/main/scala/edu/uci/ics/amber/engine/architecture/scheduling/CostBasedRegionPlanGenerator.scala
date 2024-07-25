package edu.uci.ics.amber.engine.architecture.scheduling

import com.typesafe.scalalogging.LazyLogging
import edu.uci.ics.amber.engine.common.AmberConfig
import edu.uci.ics.amber.engine.common.virtualidentity.PhysicalOpIdentity
import edu.uci.ics.amber.engine.common.workflow.PhysicalLink
import edu.uci.ics.texera.web.resource.dashboard.user.workflow.WorkflowExecutionsResource
import edu.uci.ics.texera.workflow.common.WorkflowContext
import edu.uci.ics.texera.workflow.common.storage.OpResultStorage
import edu.uci.ics.texera.workflow.common.workflow.PhysicalPlan
import edu.uci.ics.texera.workflow.common.workflow.WorkflowParser.{renderInputPhysicalPlanToFile, renderRegionPlanToFile}
import org.jgrapht.alg.connectivity.BiconnectivityInspector
import org.jgrapht.alg.cycle.HawickJamesSimpleCycles
import org.jgrapht.graph.{DirectedAcyclicGraph, DirectedPseudograph}
import org.jgrapht.util.SupplierUtil
import org.jooq.types.UInteger

import scala.collection.convert.ImplicitConversions.`collection AsScalaIterable`
import scala.collection.mutable
import scala.jdk.CollectionConverters.{CollectionHasAsScala, IteratorHasAsScala}



class CostBasedRegionPlanGenerator(
    workflowContext: WorkflowContext,
    initialPhysicalPlan: PhysicalPlan,
    opResultStorage: OpResultStorage,
    costFunction: String = "WALL_CLOCK_RUNTIME",
    schedulingMethod: String = "ALL_MAT"
) extends RegionPlanGenerator(
      workflowContext,
      initialPhysicalPlan,
      opResultStorage
    )
    with LazyLogging {

  private var firstExecution: Boolean = false

  private val operatorEstimatedTime = getOperatorEstimatedTime

  private def getOperatorEstimatedTime: Map[String, Double] = {
    val wid: UInteger = UInteger.valueOf(this.workflowContext.workflowId.id)
    val previousStats = WorkflowExecutionsResource.getStatsForPasta(wid)
    if (previousStats.isEmpty) firstExecution = true
    previousStats.map {
      case(operatorId, statsList)=>
        val latestStat = statsList.maxByOption(_.timestamp)
        operatorId -> (latestStat.get.dataProcessingTime.doubleValue() + latestStat.get.controlProcessingTime.doubleValue())/1E9
    }
  }

  case class SearchResult(
      state: Set[PhysicalLink],
      regionDAG: DirectedAcyclicGraph[Region, RegionLink],
      cost: Double,
      searchTime: Double = 0.0,
      searchFinished: Boolean = false,
      numStatesExplored: Int = 0
  )

  def getNaiveSchedulability(): Boolean = {
    println("Analyzing schedulability")
    tryConnectRegionDAG(this.physicalPlan.nonMaterializedBlockingAndDependeeLinks) match {
      case Left(_) => true
      case Right(_) => false
    }
  }

  def runPasta(): SearchResult = {
    val allResults: Set[SearchResult] = Set(
      topDownSearch(globalSearch = false),
      bottomUpSearch(globalSearch = false),
      topDownSearch(),
      bottomUpSearch()
    )
    allResults.minBy(res=>res.cost)
  }

  def generate(): (RegionPlan, PhysicalPlan) = {

    val regionDAG = createRegionDAG()
    (
      RegionPlan(
        regions = regionDAG.iterator().asScala.toSet,
        regionLinks = regionDAG.edgeSet().asScala.toSet
      ),
      physicalPlan
    )
  }

  /**
    * Create regions based on only pipelined edges. This does not add the region links.
    * @param physicalPlan The original physical plan without materializations added yet.
    * @param matEdges Set of edges to materialize (including the original blocking edges).
    * @return A set of regions.
    */
  private def createRegions(
      physicalPlan: PhysicalPlan,
      matEdges: Set[PhysicalLink]
  ): Set[Region] = {
    val matEdgesRemovedDAG = matEdges.foldLeft(physicalPlan) { (currentPlan, linkToRemove) =>
      currentPlan.removeLink(linkToRemove)
    }
    val connectedComponents = new BiconnectivityInspector[PhysicalOpIdentity, PhysicalLink](
      matEdgesRemovedDAG.dag
    ).getConnectedComponents.asScala.toSet
    connectedComponents.zipWithIndex.map {
      case (connectedSubDAG, idx) =>
        val operatorIds = connectedSubDAG.vertexSet().asScala.toSet
        val links = operatorIds
          .flatMap(operatorId => {
            physicalPlan.getUpstreamPhysicalLinks(operatorId) ++ physicalPlan
              .getDownstreamPhysicalLinks(operatorId)
          })
          .filter(link => operatorIds.contains(link.fromOpId))
        val operators = operatorIds.map(operatorId => physicalPlan.getOperator(operatorId))
        Region(
          id = RegionIdentity(idx),
          physicalOps = operators,
          physicalLinks = links
        )
    }
  }

  /**
    * Checks a plan for schedulability, and returns a region DAG if the plan is schedulable.
    * @param matEdges Set of edges to materialize (including the original blocking edges).
    * @return If the plan is schedulable, a region DAG will be returned. Otherwise a DirectedPseudograph (with directed
    *         cycles) will be returned to indicate that the plan is unschedulable.
    */
  private def tryConnectRegionDAG(
      matEdges: Set[PhysicalLink]
  ): Either[DirectedAcyclicGraph[Region, RegionLink], DirectedPseudograph[Region, RegionLink]] = {
    val regionDAG = new DirectedAcyclicGraph[Region, RegionLink](
      null, // vertexSupplier
      SupplierUtil.createSupplier(classOf[RegionLink]), // edgeSupplier
      true, // weighted
      true // allowMultipleEdges
    )
    val regionGraph = new DirectedPseudograph[Region, RegionLink](classOf[RegionLink])
    val opToRegionMap = new mutable.HashMap[PhysicalOpIdentity, Region]
    createRegions(physicalPlan, matEdges).foreach(region => {
      region.getOperators.foreach(op => opToRegionMap(op.id) = region)
      regionGraph.addVertex(region)
      regionDAG.addVertex(region)
    })
    var isAcyclic = true
    matEdges.foreach(physicalLink => {
      val fromRegion = opToRegionMap(physicalLink.fromOpId)
      val toRegion = opToRegionMap(physicalLink.toOpId)
      regionGraph.addEdge(fromRegion, toRegion, RegionLink(fromRegion.id, toRegion.id, Some(physicalLink)))
      try {
        regionDAG.addEdge(fromRegion, toRegion, RegionLink(fromRegion.id, toRegion.id, Some(physicalLink)))
      } catch {
        case _: IllegalArgumentException =>
          isAcyclic = false
      }
    })
    if (isAcyclic) Left(regionDAG)
    else Right(regionGraph)
  }

  /**
    * Performs a search to generate a region DAG.
    * Materializations are added only after the plan is determined to be schedulable.
    * @return A region DAG.
    */
  private def createRegionDAG(): DirectedAcyclicGraph[Region, RegionLink] = {
    renderInputPhysicalPlanToFile(physicalPlan = physicalPlan, imageOutputPath = "/Users/xzliu/Downloads/" + workflowContext.workflowId.id + ".png")
    println(s"numOperators: ${physicalPlan.operators.size}, numLinks: ${physicalPlan.links.size}")

    val searchResult = schedulingMethod match {
      case "ALL_MAT" => allMatMethod
      case "TOP_DOWN_GLOBAL" => topDownSearch()
      case "BOTTOM_UP_GLOBAL" => bottomUpSearch()
      case "TOP_DOWN_GREEDY" => topDownSearch(globalSearch = false)
      case "BOTTOM_UP_GREEDY" => bottomUpSearch(globalSearch = false)
      case "BASELINE" => baselineMethod
      case _ => allMatMethod
    }
    logger.info(s"WID: ${workflowContext.workflowId}, EID: ${workflowContext.executionId}, Scheduling method: $schedulingMethod, Cost of schedule: ${searchResult.cost}, scheduler ran for: ${searchResult.searchTime / 10E6}")
    // Only a non-dependee blocking link that has not already been materialized should be replaced
    // with a materialization write op + materialization read op.
    val linksToMaterialize =
      (searchResult.state ++ physicalPlan.nonMaterializedBlockingAndDependeeLinks).diff(physicalPlan.getDependeeLinks)
    if (linksToMaterialize.nonEmpty) {
      val matReaderWriterPairs = new mutable.HashMap[PhysicalOpIdentity, PhysicalOpIdentity]()
      linksToMaterialize.foreach(link =>
        physicalPlan = replaceLinkWithMaterialization(
          link,
          matReaderWriterPairs
        )
      )
    }
    // Since the plan is now schedulable, calling the search directly returns a region DAG.
    val regionDAG = (schedulingMethod match {
      case "ALL_MAT" => allMatMethod
      case "TOP_DOWN_GLOBAL" => topDownSearch()
      case "BOTTOM_UP_GLOBAL" => bottomUpSearch()
      case "TOP_DOWN_GREEDY" => topDownSearch(globalSearch = false)
      case "BOTTOM_UP_GREEDY" => bottomUpSearch(globalSearch = false)
      case "BASELINE" => baselineMethod
      case _ => allMatMethod
    }).regionDAG
    addMaterializationsAsRegionLinks(linksToMaterialize, regionDAG)
    populateDependeeLinks(regionDAG)
    if (workflowContext.userId.nonEmpty) allocateResource(regionDAG)
    regionDAG
  }

  /**
    * Adds materialization links as region links within the given region DAG.
    * This method processes each physical link in the input set, identifying the source and destination
    * regions for each link. It then adds an edge between these regions in the DAG to represent
    * the materialization relationship.
    *
    * @param linksToMaterialize The set of physical links to be materialized as region links in the DAG.
    * @param regionDAG The DAG of regions to be modified
    */
  private def addMaterializationsAsRegionLinks(
      linksToMaterialize: Set[PhysicalLink],
      regionDAG: DirectedAcyclicGraph[Region, RegionLink]
  ): Unit = {
    linksToMaterialize.foreach(link => {
      val fromOpRegions = getRegions(link.fromOpId, regionDAG)
      val toOpRegions = getRegions(link.toOpId, regionDAG)
      fromOpRegions.foreach(fromRegion => {
        toOpRegions.foreach(toRegion => {
          regionDAG.addEdge(fromRegion, toRegion, RegionLink(fromRegion.id, toRegion.id))
        })
      })
    })
  }

  /**
    * The core of the search algorithm. If the input physical plan is already schedulable, no search will be executed.
    * Otherwise, depending on the configuration, either a global search or a greedy search will be performed to find
    * an optimal plan. The search starts from a plan where all non-blocking edges are pipelined, and leads to a low-cost
    * schedulable plan. Optimizations based on chains and bridges are included in the search.
    *
    * @return A SearchResult containing the plan, the region DAG (without materializations added yet) and the cost.
    */
   def bottomUpSearch(globalSearch: Boolean = true, oChains: Boolean = true, oCleanEdges: Boolean = true, oEarlyStop: Boolean = true): SearchResult = {
    val startTime = System.nanoTime()
    val originalNonBlockingEdges = if (oCleanEdges) physicalPlan.nonCleanEdgeNonBlockingLinks else physicalPlan.nonBlockingLinks // Optimization 2: Bridges
    // Queue to hold states to be explored, starting with the empty set
    val queue: mutable.Queue[Set[PhysicalLink]] = mutable.Queue(Set.empty[PhysicalLink])
    // Keep track of visited states to avoid revisiting
    val visited: mutable.Set[Set[PhysicalLink]] = mutable.Set.empty[Set[PhysicalLink]]
    // For O3: early stop
    val schedulableStates: mutable.Set[Set[PhysicalLink]] = mutable.Set.empty[Set[PhysicalLink]]
    // Initialize the bestResult with an impossible high cost for comparison
    var bestResult: SearchResult = SearchResult(
      state = Set.empty,
      regionDAG = new DirectedAcyclicGraph[Region, RegionLink](classOf[RegionLink]),
      cost = Double.PositiveInfinity
    )

    while (queue.nonEmpty) {
      val searchTime = System.nanoTime() - startTime
      if (searchTime > 3E11) return bestResult.copy(searchTime=searchTime, searchFinished = false, numStatesExplored = visited.size)
      // A state is represented as a set of materialized non-blocking edges.
      val currentState = queue.dequeue()
      visited.add(currentState)

      tryConnectRegionDAG(
        physicalPlan.nonMaterializedBlockingAndDependeeLinks ++ currentState
      ) match {
        case Left(regionDAG) =>
          checkSchedulableState(regionDAG)
          if (!oEarlyStop) expandFrontier()
        // No need to explore further
        case Right(_) =>
          expandFrontier()
      }

      def checkSchedulableState(regionDAG: DirectedAcyclicGraph[Region, RegionLink]): Unit = {
        if (oEarlyStop) schedulableStates.add(currentState)
        // Calculate the current state's cost and update the bestResult if it's lower
        val cost =
          evaluate(regionDAG.vertexSet().asScala.toSet, regionDAG.edgeSet().asScala.toSet)
        if (cost < bestResult.cost) {
          bestResult = SearchResult(currentState, regionDAG, cost)
        }
      }

      def expandFrontier(): Unit = {
        val allBlockingEdges = // TODO: rename
          currentState ++ physicalPlan.nonMaterializedBlockingAndDependeeLinks
        // Generate and enqueue all neighbour states that haven't been visited
        var candidateEdges = originalNonBlockingEdges
          .diff(currentState)
        if (oChains) {
          val edgesInChainWithBlockingEdge = physicalPlan.maxChains
            .filter(chain => chain.intersect(allBlockingEdges).nonEmpty)
            .flatten
          candidateEdges = candidateEdges.diff(edgesInChainWithBlockingEdge) // Edges in chain with blocking edges should not be materialized
          if (this.costFunction == "MATERIALIZATION_SIZES") {
            val nonMinCostEdgesInChainWithAllNonBlockingEdge = physicalPlan.maxChains
              .filter(chainSet => chainSet.forall(link => physicalPlan.nonBlockingLinks.contains(link)))
              .flatMap(chainSet => chainSet - chainSet.minBy(link => physicalPlan.dag.getEdgeWeight(link)))
            candidateEdges = candidateEdges.diff(nonMinCostEdgesInChainWithAllNonBlockingEdge)
          }
        }

        var unvisitedNeighborStates = candidateEdges.map(edge=>currentState + edge).filter(neighborState => !visited.contains(neighborState) && !queue.contains(neighborState))

        if (oEarlyStop) unvisitedNeighborStates = unvisitedNeighborStates.filter(neighborState => !schedulableStates.exists(ancestorState=>ancestorState.subsetOf(neighborState)))

        if (globalSearch) {
          unvisitedNeighborStates.foreach(neighborState => queue.enqueue(neighborState))
        } else {
          if (unvisitedNeighborStates.nonEmpty) {
            val minCostNeighborState = unvisitedNeighborStates.minBy(neighborState => tryConnectRegionDAG(physicalPlan.nonMaterializedBlockingAndDependeeLinks ++ neighborState) match {
              case Left(regionDAG) =>
                evaluate(regionDAG.vertexSet().asScala.toSet, regionDAG.edgeSet().asScala.toSet)
              case Right(regionGraph) =>
                Double.MaxValue
//                evaluate(
//                  regionGraph.vertexSet().asScala.toSet,
//                  regionGraph.edgeSet().asScala.toSet
//                )
            })
            queue.enqueue(minCostNeighborState)
          }
        }
      }
    }

    val searchTime = System.nanoTime() - startTime
    bestResult.copy(searchTime=searchTime, searchFinished = true, numStatesExplored = visited.size)
  }

  def topDownSearch(globalSearch: Boolean = true, oChains: Boolean = true, oCleanEdges: Boolean = true, oEarlyStop: Boolean = true): SearchResult = {
    val startTime = System.nanoTime()

    // Queue to hold states to be explored, starting with the empty set
    var seedState = this.physicalPlan.nonBlockingLinks

    if (oChains) {
      val edgesInChainWithBlockingEdge = physicalPlan.maxChains
        .filter(chain => chain.intersect(physicalPlan.nonMaterializedBlockingAndDependeeLinks).nonEmpty)
        .flatten
      seedState = seedState.diff(edgesInChainWithBlockingEdge)
      if (this.costFunction == "MATERIALIZATION_SIZES") {
        val nonMinCostEdgesInChainWithAllNonBlockingEdge = physicalPlan.maxChains
          .filter(chainSet => chainSet.forall(link => physicalPlan.nonBlockingLinks.contains(link)))
          .flatMap(chainSet => chainSet - chainSet.minBy(link => physicalPlan.dag.getEdgeWeight(link)))
        seedState = seedState.diff(nonMinCostEdgesInChainWithAllNonBlockingEdge)
      }
    }

    if (oCleanEdges) seedState = seedState.intersect(physicalPlan.nonCleanEdgeNonBlockingLinks)

    val queue: mutable.Queue[Set[PhysicalLink]] = mutable.Queue(seedState)
    // Keep track of visited states to avoid revisiting
    val visited: mutable.Set[Set[PhysicalLink]] = mutable.Set.empty[Set[PhysicalLink]]
    // Initialize the bestResult with an impossible high cost for comparison
    val hopelessStates: mutable.Set[Set[PhysicalLink]] = mutable.Set.empty[Set[PhysicalLink]]
    var bestResult: SearchResult = SearchResult(
      state = Set.empty,
      regionDAG = new DirectedAcyclicGraph[Region, RegionLink](classOf[RegionLink]),
      cost = Double.PositiveInfinity
    )

    while (queue.nonEmpty) {
      val searchTime = System.nanoTime() - startTime
      if (searchTime > 3E11)
        return bestResult.copy(searchTime=searchTime, searchFinished = false, numStatesExplored = visited.size)

      val currentState = queue.dequeue()
      visited.add(currentState)
      tryConnectRegionDAG(
        physicalPlan.nonMaterializedBlockingAndDependeeLinks ++ currentState
      ) match {
        case Left(regionDAG) =>
          checkSchedulableState(regionDAG)
          expandFrontier()
        // No need to explore further
        case Right(regionGraph) =>
          var isCurrentStateHopeless = false
          if (oEarlyStop) {
            // Check if the current state is also hopeless
            val physicalLinksInRegionCycles = new HawickJamesSimpleCycles(regionGraph).findSimpleCycles().flatMap{cycle =>
              cycle.toList.sliding(2)
                .map {
                case List(v1, v2) => regionGraph.getAllEdges(v1, v2).toSet
                case _ => Set.empty[RegionLink]
              }
            }.toSet.flatten.map { regionLink =>
              regionLink.originalPhysicalLink match {
                case Some(physicalLink: PhysicalLink) => physicalLink
                case None => PhysicalLink.defaultInstance
              }
            }
            isCurrentStateHopeless = physicalPlan.nonMaterializedBlockingAndDependeeLinks.intersect(physicalLinksInRegionCycles).nonEmpty
            if (isCurrentStateHopeless) {
              hopelessStates.add(currentState)
            }
          }
          if (!oEarlyStop || !isCurrentStateHopeless) expandFrontier()
      }

      def checkSchedulableState(regionDAG: DirectedAcyclicGraph[Region, RegionLink]): Unit = {
        // Calculate the current state's cost and update the bestResult if it's lower
        val cost =
          evaluate(regionDAG.vertexSet().asScala.toSet, regionDAG.edgeSet().asScala.toSet)
        if (cost < bestResult.cost) {
          bestResult = SearchResult(currentState, regionDAG, cost)
        }
      }

      def expandFrontier(): Unit = {
        // Generate and enqueue all neighbour states that haven't been visited
        var unvisitedNeighborStates = currentState.map(edge => currentState - edge).filter(neighborState => !visited.contains(neighborState) && !queue.contains(neighborState))

        if (oEarlyStop) unvisitedNeighborStates = unvisitedNeighborStates.filter(neighborState => !hopelessStates.exists(ancestorState => neighborState.subsetOf(ancestorState)))

        if (globalSearch) {
          unvisitedNeighborStates.foreach(neighborState => queue.enqueue(neighborState))
        } else {
          if (unvisitedNeighborStates.nonEmpty) {
            val minCostNeighborState = unvisitedNeighborStates.minBy(neighborState => tryConnectRegionDAG(physicalPlan.nonMaterializedBlockingAndDependeeLinks ++ neighborState) match {
              case Left(regionDAG) =>
                evaluate(regionDAG.vertexSet().asScala.toSet, regionDAG.edgeSet().asScala.toSet)
              case Right(regionGraph) =>
                Double.MaxValue
//                evaluate(
//                  regionGraph.vertexSet().asScala.toSet,
//                  regionGraph.edgeSet().asScala.toSet
//                )
            })
            queue.enqueue(minCostNeighborState)
          }
        }
      }
    }
    val searchTime = System.nanoTime() - startTime
    bestResult.copy(searchTime=searchTime, searchFinished = true, numStatesExplored = visited.size)
  }

  def allMatMethod: SearchResult = {
    val startTime = System.nanoTime()
    val allNonMaterializedLinks = this.physicalPlan.getAllNonMaterializedLinks
    var cost = 0.0
    SearchResult(
      state = allNonMaterializedLinks.diff(physicalPlan.nonMaterializedBlockingAndDependeeLinks),
      regionDAG = tryConnectRegionDAG(allNonMaterializedLinks) match {
        case Left(regionDAG) => {
          cost = evaluate(regionDAG.vertexSet().asScala.toSet, regionDAG.edgeSet().asScala.toSet)
          regionDAG
        }
        case Right(_) =>
          throw new RuntimeException("All-materialized plan cannot be cyclic.")
      },
      cost = cost,
      searchFinished = true,
      searchTime = System.nanoTime() - startTime
    )
  }

   def baselineMethod: SearchResult = {
    val startTime = System.nanoTime()
    var currentRegionPlan = physicalPlan.links
    physicalPlan.topologicalIterator().foreach(physicalOpId=> {
      physicalPlan.getUpstreamPhysicalLinks(physicalOpId).foreach(physicalLink=>{
        if (!physicalPlan.nonMaterializedBlockingAndDependeeLinks.contains(physicalLink)) {
          val newRegionPlan = currentRegionPlan - physicalLink
          tryConnectRegionDAG(newRegionPlan) match {
            case Left(_) => currentRegionPlan = newRegionPlan
            case Right(_) =>
          }
        }
      })
    })
    tryConnectRegionDAG(currentRegionPlan) match {
      case Left(regionDAG) =>
        val searchTime = System.nanoTime() - startTime
        SearchResult(state = currentRegionPlan.diff(physicalPlan.nonMaterializedBlockingAndDependeeLinks), regionDAG = regionDAG, cost = evaluate(regionDAG.vertexSet().asScala.toSet, regionDAG.edgeSet().asScala.toSet), searchTime=searchTime)
      case Right(_) => throw new Exception("Baseline method should return a region DAG.")
    }
  }

  /**
    * The cost function used by the search. Takes in a region graph represented as set of regions and links.
    * @param regions A set of regions created based on a search state.
    * @param regionLinks A set of links to indicate dependencies between regions, based on the materialization edges.
    * @return A cost determined by the resource allocator.
    */
  private def evaluate(regions: Set[Region], regionLinks: Set[RegionLink]): Double = {
    // Using number of materialization (region) edges as the cost.
    // This is independent of the schedule / resource allocator.
    // In the future we may need to use the ResourceAllocator to get the cost.
    this.costFunction match {
      case "WALL_CLOCK_RUNTIME" => regions.foldLeft(0.0) {
        (sum, region) => {
          val times = region.getOperators.map(op=> {
            this.operatorEstimatedTime.getOrElse(op.id.logicalOpId.id, 1.0)
          })
          val maxTime = if (times.nonEmpty) times.max else 0.0
          sum + maxTime
        }
      }
      case "MATERIALIZATION_SIZES" => regionLinks.toList.map(regionLink => physicalPlan.dag.getEdgeWeight(regionLink.originalPhysicalLink.get)).sum
      case _ => throw new UnsupportedOperationException("Unsupported cost function!")
    }
  }

}
