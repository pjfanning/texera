// Generated by the Scala Plugin for the Protocol Buffer Compiler.
// Do not edit!
//
// Protofile syntax: PROTO3

package edu.uci.ics.texera.web.workflowruntimestate

@SerialVersionUID(0L)
final case class OperatorRuntimeStats(
    state: edu.uci.ics.texera.web.workflowruntimestate.WorkflowAggregatedState = edu.uci.ics.texera.web.workflowruntimestate.WorkflowAggregatedState.UNINITIALIZED,
    inputCount: _root_.scala.Long = 0L,
    outputCount: _root_.scala.Long = 0L,
    workerInfo: _root_.scala.collection.immutable.Map[_root_.scala.Predef.String, edu.uci.ics.texera.web.workflowruntimestate.WorkerRuntimeStats] = _root_.scala.collection.immutable.Map.empty
    ) extends scalapb.GeneratedMessage with scalapb.lenses.Updatable[OperatorRuntimeStats] {
    @transient
    private[this] var __serializedSizeCachedValue: _root_.scala.Int = 0
    private[this] def __computeSerializedValue(): _root_.scala.Int = {
      var __size = 0
      
      {
        val __value = state.value
        if (__value != 0) {
          __size += _root_.com.google.protobuf.CodedOutputStream.computeEnumSize(1, __value)
        }
      };
      
      {
        val __value = inputCount
        if (__value != 0L) {
          __size += _root_.com.google.protobuf.CodedOutputStream.computeInt64Size(2, __value)
        }
      };
      
      {
        val __value = outputCount
        if (__value != 0L) {
          __size += _root_.com.google.protobuf.CodedOutputStream.computeInt64Size(3, __value)
        }
      };
      workerInfo.foreach { __item =>
        val __value = edu.uci.ics.texera.web.workflowruntimestate.OperatorRuntimeStats._typemapper_workerInfo.toBase(__item)
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
        val __v = state.value
        if (__v != 0) {
          _output__.writeEnum(1, __v)
        }
      };
      {
        val __v = inputCount
        if (__v != 0L) {
          _output__.writeInt64(2, __v)
        }
      };
      {
        val __v = outputCount
        if (__v != 0L) {
          _output__.writeInt64(3, __v)
        }
      };
      workerInfo.foreach { __v =>
        val __m = edu.uci.ics.texera.web.workflowruntimestate.OperatorRuntimeStats._typemapper_workerInfo.toBase(__v)
        _output__.writeTag(4, 2)
        _output__.writeUInt32NoTag(__m.serializedSize)
        __m.writeTo(_output__)
      };
    }
    def withState(__v: edu.uci.ics.texera.web.workflowruntimestate.WorkflowAggregatedState): OperatorRuntimeStats = copy(state = __v)
    def withInputCount(__v: _root_.scala.Long): OperatorRuntimeStats = copy(inputCount = __v)
    def withOutputCount(__v: _root_.scala.Long): OperatorRuntimeStats = copy(outputCount = __v)
    def clearWorkerInfo = copy(workerInfo = _root_.scala.collection.immutable.Map.empty)
    def addWorkerInfo(__vs: (_root_.scala.Predef.String, edu.uci.ics.texera.web.workflowruntimestate.WorkerRuntimeStats)*): OperatorRuntimeStats = addAllWorkerInfo(__vs)
    def addAllWorkerInfo(__vs: Iterable[(_root_.scala.Predef.String, edu.uci.ics.texera.web.workflowruntimestate.WorkerRuntimeStats)]): OperatorRuntimeStats = copy(workerInfo = workerInfo ++ __vs)
    def withWorkerInfo(__v: _root_.scala.collection.immutable.Map[_root_.scala.Predef.String, edu.uci.ics.texera.web.workflowruntimestate.WorkerRuntimeStats]): OperatorRuntimeStats = copy(workerInfo = __v)
    def getFieldByNumber(__fieldNumber: _root_.scala.Int): _root_.scala.Any = {
      (__fieldNumber: @_root_.scala.unchecked) match {
        case 1 => {
          val __t = state.javaValueDescriptor
          if (__t.getNumber() != 0) __t else null
        }
        case 2 => {
          val __t = inputCount
          if (__t != 0L) __t else null
        }
        case 3 => {
          val __t = outputCount
          if (__t != 0L) __t else null
        }
        case 4 => workerInfo.iterator.map(edu.uci.ics.texera.web.workflowruntimestate.OperatorRuntimeStats._typemapper_workerInfo.toBase(_)).toSeq
      }
    }
    def getField(__field: _root_.scalapb.descriptors.FieldDescriptor): _root_.scalapb.descriptors.PValue = {
      _root_.scala.Predef.require(__field.containingMessage eq companion.scalaDescriptor)
      (__field.number: @_root_.scala.unchecked) match {
        case 1 => _root_.scalapb.descriptors.PEnum(state.scalaValueDescriptor)
        case 2 => _root_.scalapb.descriptors.PLong(inputCount)
        case 3 => _root_.scalapb.descriptors.PLong(outputCount)
        case 4 => _root_.scalapb.descriptors.PRepeated(workerInfo.iterator.map(edu.uci.ics.texera.web.workflowruntimestate.OperatorRuntimeStats._typemapper_workerInfo.toBase(_).toPMessage).toVector)
      }
    }
    def toProtoString: _root_.scala.Predef.String = _root_.scalapb.TextFormat.printToSingleLineUnicodeString(this)
    def companion = edu.uci.ics.texera.web.workflowruntimestate.OperatorRuntimeStats
    // @@protoc_insertion_point(GeneratedMessage[edu.uci.ics.texera.web.OperatorRuntimeStats])
}

object OperatorRuntimeStats extends scalapb.GeneratedMessageCompanion[edu.uci.ics.texera.web.workflowruntimestate.OperatorRuntimeStats] {
  implicit def messageCompanion: scalapb.GeneratedMessageCompanion[edu.uci.ics.texera.web.workflowruntimestate.OperatorRuntimeStats] = this
  def parseFrom(`_input__`: _root_.com.google.protobuf.CodedInputStream): edu.uci.ics.texera.web.workflowruntimestate.OperatorRuntimeStats = {
    var __state: edu.uci.ics.texera.web.workflowruntimestate.WorkflowAggregatedState = edu.uci.ics.texera.web.workflowruntimestate.WorkflowAggregatedState.UNINITIALIZED
    var __inputCount: _root_.scala.Long = 0L
    var __outputCount: _root_.scala.Long = 0L
    val __workerInfo: _root_.scala.collection.mutable.Builder[(_root_.scala.Predef.String, edu.uci.ics.texera.web.workflowruntimestate.WorkerRuntimeStats), _root_.scala.collection.immutable.Map[_root_.scala.Predef.String, edu.uci.ics.texera.web.workflowruntimestate.WorkerRuntimeStats]] = _root_.scala.collection.immutable.Map.newBuilder[_root_.scala.Predef.String, edu.uci.ics.texera.web.workflowruntimestate.WorkerRuntimeStats]
    var _done__ = false
    while (!_done__) {
      val _tag__ = _input__.readTag()
      _tag__ match {
        case 0 => _done__ = true
        case 8 =>
          __state = edu.uci.ics.texera.web.workflowruntimestate.WorkflowAggregatedState.fromValue(_input__.readEnum())
        case 16 =>
          __inputCount = _input__.readInt64()
        case 24 =>
          __outputCount = _input__.readInt64()
        case 34 =>
          __workerInfo += edu.uci.ics.texera.web.workflowruntimestate.OperatorRuntimeStats._typemapper_workerInfo.toCustom(_root_.scalapb.LiteParser.readMessage[edu.uci.ics.texera.web.workflowruntimestate.OperatorRuntimeStats.WorkerInfoEntry](_input__))
        case tag => _input__.skipField(tag)
      }
    }
    edu.uci.ics.texera.web.workflowruntimestate.OperatorRuntimeStats(
        state = __state,
        inputCount = __inputCount,
        outputCount = __outputCount,
        workerInfo = __workerInfo.result()
    )
  }
  implicit def messageReads: _root_.scalapb.descriptors.Reads[edu.uci.ics.texera.web.workflowruntimestate.OperatorRuntimeStats] = _root_.scalapb.descriptors.Reads{
    case _root_.scalapb.descriptors.PMessage(__fieldsMap) =>
      _root_.scala.Predef.require(__fieldsMap.keys.forall(_.containingMessage eq scalaDescriptor), "FieldDescriptor does not match message type.")
      edu.uci.ics.texera.web.workflowruntimestate.OperatorRuntimeStats(
        state = edu.uci.ics.texera.web.workflowruntimestate.WorkflowAggregatedState.fromValue(__fieldsMap.get(scalaDescriptor.findFieldByNumber(1).get).map(_.as[_root_.scalapb.descriptors.EnumValueDescriptor]).getOrElse(edu.uci.ics.texera.web.workflowruntimestate.WorkflowAggregatedState.UNINITIALIZED.scalaValueDescriptor).number),
        inputCount = __fieldsMap.get(scalaDescriptor.findFieldByNumber(2).get).map(_.as[_root_.scala.Long]).getOrElse(0L),
        outputCount = __fieldsMap.get(scalaDescriptor.findFieldByNumber(3).get).map(_.as[_root_.scala.Long]).getOrElse(0L),
        workerInfo = __fieldsMap.get(scalaDescriptor.findFieldByNumber(4).get).map(_.as[_root_.scala.Seq[edu.uci.ics.texera.web.workflowruntimestate.OperatorRuntimeStats.WorkerInfoEntry]]).getOrElse(_root_.scala.Seq.empty).iterator.map(edu.uci.ics.texera.web.workflowruntimestate.OperatorRuntimeStats._typemapper_workerInfo.toCustom(_)).toMap
      )
    case _ => throw new RuntimeException("Expected PMessage")
  }
  def javaDescriptor: _root_.com.google.protobuf.Descriptors.Descriptor = WorkflowruntimestateProto.javaDescriptor.getMessageTypes().get(7)
  def scalaDescriptor: _root_.scalapb.descriptors.Descriptor = WorkflowruntimestateProto.scalaDescriptor.messages(7)
  def messageCompanionForFieldNumber(__number: _root_.scala.Int): _root_.scalapb.GeneratedMessageCompanion[_] = {
    var __out: _root_.scalapb.GeneratedMessageCompanion[_] = null
    (__number: @_root_.scala.unchecked) match {
      case 4 => __out = edu.uci.ics.texera.web.workflowruntimestate.OperatorRuntimeStats.WorkerInfoEntry
    }
    __out
  }
  lazy val nestedMessagesCompanions: Seq[_root_.scalapb.GeneratedMessageCompanion[_ <: _root_.scalapb.GeneratedMessage]] =
    Seq[_root_.scalapb.GeneratedMessageCompanion[_ <: _root_.scalapb.GeneratedMessage]](
      _root_.edu.uci.ics.texera.web.workflowruntimestate.OperatorRuntimeStats.WorkerInfoEntry
    )
  def enumCompanionForFieldNumber(__fieldNumber: _root_.scala.Int): _root_.scalapb.GeneratedEnumCompanion[_] = {
    (__fieldNumber: @_root_.scala.unchecked) match {
      case 1 => edu.uci.ics.texera.web.workflowruntimestate.WorkflowAggregatedState
    }
  }
  lazy val defaultInstance = edu.uci.ics.texera.web.workflowruntimestate.OperatorRuntimeStats(
    state = edu.uci.ics.texera.web.workflowruntimestate.WorkflowAggregatedState.UNINITIALIZED,
    inputCount = 0L,
    outputCount = 0L,
    workerInfo = _root_.scala.collection.immutable.Map.empty
  )
  @SerialVersionUID(0L)
  final case class WorkerInfoEntry(
      key: _root_.scala.Predef.String = "",
      value: _root_.scala.Option[edu.uci.ics.texera.web.workflowruntimestate.WorkerRuntimeStats] = _root_.scala.None
      ) extends scalapb.GeneratedMessage with scalapb.lenses.Updatable[WorkerInfoEntry] {
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
        if (value.isDefined) {
          val __value = value.get
          __size += 1 + _root_.com.google.protobuf.CodedOutputStream.computeUInt32SizeNoTag(__value.serializedSize) + __value.serializedSize
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
        value.foreach { __v =>
          val __m = __v
          _output__.writeTag(2, 2)
          _output__.writeUInt32NoTag(__m.serializedSize)
          __m.writeTo(_output__)
        };
      }
      def withKey(__v: _root_.scala.Predef.String): WorkerInfoEntry = copy(key = __v)
      def getValue: edu.uci.ics.texera.web.workflowruntimestate.WorkerRuntimeStats = value.getOrElse(edu.uci.ics.texera.web.workflowruntimestate.WorkerRuntimeStats.defaultInstance)
      def clearValue: WorkerInfoEntry = copy(value = _root_.scala.None)
      def withValue(__v: edu.uci.ics.texera.web.workflowruntimestate.WorkerRuntimeStats): WorkerInfoEntry = copy(value = Option(__v))
      def getFieldByNumber(__fieldNumber: _root_.scala.Int): _root_.scala.Any = {
        (__fieldNumber: @_root_.scala.unchecked) match {
          case 1 => {
            val __t = key
            if (__t != "") __t else null
          }
          case 2 => value.orNull
        }
      }
      def getField(__field: _root_.scalapb.descriptors.FieldDescriptor): _root_.scalapb.descriptors.PValue = {
        _root_.scala.Predef.require(__field.containingMessage eq companion.scalaDescriptor)
        (__field.number: @_root_.scala.unchecked) match {
          case 1 => _root_.scalapb.descriptors.PString(key)
          case 2 => value.map(_.toPMessage).getOrElse(_root_.scalapb.descriptors.PEmpty)
        }
      }
      def toProtoString: _root_.scala.Predef.String = _root_.scalapb.TextFormat.printToSingleLineUnicodeString(this)
      def companion = edu.uci.ics.texera.web.workflowruntimestate.OperatorRuntimeStats.WorkerInfoEntry
      // @@protoc_insertion_point(GeneratedMessage[edu.uci.ics.texera.web.OperatorRuntimeStats.WorkerInfoEntry])
  }
  
  object WorkerInfoEntry extends scalapb.GeneratedMessageCompanion[edu.uci.ics.texera.web.workflowruntimestate.OperatorRuntimeStats.WorkerInfoEntry] {
    implicit def messageCompanion: scalapb.GeneratedMessageCompanion[edu.uci.ics.texera.web.workflowruntimestate.OperatorRuntimeStats.WorkerInfoEntry] = this
    def parseFrom(`_input__`: _root_.com.google.protobuf.CodedInputStream): edu.uci.ics.texera.web.workflowruntimestate.OperatorRuntimeStats.WorkerInfoEntry = {
      var __key: _root_.scala.Predef.String = ""
      var __value: _root_.scala.Option[edu.uci.ics.texera.web.workflowruntimestate.WorkerRuntimeStats] = _root_.scala.None
      var _done__ = false
      while (!_done__) {
        val _tag__ = _input__.readTag()
        _tag__ match {
          case 0 => _done__ = true
          case 10 =>
            __key = _input__.readStringRequireUtf8()
          case 18 =>
            __value = Option(__value.fold(_root_.scalapb.LiteParser.readMessage[edu.uci.ics.texera.web.workflowruntimestate.WorkerRuntimeStats](_input__))(_root_.scalapb.LiteParser.readMessage(_input__, _)))
          case tag => _input__.skipField(tag)
        }
      }
      edu.uci.ics.texera.web.workflowruntimestate.OperatorRuntimeStats.WorkerInfoEntry(
          key = __key,
          value = __value
      )
    }
    implicit def messageReads: _root_.scalapb.descriptors.Reads[edu.uci.ics.texera.web.workflowruntimestate.OperatorRuntimeStats.WorkerInfoEntry] = _root_.scalapb.descriptors.Reads{
      case _root_.scalapb.descriptors.PMessage(__fieldsMap) =>
        _root_.scala.Predef.require(__fieldsMap.keys.forall(_.containingMessage eq scalaDescriptor), "FieldDescriptor does not match message type.")
        edu.uci.ics.texera.web.workflowruntimestate.OperatorRuntimeStats.WorkerInfoEntry(
          key = __fieldsMap.get(scalaDescriptor.findFieldByNumber(1).get).map(_.as[_root_.scala.Predef.String]).getOrElse(""),
          value = __fieldsMap.get(scalaDescriptor.findFieldByNumber(2).get).flatMap(_.as[_root_.scala.Option[edu.uci.ics.texera.web.workflowruntimestate.WorkerRuntimeStats]])
        )
      case _ => throw new RuntimeException("Expected PMessage")
    }
    def javaDescriptor: _root_.com.google.protobuf.Descriptors.Descriptor = edu.uci.ics.texera.web.workflowruntimestate.OperatorRuntimeStats.javaDescriptor.getNestedTypes().get(0)
    def scalaDescriptor: _root_.scalapb.descriptors.Descriptor = edu.uci.ics.texera.web.workflowruntimestate.OperatorRuntimeStats.scalaDescriptor.nestedMessages(0)
    def messageCompanionForFieldNumber(__number: _root_.scala.Int): _root_.scalapb.GeneratedMessageCompanion[_] = {
      var __out: _root_.scalapb.GeneratedMessageCompanion[_] = null
      (__number: @_root_.scala.unchecked) match {
        case 2 => __out = edu.uci.ics.texera.web.workflowruntimestate.WorkerRuntimeStats
      }
      __out
    }
    lazy val nestedMessagesCompanions: Seq[_root_.scalapb.GeneratedMessageCompanion[_ <: _root_.scalapb.GeneratedMessage]] = Seq.empty
    def enumCompanionForFieldNumber(__fieldNumber: _root_.scala.Int): _root_.scalapb.GeneratedEnumCompanion[_] = throw new MatchError(__fieldNumber)
    lazy val defaultInstance = edu.uci.ics.texera.web.workflowruntimestate.OperatorRuntimeStats.WorkerInfoEntry(
      key = "",
      value = _root_.scala.None
    )
    implicit class WorkerInfoEntryLens[UpperPB](_l: _root_.scalapb.lenses.Lens[UpperPB, edu.uci.ics.texera.web.workflowruntimestate.OperatorRuntimeStats.WorkerInfoEntry]) extends _root_.scalapb.lenses.ObjectLens[UpperPB, edu.uci.ics.texera.web.workflowruntimestate.OperatorRuntimeStats.WorkerInfoEntry](_l) {
      def key: _root_.scalapb.lenses.Lens[UpperPB, _root_.scala.Predef.String] = field(_.key)((c_, f_) => c_.copy(key = f_))
      def value: _root_.scalapb.lenses.Lens[UpperPB, edu.uci.ics.texera.web.workflowruntimestate.WorkerRuntimeStats] = field(_.getValue)((c_, f_) => c_.copy(value = Option(f_)))
      def optionalValue: _root_.scalapb.lenses.Lens[UpperPB, _root_.scala.Option[edu.uci.ics.texera.web.workflowruntimestate.WorkerRuntimeStats]] = field(_.value)((c_, f_) => c_.copy(value = f_))
    }
    final val KEY_FIELD_NUMBER = 1
    final val VALUE_FIELD_NUMBER = 2
    @transient
    implicit val keyValueMapper: _root_.scalapb.TypeMapper[edu.uci.ics.texera.web.workflowruntimestate.OperatorRuntimeStats.WorkerInfoEntry, (_root_.scala.Predef.String, edu.uci.ics.texera.web.workflowruntimestate.WorkerRuntimeStats)] =
      _root_.scalapb.TypeMapper[edu.uci.ics.texera.web.workflowruntimestate.OperatorRuntimeStats.WorkerInfoEntry, (_root_.scala.Predef.String, edu.uci.ics.texera.web.workflowruntimestate.WorkerRuntimeStats)](__m => (__m.key, __m.getValue))(__p => edu.uci.ics.texera.web.workflowruntimestate.OperatorRuntimeStats.WorkerInfoEntry(__p._1, Some(__p._2)))
    def of(
      key: _root_.scala.Predef.String,
      value: _root_.scala.Option[edu.uci.ics.texera.web.workflowruntimestate.WorkerRuntimeStats]
    ): _root_.edu.uci.ics.texera.web.workflowruntimestate.OperatorRuntimeStats.WorkerInfoEntry = _root_.edu.uci.ics.texera.web.workflowruntimestate.OperatorRuntimeStats.WorkerInfoEntry(
      key,
      value
    )
    // @@protoc_insertion_point(GeneratedMessageCompanion[edu.uci.ics.texera.web.OperatorRuntimeStats.WorkerInfoEntry])
  }
  
  implicit class OperatorRuntimeStatsLens[UpperPB](_l: _root_.scalapb.lenses.Lens[UpperPB, edu.uci.ics.texera.web.workflowruntimestate.OperatorRuntimeStats]) extends _root_.scalapb.lenses.ObjectLens[UpperPB, edu.uci.ics.texera.web.workflowruntimestate.OperatorRuntimeStats](_l) {
    def state: _root_.scalapb.lenses.Lens[UpperPB, edu.uci.ics.texera.web.workflowruntimestate.WorkflowAggregatedState] = field(_.state)((c_, f_) => c_.copy(state = f_))
    def inputCount: _root_.scalapb.lenses.Lens[UpperPB, _root_.scala.Long] = field(_.inputCount)((c_, f_) => c_.copy(inputCount = f_))
    def outputCount: _root_.scalapb.lenses.Lens[UpperPB, _root_.scala.Long] = field(_.outputCount)((c_, f_) => c_.copy(outputCount = f_))
    def workerInfo: _root_.scalapb.lenses.Lens[UpperPB, _root_.scala.collection.immutable.Map[_root_.scala.Predef.String, edu.uci.ics.texera.web.workflowruntimestate.WorkerRuntimeStats]] = field(_.workerInfo)((c_, f_) => c_.copy(workerInfo = f_))
  }
  final val STATE_FIELD_NUMBER = 1
  final val INPUT_COUNT_FIELD_NUMBER = 2
  final val OUTPUT_COUNT_FIELD_NUMBER = 3
  final val WORKER_INFO_FIELD_NUMBER = 4
  @transient
  private[workflowruntimestate] val _typemapper_workerInfo: _root_.scalapb.TypeMapper[edu.uci.ics.texera.web.workflowruntimestate.OperatorRuntimeStats.WorkerInfoEntry, (_root_.scala.Predef.String, edu.uci.ics.texera.web.workflowruntimestate.WorkerRuntimeStats)] = implicitly[_root_.scalapb.TypeMapper[edu.uci.ics.texera.web.workflowruntimestate.OperatorRuntimeStats.WorkerInfoEntry, (_root_.scala.Predef.String, edu.uci.ics.texera.web.workflowruntimestate.WorkerRuntimeStats)]]
  def of(
    state: edu.uci.ics.texera.web.workflowruntimestate.WorkflowAggregatedState,
    inputCount: _root_.scala.Long,
    outputCount: _root_.scala.Long,
    workerInfo: _root_.scala.collection.immutable.Map[_root_.scala.Predef.String, edu.uci.ics.texera.web.workflowruntimestate.WorkerRuntimeStats]
  ): _root_.edu.uci.ics.texera.web.workflowruntimestate.OperatorRuntimeStats = _root_.edu.uci.ics.texera.web.workflowruntimestate.OperatorRuntimeStats(
    state,
    inputCount,
    outputCount,
    workerInfo
  )
  // @@protoc_insertion_point(GeneratedMessageCompanion[edu.uci.ics.texera.web.OperatorRuntimeStats])
}
