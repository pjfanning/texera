// Generated by the Scala Plugin for the Protocol Buffer Compiler.
// Do not edit!
//
// Protofile syntax: PROTO3

package edu.uci.ics.amber.engine.architecture.worker.controlcommands

sealed abstract class ConsoleMessageType(val value: _root_.scala.Int) extends _root_.scalapb.GeneratedEnum {
  type EnumType = ConsoleMessageType
  def isPrint: _root_.scala.Boolean = false
  def isError: _root_.scala.Boolean = false
  def isCommand: _root_.scala.Boolean = false
  def isDebugger: _root_.scala.Boolean = false
  def companion: _root_.scalapb.GeneratedEnumCompanion[ConsoleMessageType] = edu.uci.ics.amber.engine.architecture.worker.controlcommands.ConsoleMessageType
  final def asRecognized: _root_.scala.Option[edu.uci.ics.amber.engine.architecture.worker.controlcommands.ConsoleMessageType.Recognized] = if (isUnrecognized) _root_.scala.None else _root_.scala.Some(this.asInstanceOf[edu.uci.ics.amber.engine.architecture.worker.controlcommands.ConsoleMessageType.Recognized])
}

object ConsoleMessageType extends _root_.scalapb.GeneratedEnumCompanion[ConsoleMessageType] {
  sealed trait Recognized extends ConsoleMessageType
  implicit def enumCompanion: _root_.scalapb.GeneratedEnumCompanion[ConsoleMessageType] = this
  @SerialVersionUID(0L)
  case object PRINT extends ConsoleMessageType(0) with ConsoleMessageType.Recognized {
    val index = 0
    val name = "PRINT"
    override def isPrint: _root_.scala.Boolean = true
  }
  
  @SerialVersionUID(0L)
  case object ERROR extends ConsoleMessageType(1) with ConsoleMessageType.Recognized {
    val index = 1
    val name = "ERROR"
    override def isError: _root_.scala.Boolean = true
  }
  
  @SerialVersionUID(0L)
  case object COMMAND extends ConsoleMessageType(2) with ConsoleMessageType.Recognized {
    val index = 2
    val name = "COMMAND"
    override def isCommand: _root_.scala.Boolean = true
  }
  
  @SerialVersionUID(0L)
  case object DEBUGGER extends ConsoleMessageType(3) with ConsoleMessageType.Recognized {
    val index = 3
    val name = "DEBUGGER"
    override def isDebugger: _root_.scala.Boolean = true
  }
  
  @SerialVersionUID(0L)
  final case class Unrecognized(unrecognizedValue: _root_.scala.Int) extends ConsoleMessageType(unrecognizedValue) with _root_.scalapb.UnrecognizedEnum
  
  lazy val values = scala.collection.immutable.Seq(PRINT, ERROR, COMMAND, DEBUGGER)
  def fromValue(__value: _root_.scala.Int): ConsoleMessageType = __value match {
    case 0 => PRINT
    case 1 => ERROR
    case 2 => COMMAND
    case 3 => DEBUGGER
    case __other => Unrecognized(__other)
  }
  def javaDescriptor: _root_.com.google.protobuf.Descriptors.EnumDescriptor = ControlcommandsProto.javaDescriptor.getEnumTypes().get(0)
  def scalaDescriptor: _root_.scalapb.descriptors.EnumDescriptor = ControlcommandsProto.scalaDescriptor.enums(0)
}