package edu.uci.ics.amber.engine.architecture.worker.processing.promisehandlers

import edu.uci.ics.amber.engine.architecture.sendsemantics.partitionings.Partitioning
import AddPartitioningHandler.AddPartitioning
import edu.uci.ics.amber.engine.architecture.worker.processing.{
  DataProcessor,
  DataProcessorRPCHandlerInitializer
}
import edu.uci.ics.amber.engine.architecture.worker.statistics.WorkerState.{PAUSED, READY, RUNNING}
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCHandlerInitializer
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.ControlCommand
import edu.uci.ics.amber.engine.common.virtualidentity.LinkIdentity

object AddPartitioningHandler {
  final case class AddPartitioning(tag: LinkIdentity, partitioning: Partitioning)
      extends ControlCommand[Unit]
}

trait AddPartitioningHandler {
  this: DataProcessorRPCHandlerInitializer =>

  registerHandler { (msg: AddPartitioning, sender) =>
    dp.stateManager.assertState(READY, RUNNING, PAUSED)
    dp.outputManager.addPartitionerWithPartitioning(msg.tag, msg.partitioning)
  }

}
