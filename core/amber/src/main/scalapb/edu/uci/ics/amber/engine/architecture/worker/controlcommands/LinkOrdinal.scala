// Generated by the Scala Plugin for the Protocol Buffer Compiler.
// Do not edit!
//
// Protofile syntax: PROTO3

package edu.uci.ics.amber.engine.architecture.worker.controlcommands

@SerialVersionUID(0L)
final case class LinkOrdinal(
    linkId: edu.uci.ics.amber.engine.common.virtualidentity.PhysicalLink,
    portOrdinal: _root_.scala.Long
    ) extends scalapb.GeneratedMessage with scalapb.lenses.Updatable[LinkOrdinal] {
    @transient
    private[this] var __serializedSizeCachedValue: _root_.scala.Int = 0
    private[this] def __computeSerializedValue(): _root_.scala.Int = {
      var __size = 0
      
      {
        val __value = linkId
        if (__value != edu.uci.ics.amber.engine.common.virtualidentity.PhysicalLink.defaultInstance) {
          __size += 1 + _root_.com.google.protobuf.CodedOutputStream.computeUInt32SizeNoTag(__value.serializedSize) + __value.serializedSize
        }
      };
      
      {
        val __value = portOrdinal
        if (__value != 0L) {
          __size += _root_.com.google.protobuf.CodedOutputStream.computeInt64Size(2, __value)
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
        val __v = linkId
        if (__v != edu.uci.ics.amber.engine.common.virtualidentity.PhysicalLink.defaultInstance) {
          _output__.writeTag(1, 2)
          _output__.writeUInt32NoTag(__v.serializedSize)
          __v.writeTo(_output__)
        }
      };
      {
        val __v = portOrdinal
        if (__v != 0L) {
          _output__.writeInt64(2, __v)
        }
      };
    }
    def withLinkId(__v: edu.uci.ics.amber.engine.common.virtualidentity.PhysicalLink): LinkOrdinal = copy(linkId = __v)
    def withPortOrdinal(__v: _root_.scala.Long): LinkOrdinal = copy(portOrdinal = __v)
    def getFieldByNumber(__fieldNumber: _root_.scala.Int): _root_.scala.Any = {
      (__fieldNumber: @_root_.scala.unchecked) match {
        case 1 => {
          val __t = linkId
          if (__t != edu.uci.ics.amber.engine.common.virtualidentity.PhysicalLink.defaultInstance) __t else null
        }
        case 2 => {
          val __t = portOrdinal
          if (__t != 0L) __t else null
        }
      }
    }
    def getField(__field: _root_.scalapb.descriptors.FieldDescriptor): _root_.scalapb.descriptors.PValue = {
      _root_.scala.Predef.require(__field.containingMessage eq companion.scalaDescriptor)
      (__field.number: @_root_.scala.unchecked) match {
        case 1 => linkId.toPMessage
        case 2 => _root_.scalapb.descriptors.PLong(portOrdinal)
      }
    }
    def toProtoString: _root_.scala.Predef.String = _root_.scalapb.TextFormat.printToSingleLineUnicodeString(this)
    def companion = edu.uci.ics.amber.engine.architecture.worker.controlcommands.LinkOrdinal
    // @@protoc_insertion_point(GeneratedMessage[edu.uci.ics.amber.engine.architecture.worker.LinkOrdinal])
}

object LinkOrdinal extends scalapb.GeneratedMessageCompanion[edu.uci.ics.amber.engine.architecture.worker.controlcommands.LinkOrdinal] {
  implicit def messageCompanion: scalapb.GeneratedMessageCompanion[edu.uci.ics.amber.engine.architecture.worker.controlcommands.LinkOrdinal] = this
  def parseFrom(`_input__`: _root_.com.google.protobuf.CodedInputStream): edu.uci.ics.amber.engine.architecture.worker.controlcommands.LinkOrdinal = {
    var __linkId: _root_.scala.Option[edu.uci.ics.amber.engine.common.virtualidentity.PhysicalLink] = _root_.scala.None
    var __portOrdinal: _root_.scala.Long = 0L
    var _done__ = false
    while (!_done__) {
      val _tag__ = _input__.readTag()
      _tag__ match {
        case 0 => _done__ = true
        case 10 =>
          __linkId = _root_.scala.Some(__linkId.fold(_root_.scalapb.LiteParser.readMessage[edu.uci.ics.amber.engine.common.virtualidentity.PhysicalLink](_input__))(_root_.scalapb.LiteParser.readMessage(_input__, _)))
        case 16 =>
          __portOrdinal = _input__.readInt64()
        case tag => _input__.skipField(tag)
      }
    }
    edu.uci.ics.amber.engine.architecture.worker.controlcommands.LinkOrdinal(
        linkId = __linkId.getOrElse(edu.uci.ics.amber.engine.common.virtualidentity.PhysicalLink.defaultInstance),
        portOrdinal = __portOrdinal
    )
  }
  implicit def messageReads: _root_.scalapb.descriptors.Reads[edu.uci.ics.amber.engine.architecture.worker.controlcommands.LinkOrdinal] = _root_.scalapb.descriptors.Reads{
    case _root_.scalapb.descriptors.PMessage(__fieldsMap) =>
      _root_.scala.Predef.require(__fieldsMap.keys.forall(_.containingMessage eq scalaDescriptor), "FieldDescriptor does not match message type.")
      edu.uci.ics.amber.engine.architecture.worker.controlcommands.LinkOrdinal(
        linkId = __fieldsMap.get(scalaDescriptor.findFieldByNumber(1).get).map(_.as[edu.uci.ics.amber.engine.common.virtualidentity.PhysicalLink]).getOrElse(edu.uci.ics.amber.engine.common.virtualidentity.PhysicalLink.defaultInstance),
        portOrdinal = __fieldsMap.get(scalaDescriptor.findFieldByNumber(2).get).map(_.as[_root_.scala.Long]).getOrElse(0L)
      )
    case _ => throw new RuntimeException("Expected PMessage")
  }
  def javaDescriptor: _root_.com.google.protobuf.Descriptors.Descriptor = ControlcommandsProto.javaDescriptor.getMessageTypes().get(11)
  def scalaDescriptor: _root_.scalapb.descriptors.Descriptor = ControlcommandsProto.scalaDescriptor.messages(11)
  def messageCompanionForFieldNumber(__number: _root_.scala.Int): _root_.scalapb.GeneratedMessageCompanion[_] = {
    var __out: _root_.scalapb.GeneratedMessageCompanion[_] = null
    (__number: @_root_.scala.unchecked) match {
      case 1 => __out = edu.uci.ics.amber.engine.common.virtualidentity.PhysicalLink
    }
    __out
  }
  lazy val nestedMessagesCompanions: Seq[_root_.scalapb.GeneratedMessageCompanion[_ <: _root_.scalapb.GeneratedMessage]] = Seq.empty
  def enumCompanionForFieldNumber(__fieldNumber: _root_.scala.Int): _root_.scalapb.GeneratedEnumCompanion[_] = throw new MatchError(__fieldNumber)
  lazy val defaultInstance = edu.uci.ics.amber.engine.architecture.worker.controlcommands.LinkOrdinal(
    linkId = edu.uci.ics.amber.engine.common.virtualidentity.PhysicalLink.defaultInstance,
    portOrdinal = 0L
  )
  implicit class LinkOrdinalLens[UpperPB](_l: _root_.scalapb.lenses.Lens[UpperPB, edu.uci.ics.amber.engine.architecture.worker.controlcommands.LinkOrdinal]) extends _root_.scalapb.lenses.ObjectLens[UpperPB, edu.uci.ics.amber.engine.architecture.worker.controlcommands.LinkOrdinal](_l) {
    def linkId: _root_.scalapb.lenses.Lens[UpperPB, edu.uci.ics.amber.engine.common.virtualidentity.PhysicalLink] = field(_.linkId)((c_, f_) => c_.copy(linkId = f_))
    def portOrdinal: _root_.scalapb.lenses.Lens[UpperPB, _root_.scala.Long] = field(_.portOrdinal)((c_, f_) => c_.copy(portOrdinal = f_))
  }
  final val LINK_ID_FIELD_NUMBER = 1
  final val PORT_ORDINAL_FIELD_NUMBER = 2
  def of(
    linkId: edu.uci.ics.amber.engine.common.virtualidentity.PhysicalLink,
    portOrdinal: _root_.scala.Long
  ): _root_.edu.uci.ics.amber.engine.architecture.worker.controlcommands.LinkOrdinal = _root_.edu.uci.ics.amber.engine.architecture.worker.controlcommands.LinkOrdinal(
    linkId,
    portOrdinal
  )
  // @@protoc_insertion_point(GeneratedMessageCompanion[edu.uci.ics.amber.engine.architecture.worker.LinkOrdinal])
}
