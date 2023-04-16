package edu.uci.ics.texera.web

import akka.actor.ActorSystem
import akka.serialization.SerializationExtension
import edu.uci.ics.amber.engine.architecture.checkpoint.SavedCheckpoint
import edu.uci.ics.amber.engine.architecture.worker.WorkerInternalQueue.DPMessage
import edu.uci.ics.amber.engine.common.AmberUtils.akkaConfig
import edu.uci.ics.amber.engine.common.ambermessage.{ChannelEndpointID, EndOfUpstream, WorkflowFIFOMessage}
import edu.uci.ics.amber.engine.common.lbmq.{LinkedBlockingMultiQueue, LinkedBlockingSubQueue}
import edu.uci.ics.amber.engine.common.virtualidentity.util.{CLIENT, CONTROLLER, SELF}
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock
import scala.collection.mutable

object JsonTest {

  def main(args: Array[String]): Unit = {

    val system = ActorSystem("Amber", akkaConfig)

    val q = new LinkedBlockingMultiQueue[ChannelEndpointID, DPMessage]()

    q.addSubQueue(ChannelEndpointID(SELF, true),0)
    q.addSubQueue(ChannelEndpointID(CLIENT, true), 0)
    q.addSubQueue(ChannelEndpointID(CONTROLLER, false), 1)

    val q1 = q.getSubQueue(ChannelEndpointID(SELF, true))
    val q2 = q.getSubQueue(ChannelEndpointID(CLIENT, true))
    val q3 = q.getSubQueue(ChannelEndpointID(CONTROLLER, false))

    val m = new mutable.HashMap[ChannelEndpointID, LinkedBlockingSubQueue[ChannelEndpointID, DPMessage]]()
    m(ChannelEndpointID(SELF, true)) = q1
    m(ChannelEndpointID(CLIENT, true)) = q2
    m(ChannelEndpointID(CONTROLLER, false)) = q3

    q1.put(DPMessage(ChannelEndpointID(CLIENT, true), EndOfUpstream()))
    q2.put(DPMessage(ChannelEndpointID(CLIENT, true), EndOfUpstream()))
    q3.put(DPMessage(ChannelEndpointID(CLIENT, true), EndOfUpstream()))
    q1.put(DPMessage(ChannelEndpointID(CLIENT, true), EndOfUpstream()))

    q.take()

    val l = new ReentrantLock()
    val qq = new LinkedBlockingSubQueue[ChannelEndpointID, DPMessage](ChannelEndpointID(CLIENT, true), 1000, new AtomicInteger(), l, l.newCondition())
    val chkpt = new SavedCheckpoint()
    chkpt.attachSerialization(SerializationExtension(system))
    chkpt.save("map", m)
    chkpt.save("q",q)
    chkpt.save("qq",qq)
    val qqc = chkpt.load("qq").asInstanceOf[LinkedBlockingSubQueue[ChannelEndpointID, DPMessage]]
    val qc = chkpt.load("q").asInstanceOf[LinkedBlockingMultiQueue[ChannelEndpointID, DPMessage]]
    val mc = chkpt.load("map").asInstanceOf[mutable.HashMap[ChannelEndpointID, LinkedBlockingSubQueue[ChannelEndpointID, DPMessage]]]
    println(mc)
//    val file = Paths.get("").resolve("latest-interation-history")
//    if(!Files.exists(file)){
//      println("no interaction history found!")
//      return
//    }
//    val ois = new ObjectInputStream(new FileInputStream(file.toFile))
//    val history = ois.readObject.asInstanceOf[ProcessingHistory]
//    ois.close()
//
//    val planner = new ReplayPlanner(history)
//
//    val mem = mutable.HashMap[Int, (Iterable[Map[ActorVirtualIdentity, Int]], Long)]()
//    history.inputConstant = 1
//    val result = planner.getReplayPlan(history.getInteractionIdxes.length, 7000, mem)
//    println(result)
  }
}

class JsonTest {}
