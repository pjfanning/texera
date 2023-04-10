package edu.uci.ics.texera.web

import edu.uci.ics.amber.engine.architecture.common.{LogicalExecutionSnapshot, ProcessingHistory, VirtualIdentityUtils}
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity
import edu.uci.ics.texera.web.service.ReplayPlanner

import java.io.{FileInputStream, ObjectInputStream}
import java.nio.file.{Files, Paths}
import scala.collection.mutable

object JsonTest {

  def main(args: Array[String]): Unit = {

    val file = Paths.get("").resolve("latest-interation-history")
    if(!Files.exists(file)){
      println("no interaction history found!")
      return
    }
    val ois = new ObjectInputStream(new FileInputStream(file.toFile))
    val history = ois.readObject.asInstanceOf[ProcessingHistory]
    ois.close()

    val planner = new ReplayPlanner(history)

    val mem = mutable.HashMap[Int, (Iterable[Map[ActorVirtualIdentity, Int]], Long)]()
    history.inputConstant = 1
    val result = planner.getReplayPlan(history.getInteractionIdxes.length, 7000, mem)
    println(result)
//    planner.startPlanning(15, "Complete - optimized", 3)
//    while(planner.hasNext){
//      planner.next()
//    }
//    val planner2 = new ReplayPlanner(history)
//    planner.startPlanning(4, "Partial - naive", 3)
//    while(planner.hasNext){
//      planner.next()
//    }
//    planner.startPlanning(6, "Complete - all", 1)
//    val plan1 = planner.dynamicProgrammingPlanner(0, history.getInteractionTimes.length -1, 1000)
//    val plan1ToPartial = history.getCheckpointCost(plan1, Map[Int, Set[ActorVirtualIdentity]]())
//    val plan2 = planner.partialIterativePlanner(history.getInteractionTimes.length -1, 1000)
//    println("best plan: " + plan1 + "cost = "+plan1ToPartial)
//    println("best plan2: " + plan2.mkString("\n"))
//    println("best plan: " + planner.bruteForcePlanner(0, 7, 5000))
    //println("best plan: "+planner.dynamicProgrammingPlanner(0,7, 4000))
  }
}

class JsonTest {}
