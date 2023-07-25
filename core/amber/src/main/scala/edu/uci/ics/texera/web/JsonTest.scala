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

    val actorSystem = AmberUtils.startActorMaster(false)
    val serde = SerializationExtension(actorSystem)
    val promise = Promise[Int]()
    promise.map{
      x =>
        println("123")
    }
    val state = SerializedState.fromObject(promise, serde)
    val promise2:Promise[Int] = state.toObject(serde)
    try{
      promise2.setValue(1)
    }catch{
      case x: Throwable =>
        println(x)
    }

    val file = Paths.get("").resolve("latest-interation-history")
    if(!Files.exists(file)){
      println("no interaction history found!")
      return
    }
    val ois = new ObjectInputStream(new FileInputStream(file.toFile))
    val history = ois.readObject.asInstanceOf[ProcessingHistory]
    ois.close()

    val planner = new ReplayCheckpointPlanner(history, 5000)
    val plan = planner.doPrepPhase()
    println(plan)
  }
}

class JsonTest {}
