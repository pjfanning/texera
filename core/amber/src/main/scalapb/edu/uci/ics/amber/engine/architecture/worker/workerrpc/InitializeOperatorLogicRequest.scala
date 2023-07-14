// Generated by the Scala Plugin for the Protocol Buffer Compiler.
// Do not edit!
//
// Protofile syntax: PROTO3

package edu.uci.ics.amber.engine.architecture.worker.workerrpc

@SerialVersionUID(0L)
final case class InitializeOperatorLogicRequest(
    code: _root_.scala.Predef.String = "",
    isSource: _root_.scala.Boolean = false,
    inputOrdinalMapping: _root_.scala.Seq[edu.uci.ics.amber.engine.architecture.worker.workerrpc.LinkOrdinal] = _root_.scala.Seq.empty,
    outputOrdinalMapping: _root_.scala.Seq[edu.uci.ics.amber.engine.architecture.worker.workerrpc.LinkOrdinal] = _root_.scala.Seq.empty,
    outputSchema: _root_.scala.collection.immutable.Map[_root_.scala.Predef.String, _root_.scala.Predef.String] = _root_.scala.collection.immutable.Map.empty
    ) extends scalapb.GeneratedMessage with scalapb.lenses.Updatable[InitializeOperatorLogicRequest] {
    @transient
    private[this] var __serializedSizeCachedValue: _root_.scala.Int = 0
    private[this] def __computeSerializedValue(): _root_.scala.Int = {
      var __size = 0
      
      {
        val __value = code
        if (!__value.isEmpty) {
          __size += _root_.com.google.protobuf.CodedOutputStream.computeStringSize(1, __value)
        }
      };
      
      {
        val __value = isSource
        if (__value != false) {
          __size += _root_.com.google.protobuf.CodedOutputStream.computeBoolSize(2, __value)
        }
      };
      inputOrdinalMapping.foreach { __item =>
        val __value = __item
        __size += 1 + _root_.com.google.protobuf.CodedOutputStream.computeUInt32SizeNoTag(__value.serializedSize) + __value.serializedSize
      }
      outputOrdinalMapping.foreach { __item =>
        val __value = __item
        __size += 1 + _root_.com.google.protobuf.CodedOutputStream.computeUInt32SizeNoTag(__value.serializedSize) + __value.serializedSize
      }
      outputSchema.foreach { __item =>
        val __value = edu.uci.ics.amber.engine.architecture.worker.workerrpc.InitializeOperatorLogicRequest._typemapper_outputSchema.toBase(__item)
        __size += 1 + _root_.com.google.protobuf.CodedOutputStream.computeUInt32SizeNoTag(__value.serializedSize) + __value.serializedSize
      }
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
        val __v = code
        if (!__v.isEmpty) {
          _output__.writeString(1, __v)
        }
      };
      {
        val __v = isSource
        if (__v != false) {
          _output__.writeBool(2, __v)
        }
      };
      inputOrdinalMapping.foreach { __v =>
        val __m = __v
        _output__.writeTag(3, 2)
        _output__.writeUInt32NoTag(__m.serializedSize)
        __m.writeTo(_output__)
      };
      outputOrdinalMapping.foreach { __v =>
        val __m = __v
        _output__.writeTag(4, 2)
        _output__.writeUInt32NoTag(__m.serializedSize)
        __m.writeTo(_output__)
      };
      outputSchema.foreach { __v =>
        val __m = edu.uci.ics.amber.engine.architecture.worker.workerrpc.InitializeOperatorLogicRequest._typemapper_outputSchema.toBase(__v)
        _output__.writeTag(5, 2)
        _output__.writeUInt32NoTag(__m.serializedSize)
        __m.writeTo(_output__)
      };
    }
    def withCode(__v: _root_.scala.Predef.String): InitializeOperatorLogicRequest = copy(code = __v)
    def withIsSource(__v: _root_.scala.Boolean): InitializeOperatorLogicRequest = copy(isSource = __v)
    def clearInputOrdinalMapping = copy(inputOrdinalMapping = _root_.scala.Seq.empty)
    def addInputOrdinalMapping(__vs: edu.uci.ics.amber.engine.architecture.worker.workerrpc.LinkOrdinal*): InitializeOperatorLogicRequest = addAllInputOrdinalMapping(__vs)
    def addAllInputOrdinalMapping(__vs: Iterable[edu.uci.ics.amber.engine.architecture.worker.workerrpc.LinkOrdinal]): InitializeOperatorLogicRequest = copy(inputOrdinalMapping = inputOrdinalMapping ++ __vs)
    def withInputOrdinalMapping(__v: _root_.scala.Seq[edu.uci.ics.amber.engine.architecture.worker.workerrpc.LinkOrdinal]): InitializeOperatorLogicRequest = copy(inputOrdinalMapping = __v)
    def clearOutputOrdinalMapping = copy(outputOrdinalMapping = _root_.scala.Seq.empty)
    def addOutputOrdinalMapping(__vs: edu.uci.ics.amber.engine.architecture.worker.workerrpc.LinkOrdinal*): InitializeOperatorLogicRequest = addAllOutputOrdinalMapping(__vs)
    def addAllOutputOrdinalMapping(__vs: Iterable[edu.uci.ics.amber.engine.architecture.worker.workerrpc.LinkOrdinal]): InitializeOperatorLogicRequest = copy(outputOrdinalMapping = outputOrdinalMapping ++ __vs)
    def withOutputOrdinalMapping(__v: _root_.scala.Seq[edu.uci.ics.amber.engine.architecture.worker.workerrpc.LinkOrdinal]): InitializeOperatorLogicRequest = copy(outputOrdinalMapping = __v)
    def clearOutputSchema = copy(outputSchema = _root_.scala.collection.immutable.Map.empty)
    def addOutputSchema(__vs: (_root_.scala.Predef.String, _root_.scala.Predef.String)*): InitializeOperatorLogicRequest = addAllOutputSchema(__vs)
    def addAllOutputSchema(__vs: Iterable[(_root_.scala.Predef.String, _root_.scala.Predef.String)]): InitializeOperatorLogicRequest = copy(outputSchema = outputSchema ++ __vs)
    def withOutputSchema(__v: _root_.scala.collection.immutable.Map[_root_.scala.Predef.String, _root_.scala.Predef.String]): InitializeOperatorLogicRequest = copy(outputSchema = __v)
    def getFieldByNumber(__fieldNumber: _root_.scala.Int): _root_.scala.Any = {
      (__fieldNumber: @_root_.scala.unchecked) match {
        case 1 => {
          val __t = code
          if (__t != "") __t else null
        }
        case 2 => {
          val __t = isSource
          if (__t != false) __t else null
        }
        case 3 => inputOrdinalMapping
        case 4 => outputOrdinalMapping
        case 5 => outputSchema.iterator.map(edu.uci.ics.amber.engine.architecture.worker.workerrpc.InitializeOperatorLogicRequest._typemapper_outputSchema.toBase(_)).toSeq
      }
    }
    def getField(__field: _root_.scalapb.descriptors.FieldDescriptor): _root_.scalapb.descriptors.PValue = {
      _root_.scala.Predef.require(__field.containingMessage eq companion.scalaDescriptor)
      (__field.number: @_root_.scala.unchecked) match {
        case 1 => _root_.scalapb.descriptors.PString(code)
        case 2 => _root_.scalapb.descriptors.PBoolean(isSource)
        case 3 => _root_.scalapb.descriptors.PRepeated(inputOrdinalMapping.iterator.map(_.toPMessage).toVector)
        case 4 => _root_.scalapb.descriptors.PRepeated(outputOrdinalMapping.iterator.map(_.toPMessage).toVector)
        case 5 => _root_.scalapb.descriptors.PRepeated(outputSchema.iterator.map(edu.uci.ics.amber.engine.architecture.worker.workerrpc.InitializeOperatorLogicRequest._typemapper_outputSchema.toBase(_).toPMessage).toVector)
      }
    }
    def toProtoString: _root_.scala.Predef.String = _root_.scalapb.TextFormat.printToSingleLineUnicodeString(this)
    def companion = edu.uci.ics.amber.engine.architecture.worker.workerrpc.InitializeOperatorLogicRequest
    // @@protoc_insertion_point(GeneratedMessage[edu.uci.ics.amber.engine.architecture.worker.InitializeOperatorLogicRequest])
}

object InitializeOperatorLogicRequest extends scalapb.GeneratedMessageCompanion[edu.uci.ics.amber.engine.architecture.worker.workerrpc.InitializeOperatorLogicRequest] {
  implicit def messageCompanion: scalapb.GeneratedMessageCompanion[edu.uci.ics.amber.engine.architecture.worker.workerrpc.InitializeOperatorLogicRequest] = this
  def parseFrom(`_input__`: _root_.com.google.protobuf.CodedInputStream): edu.uci.ics.amber.engine.architecture.worker.workerrpc.InitializeOperatorLogicRequest = {
    var __code: _root_.scala.Predef.String = ""
    var __isSource: _root_.scala.Boolean = false
    val __inputOrdinalMapping: _root_.scala.collection.immutable.VectorBuilder[edu.uci.ics.amber.engine.architecture.worker.workerrpc.LinkOrdinal] = new _root_.scala.collection.immutable.VectorBuilder[edu.uci.ics.amber.engine.architecture.worker.workerrpc.LinkOrdinal]
    val __outputOrdinalMapping: _root_.scala.collection.immutable.VectorBuilder[edu.uci.ics.amber.engine.architecture.worker.workerrpc.LinkOrdinal] = new _root_.scala.collection.immutable.VectorBuilder[edu.uci.ics.amber.engine.architecture.worker.workerrpc.LinkOrdinal]
    val __outputSchema: _root_.scala.collection.mutable.Builder[(_root_.scala.Predef.String, _root_.scala.Predef.String), _root_.scala.collection.immutable.Map[_root_.scala.Predef.String, _root_.scala.Predef.String]] = _root_.scala.collection.immutable.Map.newBuilder[_root_.scala.Predef.String, _root_.scala.Predef.String]
    var _done__ = false
    while (!_done__) {
      val _tag__ = _input__.readTag()
      _tag__ match {
        case 0 => _done__ = true
        case 10 =>
          __code = _input__.readStringRequireUtf8()
        case 16 =>
          __isSource = _input__.readBool()
        case 26 =>
          __inputOrdinalMapping += _root_.scalapb.LiteParser.readMessage[edu.uci.ics.amber.engine.architecture.worker.workerrpc.LinkOrdinal](_input__)
        case 34 =>
          __outputOrdinalMapping += _root_.scalapb.LiteParser.readMessage[edu.uci.ics.amber.engine.architecture.worker.workerrpc.LinkOrdinal](_input__)
        case 42 =>
          __outputSchema += edu.uci.ics.amber.engine.architecture.worker.workerrpc.InitializeOperatorLogicRequest._typemapper_outputSchema.toCustom(_root_.scalapb.LiteParser.readMessage[edu.uci.ics.amber.engine.architecture.worker.workerrpc.InitializeOperatorLogicRequest.OutputSchemaEntry](_input__))
        case tag => _input__.skipField(tag)
      }
    }
    edu.uci.ics.amber.engine.architecture.worker.workerrpc.InitializeOperatorLogicRequest(
        code = __code,
        isSource = __isSource,
        inputOrdinalMapping = __inputOrdinalMapping.result(),
        outputOrdinalMapping = __outputOrdinalMapping.result(),
        outputSchema = __outputSchema.result()
    )
  }
  implicit def messageReads: _root_.scalapb.descriptors.Reads[edu.uci.ics.amber.engine.architecture.worker.workerrpc.InitializeOperatorLogicRequest] = _root_.scalapb.descriptors.Reads{
    case _root_.scalapb.descriptors.PMessage(__fieldsMap) =>
      _root_.scala.Predef.require(__fieldsMap.keys.forall(_.containingMessage eq scalaDescriptor), "FieldDescriptor does not match message type.")
      edu.uci.ics.amber.engine.architecture.worker.workerrpc.InitializeOperatorLogicRequest(
        code = __fieldsMap.get(scalaDescriptor.findFieldByNumber(1).get).map(_.as[_root_.scala.Predef.String]).getOrElse(""),
        isSource = __fieldsMap.get(scalaDescriptor.findFieldByNumber(2).get).map(_.as[_root_.scala.Boolean]).getOrElse(false),
        inputOrdinalMapping = __fieldsMap.get(scalaDescriptor.findFieldByNumber(3).get).map(_.as[_root_.scala.Seq[edu.uci.ics.amber.engine.architecture.worker.workerrpc.LinkOrdinal]]).getOrElse(_root_.scala.Seq.empty),
        outputOrdinalMapping = __fieldsMap.get(scalaDescriptor.findFieldByNumber(4).get).map(_.as[_root_.scala.Seq[edu.uci.ics.amber.engine.architecture.worker.workerrpc.LinkOrdinal]]).getOrElse(_root_.scala.Seq.empty),
        outputSchema = __fieldsMap.get(scalaDescriptor.findFieldByNumber(5).get).map(_.as[_root_.scala.Seq[edu.uci.ics.amber.engine.architecture.worker.workerrpc.InitializeOperatorLogicRequest.OutputSchemaEntry]]).getOrElse(_root_.scala.Seq.empty).iterator.map(edu.uci.ics.amber.engine.architecture.worker.workerrpc.InitializeOperatorLogicRequest._typemapper_outputSchema.toCustom(_)).toMap
      )
    case _ => throw new RuntimeException("Expected PMessage")
  }
  def javaDescriptor: _root_.com.google.protobuf.Descriptors.Descriptor = WorkerrpcProto.javaDescriptor.getMessageTypes().get(42)
  def scalaDescriptor: _root_.scalapb.descriptors.Descriptor = WorkerrpcProto.scalaDescriptor.messages(42)
  def messageCompanionForFieldNumber(__number: _root_.scala.Int): _root_.scalapb.GeneratedMessageCompanion[_] = {
    var __out: _root_.scalapb.GeneratedMessageCompanion[_] = null
    (__number: @_root_.scala.unchecked) match {
      case 3 => __out = edu.uci.ics.amber.engine.architecture.worker.workerrpc.LinkOrdinal
      case 4 => __out = edu.uci.ics.amber.engine.architecture.worker.workerrpc.LinkOrdinal
      case 5 => __out = edu.uci.ics.amber.engine.architecture.worker.workerrpc.InitializeOperatorLogicRequest.OutputSchemaEntry
    }
    __out
  }
  lazy val nestedMessagesCompanions: Seq[_root_.scalapb.GeneratedMessageCompanion[_ <: _root_.scalapb.GeneratedMessage]] =
    Seq[_root_.scalapb.GeneratedMessageCompanion[_ <: _root_.scalapb.GeneratedMessage]](
      _root_.edu.uci.ics.amber.engine.architecture.worker.workerrpc.InitializeOperatorLogicRequest.OutputSchemaEntry
    )
  def enumCompanionForFieldNumber(__fieldNumber: _root_.scala.Int): _root_.scalapb.GeneratedEnumCompanion[_] = throw new MatchError(__fieldNumber)
  lazy val defaultInstance = edu.uci.ics.amber.engine.architecture.worker.workerrpc.InitializeOperatorLogicRequest(
    code = "",
    isSource = false,
    inputOrdinalMapping = _root_.scala.Seq.empty,
    outputOrdinalMapping = _root_.scala.Seq.empty,
    outputSchema = _root_.scala.collection.immutable.Map.empty
  )
  @SerialVersionUID(0L)
  final case class OutputSchemaEntry(
      key: _root_.scala.Predef.String = "",
      value: _root_.scala.Predef.String = ""
      ) extends scalapb.GeneratedMessage with scalapb.lenses.Updatable[OutputSchemaEntry] {
      @transient
      private[this] var __serializedSizeCachedValue: _root_.scala.Int = 0
      private[this] def __computeSerializedValue(): _root_.scala.Int = {
        var __size = 0
        
        {
          val __value = key
          if (!__value.isEmpty) {
            __size += _root_.com.google.protobuf.CodedOutputStream.computeStringSize(1, __value)
          }
        };
        
        {
          val __value = value
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
          val __v = key
          if (!__v.isEmpty) {
            _output__.writeString(1, __v)
          }
        };
        {
          val __v = value
          if (!__v.isEmpty) {
            _output__.writeString(2, __v)
          }
        };
      }
      def withKey(__v: _root_.scala.Predef.String): OutputSchemaEntry = copy(key = __v)
      def withValue(__v: _root_.scala.Predef.String): OutputSchemaEntry = copy(value = __v)
      def getFieldByNumber(__fieldNumber: _root_.scala.Int): _root_.scala.Any = {
        (__fieldNumber: @_root_.scala.unchecked) match {
          case 1 => {
            val __t = key
            if (__t != "") __t else null
          }
          case 2 => {
            val __t = value
            if (__t != "") __t else null
          }
        }
      }
      def getField(__field: _root_.scalapb.descriptors.FieldDescriptor): _root_.scalapb.descriptors.PValue = {
        _root_.scala.Predef.require(__field.containingMessage eq companion.scalaDescriptor)
        (__field.number: @_root_.scala.unchecked) match {
          case 1 => _root_.scalapb.descriptors.PString(key)
          case 2 => _root_.scalapb.descriptors.PString(value)
        }
      }
      def toProtoString: _root_.scala.Predef.String = _root_.scalapb.TextFormat.printToSingleLineUnicodeString(this)
      def companion = edu.uci.ics.amber.engine.architecture.worker.workerrpc.InitializeOperatorLogicRequest.OutputSchemaEntry
      // @@protoc_insertion_point(GeneratedMessage[edu.uci.ics.amber.engine.architecture.worker.InitializeOperatorLogicRequest.OutputSchemaEntry])
  }
  
  object OutputSchemaEntry extends scalapb.GeneratedMessageCompanion[edu.uci.ics.amber.engine.architecture.worker.workerrpc.InitializeOperatorLogicRequest.OutputSchemaEntry] {
    implicit def messageCompanion: scalapb.GeneratedMessageCompanion[edu.uci.ics.amber.engine.architecture.worker.workerrpc.InitializeOperatorLogicRequest.OutputSchemaEntry] = this
    def parseFrom(`_input__`: _root_.com.google.protobuf.CodedInputStream): edu.uci.ics.amber.engine.architecture.worker.workerrpc.InitializeOperatorLogicRequest.OutputSchemaEntry = {
      var __key: _root_.scala.Predef.String = ""
      var __value: _root_.scala.Predef.String = ""
      var _done__ = false
      while (!_done__) {
        val _tag__ = _input__.readTag()
        _tag__ match {
          case 0 => _done__ = true
          case 10 =>
            __key = _input__.readStringRequireUtf8()
          case 18 =>
            __value = _input__.readStringRequireUtf8()
          case tag => _input__.skipField(tag)
        }
      }
      edu.uci.ics.amber.engine.architecture.worker.workerrpc.InitializeOperatorLogicRequest.OutputSchemaEntry(
          key = __key,
          value = __value
      )
    }
    implicit def messageReads: _root_.scalapb.descriptors.Reads[edu.uci.ics.amber.engine.architecture.worker.workerrpc.InitializeOperatorLogicRequest.OutputSchemaEntry] = _root_.scalapb.descriptors.Reads{
      case _root_.scalapb.descriptors.PMessage(__fieldsMap) =>
        _root_.scala.Predef.require(__fieldsMap.keys.forall(_.containingMessage eq scalaDescriptor), "FieldDescriptor does not match message type.")
        edu.uci.ics.amber.engine.architecture.worker.workerrpc.InitializeOperatorLogicRequest.OutputSchemaEntry(
          key = __fieldsMap.get(scalaDescriptor.findFieldByNumber(1).get).map(_.as[_root_.scala.Predef.String]).getOrElse(""),
          value = __fieldsMap.get(scalaDescriptor.findFieldByNumber(2).get).map(_.as[_root_.scala.Predef.String]).getOrElse("")
        )
      case _ => throw new RuntimeException("Expected PMessage")
    }
    def javaDescriptor: _root_.com.google.protobuf.Descriptors.Descriptor = edu.uci.ics.amber.engine.architecture.worker.workerrpc.InitializeOperatorLogicRequest.javaDescriptor.getNestedTypes().get(0)
    def scalaDescriptor: _root_.scalapb.descriptors.Descriptor = edu.uci.ics.amber.engine.architecture.worker.workerrpc.InitializeOperatorLogicRequest.scalaDescriptor.nestedMessages(0)
    def messageCompanionForFieldNumber(__number: _root_.scala.Int): _root_.scalapb.GeneratedMessageCompanion[_] = throw new MatchError(__number)
    lazy val nestedMessagesCompanions: Seq[_root_.scalapb.GeneratedMessageCompanion[_ <: _root_.scalapb.GeneratedMessage]] = Seq.empty
    def enumCompanionForFieldNumber(__fieldNumber: _root_.scala.Int): _root_.scalapb.GeneratedEnumCompanion[_] = throw new MatchError(__fieldNumber)
    lazy val defaultInstance = edu.uci.ics.amber.engine.architecture.worker.workerrpc.InitializeOperatorLogicRequest.OutputSchemaEntry(
      key = "",
      value = ""
    )
    implicit class OutputSchemaEntryLens[UpperPB](_l: _root_.scalapb.lenses.Lens[UpperPB, edu.uci.ics.amber.engine.architecture.worker.workerrpc.InitializeOperatorLogicRequest.OutputSchemaEntry]) extends _root_.scalapb.lenses.ObjectLens[UpperPB, edu.uci.ics.amber.engine.architecture.worker.workerrpc.InitializeOperatorLogicRequest.OutputSchemaEntry](_l) {
      def key: _root_.scalapb.lenses.Lens[UpperPB, _root_.scala.Predef.String] = field(_.key)((c_, f_) => c_.copy(key = f_))
      def value: _root_.scalapb.lenses.Lens[UpperPB, _root_.scala.Predef.String] = field(_.value)((c_, f_) => c_.copy(value = f_))
    }
    final val KEY_FIELD_NUMBER = 1
    final val VALUE_FIELD_NUMBER = 2
    @transient
    implicit val keyValueMapper: _root_.scalapb.TypeMapper[edu.uci.ics.amber.engine.architecture.worker.workerrpc.InitializeOperatorLogicRequest.OutputSchemaEntry, (_root_.scala.Predef.String, _root_.scala.Predef.String)] =
      _root_.scalapb.TypeMapper[edu.uci.ics.amber.engine.architecture.worker.workerrpc.InitializeOperatorLogicRequest.OutputSchemaEntry, (_root_.scala.Predef.String, _root_.scala.Predef.String)](__m => (__m.key, __m.value))(__p => edu.uci.ics.amber.engine.architecture.worker.workerrpc.InitializeOperatorLogicRequest.OutputSchemaEntry(__p._1, __p._2))
    def of(
      key: _root_.scala.Predef.String,
      value: _root_.scala.Predef.String
    ): _root_.edu.uci.ics.amber.engine.architecture.worker.workerrpc.InitializeOperatorLogicRequest.OutputSchemaEntry = _root_.edu.uci.ics.amber.engine.architecture.worker.workerrpc.InitializeOperatorLogicRequest.OutputSchemaEntry(
      key,
      value
    )
    // @@protoc_insertion_point(GeneratedMessageCompanion[edu.uci.ics.amber.engine.architecture.worker.InitializeOperatorLogicRequest.OutputSchemaEntry])
  }
  
  implicit class InitializeOperatorLogicRequestLens[UpperPB](_l: _root_.scalapb.lenses.Lens[UpperPB, edu.uci.ics.amber.engine.architecture.worker.workerrpc.InitializeOperatorLogicRequest]) extends _root_.scalapb.lenses.ObjectLens[UpperPB, edu.uci.ics.amber.engine.architecture.worker.workerrpc.InitializeOperatorLogicRequest](_l) {
    def code: _root_.scalapb.lenses.Lens[UpperPB, _root_.scala.Predef.String] = field(_.code)((c_, f_) => c_.copy(code = f_))
    def isSource: _root_.scalapb.lenses.Lens[UpperPB, _root_.scala.Boolean] = field(_.isSource)((c_, f_) => c_.copy(isSource = f_))
    def inputOrdinalMapping: _root_.scalapb.lenses.Lens[UpperPB, _root_.scala.Seq[edu.uci.ics.amber.engine.architecture.worker.workerrpc.LinkOrdinal]] = field(_.inputOrdinalMapping)((c_, f_) => c_.copy(inputOrdinalMapping = f_))
    def outputOrdinalMapping: _root_.scalapb.lenses.Lens[UpperPB, _root_.scala.Seq[edu.uci.ics.amber.engine.architecture.worker.workerrpc.LinkOrdinal]] = field(_.outputOrdinalMapping)((c_, f_) => c_.copy(outputOrdinalMapping = f_))
    def outputSchema: _root_.scalapb.lenses.Lens[UpperPB, _root_.scala.collection.immutable.Map[_root_.scala.Predef.String, _root_.scala.Predef.String]] = field(_.outputSchema)((c_, f_) => c_.copy(outputSchema = f_))
  }
  final val CODE_FIELD_NUMBER = 1
  final val IS_SOURCE_FIELD_NUMBER = 2
  final val INPUT_ORDINAL_MAPPING_FIELD_NUMBER = 3
  final val OUTPUT_ORDINAL_MAPPING_FIELD_NUMBER = 4
  final val OUTPUT_SCHEMA_FIELD_NUMBER = 5
  @transient
  private[workerrpc] val _typemapper_outputSchema: _root_.scalapb.TypeMapper[edu.uci.ics.amber.engine.architecture.worker.workerrpc.InitializeOperatorLogicRequest.OutputSchemaEntry, (_root_.scala.Predef.String, _root_.scala.Predef.String)] = implicitly[_root_.scalapb.TypeMapper[edu.uci.ics.amber.engine.architecture.worker.workerrpc.InitializeOperatorLogicRequest.OutputSchemaEntry, (_root_.scala.Predef.String, _root_.scala.Predef.String)]]
  def of(
    code: _root_.scala.Predef.String,
    isSource: _root_.scala.Boolean,
    inputOrdinalMapping: _root_.scala.Seq[edu.uci.ics.amber.engine.architecture.worker.workerrpc.LinkOrdinal],
    outputOrdinalMapping: _root_.scala.Seq[edu.uci.ics.amber.engine.architecture.worker.workerrpc.LinkOrdinal],
    outputSchema: _root_.scala.collection.immutable.Map[_root_.scala.Predef.String, _root_.scala.Predef.String]
  ): _root_.edu.uci.ics.amber.engine.architecture.worker.workerrpc.InitializeOperatorLogicRequest = _root_.edu.uci.ics.amber.engine.architecture.worker.workerrpc.InitializeOperatorLogicRequest(
    code,
    isSource,
    inputOrdinalMapping,
    outputOrdinalMapping,
    outputSchema
  )
  // @@protoc_insertion_point(GeneratedMessageCompanion[edu.uci.ics.amber.engine.architecture.worker.InitializeOperatorLogicRequest])
}
