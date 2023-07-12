package edu.uci.ics.amber.engine.architecture.checkpoint

import akka.serialization.Serialization
import edu.uci.ics.amber.engine.common.ambermessage.{ChannelEndpointID, WorkflowFIFOMessagePayload}

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class SavedCheckpoint {

  private val states = new mutable.HashMap[String, SerializedState]()
  private val recordedInputData =
    new mutable.HashMap[ChannelEndpointID, mutable.ArrayBuffer[WorkflowFIFOMessagePayload]]
  private val internalData =
    new mutable.HashMap[ChannelEndpointID, mutable.ArrayBuffer[WorkflowFIFOMessagePayload]]

  @transient
  private var serde: Serialization = _

  def addInputData(channel: ChannelEndpointID, data: WorkflowFIFOMessagePayload): Unit = {
    recordedInputData
      .getOrElseUpdate(channel, new ArrayBuffer[WorkflowFIFOMessagePayload]())
      .append(data)
  }

  def addInternalData(channel: ChannelEndpointID, data: WorkflowFIFOMessagePayload): Unit = {
    internalData
      .getOrElseUpdate(channel, new ArrayBuffer[WorkflowFIFOMessagePayload]())
      .append(data)
  }

  def getInputData: mutable.Map[ChannelEndpointID, ArrayBuffer[WorkflowFIFOMessagePayload]] =
    recordedInputData

  def getInternalData: mutable.Map[ChannelEndpointID, ArrayBuffer[WorkflowFIFOMessagePayload]] =
    internalData

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
