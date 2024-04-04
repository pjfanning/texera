package edu.uci.ics.amber.engine.common

import akka.serialization.{Serialization, Serializers}

object SerializedState {

  val CP_STATE_KEY = "Amber_CPState"
  val DP_STATE_KEY = "Amber_DPState"
  val IN_FLIGHT_MSG_KEY = "Amber_Inflight_Messages"
  val DP_QUEUED_MSG_KEY = "Amber_DP_Queued_Messages"
  val OUTPUT_MSG_KEY = "Amber_Output_Messages"

  def fromObject[T <: AnyRef](obj: T, serialization: Serialization): SerializedState = {
    val bytes = serialization.serialize(obj).get
    val ser = serialization.findSerializerFor(obj)
    val manifest = Serializers.manifestFor(ser, obj)
    SerializedState(bytes, ser.identifier, manifest)
  }

  def fromObjectToString[T <: AnyRef](obj:T):String = {
    val serializedObj = fromObject(obj, AmberUtils.serde)
    s"${serializedObj.manifest}amber_serialization${serializedObj.serializerId}amber_serialization" + new String(serializedObj.bytes, "UTF-8")
  }

  def stringToObject(str:String):AnyRef = {
    val fields = str.split("amber_serialization")
    SerializedState(fields(2).getBytes("UTF-8"),fields(1).toInt,fields(0)).toObject[AnyRef](AmberUtils.serde)
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
