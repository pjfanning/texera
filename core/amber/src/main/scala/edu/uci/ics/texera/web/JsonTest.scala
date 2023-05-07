package edu.uci.ics.texera.web

import akka.actor.ActorSystem
import akka.serialization.SerializationExtension
import edu.uci.ics.amber.engine.architecture.checkpoint.{CheckpointHolder, SavedCheckpoint}
import edu.uci.ics.amber.engine.architecture.common.ProcessingHistory
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

    val planner = new ReplayCheckpointPlanner(history, 5000)
//    val a = planner.pickInRange(50,55)
//    val aplus = a._1 + (ActorVirtualIdentity("Worker:WF1-SimpleSink-operator-058c6027-5134-4a0d-b345-77227115ee76-main-0") -> 55)
//    val b = history.getPlanCost(aplus)
    val plan = planner.generateReplayPlan(history.getInteractionTimes.last)
    println(plan)
    val plan2 = planner.generateReplayPlan(history.getInteractionTimes.last)
    println(plan2)
  }
}

class JsonTest {}
