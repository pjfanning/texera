package edu.uci.ics.amber.engine.architecture.checkpoint

import akka.serialization.Serialization
import edu.uci.ics.amber.engine.common.ambermessage.WorkflowFIFOMessagePayload
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class SavedCheckpoint {

  private val states = new mutable.HashMap[String, SerializedState]()
  private val recordedInputData = new mutable.HashMap[(ActorVirtualIdentity, Boolean), mutable.ArrayBuffer[WorkflowFIFOMessagePayload]]
  @transient
  private var serde: Serialization = _

  def addInputData(channel: (ActorVirtualIdentity, Boolean), data:WorkflowFIFOMessagePayload): Unit ={
    recordedInputData.getOrElseUpdate(channel, new ArrayBuffer[WorkflowFIFOMessagePayload]()).append(data)
  }

  def getInputData: mutable.Map[(ActorVirtualIdentity, Boolean), ArrayBuffer[WorkflowFIFOMessagePayload]] = recordedInputData

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
