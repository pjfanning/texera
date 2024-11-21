package edu.uci.ics.amber.engine.architecture.scheduling

import edu.uci.ics.amber.engine.common.model.{PhysicalPlan, WorkflowContext}
import edu.uci.ics.amber.engine.common.virtualidentity.{ActorVirtualIdentity, PhysicalOpIdentity}
import edu.uci.ics.amber.engine.common.workflow.PhysicalLink
import edu.uci.ics.amber.engine.common.{AmberConfig, AmberLogging}
import edu.uci.ics.texera.workflow.common.storage.OpResultStorage
import org.jgrapht.alg.connectivity.BiconnectivityInspector
import org.jgrapht.graph.{DirectedAcyclicGraph, DirectedPseudograph}

import scala.collection.mutable
import scala.jdk.CollectionConverters._

class CostBasedRegionPlanGenerator(
    workflowContext: WorkflowContext,
    initialPhysicalPlan: PhysicalPlan,
    opResultStorage: OpResultStorage,
    val actorId: ActorVirtualIdentity
) extends RegionPlanGenerator(
      workflowContext,
      initialPhysicalPlan,
      opResultStorage
    )
    with AmberLogging {

  case class SearchResult(
      state: Set[PhysicalLink],
      regionDAG: DirectedAcyclicGraph[Region, RegionLink],
      cost: Double,
      searchTimeNanoSeconds: Long = 0,
      numStatesExplored: Int = 0
  )

  def generate(): (RegionPlan, PhysicalPlan) = {

    val startTime = System.nanoTime()
    val regionDAG = createRegionDAG()
    val totalRPGTime = System.nanoTime() - startTime
    logger.info(
      s"WID: ${workflowContext.workflowId.id}, EID: ${workflowContext.executionId.id}, total RPG time: " +
        s"${totalRPGTime / 1e6} ms."
    )
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
        val materializedPortIds: Set[GlobalPortIdentity] = matEdges.flatMap(link =>
          List(
            GlobalPortIdentity(link.fromOpId, link.fromPortId, input = false)
          )
        )
        Region(
          id = RegionIdentity(idx),
          physicalOps = operators,
          physicalLinks = links,
          materializedPortIds = materializedPortIds
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
    matEdges.foreach(matEdge => {
      val fromRegion = opToRegionMap(matEdge.fromOpId)
      val toRegion = opToRegionMap(matEdge.toOpId)
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
    val searchResult = bottomUpSearch(globalSearch = AmberConfig.useGlobalSearch)
    // Only a non-dependee blocking link that has not already been materialized should be replaced
    // with a materialization write op + materialization read op.
    logger.info(
      s"WID: ${workflowContext.workflowId.id}, EID: ${workflowContext.executionId.id}, search time: " +
        s"${searchResult.searchTimeNanoSeconds / 1e6} ms."
    )
    val linksToMaterialize =
      (searchResult.state ++ physicalPlan.getNonMaterializedBlockingAndDependeeLinks).diff(
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
    allocateResource(regionDAG)
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
    * schedulable plan by changing pipelined non-blocking edges to materialized. By default all pruning techniques
    * are enabled (chains, clean edges).
    *
    * @return A SearchResult containing the plan, the region DAG (without materializations added yet), the cost, the
    *         time to finish search, and the number of states explored.
    */
  def bottomUpSearch(
      globalSearch: Boolean = false,
      oChains: Boolean = true,
      oCleanEdges: Boolean = true
  ): SearchResult = {
    val startTime = System.nanoTime()
    val originalNonBlockingEdges =
      if (oCleanEdges) {
        physicalPlan.getNonBridgeNonBlockingLinks
      } else {
        physicalPlan.links.diff(
          physicalPlan.getNonMaterializedBlockingAndDependeeLinks
        )
      }
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
      // A state is represented as a set of materialized non-blocking edges.
      val currentState = queue.dequeue()
      visited.add(currentState)

      tryConnectRegionDAG(
        physicalPlan.getNonMaterializedBlockingAndDependeeLinks ++ currentState
      ) match {
        case Left(regionDAG) =>
          updateOptimumIfApplicable(regionDAG)
          addNeighborStatesToFrontier()
        case Right(_) =>
          addNeighborStatesToFrontier()
      }

      /**
        * An internal method of bottom-up search that updates the current optimum if the examined state is schedulable
        * and has a lower cost.
        */
      def updateOptimumIfApplicable(regionDAG: DirectedAcyclicGraph[Region, RegionLink]): Unit = {
        // Calculate the current state's cost and update the bestResult if it's lower
        val cost =
          evaluate(regionDAG.vertexSet().asScala.toSet, regionDAG.edgeSet().asScala.toSet)
        if (cost < bestResult.cost) {
          bestResult = SearchResult(currentState, regionDAG, cost)
        }
      }

      /**
        * An internal method of bottom-up search that performs state transitions (changing an pipelined edge to
        * materialized) to include the unvisited neighbor(s) of the current state in the frontier (i.e., the queue).
        * If using global search, all unvisited neighbors will be included. Otherwise in a greedy search, only the
        * neighbor with the lowest cost will be included.
        */
      def addNeighborStatesToFrontier(): Unit = {
        val allCurrentMaterializedEdges =
          currentState ++ physicalPlan.getNonMaterializedBlockingAndDependeeLinks
        // Generate and enqueue all neighbour states that haven't been visited
        var candidateEdges = originalNonBlockingEdges
          .diff(currentState)
        if (oChains) {
          val edgesInChainWithMaterializedEdges = physicalPlan.maxChains
            .filter(chain => chain.intersect(allCurrentMaterializedEdges).nonEmpty)
            .flatten
          candidateEdges = candidateEdges.diff(
            edgesInChainWithMaterializedEdges
          ) // Edges in chain with blocking edges should not be materialized
        }

        val unvisitedNeighborStates = candidateEdges
          .map(edge => currentState + edge)
          .filter(neighborState =>
            !visited.contains(neighborState) && !queue.contains(neighborState)
          )

        if (globalSearch) {
          // include all unvisited neighbors
          unvisitedNeighborStates.foreach(neighborState => queue.enqueue(neighborState))
        } else {
          // greedy search, only include an unvisited neighbor with the lowest cost
          if (unvisitedNeighborStates.nonEmpty) {
            val minCostNeighborState = unvisitedNeighborStates.minBy(neighborState =>
              tryConnectRegionDAG(
                physicalPlan.getNonMaterializedBlockingAndDependeeLinks ++ neighborState
              ) match {
                case Left(regionDAG) =>
                  evaluate(regionDAG.vertexSet().asScala.toSet, regionDAG.edgeSet().asScala.toSet)
                case Right(_) =>
                  Double.MaxValue
              }
            )
            queue.enqueue(minCostNeighborState)
          }
        }
      }
    }

    val searchTime = System.nanoTime() - startTime
    bestResult.copy(
      searchTimeNanoSeconds = searchTime,
      numStatesExplored = visited.size
    )
  }

  /**
    * The cost function used by the search. Takes in a region graph represented as set of regions and links.
    * @param regions A set of regions created based on a search state.
    * @param regionLinks A set of links to indicate dependencies between regions, based on the materialization edges.
    * @return A cost determined by the resource allocator.
    */
  private def evaluate(regions: Set[Region], regionLinks: Set[RegionLink]): Double = {
    // Using number of materialized ports as the cost.
    // This is independent of the schedule / resource allocator.
    // In the future we may need to use the ResourceAllocator to get the cost.
    regions.flatMap(_.materializedPortIds).size
  }

}
