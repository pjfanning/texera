// Generated by the Scala Plugin for the Protocol Buffer Compiler.
// Do not edit!
//
// Protofile syntax: PROTO3

package edu.uci.ics.amber.engine.architecture.rpc.controlcommands

@SerialVersionUID(0L)
final case class ControlInvocation(
    methodName: _root_.scala.Predef.String,
    command: edu.uci.ics.amber.engine.architecture.rpc.controlcommands.ControlRequest,
    context: edu.uci.ics.amber.engine.architecture.rpc.controlcommands.AsyncRPCContext,
    commandId: _root_.scala.Long
    ) extends scalapb.GeneratedMessage with scalapb.lenses.Updatable[ControlInvocation] with edu.uci.ics.amber.engine.common.ambermessage.ControlPayload {
    @transient
    private[this] var __serializedSizeMemoized: _root_.scala.Int = 0
    private[this] def __computeSerializedSize(): _root_.scala.Int = {
      var __size = 0
      
      {
        val __value = methodName
        if (!__value.isEmpty) {
          __size += _root_.com.google.protobuf.CodedOutputStream.computeStringSize(1, __value)
        }
      };
      
      {
        val __value = edu.uci.ics.amber.engine.architecture.rpc.controlcommands.ControlInvocation._typemapper_command.toBase(command)
        if (__value.serializedSize != 0) {
          __size += 1 + _root_.com.google.protobuf.CodedOutputStream.computeUInt32SizeNoTag(__value.serializedSize) + __value.serializedSize
        }
      };
      
      {
        val __value = context
        if (__value.serializedSize != 0) {
          __size += 1 + _root_.com.google.protobuf.CodedOutputStream.computeUInt32SizeNoTag(__value.serializedSize) + __value.serializedSize
        }
      };
      
      {
        val __value = commandId
        if (__value != 0L) {
          __size += _root_.com.google.protobuf.CodedOutputStream.computeInt64Size(4, __value)
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
        val __v = methodName
        if (!__v.isEmpty) {
          _output__.writeString(1, __v)
        }
      };
      {
        val __v = edu.uci.ics.amber.engine.architecture.rpc.controlcommands.ControlInvocation._typemapper_command.toBase(command)
        if (__v.serializedSize != 0) {
          _output__.writeTag(2, 2)
          _output__.writeUInt32NoTag(__v.serializedSize)
          __v.writeTo(_output__)
        }
      };
      {
        val __v = context
        if (__v.serializedSize != 0) {
          _output__.writeTag(3, 2)
          _output__.writeUInt32NoTag(__v.serializedSize)
          __v.writeTo(_output__)
        }
      };
      {
        val __v = commandId
        if (__v != 0L) {
          _output__.writeInt64(4, __v)
        }
      };
    }
    def withMethodName(__v: _root_.scala.Predef.String): ControlInvocation = copy(methodName = __v)
    def withCommand(__v: edu.uci.ics.amber.engine.architecture.rpc.controlcommands.ControlRequest): ControlInvocation = copy(command = __v)
    def withContext(__v: edu.uci.ics.amber.engine.architecture.rpc.controlcommands.AsyncRPCContext): ControlInvocation = copy(context = __v)
    def withCommandId(__v: _root_.scala.Long): ControlInvocation = copy(commandId = __v)
    def getFieldByNumber(__fieldNumber: _root_.scala.Int): _root_.scala.Any = {
      (__fieldNumber: @_root_.scala.unchecked) match {
        case 1 => {
          val __t = methodName
          if (__t != "") __t else null
        }
        case 2 => {
          val __t = edu.uci.ics.amber.engine.architecture.rpc.controlcommands.ControlInvocation._typemapper_command.toBase(command)
          if (__t != edu.uci.ics.amber.engine.architecture.rpc.controlcommands.ControlRequestMessage.defaultInstance) __t else null
        }
        case 3 => {
          val __t = context
          if (__t != edu.uci.ics.amber.engine.architecture.rpc.controlcommands.AsyncRPCContext.defaultInstance) __t else null
        }
        case 4 => {
          val __t = commandId
          if (__t != 0L) __t else null
        }
      }
    }
    def getField(__field: _root_.scalapb.descriptors.FieldDescriptor): _root_.scalapb.descriptors.PValue = {
      _root_.scala.Predef.require(__field.containingMessage eq companion.scalaDescriptor)
      (__field.number: @_root_.scala.unchecked) match {
        case 1 => _root_.scalapb.descriptors.PString(methodName)
        case 2 => edu.uci.ics.amber.engine.architecture.rpc.controlcommands.ControlInvocation._typemapper_command.toBase(command).toPMessage
        case 3 => context.toPMessage
        case 4 => _root_.scalapb.descriptors.PLong(commandId)
      }
    }
    def toProtoString: _root_.scala.Predef.String = _root_.scalapb.TextFormat.printToSingleLineUnicodeString(this)
    def companion: edu.uci.ics.amber.engine.architecture.rpc.controlcommands.ControlInvocation.type = edu.uci.ics.amber.engine.architecture.rpc.controlcommands.ControlInvocation
    // @@protoc_insertion_point(GeneratedMessage[edu.uci.ics.amber.engine.architecture.rpc.ControlInvocation])
}

object ControlInvocation extends scalapb.GeneratedMessageCompanion[edu.uci.ics.amber.engine.architecture.rpc.controlcommands.ControlInvocation] {
  implicit def messageCompanion: scalapb.GeneratedMessageCompanion[edu.uci.ics.amber.engine.architecture.rpc.controlcommands.ControlInvocation] = this
  def parseFrom(`_input__`: _root_.com.google.protobuf.CodedInputStream): edu.uci.ics.amber.engine.architecture.rpc.controlcommands.ControlInvocation = {
    var __methodName: _root_.scala.Predef.String = ""
    var __command: _root_.scala.Option[edu.uci.ics.amber.engine.architecture.rpc.controlcommands.ControlRequestMessage] = _root_.scala.None
    var __context: _root_.scala.Option[edu.uci.ics.amber.engine.architecture.rpc.controlcommands.AsyncRPCContext] = _root_.scala.None
    var __commandId: _root_.scala.Long = 0L
    var _done__ = false
    while (!_done__) {
      val _tag__ = _input__.readTag()
      _tag__ match {
        case 0 => _done__ = true
        case 10 =>
          __methodName = _input__.readStringRequireUtf8()
        case 18 =>
          __command = _root_.scala.Some(__command.fold(_root_.scalapb.LiteParser.readMessage[edu.uci.ics.amber.engine.architecture.rpc.controlcommands.ControlRequestMessage](_input__))(_root_.scalapb.LiteParser.readMessage(_input__, _)))
        case 26 =>
          __context = _root_.scala.Some(__context.fold(_root_.scalapb.LiteParser.readMessage[edu.uci.ics.amber.engine.architecture.rpc.controlcommands.AsyncRPCContext](_input__))(_root_.scalapb.LiteParser.readMessage(_input__, _)))
        case 32 =>
          __commandId = _input__.readInt64()
        case tag => _input__.skipField(tag)
      }
    }
    edu.uci.ics.amber.engine.architecture.rpc.controlcommands.ControlInvocation(
        methodName = __methodName,
        command = edu.uci.ics.amber.engine.architecture.rpc.controlcommands.ControlInvocation._typemapper_command.toCustom(__command.getOrElse(edu.uci.ics.amber.engine.architecture.rpc.controlcommands.ControlRequestMessage.defaultInstance)),
        context = __context.getOrElse(edu.uci.ics.amber.engine.architecture.rpc.controlcommands.AsyncRPCContext.defaultInstance),
        commandId = __commandId
    )
  }
  implicit def messageReads: _root_.scalapb.descriptors.Reads[edu.uci.ics.amber.engine.architecture.rpc.controlcommands.ControlInvocation] = _root_.scalapb.descriptors.Reads{
    case _root_.scalapb.descriptors.PMessage(__fieldsMap) =>
      _root_.scala.Predef.require(__fieldsMap.keys.forall(_.containingMessage eq scalaDescriptor), "FieldDescriptor does not match message type.")
      edu.uci.ics.amber.engine.architecture.rpc.controlcommands.ControlInvocation(
        methodName = __fieldsMap.get(scalaDescriptor.findFieldByNumber(1).get).map(_.as[_root_.scala.Predef.String]).getOrElse(""),
        command = edu.uci.ics.amber.engine.architecture.rpc.controlcommands.ControlInvocation._typemapper_command.toCustom(__fieldsMap.get(scalaDescriptor.findFieldByNumber(2).get).map(_.as[edu.uci.ics.amber.engine.architecture.rpc.controlcommands.ControlRequestMessage]).getOrElse(edu.uci.ics.amber.engine.architecture.rpc.controlcommands.ControlRequestMessage.defaultInstance)),
        context = __fieldsMap.get(scalaDescriptor.findFieldByNumber(3).get).map(_.as[edu.uci.ics.amber.engine.architecture.rpc.controlcommands.AsyncRPCContext]).getOrElse(edu.uci.ics.amber.engine.architecture.rpc.controlcommands.AsyncRPCContext.defaultInstance),
        commandId = __fieldsMap.get(scalaDescriptor.findFieldByNumber(4).get).map(_.as[_root_.scala.Long]).getOrElse(0L)
      )
    case _ => throw new RuntimeException("Expected PMessage")
  }
  def javaDescriptor: _root_.com.google.protobuf.Descriptors.Descriptor = ControlcommandsProto.javaDescriptor.getMessageTypes().get(3)
  def scalaDescriptor: _root_.scalapb.descriptors.Descriptor = ControlcommandsProto.scalaDescriptor.messages(3)
  def messageCompanionForFieldNumber(__number: _root_.scala.Int): _root_.scalapb.GeneratedMessageCompanion[_] = {
    var __out: _root_.scalapb.GeneratedMessageCompanion[_] = null
    (__number: @_root_.scala.unchecked) match {
      case 2 => __out = edu.uci.ics.amber.engine.architecture.rpc.controlcommands.ControlRequestMessage
      case 3 => __out = edu.uci.ics.amber.engine.architecture.rpc.controlcommands.AsyncRPCContext
    }
    __out
  }
  lazy val nestedMessagesCompanions: Seq[_root_.scalapb.GeneratedMessageCompanion[_ <: _root_.scalapb.GeneratedMessage]] = Seq.empty
  def enumCompanionForFieldNumber(__fieldNumber: _root_.scala.Int): _root_.scalapb.GeneratedEnumCompanion[_] = throw new MatchError(__fieldNumber)
  lazy val defaultInstance = edu.uci.ics.amber.engine.architecture.rpc.controlcommands.ControlInvocation(
    methodName = "",
    command = edu.uci.ics.amber.engine.architecture.rpc.controlcommands.ControlInvocation._typemapper_command.toCustom(edu.uci.ics.amber.engine.architecture.rpc.controlcommands.ControlRequestMessage.defaultInstance),
    context = edu.uci.ics.amber.engine.architecture.rpc.controlcommands.AsyncRPCContext.defaultInstance,
    commandId = 0L
  )
  implicit class ControlInvocationLens[UpperPB](_l: _root_.scalapb.lenses.Lens[UpperPB, edu.uci.ics.amber.engine.architecture.rpc.controlcommands.ControlInvocation]) extends _root_.scalapb.lenses.ObjectLens[UpperPB, edu.uci.ics.amber.engine.architecture.rpc.controlcommands.ControlInvocation](_l) {
    def methodName: _root_.scalapb.lenses.Lens[UpperPB, _root_.scala.Predef.String] = field(_.methodName)((c_, f_) => c_.copy(methodName = f_))
    def command: _root_.scalapb.lenses.Lens[UpperPB, edu.uci.ics.amber.engine.architecture.rpc.controlcommands.ControlRequest] = field(_.command)((c_, f_) => c_.copy(command = f_))
    def context: _root_.scalapb.lenses.Lens[UpperPB, edu.uci.ics.amber.engine.architecture.rpc.controlcommands.AsyncRPCContext] = field(_.context)((c_, f_) => c_.copy(context = f_))
    def commandId: _root_.scalapb.lenses.Lens[UpperPB, _root_.scala.Long] = field(_.commandId)((c_, f_) => c_.copy(commandId = f_))
  }
  final val METHODNAME_FIELD_NUMBER = 1
  final val COMMAND_FIELD_NUMBER = 2
  final val CONTEXT_FIELD_NUMBER = 3
  final val COMMANDID_FIELD_NUMBER = 4
  @transient
  private[controlcommands] val _typemapper_command: _root_.scalapb.TypeMapper[edu.uci.ics.amber.engine.architecture.rpc.controlcommands.ControlRequestMessage, edu.uci.ics.amber.engine.architecture.rpc.controlcommands.ControlRequest] = implicitly[_root_.scalapb.TypeMapper[edu.uci.ics.amber.engine.architecture.rpc.controlcommands.ControlRequestMessage, edu.uci.ics.amber.engine.architecture.rpc.controlcommands.ControlRequest]]
  def of(
    methodName: _root_.scala.Predef.String,
    command: edu.uci.ics.amber.engine.architecture.rpc.controlcommands.ControlRequest,
    context: edu.uci.ics.amber.engine.architecture.rpc.controlcommands.AsyncRPCContext,
    commandId: _root_.scala.Long
  ): _root_.edu.uci.ics.amber.engine.architecture.rpc.controlcommands.ControlInvocation = _root_.edu.uci.ics.amber.engine.architecture.rpc.controlcommands.ControlInvocation(
    methodName,
    command,
    context,
    commandId
  )
  // @@protoc_insertion_point(GeneratedMessageCompanion[edu.uci.ics.amber.engine.architecture.rpc.ControlInvocation])
}
