package edu.uci.ics.amber.engine.architecture.worker.promisehandlers

import com.twitter.util.Future
import edu.uci.ics.amber.engine.architecture.rpc.{
  AssignPortRequest,
  AsyncRPCContext
}
import edu.uci.ics.amber.engine.architecture.rpc.EmptyReturn
import edu.uci.ics.amber.engine.architecture.worker.DataProcessorRPCHandlerInitializer
import edu.uci.ics.amber.engine.common.model.tuple.Schema

trait AssignPortHandler {
  this: DataProcessorRPCHandlerInitializer =>

  override def assignPort(msg: AssignPortRequest, ctx: AsyncRPCContext): Future[EmptyReturn] = {
    val schema = Schema.fromRawSchema(msg.schema)
    if (msg.input) {
      dp.inputManager.addPort(msg.portId, schema)
    } else {
      dp.outputManager.addPort(msg.portId, schema)
    }
    EmptyReturn()
  }

}
