// Generated by the Scala Plugin for the Protocol Buffer Compiler.
// Do not edit!
//
// Protofile syntax: PROTO3

package edu.uci.ics.texera.web.workflowruntimestate

@SerialVersionUID(0L)
final case class JobMetadataStore(
    state: edu.uci.ics.texera.web.workflowruntimestate.WorkflowAggregatedState = edu.uci.ics.texera.web.workflowruntimestate.WorkflowAggregatedState.UNINITIALIZED,
    error: _root_.scala.Predef.String = "",
    eid: _root_.scala.Long = 0L,
    isRecovering: _root_.scala.Boolean = false,
    isReplaying: _root_.scala.Boolean = false,
    currentReplayPos: _root_.scala.Int = 0,
    interactionHistory: _root_.scala.Seq[_root_.scala.Int] = _root_.scala.Seq.empty,
    operatorInfoStr: _root_.scala.Predef.String = "",
    checkpointedStates: _root_.scala.Seq[_root_.scala.Int] = _root_.scala.Seq.empty
    ) extends scalapb.GeneratedMessage with scalapb.lenses.Updatable[JobMetadataStore] {
    private[this] def interactionHistorySerializedSize = {
      if (__interactionHistorySerializedSizeField == 0) __interactionHistorySerializedSizeField = {
        var __s: _root_.scala.Int = 0
        interactionHistory.foreach(__i => __s += _root_.com.google.protobuf.CodedOutputStream.computeInt32SizeNoTag(__i))
        __s
      }
      __interactionHistorySerializedSizeField
    }
    @transient private[this] var __interactionHistorySerializedSizeField: _root_.scala.Int = 0
    private[this] def checkpointedStatesSerializedSize = {
      if (__checkpointedStatesSerializedSizeField == 0) __checkpointedStatesSerializedSizeField = {
        var __s: _root_.scala.Int = 0
        checkpointedStates.foreach(__i => __s += _root_.com.google.protobuf.CodedOutputStream.computeInt32SizeNoTag(__i))
        __s
      }
      __checkpointedStatesSerializedSizeField
    }
    @transient private[this] var __checkpointedStatesSerializedSizeField: _root_.scala.Int = 0
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
        val __value = error
        if (!__value.isEmpty) {
          __size += _root_.com.google.protobuf.CodedOutputStream.computeStringSize(2, __value)
        }
      };
      
      {
        val __value = eid
        if (__value != 0L) {
          __size += _root_.com.google.protobuf.CodedOutputStream.computeInt64Size(3, __value)
        }
      };
      
      {
        val __value = isRecovering
        if (__value != false) {
          __size += _root_.com.google.protobuf.CodedOutputStream.computeBoolSize(4, __value)
        }
      };
      
      {
        val __value = isReplaying
        if (__value != false) {
          __size += _root_.com.google.protobuf.CodedOutputStream.computeBoolSize(5, __value)
        }
      };
      
      {
        val __value = currentReplayPos
        if (__value != 0) {
          __size += _root_.com.google.protobuf.CodedOutputStream.computeInt32Size(6, __value)
        }
      };
      if (interactionHistory.nonEmpty) {
        val __localsize = interactionHistorySerializedSize
        __size += 1 + _root_.com.google.protobuf.CodedOutputStream.computeUInt32SizeNoTag(__localsize) + __localsize
      }
      
      {
        val __value = operatorInfoStr
        if (!__value.isEmpty) {
          __size += _root_.com.google.protobuf.CodedOutputStream.computeStringSize(8, __value)
        }
      };
      if (checkpointedStates.nonEmpty) {
        val __localsize = checkpointedStatesSerializedSize
        __size += 1 + _root_.com.google.protobuf.CodedOutputStream.computeUInt32SizeNoTag(__localsize) + __localsize
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
        val __v = error
        if (!__v.isEmpty) {
          _output__.writeString(2, __v)
        }
      };
      {
        val __v = eid
        if (__v != 0L) {
          _output__.writeInt64(3, __v)
        }
      };
      {
        val __v = isRecovering
        if (__v != false) {
          _output__.writeBool(4, __v)
        }
      };
      {
        val __v = isReplaying
        if (__v != false) {
          _output__.writeBool(5, __v)
        }
      };
      {
        val __v = currentReplayPos
        if (__v != 0) {
          _output__.writeInt32(6, __v)
        }
      };
      if (interactionHistory.nonEmpty) {
        _output__.writeTag(7, 2)
        _output__.writeUInt32NoTag(interactionHistorySerializedSize)
        interactionHistory.foreach(_output__.writeInt32NoTag)
      };
      {
        val __v = operatorInfoStr
        if (!__v.isEmpty) {
          _output__.writeString(8, __v)
        }
      };
      if (checkpointedStates.nonEmpty) {
        _output__.writeTag(9, 2)
        _output__.writeUInt32NoTag(checkpointedStatesSerializedSize)
        checkpointedStates.foreach(_output__.writeInt32NoTag)
      };
    }
    def withState(__v: edu.uci.ics.texera.web.workflowruntimestate.WorkflowAggregatedState): JobMetadataStore = copy(state = __v)
    def withError(__v: _root_.scala.Predef.String): JobMetadataStore = copy(error = __v)
    def withEid(__v: _root_.scala.Long): JobMetadataStore = copy(eid = __v)
    def withIsRecovering(__v: _root_.scala.Boolean): JobMetadataStore = copy(isRecovering = __v)
    def withIsReplaying(__v: _root_.scala.Boolean): JobMetadataStore = copy(isReplaying = __v)
    def withCurrentReplayPos(__v: _root_.scala.Int): JobMetadataStore = copy(currentReplayPos = __v)
    def clearInteractionHistory = copy(interactionHistory = _root_.scala.Seq.empty)
    def addInteractionHistory(__vs: _root_.scala.Int*): JobMetadataStore = addAllInteractionHistory(__vs)
    def addAllInteractionHistory(__vs: Iterable[_root_.scala.Int]): JobMetadataStore = copy(interactionHistory = interactionHistory ++ __vs)
    def withInteractionHistory(__v: _root_.scala.Seq[_root_.scala.Int]): JobMetadataStore = copy(interactionHistory = __v)
    def withOperatorInfoStr(__v: _root_.scala.Predef.String): JobMetadataStore = copy(operatorInfoStr = __v)
    def clearCheckpointedStates = copy(checkpointedStates = _root_.scala.Seq.empty)
    def addCheckpointedStates(__vs: _root_.scala.Int*): JobMetadataStore = addAllCheckpointedStates(__vs)
    def addAllCheckpointedStates(__vs: Iterable[_root_.scala.Int]): JobMetadataStore = copy(checkpointedStates = checkpointedStates ++ __vs)
    def withCheckpointedStates(__v: _root_.scala.Seq[_root_.scala.Int]): JobMetadataStore = copy(checkpointedStates = __v)
    def getFieldByNumber(__fieldNumber: _root_.scala.Int): _root_.scala.Any = {
      (__fieldNumber: @_root_.scala.unchecked) match {
        case 1 => {
          val __t = state.javaValueDescriptor
          if (__t.getNumber() != 0) __t else null
        }
        case 2 => {
          val __t = error
          if (__t != "") __t else null
        }
        case 3 => {
          val __t = eid
          if (__t != 0L) __t else null
        }
        case 4 => {
          val __t = isRecovering
          if (__t != false) __t else null
        }
        case 5 => {
          val __t = isReplaying
          if (__t != false) __t else null
        }
        case 6 => {
          val __t = currentReplayPos
          if (__t != 0) __t else null
        }
        case 7 => interactionHistory
        case 8 => {
          val __t = operatorInfoStr
          if (__t != "") __t else null
        }
        case 9 => checkpointedStates
      }
    }
    def getField(__field: _root_.scalapb.descriptors.FieldDescriptor): _root_.scalapb.descriptors.PValue = {
      _root_.scala.Predef.require(__field.containingMessage eq companion.scalaDescriptor)
      (__field.number: @_root_.scala.unchecked) match {
        case 1 => _root_.scalapb.descriptors.PEnum(state.scalaValueDescriptor)
        case 2 => _root_.scalapb.descriptors.PString(error)
        case 3 => _root_.scalapb.descriptors.PLong(eid)
        case 4 => _root_.scalapb.descriptors.PBoolean(isRecovering)
        case 5 => _root_.scalapb.descriptors.PBoolean(isReplaying)
        case 6 => _root_.scalapb.descriptors.PInt(currentReplayPos)
        case 7 => _root_.scalapb.descriptors.PRepeated(interactionHistory.iterator.map(_root_.scalapb.descriptors.PInt(_)).toVector)
        case 8 => _root_.scalapb.descriptors.PString(operatorInfoStr)
        case 9 => _root_.scalapb.descriptors.PRepeated(checkpointedStates.iterator.map(_root_.scalapb.descriptors.PInt(_)).toVector)
      }
    }
    def toProtoString: _root_.scala.Predef.String = _root_.scalapb.TextFormat.printToSingleLineUnicodeString(this)
    def companion = edu.uci.ics.texera.web.workflowruntimestate.JobMetadataStore
    // @@protoc_insertion_point(GeneratedMessage[edu.uci.ics.texera.web.JobMetadataStore])
}

object JobMetadataStore extends scalapb.GeneratedMessageCompanion[edu.uci.ics.texera.web.workflowruntimestate.JobMetadataStore] {
  implicit def messageCompanion: scalapb.GeneratedMessageCompanion[edu.uci.ics.texera.web.workflowruntimestate.JobMetadataStore] = this
  def parseFrom(`_input__`: _root_.com.google.protobuf.CodedInputStream): edu.uci.ics.texera.web.workflowruntimestate.JobMetadataStore = {
    var __state: edu.uci.ics.texera.web.workflowruntimestate.WorkflowAggregatedState = edu.uci.ics.texera.web.workflowruntimestate.WorkflowAggregatedState.UNINITIALIZED
    var __error: _root_.scala.Predef.String = ""
    var __eid: _root_.scala.Long = 0L
    var __isRecovering: _root_.scala.Boolean = false
    var __isReplaying: _root_.scala.Boolean = false
    var __currentReplayPos: _root_.scala.Int = 0
    val __interactionHistory: _root_.scala.collection.immutable.VectorBuilder[_root_.scala.Int] = new _root_.scala.collection.immutable.VectorBuilder[_root_.scala.Int]
    var __operatorInfoStr: _root_.scala.Predef.String = ""
    val __checkpointedStates: _root_.scala.collection.immutable.VectorBuilder[_root_.scala.Int] = new _root_.scala.collection.immutable.VectorBuilder[_root_.scala.Int]
    var _done__ = false
    while (!_done__) {
      val _tag__ = _input__.readTag()
      _tag__ match {
        case 0 => _done__ = true
        case 8 =>
          __state = edu.uci.ics.texera.web.workflowruntimestate.WorkflowAggregatedState.fromValue(_input__.readEnum())
        case 18 =>
          __error = _input__.readStringRequireUtf8()
        case 24 =>
          __eid = _input__.readInt64()
        case 32 =>
          __isRecovering = _input__.readBool()
        case 40 =>
          __isReplaying = _input__.readBool()
        case 48 =>
          __currentReplayPos = _input__.readInt32()
        case 56 =>
          __interactionHistory += _input__.readInt32()
        case 58 => {
          val length = _input__.readRawVarint32()
          val oldLimit = _input__.pushLimit(length)
          while (_input__.getBytesUntilLimit > 0) {
            __interactionHistory += _input__.readInt32()
          }
          _input__.popLimit(oldLimit)
        }
        case 66 =>
          __operatorInfoStr = _input__.readStringRequireUtf8()
        case 72 =>
          __checkpointedStates += _input__.readInt32()
        case 74 => {
          val length = _input__.readRawVarint32()
          val oldLimit = _input__.pushLimit(length)
          while (_input__.getBytesUntilLimit > 0) {
            __checkpointedStates += _input__.readInt32()
          }
          _input__.popLimit(oldLimit)
        }
        case tag => _input__.skipField(tag)
      }
    }
    edu.uci.ics.texera.web.workflowruntimestate.JobMetadataStore(
        state = __state,
        error = __error,
        eid = __eid,
        isRecovering = __isRecovering,
        isReplaying = __isReplaying,
        currentReplayPos = __currentReplayPos,
        interactionHistory = __interactionHistory.result(),
        operatorInfoStr = __operatorInfoStr,
        checkpointedStates = __checkpointedStates.result()
    )
  }
  implicit def messageReads: _root_.scalapb.descriptors.Reads[edu.uci.ics.texera.web.workflowruntimestate.JobMetadataStore] = _root_.scalapb.descriptors.Reads{
    case _root_.scalapb.descriptors.PMessage(__fieldsMap) =>
      _root_.scala.Predef.require(__fieldsMap.keys.forall(_.containingMessage eq scalaDescriptor), "FieldDescriptor does not match message type.")
      edu.uci.ics.texera.web.workflowruntimestate.JobMetadataStore(
        state = edu.uci.ics.texera.web.workflowruntimestate.WorkflowAggregatedState.fromValue(__fieldsMap.get(scalaDescriptor.findFieldByNumber(1).get).map(_.as[_root_.scalapb.descriptors.EnumValueDescriptor]).getOrElse(edu.uci.ics.texera.web.workflowruntimestate.WorkflowAggregatedState.UNINITIALIZED.scalaValueDescriptor).number),
        error = __fieldsMap.get(scalaDescriptor.findFieldByNumber(2).get).map(_.as[_root_.scala.Predef.String]).getOrElse(""),
        eid = __fieldsMap.get(scalaDescriptor.findFieldByNumber(3).get).map(_.as[_root_.scala.Long]).getOrElse(0L),
        isRecovering = __fieldsMap.get(scalaDescriptor.findFieldByNumber(4).get).map(_.as[_root_.scala.Boolean]).getOrElse(false),
        isReplaying = __fieldsMap.get(scalaDescriptor.findFieldByNumber(5).get).map(_.as[_root_.scala.Boolean]).getOrElse(false),
        currentReplayPos = __fieldsMap.get(scalaDescriptor.findFieldByNumber(6).get).map(_.as[_root_.scala.Int]).getOrElse(0),
        interactionHistory = __fieldsMap.get(scalaDescriptor.findFieldByNumber(7).get).map(_.as[_root_.scala.Seq[_root_.scala.Int]]).getOrElse(_root_.scala.Seq.empty),
        operatorInfoStr = __fieldsMap.get(scalaDescriptor.findFieldByNumber(8).get).map(_.as[_root_.scala.Predef.String]).getOrElse(""),
        checkpointedStates = __fieldsMap.get(scalaDescriptor.findFieldByNumber(9).get).map(_.as[_root_.scala.Seq[_root_.scala.Int]]).getOrElse(_root_.scala.Seq.empty)
      )
    case _ => throw new RuntimeException("Expected PMessage")
  }
  def javaDescriptor: _root_.com.google.protobuf.Descriptors.Descriptor = WorkflowruntimestateProto.javaDescriptor.getMessageTypes().get(10)
  def scalaDescriptor: _root_.scalapb.descriptors.Descriptor = WorkflowruntimestateProto.scalaDescriptor.messages(10)
  def messageCompanionForFieldNumber(__number: _root_.scala.Int): _root_.scalapb.GeneratedMessageCompanion[_] = throw new MatchError(__number)
  lazy val nestedMessagesCompanions: Seq[_root_.scalapb.GeneratedMessageCompanion[_ <: _root_.scalapb.GeneratedMessage]] = Seq.empty
  def enumCompanionForFieldNumber(__fieldNumber: _root_.scala.Int): _root_.scalapb.GeneratedEnumCompanion[_] = {
    (__fieldNumber: @_root_.scala.unchecked) match {
      case 1 => edu.uci.ics.texera.web.workflowruntimestate.WorkflowAggregatedState
    }
  }
  lazy val defaultInstance = edu.uci.ics.texera.web.workflowruntimestate.JobMetadataStore(
    state = edu.uci.ics.texera.web.workflowruntimestate.WorkflowAggregatedState.UNINITIALIZED,
    error = "",
    eid = 0L,
    isRecovering = false,
    isReplaying = false,
    currentReplayPos = 0,
    interactionHistory = _root_.scala.Seq.empty,
    operatorInfoStr = "",
    checkpointedStates = _root_.scala.Seq.empty
  )
  implicit class JobMetadataStoreLens[UpperPB](_l: _root_.scalapb.lenses.Lens[UpperPB, edu.uci.ics.texera.web.workflowruntimestate.JobMetadataStore]) extends _root_.scalapb.lenses.ObjectLens[UpperPB, edu.uci.ics.texera.web.workflowruntimestate.JobMetadataStore](_l) {
    def state: _root_.scalapb.lenses.Lens[UpperPB, edu.uci.ics.texera.web.workflowruntimestate.WorkflowAggregatedState] = field(_.state)((c_, f_) => c_.copy(state = f_))
    def error: _root_.scalapb.lenses.Lens[UpperPB, _root_.scala.Predef.String] = field(_.error)((c_, f_) => c_.copy(error = f_))
    def eid: _root_.scalapb.lenses.Lens[UpperPB, _root_.scala.Long] = field(_.eid)((c_, f_) => c_.copy(eid = f_))
    def isRecovering: _root_.scalapb.lenses.Lens[UpperPB, _root_.scala.Boolean] = field(_.isRecovering)((c_, f_) => c_.copy(isRecovering = f_))
    def isReplaying: _root_.scalapb.lenses.Lens[UpperPB, _root_.scala.Boolean] = field(_.isReplaying)((c_, f_) => c_.copy(isReplaying = f_))
    def currentReplayPos: _root_.scalapb.lenses.Lens[UpperPB, _root_.scala.Int] = field(_.currentReplayPos)((c_, f_) => c_.copy(currentReplayPos = f_))
    def interactionHistory: _root_.scalapb.lenses.Lens[UpperPB, _root_.scala.Seq[_root_.scala.Int]] = field(_.interactionHistory)((c_, f_) => c_.copy(interactionHistory = f_))
    def operatorInfoStr: _root_.scalapb.lenses.Lens[UpperPB, _root_.scala.Predef.String] = field(_.operatorInfoStr)((c_, f_) => c_.copy(operatorInfoStr = f_))
    def checkpointedStates: _root_.scalapb.lenses.Lens[UpperPB, _root_.scala.Seq[_root_.scala.Int]] = field(_.checkpointedStates)((c_, f_) => c_.copy(checkpointedStates = f_))
  }
  final val STATE_FIELD_NUMBER = 1
  final val ERROR_FIELD_NUMBER = 2
  final val EID_FIELD_NUMBER = 3
  final val IS_RECOVERING_FIELD_NUMBER = 4
  final val IS_REPLAYING_FIELD_NUMBER = 5
  final val CURRENT_REPLAY_POS_FIELD_NUMBER = 6
  final val INTERACTION_HISTORY_FIELD_NUMBER = 7
  final val OPERATOR_INFO_STR_FIELD_NUMBER = 8
  final val CHECKPOINTED_STATES_FIELD_NUMBER = 9
  def of(
    state: edu.uci.ics.texera.web.workflowruntimestate.WorkflowAggregatedState,
    error: _root_.scala.Predef.String,
    eid: _root_.scala.Long,
    isRecovering: _root_.scala.Boolean,
    isReplaying: _root_.scala.Boolean,
    currentReplayPos: _root_.scala.Int,
    interactionHistory: _root_.scala.Seq[_root_.scala.Int],
    operatorInfoStr: _root_.scala.Predef.String,
    checkpointedStates: _root_.scala.Seq[_root_.scala.Int]
  ): _root_.edu.uci.ics.texera.web.workflowruntimestate.JobMetadataStore = _root_.edu.uci.ics.texera.web.workflowruntimestate.JobMetadataStore(
    state,
    error,
    eid,
    isRecovering,
    isReplaying,
    currentReplayPos,
    interactionHistory,
    operatorInfoStr,
    checkpointedStates
  )
  // @@protoc_insertion_point(GeneratedMessageCompanion[edu.uci.ics.texera.web.JobMetadataStore])
}
