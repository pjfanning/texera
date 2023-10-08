package edu.uci.ics.texera.web

import akka.actor.ActorSystem
import akka.serialization.SerializationExtension
import com.twitter.util.Promise
import edu.uci.ics.amber.engine.architecture.checkpoint.{CheckpointHolder, SavedCheckpoint, SerializedState}
import edu.uci.ics.amber.engine.architecture.common.ProcessingHistory
import edu.uci.ics.amber.engine.common.AmberUtils
import edu.uci.ics.amber.engine.common.AmberUtils.akkaConfig
import edu.uci.ics.amber.engine.common.ambermessage.{ChannelEndpointID, DPMessage, EndOfUpstream, WorkflowFIFOMessage}
import edu.uci.ics.amber.engine.common.lbmq.{LinkedBlockingMultiQueue, LinkedBlockingSubQueue}
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity
import edu.uci.ics.amber.engine.common.virtualidentity.util.{CLIENT, CONTROLLER, SELF}
import edu.uci.ics.texera.web.service.{ReplayCheckpointPlanner, WorkflowReplayManager}

import java.io.{FileInputStream, ObjectInputStream}
import java.nio.file.{Files, Paths}
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock
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
    history.historyArray.foreach{
      i =>
        val cost = history.getSnapshot(i).getCheckpointCost(ActorVirtualIdentity("Worker:WF3-SortPartitions-operator-4e18bee2-0a12-4478-9482-9bf1a7d32efb-main-0"))
        println(cost)
    }
    println("-----------------------------------------------")
    history.historyArray.foreach {
      i =>
        val cost = history.getSnapshot(i).getCheckpointCost(ActorVirtualIdentity("Worker:WF3-MonthlyTweetCount-operator-8fc57d16-a5c7-4839-bea3-9fcc775bb528-main-0"))
        println(cost)
    }
//    println("-----------------------------------------------")
//    history.historyArray.foreach {
//      i =>
//        val cost = history.getSnapshot(i).getCheckpointCost(ActorVirtualIdentity("Worker:WF6-SortPartitions-operator-ed6e3d3c-699a-4f61-95f3-0f245479bd9b-main-0"))
//        println(cost)
//    }
    val planner = new ReplayCheckpointPlanner(history, 5000)
    val plan2 = planner.findBestPlan(0,history.historyArray.length,5000,true, false)
    println(plan2.values.map(history.getPlanCost).sum)
    val plan3 = planner.toPartialPlan(planner.getGlobalPlan(0, history.historyArray.length,5000))
    println(plan3.values.map(history.getPlanCost).sum)
    val plan = planner.doPrepPhase("global")
    println(plan)
  }
}

class JsonTest {}
