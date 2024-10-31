// Generated by the Scala Plugin for the Protocol Buffer Compiler.
// Do not edit!
//
// Protofile syntax: PROTO3

package edu.uci.ics.amber.engine.common.workflowruntimestate

@SerialVersionUID(0L)
final case class WorkflowFatalError(
    `type`: edu.uci.ics.amber.engine.common.workflowruntimestate.FatalErrorType = edu.uci.ics.amber.engine.common.workflowruntimestate.FatalErrorType.COMPILATION_ERROR,
    timestamp: com.google.protobuf.timestamp.Timestamp = com.google.protobuf.timestamp.Timestamp.defaultInstance,
    message: _root_.scala.Predef.String = "",
    details: _root_.scala.Predef.String = "",
    operatorId: _root_.scala.Predef.String = "",
    workerId: _root_.scala.Predef.String = ""
    ) extends scalapb.GeneratedMessage with scalapb.lenses.Updatable[WorkflowFatalError] {
    @transient
    private[this] var __serializedSizeMemoized: _root_.scala.Int = 0
    private[this] def __computeSerializedSize(): _root_.scala.Int = {
      var __size = 0
      
      {
        val __value = `type`.value
        if (__value != 0) {
          __size += _root_.com.google.protobuf.CodedOutputStream.computeEnumSize(1, __value)
        }
      };
      
      {
        val __value = timestamp
        if (__value.serializedSize != 0) {
          __size += 1 + _root_.com.google.protobuf.CodedOutputStream.computeUInt32SizeNoTag(__value.serializedSize) + __value.serializedSize
        }
      };
      
      {
        val __value = message
        if (!__value.isEmpty) {
          __size += _root_.com.google.protobuf.CodedOutputStream.computeStringSize(3, __value)
        }
      };
      
      {
        val __value = details
        if (!__value.isEmpty) {
          __size += _root_.com.google.protobuf.CodedOutputStream.computeStringSize(4, __value)
        }
      };
      
      {
        val __value = operatorId
        if (!__value.isEmpty) {
          __size += _root_.com.google.protobuf.CodedOutputStream.computeStringSize(5, __value)
        }
      };
      
      {
        val __value = workerId
        if (!__value.isEmpty) {
          __size += _root_.com.google.protobuf.CodedOutputStream.computeStringSize(6, __value)
        }
      };
      __size
    }
    override def serializedSize: _root_.scala.Int = {
      var __size = __serializedSizeMemoized
      if (__size == 0) {
        __size = __computeSerializedSize() + 1
        __serializedSizeMemoized = __size
      }
      __size - 1
      
    }
    def writeTo(`_output__`: _root_.com.google.protobuf.CodedOutputStream): _root_.scala.Unit = {
      {
        val __v = `type`.value
        if (__v != 0) {
          _output__.writeEnum(1, __v)
        }
      };
      {
        val __v = timestamp
        if (__v.serializedSize != 0) {
          _output__.writeTag(2, 2)
          _output__.writeUInt32NoTag(__v.serializedSize)
          __v.writeTo(_output__)
        }
      };
      {
        val __v = message
        if (!__v.isEmpty) {
          _output__.writeString(3, __v)
        }
      };
      {
        val __v = details
        if (!__v.isEmpty) {
          _output__.writeString(4, __v)
        }
      };
      {
        val __v = operatorId
        if (!__v.isEmpty) {
          _output__.writeString(5, __v)
        }
      };
      {
        val __v = workerId
        if (!__v.isEmpty) {
          _output__.writeString(6, __v)
        }
      };
    }
    def withType(__v: edu.uci.ics.amber.engine.common.workflowruntimestate.FatalErrorType): WorkflowFatalError = copy(`type` = __v)
    def withTimestamp(__v: com.google.protobuf.timestamp.Timestamp): WorkflowFatalError = copy(timestamp = __v)
    def withMessage(__v: _root_.scala.Predef.String): WorkflowFatalError = copy(message = __v)
    def withDetails(__v: _root_.scala.Predef.String): WorkflowFatalError = copy(details = __v)
    def withOperatorId(__v: _root_.scala.Predef.String): WorkflowFatalError = copy(operatorId = __v)
    def withWorkerId(__v: _root_.scala.Predef.String): WorkflowFatalError = copy(workerId = __v)
    def getFieldByNumber(__fieldNumber: _root_.scala.Int): _root_.scala.Any = {
      (__fieldNumber: @_root_.scala.unchecked) match {
        case 1 => {
          val __t = `type`.javaValueDescriptor
          if (__t.getNumber() != 0) __t else null
        }
        case 2 => {
          val __t = timestamp
          if (__t != com.google.protobuf.timestamp.Timestamp.defaultInstance) __t else null
        }
        case 3 => {
          val __t = message
          if (__t != "") __t else null
        }
        case 4 => {
          val __t = details
          if (__t != "") __t else null
        }
        case 5 => {
          val __t = operatorId
          if (__t != "") __t else null
        }
        case 6 => {
          val __t = workerId
          if (__t != "") __t else null
        }
      }
    }
    def getField(__field: _root_.scalapb.descriptors.FieldDescriptor): _root_.scalapb.descriptors.PValue = {
      _root_.scala.Predef.require(__field.containingMessage eq companion.scalaDescriptor)
      (__field.number: @_root_.scala.unchecked) match {
        case 1 => _root_.scalapb.descriptors.PEnum(`type`.scalaValueDescriptor)
        case 2 => timestamp.toPMessage
        case 3 => _root_.scalapb.descriptors.PString(message)
        case 4 => _root_.scalapb.descriptors.PString(details)
        case 5 => _root_.scalapb.descriptors.PString(operatorId)
        case 6 => _root_.scalapb.descriptors.PString(workerId)
      }
    }
    def toProtoString: _root_.scala.Predef.String = _root_.scalapb.TextFormat.printToSingleLineUnicodeString(this)
    def companion: edu.uci.ics.amber.engine.common.workflowruntimestate.WorkflowFatalError.type = edu.uci.ics.amber.engine.common.workflowruntimestate.WorkflowFatalError
    // @@protoc_insertion_point(GeneratedMessage[edu.uci.ics.amber.engine.common.WorkflowFatalError])
}

object WorkflowFatalError extends scalapb.GeneratedMessageCompanion[edu.uci.ics.amber.engine.common.workflowruntimestate.WorkflowFatalError] {
  implicit def messageCompanion: scalapb.GeneratedMessageCompanion[edu.uci.ics.amber.engine.common.workflowruntimestate.WorkflowFatalError] = this
  def parseFrom(`_input__`: _root_.com.google.protobuf.CodedInputStream): edu.uci.ics.amber.engine.common.workflowruntimestate.WorkflowFatalError = {
    var __type: edu.uci.ics.amber.engine.common.workflowruntimestate.FatalErrorType = edu.uci.ics.amber.engine.common.workflowruntimestate.FatalErrorType.COMPILATION_ERROR
    var __timestamp: _root_.scala.Option[com.google.protobuf.timestamp.Timestamp] = _root_.scala.None
    var __message: _root_.scala.Predef.String = ""
    var __details: _root_.scala.Predef.String = ""
    var __operatorId: _root_.scala.Predef.String = ""
    var __workerId: _root_.scala.Predef.String = ""
    var _done__ = false
    while (!_done__) {
      val _tag__ = _input__.readTag()
      _tag__ match {
        case 0 => _done__ = true
        case 8 =>
          __type = edu.uci.ics.amber.engine.common.workflowruntimestate.FatalErrorType.fromValue(_input__.readEnum())
        case 18 =>
          __timestamp = _root_.scala.Some(__timestamp.fold(_root_.scalapb.LiteParser.readMessage[com.google.protobuf.timestamp.Timestamp](_input__))(_root_.scalapb.LiteParser.readMessage(_input__, _)))
        case 26 =>
          __message = _input__.readStringRequireUtf8()
        case 34 =>
          __details = _input__.readStringRequireUtf8()
        case 42 =>
          __operatorId = _input__.readStringRequireUtf8()
        case 50 =>
          __workerId = _input__.readStringRequireUtf8()
        case tag => _input__.skipField(tag)
      }
    }
    edu.uci.ics.amber.engine.common.workflowruntimestate.WorkflowFatalError(
        `type` = __type,
        timestamp = __timestamp.getOrElse(com.google.protobuf.timestamp.Timestamp.defaultInstance),
        message = __message,
        details = __details,
        operatorId = __operatorId,
        workerId = __workerId
    )
  }
  implicit def messageReads: _root_.scalapb.descriptors.Reads[edu.uci.ics.amber.engine.common.workflowruntimestate.WorkflowFatalError] = _root_.scalapb.descriptors.Reads{
    case _root_.scalapb.descriptors.PMessage(__fieldsMap) =>
      _root_.scala.Predef.require(__fieldsMap.keys.forall(_.containingMessage eq scalaDescriptor), "FieldDescriptor does not match message type.")
      edu.uci.ics.amber.engine.common.workflowruntimestate.WorkflowFatalError(
        `type` = edu.uci.ics.amber.engine.common.workflowruntimestate.FatalErrorType.fromValue(__fieldsMap.get(scalaDescriptor.findFieldByNumber(1).get).map(_.as[_root_.scalapb.descriptors.EnumValueDescriptor]).getOrElse(edu.uci.ics.amber.engine.common.workflowruntimestate.FatalErrorType.COMPILATION_ERROR.scalaValueDescriptor).number),
        timestamp = __fieldsMap.get(scalaDescriptor.findFieldByNumber(2).get).map(_.as[com.google.protobuf.timestamp.Timestamp]).getOrElse(com.google.protobuf.timestamp.Timestamp.defaultInstance),
        message = __fieldsMap.get(scalaDescriptor.findFieldByNumber(3).get).map(_.as[_root_.scala.Predef.String]).getOrElse(""),
        details = __fieldsMap.get(scalaDescriptor.findFieldByNumber(4).get).map(_.as[_root_.scala.Predef.String]).getOrElse(""),
        operatorId = __fieldsMap.get(scalaDescriptor.findFieldByNumber(5).get).map(_.as[_root_.scala.Predef.String]).getOrElse(""),
        workerId = __fieldsMap.get(scalaDescriptor.findFieldByNumber(6).get).map(_.as[_root_.scala.Predef.String]).getOrElse("")
      )
    case _ => throw new RuntimeException("Expected PMessage")
  }
  def javaDescriptor: _root_.com.google.protobuf.Descriptors.Descriptor = WorkflowruntimestateProto.javaDescriptor.getMessageTypes().get(10)
  def scalaDescriptor: _root_.scalapb.descriptors.Descriptor = WorkflowruntimestateProto.scalaDescriptor.messages(10)
  def messageCompanionForFieldNumber(__number: _root_.scala.Int): _root_.scalapb.GeneratedMessageCompanion[_] = {
    var __out: _root_.scalapb.GeneratedMessageCompanion[_] = null
    (__number: @_root_.scala.unchecked) match {
      case 2 => __out = com.google.protobuf.timestamp.Timestamp
    }
    __out
  }
  lazy val nestedMessagesCompanions: Seq[_root_.scalapb.GeneratedMessageCompanion[_ <: _root_.scalapb.GeneratedMessage]] = Seq.empty
  def enumCompanionForFieldNumber(__fieldNumber: _root_.scala.Int): _root_.scalapb.GeneratedEnumCompanion[_] = {
    (__fieldNumber: @_root_.scala.unchecked) match {
      case 1 => edu.uci.ics.amber.engine.common.workflowruntimestate.FatalErrorType
    }
  }
  lazy val defaultInstance = edu.uci.ics.amber.engine.common.workflowruntimestate.WorkflowFatalError(
    `type` = edu.uci.ics.amber.engine.common.workflowruntimestate.FatalErrorType.COMPILATION_ERROR,
    timestamp = com.google.protobuf.timestamp.Timestamp.defaultInstance,
    message = "",
    details = "",
    operatorId = "",
    workerId = ""
  )
  implicit class WorkflowFatalErrorLens[UpperPB](_l: _root_.scalapb.lenses.Lens[UpperPB, edu.uci.ics.amber.engine.common.workflowruntimestate.WorkflowFatalError]) extends _root_.scalapb.lenses.ObjectLens[UpperPB, edu.uci.ics.amber.engine.common.workflowruntimestate.WorkflowFatalError](_l) {
    def `type`: _root_.scalapb.lenses.Lens[UpperPB, edu.uci.ics.amber.engine.common.workflowruntimestate.FatalErrorType] = field(_.`type`)((c_, f_) => c_.copy(`type` = f_))
    def timestamp: _root_.scalapb.lenses.Lens[UpperPB, com.google.protobuf.timestamp.Timestamp] = field(_.timestamp)((c_, f_) => c_.copy(timestamp = f_))
    def message: _root_.scalapb.lenses.Lens[UpperPB, _root_.scala.Predef.String] = field(_.message)((c_, f_) => c_.copy(message = f_))
    def details: _root_.scalapb.lenses.Lens[UpperPB, _root_.scala.Predef.String] = field(_.details)((c_, f_) => c_.copy(details = f_))
    def operatorId: _root_.scalapb.lenses.Lens[UpperPB, _root_.scala.Predef.String] = field(_.operatorId)((c_, f_) => c_.copy(operatorId = f_))
    def workerId: _root_.scalapb.lenses.Lens[UpperPB, _root_.scala.Predef.String] = field(_.workerId)((c_, f_) => c_.copy(workerId = f_))
  }
  final val TYPE_FIELD_NUMBER = 1
  final val TIMESTAMP_FIELD_NUMBER = 2
  final val MESSAGE_FIELD_NUMBER = 3
  final val DETAILS_FIELD_NUMBER = 4
  final val OPERATORID_FIELD_NUMBER = 5
  final val WORKERID_FIELD_NUMBER = 6
  def of(
    `type`: edu.uci.ics.amber.engine.common.workflowruntimestate.FatalErrorType,
    timestamp: com.google.protobuf.timestamp.Timestamp,
    message: _root_.scala.Predef.String,
    details: _root_.scala.Predef.String,
    operatorId: _root_.scala.Predef.String,
    workerId: _root_.scala.Predef.String
  ): _root_.edu.uci.ics.amber.engine.common.workflowruntimestate.WorkflowFatalError = _root_.edu.uci.ics.amber.engine.common.workflowruntimestate.WorkflowFatalError(
    `type`,
    timestamp,
    message,
    details,
    operatorId,
    workerId
  )
  // @@protoc_insertion_point(GeneratedMessageCompanion[edu.uci.ics.amber.engine.common.WorkflowFatalError])
}
