package edu.uci.ics.amber.engine.architecture.controller

import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity

import scala.collection.mutable

class GlobalReplayManager(onRecoveryStart: () => Unit, onRecoveryComplete: () => Unit) {
  private val recovering = mutable.HashSet[ActorVirtualIdentity]()

  def isRecovering: Boolean = recovering.nonEmpty

  def markRecoveryStatus(vid: ActorVirtualIdentity, isRecovering: Boolean): Unit = {
    val globalRecovering = recovering.nonEmpty
    if (isRecovering) {
      println(s"adding $vid")
      recovering.add(vid)
    } else {
      println(s"removing $vid")
      recovering.remove(vid)
    }
    if (!globalRecovering && recovering.nonEmpty) {
      onRecoveryStart()
    }
    if (globalRecovering && recovering.isEmpty) {
      onRecoveryComplete()
    }
  }
}
