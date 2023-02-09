// Generated by the Scala Plugin for the Protocol Buffer Compiler.
// Do not edit!
//
// Protofile syntax: PROTO3

package edu.uci.ics.amber.engine.common.ambermessage

@SerialVersionUID(0L)
final case class EpochMarkerV2(
    id: _root_.scala.Long,
    destination: edu.uci.ics.amber.engine.common.virtualidentity.LayerIdentity,
    command: edu.uci.ics.amber.engine.common.ambermessage.ControlInvocationV2
    ) extends scalapb.GeneratedMessage with scalapb.lenses.Updatable[EpochMarkerV2] {
    @transient
    private[this] var __serializedSizeCachedValue: _root_.scala.Int = 0
    private[this] def __computeSerializedValue(): _root_.scala.Int = {
      var __size = 0
      
      {
        val __value = id
        if (__value != 0L) {
          __size += _root_.com.google.protobuf.CodedOutputStream.computeInt64Size(1, __value)
        }
      };
      
      {
        val __value = destination
        if (__value != edu.uci.ics.amber.engine.common.virtualidentity.LayerIdentity.defaultInstance) {
          __size += 1 + _root_.com.google.protobuf.CodedOutputStream.computeUInt32SizeNoTag(__value.serializedSize) + __value.serializedSize
        }
      };
      
      {
        val __value = command
        if (__value != edu.uci.ics.amber.engine.common.ambermessage.ControlInvocationV2.defaultInstance) {
          __size += 1 + _root_.com.google.protobuf.CodedOutputStream.computeUInt32SizeNoTag(__value.serializedSize) + __value.serializedSize
        }
      };
      __size
    }
    override def serializedSize: _root_.scala.Int = {
      var read = __serializedSizeCachedValue
      if (read == 0) {
        read = __computeSerializedValue()
        __serializedSizeCachedValue = read
      }
      read
    }
    def writeTo(`_output__`: _root_.com.google.protobuf.CodedOutputStream): _root_.scala.Unit = {
      {
        val __v = id
        if (__v != 0L) {
          _output__.writeInt64(1, __v)
        }
      };
      {
        val __v = destination
        if (__v != edu.uci.ics.amber.engine.common.virtualidentity.LayerIdentity.defaultInstance) {
          _output__.writeTag(2, 2)
          _output__.writeUInt32NoTag(__v.serializedSize)
          __v.writeTo(_output__)
        }
      };
      {
        val __v = command
        if (__v != edu.uci.ics.amber.engine.common.ambermessage.ControlInvocationV2.defaultInstance) {
          _output__.writeTag(3, 2)
          _output__.writeUInt32NoTag(__v.serializedSize)
          __v.writeTo(_output__)
        }
      };
    }
    def withId(__v: _root_.scala.Long): EpochMarkerV2 = copy(id = __v)
    def withDestination(__v: edu.uci.ics.amber.engine.common.virtualidentity.LayerIdentity): EpochMarkerV2 = copy(destination = __v)
    def withCommand(__v: edu.uci.ics.amber.engine.common.ambermessage.ControlInvocationV2): EpochMarkerV2 = copy(command = __v)
    def getFieldByNumber(__fieldNumber: _root_.scala.Int): _root_.scala.Any = {
      (__fieldNumber: @_root_.scala.unchecked) match {
        case 1 => {
          val __t = id
          if (__t != 0L) __t else null
        }
        case 2 => {
          val __t = destination
          if (__t != edu.uci.ics.amber.engine.common.virtualidentity.LayerIdentity.defaultInstance) __t else null
        }
        case 3 => {
          val __t = command
          if (__t != edu.uci.ics.amber.engine.common.ambermessage.ControlInvocationV2.defaultInstance) __t else null
        }
      }
    }
    def getField(__field: _root_.scalapb.descriptors.FieldDescriptor): _root_.scalapb.descriptors.PValue = {
      _root_.scala.Predef.require(__field.containingMessage eq companion.scalaDescriptor)
      (__field.number: @_root_.scala.unchecked) match {
        case 1 => _root_.scalapb.descriptors.PLong(id)
        case 2 => destination.toPMessage
        case 3 => command.toPMessage
      }
    }
    def toProtoString: _root_.scala.Predef.String = _root_.scalapb.TextFormat.printToSingleLineUnicodeString(this)
    def companion = edu.uci.ics.amber.engine.common.ambermessage.EpochMarkerV2
    // @@protoc_insertion_point(GeneratedMessage[edu.uci.ics.amber.engine.common.EpochMarkerV2])
}

object EpochMarkerV2 extends scalapb.GeneratedMessageCompanion[edu.uci.ics.amber.engine.common.ambermessage.EpochMarkerV2] {
  implicit def messageCompanion: scalapb.GeneratedMessageCompanion[edu.uci.ics.amber.engine.common.ambermessage.EpochMarkerV2] = this
  def parseFrom(`_input__`: _root_.com.google.protobuf.CodedInputStream): edu.uci.ics.amber.engine.common.ambermessage.EpochMarkerV2 = {
    var __id: _root_.scala.Long = 0L
    var __destination: _root_.scala.Option[edu.uci.ics.amber.engine.common.virtualidentity.LayerIdentity] = _root_.scala.None
    var __command: _root_.scala.Option[edu.uci.ics.amber.engine.common.ambermessage.ControlInvocationV2] = _root_.scala.None
    var _done__ = false
    while (!_done__) {
      val _tag__ = _input__.readTag()
      _tag__ match {
        case 0 => _done__ = true
        case 8 =>
          __id = _input__.readInt64()
        case 18 =>
          __destination = _root_.scala.Some(__destination.fold(_root_.scalapb.LiteParser.readMessage[edu.uci.ics.amber.engine.common.virtualidentity.LayerIdentity](_input__))(_root_.scalapb.LiteParser.readMessage(_input__, _)))
        case 26 =>
          __command = _root_.scala.Some(__command.fold(_root_.scalapb.LiteParser.readMessage[edu.uci.ics.amber.engine.common.ambermessage.ControlInvocationV2](_input__))(_root_.scalapb.LiteParser.readMessage(_input__, _)))
        case tag => _input__.skipField(tag)
      }
    }
    edu.uci.ics.amber.engine.common.ambermessage.EpochMarkerV2(
        id = __id,
        destination = __destination.getOrElse(edu.uci.ics.amber.engine.common.virtualidentity.LayerIdentity.defaultInstance),
        command = __command.getOrElse(edu.uci.ics.amber.engine.common.ambermessage.ControlInvocationV2.defaultInstance)
    )
  }
  implicit def messageReads: _root_.scalapb.descriptors.Reads[edu.uci.ics.amber.engine.common.ambermessage.EpochMarkerV2] = _root_.scalapb.descriptors.Reads{
    case _root_.scalapb.descriptors.PMessage(__fieldsMap) =>
      _root_.scala.Predef.require(__fieldsMap.keys.forall(_.containingMessage eq scalaDescriptor), "FieldDescriptor does not match message type.")
      edu.uci.ics.amber.engine.common.ambermessage.EpochMarkerV2(
        id = __fieldsMap.get(scalaDescriptor.findFieldByNumber(1).get).map(_.as[_root_.scala.Long]).getOrElse(0L),
        destination = __fieldsMap.get(scalaDescriptor.findFieldByNumber(2).get).map(_.as[edu.uci.ics.amber.engine.common.virtualidentity.LayerIdentity]).getOrElse(edu.uci.ics.amber.engine.common.virtualidentity.LayerIdentity.defaultInstance),
        command = __fieldsMap.get(scalaDescriptor.findFieldByNumber(3).get).map(_.as[edu.uci.ics.amber.engine.common.ambermessage.ControlInvocationV2]).getOrElse(edu.uci.ics.amber.engine.common.ambermessage.ControlInvocationV2.defaultInstance)
      )
    case _ => throw new RuntimeException("Expected PMessage")
  }
  def javaDescriptor: _root_.com.google.protobuf.Descriptors.Descriptor = AmbermessageProto.javaDescriptor.getMessageTypes().get(3)
  def scalaDescriptor: _root_.scalapb.descriptors.Descriptor = AmbermessageProto.scalaDescriptor.messages(3)
  def messageCompanionForFieldNumber(__number: _root_.scala.Int): _root_.scalapb.GeneratedMessageCompanion[_] = {
    var __out: _root_.scalapb.GeneratedMessageCompanion[_] = null
    (__number: @_root_.scala.unchecked) match {
      case 2 => __out = edu.uci.ics.amber.engine.common.virtualidentity.LayerIdentity
      case 3 => __out = edu.uci.ics.amber.engine.common.ambermessage.ControlInvocationV2
    }
    __out
  }
  lazy val nestedMessagesCompanions: Seq[_root_.scalapb.GeneratedMessageCompanion[_ <: _root_.scalapb.GeneratedMessage]] = Seq.empty
  def enumCompanionForFieldNumber(__fieldNumber: _root_.scala.Int): _root_.scalapb.GeneratedEnumCompanion[_] = throw new MatchError(__fieldNumber)
  lazy val defaultInstance = edu.uci.ics.amber.engine.common.ambermessage.EpochMarkerV2(
    id = 0L,
    destination = edu.uci.ics.amber.engine.common.virtualidentity.LayerIdentity.defaultInstance,
    command = edu.uci.ics.amber.engine.common.ambermessage.ControlInvocationV2.defaultInstance
  )
  implicit class EpochMarkerV2Lens[UpperPB](_l: _root_.scalapb.lenses.Lens[UpperPB, edu.uci.ics.amber.engine.common.ambermessage.EpochMarkerV2]) extends _root_.scalapb.lenses.ObjectLens[UpperPB, edu.uci.ics.amber.engine.common.ambermessage.EpochMarkerV2](_l) {
    def id: _root_.scalapb.lenses.Lens[UpperPB, _root_.scala.Long] = field(_.id)((c_, f_) => c_.copy(id = f_))
    def destination: _root_.scalapb.lenses.Lens[UpperPB, edu.uci.ics.amber.engine.common.virtualidentity.LayerIdentity] = field(_.destination)((c_, f_) => c_.copy(destination = f_))
    def command: _root_.scalapb.lenses.Lens[UpperPB, edu.uci.ics.amber.engine.common.ambermessage.ControlInvocationV2] = field(_.command)((c_, f_) => c_.copy(command = f_))
  }
  final val ID_FIELD_NUMBER = 1
  final val DESTINATION_FIELD_NUMBER = 2
  final val COMMAND_FIELD_NUMBER = 3
  def of(
    id: _root_.scala.Long,
    destination: edu.uci.ics.amber.engine.common.virtualidentity.LayerIdentity,
    command: edu.uci.ics.amber.engine.common.ambermessage.ControlInvocationV2
  ): _root_.edu.uci.ics.amber.engine.common.ambermessage.EpochMarkerV2 = _root_.edu.uci.ics.amber.engine.common.ambermessage.EpochMarkerV2(
    id,
    destination,
    command
  )
  // @@protoc_insertion_point(GeneratedMessageCompanion[edu.uci.ics.amber.engine.common.EpochMarkerV2])
}
