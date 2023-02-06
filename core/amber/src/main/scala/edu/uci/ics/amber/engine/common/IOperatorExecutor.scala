package edu.uci.ics.amber.engine.common

import akka.serialization.Serialization
import edu.uci.ics.amber.engine.architecture.checkpoint.SerializedState
import edu.uci.ics.amber.engine.architecture.worker.PauseManager
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCClient
import edu.uci.ics.amber.engine.common.tuple.ITuple
import edu.uci.ics.amber.engine.common.virtualidentity.LinkIdentity
import edu.uci.ics.texera.workflow.common.operators.OperatorContext

case class InputExhausted()

trait IOperatorExecutor extends Serializable {

  def open(): Unit

  def close(): Unit

  def processTuple(
      tuple: Either[ITuple, InputExhausted],
      input: Int,
      pauseManager: PauseManager,
      asyncRPCClient: AsyncRPCClient
  ): Iterator[(ITuple, Option[Int])]

  def getParam(query: String): String = { null }

  def getStateInformation: String = ""

  def serializeState(
      currentIteratorState: Iterator[(ITuple, Option[Int])],
      serializer: Serialization
  ): SerializedState = {
    null
  }

  def deserializeState(
      serializedState: SerializedState,
      deserializer: Serialization
  ): Iterator[(ITuple, Option[Int])] = {
    null
  }

}
