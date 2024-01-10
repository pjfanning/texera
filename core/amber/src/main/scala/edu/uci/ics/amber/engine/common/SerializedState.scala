package edu.uci.ics.amber.engine.common

import akka.serialization.{Serialization, Serializers}

object SerializedState {

  val DP_STATE_KEY = "Amber_DPState"
  val IN_FLIGHT_MSG_KEY = "Amber_InflightMessages"

  def fromObject[T <: AnyRef](obj: T, serialization: Serialization): SerializedState = {
    val bytes = serialization.serialize(obj).get
    val ser = serialization.findSerializerFor(obj)
    val manifest = Serializers.manifestFor(ser, obj)
    SerializedState(bytes, ser.identifier, manifest)
  }
}

case class SerializedState(bytes: Array[Byte], serializerId: Int, manifest: String) {

  def toObject[T <: AnyRef](serialization: Serialization): T = {
    serialization.deserialize(bytes, serializerId, manifest).get.asInstanceOf[T]
  }

  def size(): Long = {
    bytes.length
  }
}

