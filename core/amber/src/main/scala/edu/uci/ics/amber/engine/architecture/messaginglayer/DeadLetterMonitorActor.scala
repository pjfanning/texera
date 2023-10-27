package edu.uci.ics.amber.engine.architecture.messaginglayer

import akka.actor.{Actor, DeadLetter}
import edu.uci.ics.amber.engine.architecture.common.WorkflowActor.MessageBecomesDeadLetter
import edu.uci.ics.amber.engine.common.ambermessage.WorkflowFIFOMessage

class DeadLetterMonitorActor extends Actor {
  override def receive: Receive = {
    case d: DeadLetter =>
      d.message match {
        case msg: WorkflowFIFOMessage =>
          // d.sender is the NetworkSenderActor
          d.sender ! MessageBecomesDeadLetter(msg)
        case other =>
        // skip for now
      }
    case _ =>
  }
}
