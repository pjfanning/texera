// Generated by the Scala Plugin for the Protocol Buffer Compiler.
// Do not edit!
//
// Protofile syntax: PROTO3

package edu.uci.ics.amber.engine.architecture.worker.statistics

@SerialVersionUID(0L)
final case class WorkerStatistics(
    inputTupleCount: _root_.scala.Seq[edu.uci.ics.amber.engine.architecture.worker.statistics.PortTupleCountMapping],
    outputTupleCount: _root_.scala.Seq[edu.uci.ics.amber.engine.architecture.worker.statistics.PortTupleCountMapping],
    dataProcessingTime: _root_.scala.Long,
    controlProcessingTime: _root_.scala.Long,
    idleTime: _root_.scala.Long
    ) extends scalapb.GeneratedMessage with scalapb.lenses.Updatable[WorkerStatistics] {
    @transient
    private[this] var __serializedSizeCachedValue: _root_.scala.Int = 0
    private[this] def __computeSerializedValue(): _root_.scala.Int = {
      var __size = 0
      inputTupleCount.foreach { __item =>
        val __value = __item
        __size += 1 + _root_.com.google.protobuf.CodedOutputStream.computeUInt32SizeNoTag(__value.serializedSize) + __value.serializedSize
      }
      outputTupleCount.foreach { __item =>
        val __value = __item
        __size += 1 + _root_.com.google.protobuf.CodedOutputStream.computeUInt32SizeNoTag(__value.serializedSize) + __value.serializedSize
      }
      
      {
        val __value = dataProcessingTime
        if (__value != 0L) {
          __size += _root_.com.google.protobuf.CodedOutputStream.computeInt64Size(3, __value)
        }
      };
      
      {
        val __value = controlProcessingTime
        if (__value != 0L) {
          __size += _root_.com.google.protobuf.CodedOutputStream.computeInt64Size(4, __value)
        }
      };
      
      {
        val __value = idleTime
        if (__value != 0L) {
          __size += _root_.com.google.protobuf.CodedOutputStream.computeInt64Size(5, __value)
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
      inputTupleCount.foreach { __v =>
        val __m = __v
        _output__.writeTag(1, 2)
        _output__.writeUInt32NoTag(__m.serializedSize)
        __m.writeTo(_output__)
      };
      outputTupleCount.foreach { __v =>
        val __m = __v
        _output__.writeTag(2, 2)
        _output__.writeUInt32NoTag(__m.serializedSize)
        __m.writeTo(_output__)
      };
      {
        val __v = dataProcessingTime
        if (__v != 0L) {
          _output__.writeInt64(3, __v)
        }
      };
      {
        val __v = controlProcessingTime
        if (__v != 0L) {
          _output__.writeInt64(4, __v)
        }
      };
      {
        val __v = idleTime
        if (__v != 0L) {
          _output__.writeInt64(5, __v)
        }
      };
    }
    def clearInputTupleCount = copy(inputTupleCount = _root_.scala.Seq.empty)
    def addInputTupleCount(__vs: edu.uci.ics.amber.engine.architecture.worker.statistics.PortTupleCountMapping*): WorkerStatistics = addAllInputTupleCount(__vs)
    def addAllInputTupleCount(__vs: Iterable[edu.uci.ics.amber.engine.architecture.worker.statistics.PortTupleCountMapping]): WorkerStatistics = copy(inputTupleCount = inputTupleCount ++ __vs)
    def withInputTupleCount(__v: _root_.scala.Seq[edu.uci.ics.amber.engine.architecture.worker.statistics.PortTupleCountMapping]): WorkerStatistics = copy(inputTupleCount = __v)
    def clearOutputTupleCount = copy(outputTupleCount = _root_.scala.Seq.empty)
    def addOutputTupleCount(__vs: edu.uci.ics.amber.engine.architecture.worker.statistics.PortTupleCountMapping*): WorkerStatistics = addAllOutputTupleCount(__vs)
    def addAllOutputTupleCount(__vs: Iterable[edu.uci.ics.amber.engine.architecture.worker.statistics.PortTupleCountMapping]): WorkerStatistics = copy(outputTupleCount = outputTupleCount ++ __vs)
    def withOutputTupleCount(__v: _root_.scala.Seq[edu.uci.ics.amber.engine.architecture.worker.statistics.PortTupleCountMapping]): WorkerStatistics = copy(outputTupleCount = __v)
    def withDataProcessingTime(__v: _root_.scala.Long): WorkerStatistics = copy(dataProcessingTime = __v)
    def withControlProcessingTime(__v: _root_.scala.Long): WorkerStatistics = copy(controlProcessingTime = __v)
    def withIdleTime(__v: _root_.scala.Long): WorkerStatistics = copy(idleTime = __v)
    def getFieldByNumber(__fieldNumber: _root_.scala.Int): _root_.scala.Any = {
      (__fieldNumber: @_root_.scala.unchecked) match {
        case 1 => inputTupleCount
        case 2 => outputTupleCount
        case 3 => {
          val __t = dataProcessingTime
          if (__t != 0L) __t else null
        }
        case 4 => {
          val __t = controlProcessingTime
          if (__t != 0L) __t else null
        }
        case 5 => {
          val __t = idleTime
          if (__t != 0L) __t else null
        }
      }
    }
    def getField(__field: _root_.scalapb.descriptors.FieldDescriptor): _root_.scalapb.descriptors.PValue = {
      _root_.scala.Predef.require(__field.containingMessage eq companion.scalaDescriptor)
      (__field.number: @_root_.scala.unchecked) match {
        case 1 => _root_.scalapb.descriptors.PRepeated(inputTupleCount.iterator.map(_.toPMessage).toVector)
        case 2 => _root_.scalapb.descriptors.PRepeated(outputTupleCount.iterator.map(_.toPMessage).toVector)
        case 3 => _root_.scalapb.descriptors.PLong(dataProcessingTime)
        case 4 => _root_.scalapb.descriptors.PLong(controlProcessingTime)
        case 5 => _root_.scalapb.descriptors.PLong(idleTime)
      }
    }
    def toProtoString: _root_.scala.Predef.String = _root_.scalapb.TextFormat.printToSingleLineUnicodeString(this)
    def companion = edu.uci.ics.amber.engine.architecture.worker.statistics.WorkerStatistics
    // @@protoc_insertion_point(GeneratedMessage[edu.uci.ics.amber.engine.architecture.worker.WorkerStatistics])
}

object WorkerStatistics extends scalapb.GeneratedMessageCompanion[edu.uci.ics.amber.engine.architecture.worker.statistics.WorkerStatistics] {
  implicit def messageCompanion: scalapb.GeneratedMessageCompanion[edu.uci.ics.amber.engine.architecture.worker.statistics.WorkerStatistics] = this
  def parseFrom(`_input__`: _root_.com.google.protobuf.CodedInputStream): edu.uci.ics.amber.engine.architecture.worker.statistics.WorkerStatistics = {
    val __inputTupleCount: _root_.scala.collection.immutable.VectorBuilder[edu.uci.ics.amber.engine.architecture.worker.statistics.PortTupleCountMapping] = new _root_.scala.collection.immutable.VectorBuilder[edu.uci.ics.amber.engine.architecture.worker.statistics.PortTupleCountMapping]
    val __outputTupleCount: _root_.scala.collection.immutable.VectorBuilder[edu.uci.ics.amber.engine.architecture.worker.statistics.PortTupleCountMapping] = new _root_.scala.collection.immutable.VectorBuilder[edu.uci.ics.amber.engine.architecture.worker.statistics.PortTupleCountMapping]
    var __dataProcessingTime: _root_.scala.Long = 0L
    var __controlProcessingTime: _root_.scala.Long = 0L
    var __idleTime: _root_.scala.Long = 0L
    var _done__ = false
    while (!_done__) {
      val _tag__ = _input__.readTag()
      _tag__ match {
        case 0 => _done__ = true
        case 10 =>
          __inputTupleCount += _root_.scalapb.LiteParser.readMessage[edu.uci.ics.amber.engine.architecture.worker.statistics.PortTupleCountMapping](_input__)
        case 18 =>
          __outputTupleCount += _root_.scalapb.LiteParser.readMessage[edu.uci.ics.amber.engine.architecture.worker.statistics.PortTupleCountMapping](_input__)
        case 24 =>
          __dataProcessingTime = _input__.readInt64()
        case 32 =>
          __controlProcessingTime = _input__.readInt64()
        case 40 =>
          __idleTime = _input__.readInt64()
        case tag => _input__.skipField(tag)
      }
    }
    edu.uci.ics.amber.engine.architecture.worker.statistics.WorkerStatistics(
        inputTupleCount = __inputTupleCount.result(),
        outputTupleCount = __outputTupleCount.result(),
        dataProcessingTime = __dataProcessingTime,
        controlProcessingTime = __controlProcessingTime,
        idleTime = __idleTime
    )
  }
  implicit def messageReads: _root_.scalapb.descriptors.Reads[edu.uci.ics.amber.engine.architecture.worker.statistics.WorkerStatistics] = _root_.scalapb.descriptors.Reads{
    case _root_.scalapb.descriptors.PMessage(__fieldsMap) =>
      _root_.scala.Predef.require(__fieldsMap.keys.forall(_.containingMessage eq scalaDescriptor), "FieldDescriptor does not match message type.")
      edu.uci.ics.amber.engine.architecture.worker.statistics.WorkerStatistics(
        inputTupleCount = __fieldsMap.get(scalaDescriptor.findFieldByNumber(1).get).map(_.as[_root_.scala.Seq[edu.uci.ics.amber.engine.architecture.worker.statistics.PortTupleCountMapping]]).getOrElse(_root_.scala.Seq.empty),
        outputTupleCount = __fieldsMap.get(scalaDescriptor.findFieldByNumber(2).get).map(_.as[_root_.scala.Seq[edu.uci.ics.amber.engine.architecture.worker.statistics.PortTupleCountMapping]]).getOrElse(_root_.scala.Seq.empty),
        dataProcessingTime = __fieldsMap.get(scalaDescriptor.findFieldByNumber(3).get).map(_.as[_root_.scala.Long]).getOrElse(0L),
        controlProcessingTime = __fieldsMap.get(scalaDescriptor.findFieldByNumber(4).get).map(_.as[_root_.scala.Long]).getOrElse(0L),
        idleTime = __fieldsMap.get(scalaDescriptor.findFieldByNumber(5).get).map(_.as[_root_.scala.Long]).getOrElse(0L)
      )
    case _ => throw new RuntimeException("Expected PMessage")
  }
  def javaDescriptor: _root_.com.google.protobuf.Descriptors.Descriptor = StatisticsProto.javaDescriptor.getMessageTypes().get(1)
  def scalaDescriptor: _root_.scalapb.descriptors.Descriptor = StatisticsProto.scalaDescriptor.messages(1)
  def messageCompanionForFieldNumber(__number: _root_.scala.Int): _root_.scalapb.GeneratedMessageCompanion[_] = {
    var __out: _root_.scalapb.GeneratedMessageCompanion[_] = null
    (__number: @_root_.scala.unchecked) match {
      case 1 => __out = edu.uci.ics.amber.engine.architecture.worker.statistics.PortTupleCountMapping
      case 2 => __out = edu.uci.ics.amber.engine.architecture.worker.statistics.PortTupleCountMapping
    }
    __out
  }
  lazy val nestedMessagesCompanions: Seq[_root_.scalapb.GeneratedMessageCompanion[_ <: _root_.scalapb.GeneratedMessage]] = Seq.empty
  def enumCompanionForFieldNumber(__fieldNumber: _root_.scala.Int): _root_.scalapb.GeneratedEnumCompanion[_] = throw new MatchError(__fieldNumber)
  lazy val defaultInstance = edu.uci.ics.amber.engine.architecture.worker.statistics.WorkerStatistics(
    inputTupleCount = _root_.scala.Seq.empty,
    outputTupleCount = _root_.scala.Seq.empty,
    dataProcessingTime = 0L,
    controlProcessingTime = 0L,
    idleTime = 0L
  )
  implicit class WorkerStatisticsLens[UpperPB](_l: _root_.scalapb.lenses.Lens[UpperPB, edu.uci.ics.amber.engine.architecture.worker.statistics.WorkerStatistics]) extends _root_.scalapb.lenses.ObjectLens[UpperPB, edu.uci.ics.amber.engine.architecture.worker.statistics.WorkerStatistics](_l) {
    def inputTupleCount: _root_.scalapb.lenses.Lens[UpperPB, _root_.scala.Seq[edu.uci.ics.amber.engine.architecture.worker.statistics.PortTupleCountMapping]] = field(_.inputTupleCount)((c_, f_) => c_.copy(inputTupleCount = f_))
    def outputTupleCount: _root_.scalapb.lenses.Lens[UpperPB, _root_.scala.Seq[edu.uci.ics.amber.engine.architecture.worker.statistics.PortTupleCountMapping]] = field(_.outputTupleCount)((c_, f_) => c_.copy(outputTupleCount = f_))
    def dataProcessingTime: _root_.scalapb.lenses.Lens[UpperPB, _root_.scala.Long] = field(_.dataProcessingTime)((c_, f_) => c_.copy(dataProcessingTime = f_))
    def controlProcessingTime: _root_.scalapb.lenses.Lens[UpperPB, _root_.scala.Long] = field(_.controlProcessingTime)((c_, f_) => c_.copy(controlProcessingTime = f_))
    def idleTime: _root_.scalapb.lenses.Lens[UpperPB, _root_.scala.Long] = field(_.idleTime)((c_, f_) => c_.copy(idleTime = f_))
  }
  final val INPUT_TUPLE_COUNT_FIELD_NUMBER = 1
  final val OUTPUT_TUPLE_COUNT_FIELD_NUMBER = 2
  final val DATA_PROCESSING_TIME_FIELD_NUMBER = 3
  final val CONTROL_PROCESSING_TIME_FIELD_NUMBER = 4
  final val IDLE_TIME_FIELD_NUMBER = 5
  def of(
    inputTupleCount: _root_.scala.Seq[edu.uci.ics.amber.engine.architecture.worker.statistics.PortTupleCountMapping],
    outputTupleCount: _root_.scala.Seq[edu.uci.ics.amber.engine.architecture.worker.statistics.PortTupleCountMapping],
    dataProcessingTime: _root_.scala.Long,
    controlProcessingTime: _root_.scala.Long,
    idleTime: _root_.scala.Long
  ): _root_.edu.uci.ics.amber.engine.architecture.worker.statistics.WorkerStatistics = _root_.edu.uci.ics.amber.engine.architecture.worker.statistics.WorkerStatistics(
    inputTupleCount,
    outputTupleCount,
    dataProcessingTime,
    controlProcessingTime,
    idleTime
  )
  // @@protoc_insertion_point(GeneratedMessageCompanion[edu.uci.ics.amber.engine.architecture.worker.WorkerStatistics])
}
