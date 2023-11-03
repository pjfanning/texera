// Generated by the Scala Plugin for the Protocol Buffer Compiler.
// Do not edit!
//
// Protofile syntax: PROTO3

package edu.uci.ics.texera.web.workflowruntimestate

@SerialVersionUID(0L)
final case class JobMetadataStore(
    state: edu.uci.ics.texera.web.workflowruntimestate.WorkflowAggregatedState = edu.uci.ics.texera.web.workflowruntimestate.WorkflowAggregatedState.UNINITIALIZED,
    errors: _root_.scala.Seq[edu.uci.ics.texera.web.workflowruntimestate.JobError] = _root_.scala.Seq.empty,
    eid: _root_.scala.Long = 0L,
    isRecovering: _root_.scala.Boolean = false
    ) extends scalapb.GeneratedMessage with scalapb.lenses.Updatable[JobMetadataStore] {
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
      errors.foreach { __item =>
        val __value = __item
        __size += 1 + _root_.com.google.protobuf.CodedOutputStream.computeUInt32SizeNoTag(__value.serializedSize) + __value.serializedSize
      }
      
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
      errors.foreach { __v =>
        val __m = __v
        _output__.writeTag(2, 2)
        _output__.writeUInt32NoTag(__m.serializedSize)
        __m.writeTo(_output__)
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
    }
    def withState(__v: edu.uci.ics.texera.web.workflowruntimestate.WorkflowAggregatedState): JobMetadataStore = copy(state = __v)
    def clearErrors = copy(errors = _root_.scala.Seq.empty)
    def addErrors(__vs: edu.uci.ics.texera.web.workflowruntimestate.JobError*): JobMetadataStore = addAllErrors(__vs)
    def addAllErrors(__vs: Iterable[edu.uci.ics.texera.web.workflowruntimestate.JobError]): JobMetadataStore = copy(errors = errors ++ __vs)
    def withErrors(__v: _root_.scala.Seq[edu.uci.ics.texera.web.workflowruntimestate.JobError]): JobMetadataStore = copy(errors = __v)
    def withEid(__v: _root_.scala.Long): JobMetadataStore = copy(eid = __v)
    def withIsRecovering(__v: _root_.scala.Boolean): JobMetadataStore = copy(isRecovering = __v)
    def getFieldByNumber(__fieldNumber: _root_.scala.Int): _root_.scala.Any = {
      (__fieldNumber: @_root_.scala.unchecked) match {
        case 1 => {
          val __t = state.javaValueDescriptor
          if (__t.getNumber() != 0) __t else null
        }
        case 2 => errors
        case 3 => {
          val __t = eid
          if (__t != 0L) __t else null
        }
        case 4 => {
          val __t = isRecovering
          if (__t != false) __t else null
        }
      }
    }
    def getField(__field: _root_.scalapb.descriptors.FieldDescriptor): _root_.scalapb.descriptors.PValue = {
      _root_.scala.Predef.require(__field.containingMessage eq companion.scalaDescriptor)
      (__field.number: @_root_.scala.unchecked) match {
        case 1 => _root_.scalapb.descriptors.PEnum(state.scalaValueDescriptor)
        case 2 => _root_.scalapb.descriptors.PRepeated(errors.iterator.map(_.toPMessage).toVector)
        case 3 => _root_.scalapb.descriptors.PLong(eid)
        case 4 => _root_.scalapb.descriptors.PBoolean(isRecovering)
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
    val __errors: _root_.scala.collection.immutable.VectorBuilder[edu.uci.ics.texera.web.workflowruntimestate.JobError] = new _root_.scala.collection.immutable.VectorBuilder[edu.uci.ics.texera.web.workflowruntimestate.JobError]
    var __eid: _root_.scala.Long = 0L
    var __isRecovering: _root_.scala.Boolean = false
    var _done__ = false
    while (!_done__) {
      val _tag__ = _input__.readTag()
      _tag__ match {
        case 0 => _done__ = true
        case 8 =>
          __state = edu.uci.ics.texera.web.workflowruntimestate.WorkflowAggregatedState.fromValue(_input__.readEnum())
        case 18 =>
          __errors += _root_.scalapb.LiteParser.readMessage[edu.uci.ics.texera.web.workflowruntimestate.JobError](_input__)
        case 24 =>
          __eid = _input__.readInt64()
        case 32 =>
          __isRecovering = _input__.readBool()
        case tag => _input__.skipField(tag)
      }
    }
    edu.uci.ics.texera.web.workflowruntimestate.JobMetadataStore(
        state = __state,
        errors = __errors.result(),
        eid = __eid,
        isRecovering = __isRecovering
    )
  }
  implicit def messageReads: _root_.scalapb.descriptors.Reads[edu.uci.ics.texera.web.workflowruntimestate.JobMetadataStore] = _root_.scalapb.descriptors.Reads{
    case _root_.scalapb.descriptors.PMessage(__fieldsMap) =>
      _root_.scala.Predef.require(__fieldsMap.keys.forall(_.containingMessage eq scalaDescriptor), "FieldDescriptor does not match message type.")
      edu.uci.ics.texera.web.workflowruntimestate.JobMetadataStore(
        state = edu.uci.ics.texera.web.workflowruntimestate.WorkflowAggregatedState.fromValue(__fieldsMap.get(scalaDescriptor.findFieldByNumber(1).get).map(_.as[_root_.scalapb.descriptors.EnumValueDescriptor]).getOrElse(edu.uci.ics.texera.web.workflowruntimestate.WorkflowAggregatedState.UNINITIALIZED.scalaValueDescriptor).number),
        errors = __fieldsMap.get(scalaDescriptor.findFieldByNumber(2).get).map(_.as[_root_.scala.Seq[edu.uci.ics.texera.web.workflowruntimestate.JobError]]).getOrElse(_root_.scala.Seq.empty),
        eid = __fieldsMap.get(scalaDescriptor.findFieldByNumber(3).get).map(_.as[_root_.scala.Long]).getOrElse(0L),
        isRecovering = __fieldsMap.get(scalaDescriptor.findFieldByNumber(4).get).map(_.as[_root_.scala.Boolean]).getOrElse(false)
      )
    case _ => throw new RuntimeException("Expected PMessage")
  }
  def javaDescriptor: _root_.com.google.protobuf.Descriptors.Descriptor = WorkflowruntimestateProto.javaDescriptor.getMessageTypes().get(10)
  def scalaDescriptor: _root_.scalapb.descriptors.Descriptor = WorkflowruntimestateProto.scalaDescriptor.messages(10)
  def messageCompanionForFieldNumber(__number: _root_.scala.Int): _root_.scalapb.GeneratedMessageCompanion[_] = {
    var __out: _root_.scalapb.GeneratedMessageCompanion[_] = null
    (__number: @_root_.scala.unchecked) match {
      case 2 => __out = edu.uci.ics.texera.web.workflowruntimestate.JobError
    }
    __out
  }
  lazy val nestedMessagesCompanions: Seq[_root_.scalapb.GeneratedMessageCompanion[_ <: _root_.scalapb.GeneratedMessage]] = Seq.empty
  def enumCompanionForFieldNumber(__fieldNumber: _root_.scala.Int): _root_.scalapb.GeneratedEnumCompanion[_] = {
    (__fieldNumber: @_root_.scala.unchecked) match {
      case 1 => edu.uci.ics.texera.web.workflowruntimestate.WorkflowAggregatedState
    }
  }
  lazy val defaultInstance = edu.uci.ics.texera.web.workflowruntimestate.JobMetadataStore(
    state = edu.uci.ics.texera.web.workflowruntimestate.WorkflowAggregatedState.UNINITIALIZED,
    errors = _root_.scala.Seq.empty,
    eid = 0L,
    isRecovering = false
  )
  implicit class JobMetadataStoreLens[UpperPB](_l: _root_.scalapb.lenses.Lens[UpperPB, edu.uci.ics.texera.web.workflowruntimestate.JobMetadataStore]) extends _root_.scalapb.lenses.ObjectLens[UpperPB, edu.uci.ics.texera.web.workflowruntimestate.JobMetadataStore](_l) {
    def state: _root_.scalapb.lenses.Lens[UpperPB, edu.uci.ics.texera.web.workflowruntimestate.WorkflowAggregatedState] = field(_.state)((c_, f_) => c_.copy(state = f_))
    def errors: _root_.scalapb.lenses.Lens[UpperPB, _root_.scala.Seq[edu.uci.ics.texera.web.workflowruntimestate.JobError]] = field(_.errors)((c_, f_) => c_.copy(errors = f_))
    def eid: _root_.scalapb.lenses.Lens[UpperPB, _root_.scala.Long] = field(_.eid)((c_, f_) => c_.copy(eid = f_))
    def isRecovering: _root_.scalapb.lenses.Lens[UpperPB, _root_.scala.Boolean] = field(_.isRecovering)((c_, f_) => c_.copy(isRecovering = f_))
  }
  final val STATE_FIELD_NUMBER = 1
  final val ERRORS_FIELD_NUMBER = 2
  final val EID_FIELD_NUMBER = 3
  final val IS_RECOVERING_FIELD_NUMBER = 4
  def of(
    state: edu.uci.ics.texera.web.workflowruntimestate.WorkflowAggregatedState,
    errors: _root_.scala.Seq[edu.uci.ics.texera.web.workflowruntimestate.JobError],
    eid: _root_.scala.Long,
    isRecovering: _root_.scala.Boolean
  ): _root_.edu.uci.ics.texera.web.workflowruntimestate.JobMetadataStore = _root_.edu.uci.ics.texera.web.workflowruntimestate.JobMetadataStore(
    state,
    errors,
    eid,
    isRecovering
  )
  // @@protoc_insertion_point(GeneratedMessageCompanion[edu.uci.ics.texera.web.JobMetadataStore])
}
