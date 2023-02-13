package edu.uci.ics.amber.engine.architecture.worker.promisehandlers

import edu.uci.ics.amber.engine.architecture.worker.{
  DataProcessor,
  PauseType
}
import edu.uci.ics.amber.engine.architecture.worker.promisehandlers.AcceptMutableStateHandler.AcceptMutableState
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.ControlCommand
import edu.uci.ics.texera.workflow.common.tuple.Tuple
import edu.uci.ics.texera.workflow.operators.hashJoin.HashJoinOpExec
import edu.uci.ics.texera.workflow.operators.sortPartitions.SortPartitionOpExec

import scala.collection.mutable.ArrayBuffer

/**
  * This handler is used to receive the mutable state migrated during Reshape.
  * e.g., A helper worker of Sort will send an `AcceptMutableState`
  * message to skewed worker and the tuples will be in the
  * message.
  *
  * Possible sender: Helper worker of the same operator as this worker.
  * (SendMutableStateHandler)
  */
object AcceptMutableStateHandler {
  final case class AcceptMutableState(
      tuples: ArrayBuffer[Tuple],
      totalMessagesToExpect: Int
  ) extends ControlCommand[Boolean]
}

trait AcceptMutableStateHandler {
  this: DataProcessor =>

  registerHandler { (cmd: AcceptMutableState, sender) =>
    try {
      val canResume = operator
        .asInstanceOf[SortPartitionOpExec]
        .mergeIntoStoredTuplesList(cmd.tuples, cmd.totalMessagesToExpect)

      if (canResume && pauseManager.getPauseStatusByType(PauseType.OperatorLogicPause)) {
        // All tuples have been received. The worker is paused due to operator logic
        // and not due to user pressing pause
        pauseManager.recordRequest(PauseType.OperatorLogicPause, false)
        setCurrentOutputIterator(
          operator
            .asInstanceOf[SortPartitionOpExec]
            .sortTuples()
        )
      }

      true
    } catch {
      case exception: Exception =>
        logger.error("Reshape: ", exception)
        false
    }
  }
}
