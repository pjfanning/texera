package edu.uci.ics.amber.engine.architecture.control.utils

import com.twitter.util.Future
import edu.uci.ics.amber.engine.architecture.rpc._

trait ErrorHandler {
  this: TesterAsyncRPCHandlerInitializer =>

  override def sendErrorCommand(
      request: ErrorCommand,
      ctx: AsyncRPCContext
  ): Future[StringResponse] = {
    throw new RuntimeException("this is an EXPECTED exception for testing")
  }

}
