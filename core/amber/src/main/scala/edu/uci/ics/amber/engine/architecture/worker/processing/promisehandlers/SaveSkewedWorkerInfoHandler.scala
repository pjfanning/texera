package edu.uci.ics.amber.engine.architecture.worker.processing.promisehandlers

import com.twitter.util.Future
import SaveSkewedWorkerInfoHandler.SaveSkewedWorkerInfo
import edu.uci.ics.amber.engine.architecture.worker.processing.{DataProcessor, DataProcessorRPCHandlerInitializer}
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.ControlCommand
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity
import edu.uci.ics.texera.workflow.operators.sortPartitions.SortPartitionOpExec

/**
  * The skewed worker of mutable state operators like sort uses this message to notify
  * the helper worker about the corresponding skewed worker. The helper uses this info
  * to send state back to the skewed worker at the end.
  *
  * Possible sender: Skewed worker (SendImmutableStateOrNotifyHelperHandler).
  */
object SaveSkewedWorkerInfoHandler {
  final case class SaveSkewedWorkerInfo(
      skewedWorkerId: ActorVirtualIdentity
  ) extends ControlCommand[Boolean]
}

trait SaveSkewedWorkerInfoHandler {
  this: DataProcessorRPCHandlerInitializer =>

  registerHandler { (cmd: SaveSkewedWorkerInfo, sender) =>
    if (dp.operator.isInstanceOf[SortPartitionOpExec]) {
      dp.operator.asInstanceOf[SortPartitionOpExec].skewedWorkerIdentity = cmd.skewedWorkerId
      Future.True
    } else {
      Future.False
    }
  }
}
