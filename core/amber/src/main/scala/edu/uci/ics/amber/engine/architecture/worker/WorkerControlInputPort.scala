package edu.uci.ics.amber.engine.architecture.worker

import edu.uci.ics.amber.engine.architecture.messaginglayer.ControlInputPort
import edu.uci.ics.amber.engine.architecture.worker.WorkerInternalQueue.UnblockForControlCommands
import edu.uci.ics.amber.engine.common.WorkflowLogger
import edu.uci.ics.amber.engine.common.rpc.{AsyncRPCClient, AsyncRPCServer}
import edu.uci.ics.amber.engine.common.virtualidentity.{ActorVirtualIdentity, VirtualIdentity}

class WorkerControlInputPort(
    dataProcessor: DataProcessor,
    logger: WorkflowLogger,
    asyncRPCClient: AsyncRPCClient,
    asyncRPCServer: AsyncRPCServer
) extends ControlInputPort(logger, asyncRPCClient, asyncRPCServer) {

  override def processControlInvocation(
      invocation: AsyncRPCClient.ControlInvocation,
      from: VirtualIdentity
  ): Unit = {
    // let dp thread process it
    assert(from.isInstanceOf[ActorVirtualIdentity])
    // this enqueue operation MUST happen before checking data queue.
    dataProcessor.enqueueCommand(invocation, from)
    if (dataProcessor.isDataQueueEmpty) {
      dataProcessor.appendElement(UnblockForControlCommands)
    }
  }

  override def processReturnPayload(
      ret: AsyncRPCClient.ReturnPayload,
      from: VirtualIdentity
  ): Unit = {
    // let dp thread process it
    // this enqueue operation MUST happen before checking data queue.
    dataProcessor.enqueueCommand(ret, from)
    if (dataProcessor.isDataQueueEmpty) {
      dataProcessor.appendElement(UnblockForControlCommands)
    }
  }

}
