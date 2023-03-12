package edu.uci.ics.amber.engine.architecture.checkpoint

import akka.serialization.Serialization

import scala.collection.mutable

class SavedCheckpoint {

  private val states = new mutable.HashMap[String, SerializedState]()

  var prevCheckpoint: Option[Long] = None
  var pointerToCompletion: Option[Long] = None

  @transient
  private var serde: Serialization = _

  def attachSerialization(serialization: Serialization): Unit = {
    serde = serialization
  }

  def save[T <: Any](key: String, state: T): Unit = {
    states(key) = SerializedState.fromObject(state.asInstanceOf[AnyRef], serde)
  }

  def has(key: String): Boolean = {
    states.contains(key)
  }

  def load[T <: Any](key: String): T = {
    if (states.contains(key)) {
      states(key).toObject(serde).asInstanceOf[T]
    } else {
      throw new RuntimeException(s"no state saved for key = $key")
    }
  }

  def size(): Long = {
    states.filter(_._2 != null).map(_._2.size()).sum
  }

}
