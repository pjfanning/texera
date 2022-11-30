// Generated by the Scala Plugin for the Protocol Buffer Compiler.
// Do not edit!
//
// Protofile syntax: PROTO3

package edu.uci.ics.texera.web.workflowruntimestate

@SerialVersionUID(0L)
final case class PythonWorkerInfo(
    pythonConsoleMessages: _root_.scala.Seq[edu.uci.ics.amber.engine.architecture.worker.controlcommands.PythonConsoleMessageV2] = _root_.scala.Seq.empty
    ) extends scalapb.GeneratedMessage with scalapb.lenses.Updatable[PythonWorkerInfo] {
    @transient
    private[this] var __serializedSizeCachedValue: _root_.scala.Int = 0
    private[this] def __computeSerializedValue(): _root_.scala.Int = {
      var __size = 0
      pythonConsoleMessages.foreach { __item =>
        val __value = __item
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
      pythonConsoleMessages.foreach { __v =>
        val __m = __v
        _output__.writeTag(1, 2)
        _output__.writeUInt32NoTag(__m.serializedSize)
        __m.writeTo(_output__)
      };
    }
    def clearPythonConsoleMessages = copy(pythonConsoleMessages = _root_.scala.Seq.empty)
    def addPythonConsoleMessages(__vs: edu.uci.ics.amber.engine.architecture.worker.controlcommands.PythonConsoleMessageV2*): PythonWorkerInfo = addAllPythonConsoleMessages(__vs)
    def addAllPythonConsoleMessages(__vs: Iterable[edu.uci.ics.amber.engine.architecture.worker.controlcommands.PythonConsoleMessageV2]): PythonWorkerInfo = copy(pythonConsoleMessages = pythonConsoleMessages ++ __vs)
    def withPythonConsoleMessages(__v: _root_.scala.Seq[edu.uci.ics.amber.engine.architecture.worker.controlcommands.PythonConsoleMessageV2]): PythonWorkerInfo = copy(pythonConsoleMessages = __v)
    def getFieldByNumber(__fieldNumber: _root_.scala.Int): _root_.scala.Any = {
      (__fieldNumber: @_root_.scala.unchecked) match {
        case 1 => pythonConsoleMessages
      }
    }
    def getField(__field: _root_.scalapb.descriptors.FieldDescriptor): _root_.scalapb.descriptors.PValue = {
      _root_.scala.Predef.require(__field.containingMessage eq companion.scalaDescriptor)
      (__field.number: @_root_.scala.unchecked) match {
        case 1 => _root_.scalapb.descriptors.PRepeated(pythonConsoleMessages.iterator.map(_.toPMessage).toVector)
      }
    }
    def toProtoString: _root_.scala.Predef.String = _root_.scalapb.TextFormat.printToSingleLineUnicodeString(this)
    def companion = edu.uci.ics.texera.web.workflowruntimestate.PythonWorkerInfo
    // @@protoc_insertion_point(GeneratedMessage[edu.uci.ics.texera.web.PythonWorkerInfo])
}

object PythonWorkerInfo extends scalapb.GeneratedMessageCompanion[edu.uci.ics.texera.web.workflowruntimestate.PythonWorkerInfo] {
  implicit def messageCompanion: scalapb.GeneratedMessageCompanion[edu.uci.ics.texera.web.workflowruntimestate.PythonWorkerInfo] = this
  def parseFrom(`_input__`: _root_.com.google.protobuf.CodedInputStream): edu.uci.ics.texera.web.workflowruntimestate.PythonWorkerInfo = {
    val __pythonConsoleMessages: _root_.scala.collection.immutable.VectorBuilder[edu.uci.ics.amber.engine.architecture.worker.controlcommands.PythonConsoleMessageV2] = new _root_.scala.collection.immutable.VectorBuilder[edu.uci.ics.amber.engine.architecture.worker.controlcommands.PythonConsoleMessageV2]
    var _done__ = false
    while (!_done__) {
      val _tag__ = _input__.readTag()
      _tag__ match {
        case 0 => _done__ = true
        case 10 =>
          __pythonConsoleMessages += _root_.scalapb.LiteParser.readMessage[edu.uci.ics.amber.engine.architecture.worker.controlcommands.PythonConsoleMessageV2](_input__)
        case tag => _input__.skipField(tag)
      }
    }
    edu.uci.ics.texera.web.workflowruntimestate.PythonWorkerInfo(
        pythonConsoleMessages = __pythonConsoleMessages.result()
    )
  }
  implicit def messageReads: _root_.scalapb.descriptors.Reads[edu.uci.ics.texera.web.workflowruntimestate.PythonWorkerInfo] = _root_.scalapb.descriptors.Reads{
    case _root_.scalapb.descriptors.PMessage(__fieldsMap) =>
      _root_.scala.Predef.require(__fieldsMap.keys.forall(_.containingMessage eq scalaDescriptor), "FieldDescriptor does not match message type.")
      edu.uci.ics.texera.web.workflowruntimestate.PythonWorkerInfo(
        pythonConsoleMessages = __fieldsMap.get(scalaDescriptor.findFieldByNumber(1).get).map(_.as[_root_.scala.Seq[edu.uci.ics.amber.engine.architecture.worker.controlcommands.PythonConsoleMessageV2]]).getOrElse(_root_.scala.Seq.empty)
      )
    case _ => throw new RuntimeException("Expected PMessage")
  }
  def javaDescriptor: _root_.com.google.protobuf.Descriptors.Descriptor = WorkflowruntimestateProto.javaDescriptor.getMessageTypes().get(4)
  def scalaDescriptor: _root_.scalapb.descriptors.Descriptor = WorkflowruntimestateProto.scalaDescriptor.messages(4)
  def messageCompanionForFieldNumber(__number: _root_.scala.Int): _root_.scalapb.GeneratedMessageCompanion[_] = {
    var __out: _root_.scalapb.GeneratedMessageCompanion[_] = null
    (__number: @_root_.scala.unchecked) match {
      case 1 => __out = edu.uci.ics.amber.engine.architecture.worker.controlcommands.PythonConsoleMessageV2
    }
    __out
  }
  lazy val nestedMessagesCompanions: Seq[_root_.scalapb.GeneratedMessageCompanion[_ <: _root_.scalapb.GeneratedMessage]] = Seq.empty
  def enumCompanionForFieldNumber(__fieldNumber: _root_.scala.Int): _root_.scalapb.GeneratedEnumCompanion[_] = throw new MatchError(__fieldNumber)
  lazy val defaultInstance = edu.uci.ics.texera.web.workflowruntimestate.PythonWorkerInfo(
    pythonConsoleMessages = _root_.scala.Seq.empty
  )
  implicit class PythonWorkerInfoLens[UpperPB](_l: _root_.scalapb.lenses.Lens[UpperPB, edu.uci.ics.texera.web.workflowruntimestate.PythonWorkerInfo]) extends _root_.scalapb.lenses.ObjectLens[UpperPB, edu.uci.ics.texera.web.workflowruntimestate.PythonWorkerInfo](_l) {
    def pythonConsoleMessages: _root_.scalapb.lenses.Lens[UpperPB, _root_.scala.Seq[edu.uci.ics.amber.engine.architecture.worker.controlcommands.PythonConsoleMessageV2]] = field(_.pythonConsoleMessages)((c_, f_) => c_.copy(pythonConsoleMessages = f_))
  }
  final val PYTHON_CONSOLE_MESSAGES_FIELD_NUMBER = 1
  def of(
    pythonConsoleMessages: _root_.scala.Seq[edu.uci.ics.amber.engine.architecture.worker.controlcommands.PythonConsoleMessageV2]
  ): _root_.edu.uci.ics.texera.web.workflowruntimestate.PythonWorkerInfo = _root_.edu.uci.ics.texera.web.workflowruntimestate.PythonWorkerInfo(
    pythonConsoleMessages
  )
  // @@protoc_insertion_point(GeneratedMessageCompanion[edu.uci.ics.texera.web.PythonWorkerInfo])
}
