// Generated by the Scala Plugin for the Protocol Buffer Compiler.
// Do not edit!
//
// Protofile syntax: PROTO3

package edu.uci.ics.amber.engine.common.workflow

@SerialVersionUID(0L)
final case class OutputPort(
    id: edu.uci.ics.amber.engine.common.workflow.PortIdentity = edu.uci.ics.amber.engine.common.workflow.PortIdentity.defaultInstance,
    displayName: _root_.scala.Predef.String = "",
    blocking: _root_.scala.Boolean = false,
    storageLocation: _root_.scala.Predef.String = ""
    ) extends scalapb.GeneratedMessage with scalapb.lenses.Updatable[OutputPort] {
    @transient
    private[this] var __serializedSizeCachedValue: _root_.scala.Int = 0
    private[this] def __computeSerializedValue(): _root_.scala.Int = {
      var __size = 0
      
      {
        val __value = id
        if (__value != edu.uci.ics.amber.engine.common.workflow.PortIdentity.defaultInstance) {
          __size += 1 + _root_.com.google.protobuf.CodedOutputStream.computeUInt32SizeNoTag(__value.serializedSize) + __value.serializedSize
        }
      };
      
      {
        val __value = displayName
        if (!__value.isEmpty) {
          __size += _root_.com.google.protobuf.CodedOutputStream.computeStringSize(2, __value)
        }
      };
      
      {
        val __value = blocking
        if (__value != false) {
          __size += _root_.com.google.protobuf.CodedOutputStream.computeBoolSize(3, __value)
        }
      };
      
      {
        val __value = storageLocation
        if (!__value.isEmpty) {
          __size += _root_.com.google.protobuf.CodedOutputStream.computeStringSize(4, __value)
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
        if (__v != edu.uci.ics.amber.engine.common.workflow.PortIdentity.defaultInstance) {
          _output__.writeTag(1, 2)
          _output__.writeUInt32NoTag(__v.serializedSize)
          __v.writeTo(_output__)
        }
      };
      {
        val __v = displayName
        if (!__v.isEmpty) {
          _output__.writeString(2, __v)
        }
      };
      {
        val __v = blocking
        if (__v != false) {
          _output__.writeBool(3, __v)
        }
      };
      {
        val __v = storageLocation
        if (!__v.isEmpty) {
          _output__.writeString(4, __v)
        }
      };
    }
    def withId(__v: edu.uci.ics.amber.engine.common.workflow.PortIdentity): OutputPort = copy(id = __v)
    def withDisplayName(__v: _root_.scala.Predef.String): OutputPort = copy(displayName = __v)
    def withBlocking(__v: _root_.scala.Boolean): OutputPort = copy(blocking = __v)
    def withStorageLocation(__v: _root_.scala.Predef.String): OutputPort = copy(storageLocation = __v)
    def getFieldByNumber(__fieldNumber: _root_.scala.Int): _root_.scala.Any = {
      (__fieldNumber: @_root_.scala.unchecked) match {
        case 1 => {
          val __t = id
          if (__t != edu.uci.ics.amber.engine.common.workflow.PortIdentity.defaultInstance) __t else null
        }
        case 2 => {
          val __t = displayName
          if (__t != "") __t else null
        }
        case 3 => {
          val __t = blocking
          if (__t != false) __t else null
        }
        case 4 => {
          val __t = storageLocation
          if (__t != "") __t else null
        }
      }
    }
    def getField(__field: _root_.scalapb.descriptors.FieldDescriptor): _root_.scalapb.descriptors.PValue = {
      _root_.scala.Predef.require(__field.containingMessage eq companion.scalaDescriptor)
      (__field.number: @_root_.scala.unchecked) match {
        case 1 => id.toPMessage
        case 2 => _root_.scalapb.descriptors.PString(displayName)
        case 3 => _root_.scalapb.descriptors.PBoolean(blocking)
        case 4 => _root_.scalapb.descriptors.PString(storageLocation)
      }
    }
    def toProtoString: _root_.scala.Predef.String = _root_.scalapb.TextFormat.printToSingleLineUnicodeString(this)
    def companion = edu.uci.ics.amber.engine.common.workflow.OutputPort
    // @@protoc_insertion_point(GeneratedMessage[edu.uci.ics.amber.engine.common.OutputPort])
}

object OutputPort extends scalapb.GeneratedMessageCompanion[edu.uci.ics.amber.engine.common.workflow.OutputPort] {
  implicit def messageCompanion: scalapb.GeneratedMessageCompanion[edu.uci.ics.amber.engine.common.workflow.OutputPort] = this
  def parseFrom(`_input__`: _root_.com.google.protobuf.CodedInputStream): edu.uci.ics.amber.engine.common.workflow.OutputPort = {
    var __id: _root_.scala.Option[edu.uci.ics.amber.engine.common.workflow.PortIdentity] = _root_.scala.None
    var __displayName: _root_.scala.Predef.String = ""
    var __blocking: _root_.scala.Boolean = false
    var __storageLocation: _root_.scala.Predef.String = ""
    var _done__ = false
    while (!_done__) {
      val _tag__ = _input__.readTag()
      _tag__ match {
        case 0 => _done__ = true
        case 10 =>
          __id = _root_.scala.Some(__id.fold(_root_.scalapb.LiteParser.readMessage[edu.uci.ics.amber.engine.common.workflow.PortIdentity](_input__))(_root_.scalapb.LiteParser.readMessage(_input__, _)))
        case 18 =>
          __displayName = _input__.readStringRequireUtf8()
        case 24 =>
          __blocking = _input__.readBool()
        case 34 =>
          __storageLocation = _input__.readStringRequireUtf8()
        case tag => _input__.skipField(tag)
      }
    }
    edu.uci.ics.amber.engine.common.workflow.OutputPort(
        id = __id.getOrElse(edu.uci.ics.amber.engine.common.workflow.PortIdentity.defaultInstance),
        displayName = __displayName,
        blocking = __blocking,
        storageLocation = __storageLocation
    )
  }
  implicit def messageReads: _root_.scalapb.descriptors.Reads[edu.uci.ics.amber.engine.common.workflow.OutputPort] = _root_.scalapb.descriptors.Reads{
    case _root_.scalapb.descriptors.PMessage(__fieldsMap) =>
      _root_.scala.Predef.require(__fieldsMap.keys.forall(_.containingMessage eq scalaDescriptor), "FieldDescriptor does not match message type.")
      edu.uci.ics.amber.engine.common.workflow.OutputPort(
        id = __fieldsMap.get(scalaDescriptor.findFieldByNumber(1).get).map(_.as[edu.uci.ics.amber.engine.common.workflow.PortIdentity]).getOrElse(edu.uci.ics.amber.engine.common.workflow.PortIdentity.defaultInstance),
        displayName = __fieldsMap.get(scalaDescriptor.findFieldByNumber(2).get).map(_.as[_root_.scala.Predef.String]).getOrElse(""),
        blocking = __fieldsMap.get(scalaDescriptor.findFieldByNumber(3).get).map(_.as[_root_.scala.Boolean]).getOrElse(false),
        storageLocation = __fieldsMap.get(scalaDescriptor.findFieldByNumber(4).get).map(_.as[_root_.scala.Predef.String]).getOrElse("")
      )
    case _ => throw new RuntimeException("Expected PMessage")
  }
  def javaDescriptor: _root_.com.google.protobuf.Descriptors.Descriptor = WorkflowProto.javaDescriptor.getMessageTypes().get(2)
  def scalaDescriptor: _root_.scalapb.descriptors.Descriptor = WorkflowProto.scalaDescriptor.messages(2)
  def messageCompanionForFieldNumber(__number: _root_.scala.Int): _root_.scalapb.GeneratedMessageCompanion[_] = {
    var __out: _root_.scalapb.GeneratedMessageCompanion[_] = null
    (__number: @_root_.scala.unchecked) match {
      case 1 => __out = edu.uci.ics.amber.engine.common.workflow.PortIdentity
    }
    __out
  }
  lazy val nestedMessagesCompanions: Seq[_root_.scalapb.GeneratedMessageCompanion[_ <: _root_.scalapb.GeneratedMessage]] = Seq.empty
  def enumCompanionForFieldNumber(__fieldNumber: _root_.scala.Int): _root_.scalapb.GeneratedEnumCompanion[_] = throw new MatchError(__fieldNumber)
  lazy val defaultInstance = edu.uci.ics.amber.engine.common.workflow.OutputPort(
    id = edu.uci.ics.amber.engine.common.workflow.PortIdentity.defaultInstance,
    displayName = "",
    blocking = false,
    storageLocation = ""
  )
  implicit class OutputPortLens[UpperPB](_l: _root_.scalapb.lenses.Lens[UpperPB, edu.uci.ics.amber.engine.common.workflow.OutputPort]) extends _root_.scalapb.lenses.ObjectLens[UpperPB, edu.uci.ics.amber.engine.common.workflow.OutputPort](_l) {
    def id: _root_.scalapb.lenses.Lens[UpperPB, edu.uci.ics.amber.engine.common.workflow.PortIdentity] = field(_.id)((c_, f_) => c_.copy(id = f_))
    def displayName: _root_.scalapb.lenses.Lens[UpperPB, _root_.scala.Predef.String] = field(_.displayName)((c_, f_) => c_.copy(displayName = f_))
    def blocking: _root_.scalapb.lenses.Lens[UpperPB, _root_.scala.Boolean] = field(_.blocking)((c_, f_) => c_.copy(blocking = f_))
    def storageLocation: _root_.scalapb.lenses.Lens[UpperPB, _root_.scala.Predef.String] = field(_.storageLocation)((c_, f_) => c_.copy(storageLocation = f_))
  }
  final val ID_FIELD_NUMBER = 1
  final val DISPLAYNAME_FIELD_NUMBER = 2
  final val BLOCKING_FIELD_NUMBER = 3
  final val STORAGELOCATION_FIELD_NUMBER = 4
  def of(
    id: edu.uci.ics.amber.engine.common.workflow.PortIdentity,
    displayName: _root_.scala.Predef.String,
    blocking: _root_.scala.Boolean,
    storageLocation: _root_.scala.Predef.String
  ): _root_.edu.uci.ics.amber.engine.common.workflow.OutputPort = _root_.edu.uci.ics.amber.engine.common.workflow.OutputPort(
    id,
    displayName,
    blocking,
    storageLocation
  )
  // @@protoc_insertion_point(GeneratedMessageCompanion[edu.uci.ics.amber.engine.common.OutputPort])
}
