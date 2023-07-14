package edu.uci.ics.amber.engine.common.rpc

import com.twitter.util.Promise
import io.grpc.ClientCall

object WorkflowPromise {
  def apply[T](listener: ClientCall.Listener[T]): WorkflowPromise[T] = new WorkflowPromise[T](listener)
}

/** This class represents the promise control message, which can be passed
  * across different actors to achieve the control logic that require the
  * participation of multiple actors.
  * @tparam T the return value type.
  */
class WorkflowPromise[T](listener: ClientCall.Listener[T]) extends Promise[T] {

  type returnType = T

  override def setValue(result: returnType): Unit = super.setValue(result)
}
