// Generated by the Scala Plugin for the Protocol Buffer Compiler.
// Do not edit!
//
// Protofile syntax: PROTO3

package edu.uci.ics.amber.engine.common.virtualidentity

@SerialVersionUID(0L)
final case class PhysicalOpIdentity(
    logicalOpId: edu.uci.ics.amber.engine.common.virtualidentity.OperatorIdentity,
    layerName: _root_.scala.Predef.String
    ) extends scalapb.GeneratedMessage with scalapb.lenses.Updatable[PhysicalOpIdentity] {
    @transient
    private[this] var __serializedSizeCachedValue: _root_.scala.Int = 0
    private[this] def __computeSerializedValue(): _root_.scala.Int = {
      var __size = 0
      
      {
        val __value = logicalOpId
        if (__value != edu.uci.ics.amber.engine.common.virtualidentity.OperatorIdentity.defaultInstance) {
          __size += 1 + _root_.com.google.protobuf.CodedOutputStream.computeUInt32SizeNoTag(__value.serializedSize) + __value.serializedSize
        }
      };
      
      {
        val __value = layerName
        if (!__value.isEmpty) {
          __size += _root_.com.google.protobuf.CodedOutputStream.computeStringSize(2, __value)
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
        val __v = logicalOpId
        if (__v != edu.uci.ics.amber.engine.common.virtualidentity.OperatorIdentity.defaultInstance) {
          _output__.writeTag(1, 2)
          _output__.writeUInt32NoTag(__v.serializedSize)
          __v.writeTo(_output__)
        }
      };
      {
        val __v = layerName
        if (!__v.isEmpty) {
          _output__.writeString(2, __v)
        }
      };
    }
    def withLogicalOpId(__v: edu.uci.ics.amber.engine.common.virtualidentity.OperatorIdentity): PhysicalOpIdentity = copy(logicalOpId = __v)
    def withLayerName(__v: _root_.scala.Predef.String): PhysicalOpIdentity = copy(layerName = __v)
    def getFieldByNumber(__fieldNumber: _root_.scala.Int): _root_.scala.Any = {
      (__fieldNumber: @_root_.scala.unchecked) match {
        case 1 => {
          val __t = logicalOpId
          if (__t != edu.uci.ics.amber.engine.common.virtualidentity.OperatorIdentity.defaultInstance) __t else null
        }
        case 2 => {
          val __t = layerName
          if (__t != "") __t else null
        }
      }
    }
    def getField(__field: _root_.scalapb.descriptors.FieldDescriptor): _root_.scalapb.descriptors.PValue = {
      _root_.scala.Predef.require(__field.containingMessage eq companion.scalaDescriptor)
      (__field.number: @_root_.scala.unchecked) match {
        case 1 => logicalOpId.toPMessage
        case 2 => _root_.scalapb.descriptors.PString(layerName)
      }
    }
    def toProtoString: _root_.scala.Predef.String = _root_.scalapb.TextFormat.printToSingleLineUnicodeString(this)
    def companion = edu.uci.ics.amber.engine.common.virtualidentity.PhysicalOpIdentity
    // @@protoc_insertion_point(GeneratedMessage[edu.uci.ics.amber.engine.common.PhysicalOpIdentity])
}

object PhysicalOpIdentity extends scalapb.GeneratedMessageCompanion[edu.uci.ics.amber.engine.common.virtualidentity.PhysicalOpIdentity] {
  implicit def messageCompanion: scalapb.GeneratedMessageCompanion[edu.uci.ics.amber.engine.common.virtualidentity.PhysicalOpIdentity] = this
  def parseFrom(`_input__`: _root_.com.google.protobuf.CodedInputStream): edu.uci.ics.amber.engine.common.virtualidentity.PhysicalOpIdentity = {
    var __logicalOpId: _root_.scala.Option[edu.uci.ics.amber.engine.common.virtualidentity.OperatorIdentity] = _root_.scala.None
    var __layerName: _root_.scala.Predef.String = ""
    var _done__ = false
    while (!_done__) {
      val _tag__ = _input__.readTag()
      _tag__ match {
        case 0 => _done__ = true
        case 10 =>
          __logicalOpId = _root_.scala.Some(__logicalOpId.fold(_root_.scalapb.LiteParser.readMessage[edu.uci.ics.amber.engine.common.virtualidentity.OperatorIdentity](_input__))(_root_.scalapb.LiteParser.readMessage(_input__, _)))
        case 18 =>
          __layerName = _input__.readStringRequireUtf8()
        case tag => _input__.skipField(tag)
      }
    }
    edu.uci.ics.amber.engine.common.virtualidentity.PhysicalOpIdentity(
        logicalOpId = __logicalOpId.getOrElse(edu.uci.ics.amber.engine.common.virtualidentity.OperatorIdentity.defaultInstance),
        layerName = __layerName
    )
  }
  implicit def messageReads: _root_.scalapb.descriptors.Reads[edu.uci.ics.amber.engine.common.virtualidentity.PhysicalOpIdentity] = _root_.scalapb.descriptors.Reads{
    case _root_.scalapb.descriptors.PMessage(__fieldsMap) =>
      _root_.scala.Predef.require(__fieldsMap.keys.forall(_.containingMessage eq scalaDescriptor), "FieldDescriptor does not match message type.")
      edu.uci.ics.amber.engine.common.virtualidentity.PhysicalOpIdentity(
        logicalOpId = __fieldsMap.get(scalaDescriptor.findFieldByNumber(1).get).map(_.as[edu.uci.ics.amber.engine.common.virtualidentity.OperatorIdentity]).getOrElse(edu.uci.ics.amber.engine.common.virtualidentity.OperatorIdentity.defaultInstance),
        layerName = __fieldsMap.get(scalaDescriptor.findFieldByNumber(2).get).map(_.as[_root_.scala.Predef.String]).getOrElse("")
      )
    case _ => throw new RuntimeException("Expected PMessage")
  }
  def javaDescriptor: _root_.com.google.protobuf.Descriptors.Descriptor = VirtualidentityProto.javaDescriptor.getMessageTypes().get(4)
  def scalaDescriptor: _root_.scalapb.descriptors.Descriptor = VirtualidentityProto.scalaDescriptor.messages(4)
  def messageCompanionForFieldNumber(__number: _root_.scala.Int): _root_.scalapb.GeneratedMessageCompanion[_] = {
    var __out: _root_.scalapb.GeneratedMessageCompanion[_] = null
    (__number: @_root_.scala.unchecked) match {
      case 1 => __out = edu.uci.ics.amber.engine.common.virtualidentity.OperatorIdentity
    }
    __out
  }
  lazy val nestedMessagesCompanions: Seq[_root_.scalapb.GeneratedMessageCompanion[_ <: _root_.scalapb.GeneratedMessage]] = Seq.empty
  def enumCompanionForFieldNumber(__fieldNumber: _root_.scala.Int): _root_.scalapb.GeneratedEnumCompanion[_] = throw new MatchError(__fieldNumber)
  lazy val defaultInstance = edu.uci.ics.amber.engine.common.virtualidentity.PhysicalOpIdentity(
    logicalOpId = edu.uci.ics.amber.engine.common.virtualidentity.OperatorIdentity.defaultInstance,
    layerName = ""
  )
  implicit class PhysicalOpIdentityLens[UpperPB](_l: _root_.scalapb.lenses.Lens[UpperPB, edu.uci.ics.amber.engine.common.virtualidentity.PhysicalOpIdentity]) extends _root_.scalapb.lenses.ObjectLens[UpperPB, edu.uci.ics.amber.engine.common.virtualidentity.PhysicalOpIdentity](_l) {
    def logicalOpId: _root_.scalapb.lenses.Lens[UpperPB, edu.uci.ics.amber.engine.common.virtualidentity.OperatorIdentity] = field(_.logicalOpId)((c_, f_) => c_.copy(logicalOpId = f_))
    def layerName: _root_.scalapb.lenses.Lens[UpperPB, _root_.scala.Predef.String] = field(_.layerName)((c_, f_) => c_.copy(layerName = f_))
  }
  final val LOGICALOPID_FIELD_NUMBER = 1
  final val LAYERNAME_FIELD_NUMBER = 2
  def of(
    logicalOpId: edu.uci.ics.amber.engine.common.virtualidentity.OperatorIdentity,
    layerName: _root_.scala.Predef.String
  ): _root_.edu.uci.ics.amber.engine.common.virtualidentity.PhysicalOpIdentity = _root_.edu.uci.ics.amber.engine.common.virtualidentity.PhysicalOpIdentity(
    logicalOpId,
    layerName
  )
  // @@protoc_insertion_point(GeneratedMessageCompanion[edu.uci.ics.amber.engine.common.PhysicalOpIdentity])
}
