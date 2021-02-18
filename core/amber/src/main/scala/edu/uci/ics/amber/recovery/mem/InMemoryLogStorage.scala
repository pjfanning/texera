package edu.uci.ics.amber.recovery.mem

import edu.uci.ics.amber.recovery.MainLogStorage.MainLogElement

import scala.collection.mutable

object InMemoryLogStorage {

  private lazy val mainLogs = new mutable.HashMap[String, mutable.Queue[MainLogElement]]()

  private lazy val secondaryLogs = new mutable.HashMap[String,mutable.Queue[Long]]()

  def getMainLogOf(k:String): mutable.Queue[MainLogElement] ={
    if(mainLogs.contains(k)){
      mainLogs(k) = new mutable.Queue[MainLogElement]()
    }
    mainLogs(k)
  }

  def getSecondaryLogOf(k:String):mutable.Queue[Long] = {
    if(secondaryLogs.contains(k)){
      secondaryLogs(k) = new mutable.Queue[Long]()
    }
    secondaryLogs(k)
  }

}
