package edu.uci.ics.texera.web

import edu.uci.ics.amber.engine.architecture.common.{Interaction, InteractionHistory, VirtualIdentityUtils}
import edu.uci.ics.texera.web.service.ReplayPlanner

import java.io.{FileInputStream, ObjectInputStream}
import java.nio.file.{Files, Paths}

object JsonTest {

  def main(args: Array[String]): Unit = {

    val file = Paths.get("").resolve("latest-interation-history")
    if(!Files.exists(file)){
      println("no interaction history found!")
      return
    }
    val ois = new ObjectInputStream(new FileInputStream(file.toFile))
    val history = ois.readObject.asInstanceOf[InteractionHistory]
    ois.close()

    val planner = new ReplayPlanner(history)

    // planner.startPlanning(7)
    println("best plan: " + planner.bruteForcePlanner(0, 7, 3000))
    println("best plan2: " + planner.dynamicProgrammingPlanner(0, 7, 3000))

    println("cost 1: " + history.getCheckpointCost(3, planner.getCheckpointMap(Array[Int]())))
    println("cost 2: " + history.getCheckpointCost(5, planner.getCheckpointMap(Array[Int]())))
    println("cost 1: " + history.getCheckpointCost(5, planner.getCheckpointMap(Array(3))))
    println("cost 2: " + history.getCheckpointCost(3, planner.getCheckpointMap(Array(5))))
    //println("best plan: "+planner.dynamicProgrammingPlanner(0,7, 4000))
  }
}

class JsonTest {}
