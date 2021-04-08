package edu.uci.ics.amber.engine.recovery

import edu.uci.ics.amber.engine.architecture.messaginglayer.ControlInputPort
import edu.uci.ics.amber.engine.architecture.messaginglayer.ControlInputPort.WorkflowControlMessage
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCHandlerInitializer

class ControlLogManager(
    rpcHandlerInitializer: AsyncRPCHandlerInitializer,
    logStorage: LogStorage[WorkflowControlMessage],
    controlInputPort: ControlInputPort
) extends LogManager(logStorage) {

  // For recovery, only need to replay control messages, and then it's done
  logStorage.load().foreach { msg => controlInputPort.handleControlMessage(msg) }
  setRecoveryCompleted()

  def persistControlMessage(msg: WorkflowControlMessage): Unit = {
    logStorage.persistElement(msg)
  }

}
