// Generated by the Scala Plugin for the Protocol Buffer Compiler.
// Do not edit!
//
// Protofile syntax: PROTO3

package edu.uci.ics.amber.engine.architecture.controller.controllerrpc

@SerialVersionUID(0L)
final case class WorkerExecutionCompletedEvent(
    ) extends scalapb.GeneratedMessage with scalapb.lenses.Updatable[WorkerExecutionCompletedEvent] {
    final override def serializedSize: _root_.scala.Int = 0
    def writeTo(`_output__`: _root_.com.google.protobuf.CodedOutputStream): _root_.scala.Unit = {
    }
    def getFieldByNumber(__fieldNumber: _root_.scala.Int): _root_.scala.Any = throw new MatchError(__fieldNumber)
    def getField(__field: _root_.scalapb.descriptors.FieldDescriptor): _root_.scalapb.descriptors.PValue = throw new MatchError(__field)
    def toProtoString: _root_.scala.Predef.String = _root_.scalapb.TextFormat.printToSingleLineUnicodeString(this)
    def companion = edu.uci.ics.amber.engine.architecture.controller.controllerrpc.WorkerExecutionCompletedEvent
    // @@protoc_insertion_point(GeneratedMessage[edu.uci.ics.amber.engine.architecture.controller.WorkerExecutionCompletedEvent])
}

object WorkerExecutionCompletedEvent extends scalapb.GeneratedMessageCompanion[edu.uci.ics.amber.engine.architecture.controller.controllerrpc.WorkerExecutionCompletedEvent] {
  implicit def messageCompanion: scalapb.GeneratedMessageCompanion[edu.uci.ics.amber.engine.architecture.controller.controllerrpc.WorkerExecutionCompletedEvent] = this
  def parseFrom(`_input__`: _root_.com.google.protobuf.CodedInputStream): edu.uci.ics.amber.engine.architecture.controller.controllerrpc.WorkerExecutionCompletedEvent = {
    var _done__ = false
    while (!_done__) {
      val _tag__ = _input__.readTag()
      _tag__ match {
        case 0 => _done__ = true
        case tag => _input__.skipField(tag)
      }
    }
    edu.uci.ics.amber.engine.architecture.controller.controllerrpc.WorkerExecutionCompletedEvent(
    )
  }
  implicit def messageReads: _root_.scalapb.descriptors.Reads[edu.uci.ics.amber.engine.architecture.controller.controllerrpc.WorkerExecutionCompletedEvent] = _root_.scalapb.descriptors.Reads{
    case _root_.scalapb.descriptors.PMessage(__fieldsMap) =>
      _root_.scala.Predef.require(__fieldsMap.keys.forall(_.containingMessage eq scalaDescriptor), "FieldDescriptor does not match message type.")
      edu.uci.ics.amber.engine.architecture.controller.controllerrpc.WorkerExecutionCompletedEvent(
      )
    case _ => throw new RuntimeException("Expected PMessage")
  }
  def javaDescriptor: _root_.com.google.protobuf.Descriptors.Descriptor = ControllerrpcProto.javaDescriptor.getMessageTypes().get(22)
  def scalaDescriptor: _root_.scalapb.descriptors.Descriptor = ControllerrpcProto.scalaDescriptor.messages(22)
  def messageCompanionForFieldNumber(__number: _root_.scala.Int): _root_.scalapb.GeneratedMessageCompanion[_] = throw new MatchError(__number)
  lazy val nestedMessagesCompanions: Seq[_root_.scalapb.GeneratedMessageCompanion[_ <: _root_.scalapb.GeneratedMessage]] = Seq.empty
  def enumCompanionForFieldNumber(__fieldNumber: _root_.scala.Int): _root_.scalapb.GeneratedEnumCompanion[_] = throw new MatchError(__fieldNumber)
  lazy val defaultInstance = edu.uci.ics.amber.engine.architecture.controller.controllerrpc.WorkerExecutionCompletedEvent(
  )
  implicit class WorkerExecutionCompletedEventLens[UpperPB](_l: _root_.scalapb.lenses.Lens[UpperPB, edu.uci.ics.amber.engine.architecture.controller.controllerrpc.WorkerExecutionCompletedEvent]) extends _root_.scalapb.lenses.ObjectLens[UpperPB, edu.uci.ics.amber.engine.architecture.controller.controllerrpc.WorkerExecutionCompletedEvent](_l) {
  }
  def of(
  ): _root_.edu.uci.ics.amber.engine.architecture.controller.controllerrpc.WorkerExecutionCompletedEvent = _root_.edu.uci.ics.amber.engine.architecture.controller.controllerrpc.WorkerExecutionCompletedEvent(
  )
  // @@protoc_insertion_point(GeneratedMessageCompanion[edu.uci.ics.amber.engine.architecture.controller.WorkerExecutionCompletedEvent])
}
