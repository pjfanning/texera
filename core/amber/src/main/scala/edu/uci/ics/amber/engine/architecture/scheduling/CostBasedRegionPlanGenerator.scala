package edu.uci.ics.amber.engine.architecture.scheduling

import com.typesafe.scalalogging.LazyLogging
import edu.uci.ics.amber.engine.common.AmberConfig
import edu.uci.ics.amber.engine.common.virtualidentity.PhysicalOpIdentity
import edu.uci.ics.amber.engine.common.workflow.PhysicalLink
import edu.uci.ics.texera.web.resource.dashboard.user.workflow.WorkflowExecutionsResource
import edu.uci.ics.texera.workflow.common.WorkflowContext
import edu.uci.ics.texera.workflow.common.storage.OpResultStorage
import edu.uci.ics.texera.workflow.common.workflow.PhysicalPlan
import org.jgrapht.alg.connectivity.BiconnectivityInspector
import org.jgrapht.graph.{DirectedAcyclicGraph, DirectedPseudograph}
import org.jooq.types.UInteger

import scala.collection.mutable
import scala.jdk.CollectionConverters.{CollectionHasAsScala, IteratorHasAsScala}

class CostBasedRegionPlanGenerator(
    workflowContext: WorkflowContext,
    initialPhysicalPlan: PhysicalPlan,
    opResultStorage: OpResultStorage
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
        operatorId -> latestStat.get.dataProcessingTime.doubleValue()/1E9
    }
  }

  private case class SearchResult(
      state: Set[PhysicalLink],
      regionDAG: DirectedAcyclicGraph[Region, RegionLink],
      cost: Double
  )

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
    val regionDAG = new DirectedAcyclicGraph[Region, RegionLink](classOf[RegionLink])
    val regionGraph = new DirectedPseudograph[Region, RegionLink](classOf[RegionLink])
    val opToRegionMap = new mutable.HashMap[PhysicalOpIdentity, Region]
    createRegions(physicalPlan, matEdges).foreach(region => {
      region.getOperators.foreach(op => opToRegionMap(op.id) = region)
      regionGraph.addVertex(region)
      regionDAG.addVertex(region)
    })
    var isAcyclic = true
    matEdges.foreach(blockingEdge => {
      val fromRegion = opToRegionMap(blockingEdge.fromOpId)
      val toRegion = opToRegionMap(blockingEdge.toOpId)
      regionGraph.addEdge(fromRegion, toRegion, RegionLink(fromRegion.id, toRegion.id))
      try {
        regionDAG.addEdge(fromRegion, toRegion, RegionLink(fromRegion.id, toRegion.id))
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
    val searchResult = bottomUpSearch()
    // Only a non-dependee blocking link that has not already been materialized should be replaced
    // with a materialization write op + materialization read op.
    val linksToMaterialize =
      searchResult.state ++ physicalPlan.getNonMaterializedBlockingAndDependeeLinks.diff(
        physicalPlan.getDependeeLinks
      )
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
    val regionDAG = bottomUpSearch().regionDAG
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
  private def bottomUpSearch(globalSearch: Boolean = true, oChains: Boolean = true, oCleanEdges: Boolean = true, oEarlyStop: Boolean = true): SearchResult = {
    if (this.firstExecution) {
      val allNonMaterializedLinks = this.physicalPlan.getAllNonMaterializedLinks
      return SearchResult(
        state = allNonMaterializedLinks,
        regionDAG = tryConnectRegionDAG(allNonMaterializedLinks) match {
          case Left(regionDAG) => regionDAG
          case Right(_) =>
            throw new RuntimeException("All-materialized plan cannot be cyclic.")
        },
        cost = 0.0
      )
    }

    val originalNonBlockingEdges = if (oCleanEdges) physicalPlan.getNonBridgeNonBlockingLinks else physicalPlan.getNonBlockingLinks // Optimization 2: Bridges
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
      // A state is represented as a set of materialized non-blocking edges.
      val currentState = queue.dequeue()
      visited.add(currentState)

      tryConnectRegionDAG(
        physicalPlan.getNonMaterializedBlockingAndDependeeLinks ++ currentState
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
        val allBlockingEdges =
          currentState ++ physicalPlan.getNonMaterializedBlockingAndDependeeLinks
        // Generate and enqueue all neighbour states that haven't been visited
        var candidateEdges = originalNonBlockingEdges
          .diff(currentState)
        if (oChains) {
          val edgesInChainWithBlockingEdge = physicalPlan.maxChains
            .filter(chain => chain.intersect(allBlockingEdges).nonEmpty)
            .flatten
          candidateEdges = candidateEdges.diff(edgesInChainWithBlockingEdge)
        }

        var unvisitedNeighborStates = candidateEdges.map(edge=>currentState + edge).filter(neighborState => !visited.contains(neighborState) && !queue.contains(neighborState))

        if (oEarlyStop) unvisitedNeighborStates = unvisitedNeighborStates.filter(neighborState => !schedulableStates.exists(ancestorState=>ancestorState.subsetOf(neighborState)))

        if (globalSearch) {
          unvisitedNeighborStates.foreach(neighborState => queue.enqueue(neighborState))
        } else {
          val minCostNeighborState = unvisitedNeighborStates.minBy(neighborState => tryConnectRegionDAG(physicalPlan.getNonMaterializedBlockingAndDependeeLinks ++ neighborState) match {
            case Left(regionDAG) =>
              evaluate(regionDAG.vertexSet().asScala.toSet, regionDAG.edgeSet().asScala.toSet)
            case Right(regionGraph) =>
              evaluate(
                regionGraph.vertexSet().asScala.toSet,
                regionGraph.edgeSet().asScala.toSet
              )
          })
          queue.enqueue(minCostNeighborState)
        }
      }
    }

    bestResult
  }

  // TODO: Migrate topDown search
  private def topDownSearch(): SearchResult = {
    val originalNonBlockingEdges = physicalPlan.getNonBridgeNonBlockingLinks // Optimization 2: Bridges
    // Queue to hold states to be explored, starting with the empty set
    val queue: mutable.Queue[Set[PhysicalLink]] = mutable.Queue(Set.empty[PhysicalLink])
    // Keep track of visited states to avoid revisiting
    val visited: mutable.Set[Set[PhysicalLink]] = mutable.Set.empty[Set[PhysicalLink]]
    // Initialize the bestResult with an impossible high cost for comparison
    var bestResult: SearchResult = SearchResult(
      state = Set.empty,
      regionDAG = new DirectedAcyclicGraph[Region, RegionLink](classOf[RegionLink]),
      cost = Double.PositiveInfinity
    )

    while (queue.nonEmpty) {
      val currentState = queue.dequeue()
      visited.add(currentState)
      tryConnectRegionDAG(
        physicalPlan.getNonMaterializedBlockingAndDependeeLinks ++ currentState
      ) match {
        case Left(regionDAG) =>
          // Calculate the current state's cost and update the bestResult if it's lower
          val cost =
            evaluate(regionDAG.vertexSet().asScala.toSet, regionDAG.edgeSet().asScala.toSet)
          if (cost < bestResult.cost) {
            bestResult = SearchResult(currentState, regionDAG, cost)
          }
        // No need to explore further
        case Right(_) =>
          val allBlockingEdges =
            currentState ++ physicalPlan.getNonMaterializedBlockingAndDependeeLinks
          // Generate and enqueue all neighbour states that haven't been visited
          val edgesInChainWithBlockingEdge = physicalPlan.maxChains
            .filter(chain => chain.intersect(allBlockingEdges).nonEmpty)
            .flatten
          val candidateEdges = originalNonBlockingEdges
            .diff(edgesInChainWithBlockingEdge) // Optimization 1: Chains
            .diff(currentState)
          if (AmberConfig.useGlobalSearch) {
            candidateEdges.foreach { link =>
              val nextState = currentState + link
              if (!visited.contains(nextState) && !queue.contains(nextState)) {
                queue.enqueue(nextState)
              }
            }
          } else {
            val nextLink = candidateEdges.minBy(edge =>
              tryConnectRegionDAG(
                physicalPlan.getNonMaterializedBlockingAndDependeeLinks ++ currentState + edge
              ) match {
                case Left(regionDAG) =>
                  evaluate(regionDAG.vertexSet().asScala.toSet, regionDAG.edgeSet().asScala.toSet)
                case Right(regionGraph) =>
                  evaluate(
                    regionGraph.vertexSet().asScala.toSet,
                    regionGraph.edgeSet().asScala.toSet
                  )
              }
            )
            val nextState = currentState + nextLink
            if (!visited.contains(nextState) && !queue.contains(nextState)) {
              queue.enqueue(nextState)
            }
          }
      }
    }

    bestResult
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
    regions.foldLeft(0.0) {
      (sum, region) => {
        val times = region.getOperators.map(op=> {
          this.operatorEstimatedTime.getOrElse(op.id.logicalOpId.id, 1.0)
        })
        val maxTime = if (times.nonEmpty) times.max else 0.0
        sum + maxTime
      }
    }
//    regionLinks.size
  }

}
