package edu.uci.ics.amber.engine.common

import akka.serialization.{Serialization, SerializationExtension}

import scala.collection.mutable


class CheckpointState {

  private val states = new mutable.HashMap[String, SerializedState]()

  // it will be recomputed after deserialization
  @transient private lazy val serde: Serialization = AmberUtils.serde

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