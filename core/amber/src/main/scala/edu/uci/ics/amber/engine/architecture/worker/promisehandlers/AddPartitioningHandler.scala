package edu.uci.ics.amber.engine.architecture.worker.promisehandlers

import edu.uci.ics.amber.engine.architecture.sendsemantics.partitionings.Partitioning
import edu.uci.ics.amber.engine.architecture.worker.DataProcessorRPCHandlerInitializer
import edu.uci.ics.amber.engine.architecture.worker.promisehandlers.AddPartitioningHandler.AddPartitioning
import edu.uci.ics.amber.engine.architecture.worker.statistics.WorkerState.{PAUSED, READY, RUNNING}
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.ControlCommand
import edu.uci.ics.amber.engine.common.workflow.PhysicalLink
import edu.uci.ics.texera.workflow.common.tuple.schema.Schema

object AddPartitioningHandler {
  final case class AddPartitioning(tag: PhysicalLink, partitioning: Partitioning, schema:Schema)
      extends ControlCommand[Unit]
}

trait AddPartitioningHandler {
  this: DataProcessorRPCHandlerInitializer =>

  registerHandler { (msg: AddPartitioning, sender) =>
    dp.stateManager.assertState(READY, RUNNING, PAUSED)
    dp.outputManager.addPartitionerWithPartitioning(msg.tag, msg.partitioning, msg.schema)
  }

}
