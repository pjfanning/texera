package edu.uci.ics.texera.web

import edu.uci.ics.texera.web.model.event.{
  FrameSynchronization,
  StateSynchronization,
  TexeraWebSocketEvent
}

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

object WebsocketLogStorage {
  class SocketMessageLog {
    private var seqNumber = 0
    private val statelessEvents = new mutable.HashMap[String, (Int, TexeraWebSocketEvent)]
    private val statefulEvents =
      new mutable.HashMap[String, mutable.ArrayBuffer[(Int, TexeraWebSocketEvent)]]
    def logEvent(evt: TexeraWebSocketEvent): Unit = {
      evt match {
        case _: FrameSynchronization =>
          val name = evt.getClass.getSimpleName
          if (!statefulEvents.contains(name)) {
            statefulEvents(name) = ArrayBuffer((seqNumber, evt))
          } else {
            statefulEvents(name).append((seqNumber, evt))
          }
          seqNumber += 1
        case _: StateSynchronization =>
          val name = evt.getClass.getSimpleName
          statelessEvents(name) = (seqNumber, evt)
          seqNumber += 1
        case _ =>
        //skip
      }
    }

    def retrieveEvents(): Seq[TexeraWebSocketEvent] = {
      (statelessEvents.values ++ statefulEvents.values.flatten).toList.sortBy(_._1).map(_._2)
    }
  }

  private val idToLogs = new mutable.HashMap[String, SocketMessageLog]

  def logMarkedEvent(id: String, evt: TexeraWebSocketEvent): Unit = {
    if (!idToLogs.contains(id)) {
      idToLogs(id) = new SocketMessageLog()
    }
    idToLogs(id).logEvent(evt)
  }

  def getLoggedEvents(id: String): Seq[TexeraWebSocketEvent] = {
    if (!idToLogs.contains(id)) {
      Seq.empty
    } else {
      idToLogs(id).retrieveEvents()
    }
  }

  def clearLoggedEvents(id: String): Unit = {
    idToLogs.remove(id)
  }

}
