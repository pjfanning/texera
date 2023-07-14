package edu.uci.ics.amber.engine.common.rpc

import scala.concurrent.ExecutionContext


object CurrentThreadExecutionContext{
  lazy val instance = new CurrentThreadExecutionContext()
}


class CurrentThreadExecutionContext extends ExecutionContext {
  override def execute(runnable: Runnable): Unit = {
    runnable.run()
  }
  override def reportFailure(cause: Throwable): Unit = {
    // no op
  }
}
