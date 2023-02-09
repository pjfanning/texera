package edu.uci.ics.amber.engine.architecture.checkpoint

import scala.collection.mutable

class SavedCheckpoint {

  private val states = new mutable.HashMap[String, SerializedState]()

  def save(key: String, state: SerializedState): Unit = {
    states(key) = state
  }

  def load(key: String): SerializedState = {
    states(key)
  }

  def size(): Long = {
    states.filter(_._2 != null).map(_._2.size()).sum
  }

}
