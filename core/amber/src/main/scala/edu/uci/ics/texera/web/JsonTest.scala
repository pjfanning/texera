package edu.uci.ics.texera.web

import edu.uci.ics.amber.engine.architecture.common.ProcessingHistory
import edu.uci.ics.texera.web.service.{ReplayCheckpointPlanner, WorkflowReplayManager}

import java.io.{FileInputStream, ObjectInputStream}
import java.nio.file.{Files, Paths}
import scala.collection.mutable

object JsonTest {

  def main(args: Array[String]): Unit = {


    val file = Paths.get("").resolve("latest-interation-history-w3")
    if(!Files.exists(file)){
      println("no interaction history found!")
      return
    }
    val ois = new ObjectInputStream(new FileInputStream(file.toFile))
    val history = ois.readObject.asInstanceOf[ProcessingHistory]
    ois.close()

    history.historyArray.foreach{
      i =>
        var cost = 0L
        try{
          cost = history.getSnapshot(i).checkpointCost
        }catch{
          case x: Throwable =>
        }
        println(i+" "+cost)
    }
//    println("-----------------------------------------------")
//    history.historyArray.foreach {
//      i =>
//        val cost = history.getSnapshot(i).getCheckpointCost(ActorVirtualIdentity("Worker:WF1-SortPartitions-operator-4e18bee2-0a12-4478-9482-9bf1a7d32efb-main-0"))
//        println(cost)
//    }
//    println("-----------------------------------------------")
//    history.historyArray.foreach {
//      i =>
//        val cost = history.getSnapshot(i).getCheckpointCost(ActorVirtualIdentity("Worker:WF1-Aggregate-operator-e8482e2e-0024-4fe6-8030-3e4db45abe02-localAgg-0"))
//        println(cost+" "+(cost < costThreshold))
//    }
    val planner = new ReplayCheckpointPlanner(history, 5000)
    val timelimits = Array(5000, 10000, 20000, 30000, 60000)
    timelimits.foreach{
      timelimit =>
        println("tau = "+timelimit+"---------------")
        var last = 0L
        var idx = 0
        val chkptset = mutable.ArrayBuffer[Int]()
        history.historyArray.foreach {
          i =>
            val to_chkpt = i - last > timelimit
            if (to_chkpt) {
              last = i
              chkptset.append(idx)
            }
            idx += 1
        }
        val plan3 = planner.getGlobalPlan(0, history.historyArray.length, timelimit)
        println(plan3.map(i => {
          history.getPlanCost(i)
        }).sum)
        println(chkptset.map(i => {
          history.getPlanCost(i)
        }).sum)
        println((0 until idx).map(i => {
          history.getPlanCost(i)
        }).sum)
    }
  }
}

class JsonTest {}
