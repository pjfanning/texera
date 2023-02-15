package edu.uci.ics.amber.engine.architecture.worker.processing.promisehandlers

import com.twitter.util.Future
<<<<<<<< HEAD:core/amber/src/main/scala/edu/uci/ics/amber/engine/architecture/worker/processing/promisehandlers/SendImmutableStateOrNotifyHelperHandler.scala
import AcceptImmutableStateHandler.AcceptImmutableState
import SaveSkewedWorkerInfoHandler.SaveSkewedWorkerInfo
import SendImmutableStateOrNotifyHelperHandler.SendImmutableStateOrNotifyHelper
import edu.uci.ics.amber.engine.architecture.worker.processing.{DataProcessor, DataProcessorRPCHandlerInitializer}
========
import edu.uci.ics.amber.engine.architecture.worker.WorkerAsyncRPCHandlerInitializer
import edu.uci.ics.amber.engine.architecture.worker.promisehandlers.AcceptImmutableStateHandler.AcceptImmutableState
import edu.uci.ics.amber.engine.architecture.worker.promisehandlers.SendImmutableStateHandler.SendImmutableState
>>>>>>>> shengquan-oom-fix:core/amber/src/main/scala/edu/uci/ics/amber/engine/architecture/worker/processing/promisehandlers/SendImmutableStateHandler.scala
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.ControlCommand
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity
import edu.uci.ics.texera.workflow.operators.hashJoin.HashJoinOpExec

import scala.collection.mutable.ArrayBuffer

/**
  * This handler is used to do state migration during Reshape for immutable state operator
  * like HashJoin.
  * e.g., The controller will send a `SendImmutableState` message to
  * a skewed worker of HashJoin operator to send its build hash map
  * to `helperReceiverId` worker.
  *
  * Possible sender: Controller (SkewDetectionHandler).
  */
object SendImmutableStateHandler {
  final case class SendImmutableState(
      helperReceiverId: ActorVirtualIdentity
  ) extends ControlCommand[Boolean]
}

<<<<<<<< HEAD:core/amber/src/main/scala/edu/uci/ics/amber/engine/architecture/worker/processing/promisehandlers/SendImmutableStateOrNotifyHelperHandler.scala
trait SendImmutableStateOrNotifyHelperHandler {
  this: DataProcessorRPCHandlerInitializer =>

  registerHandler { (cmd: SendImmutableStateOrNotifyHelper, sender) =>
    dp.operator match {
========
trait SendImmutableStateHandler {
  this: WorkerAsyncRPCHandlerInitializer =>

  registerHandler { (cmd: SendImmutableState, sender) =>
    dataProcessor.getOperatorExecutor() match {
>>>>>>>> shengquan-oom-fix:core/amber/src/main/scala/edu/uci/ics/amber/engine/architecture/worker/processing/promisehandlers/SendImmutableStateHandler.scala
      case joinOpExec: HashJoinOpExec[_] =>
        // Returns true if the build table was replicated successfully in case of HashJoin.
        try {
          if (joinOpExec.isBuildTableFinished) {
            val immutableStates = joinOpExec.getBuildHashTableBatches()
            val immutableStatesSendingFutures = new ArrayBuffer[Future[Boolean]]()
            immutableStates.foreach(map => {
              immutableStatesSendingFutures.append(
                send(AcceptImmutableState(map), cmd.helperReceiverId)
              )
            })
            Future
              .collect(immutableStatesSendingFutures)
              .flatMap(seq => {
                if (!seq.contains(false)) {
                  logger.info(
                    s"Reshape: Replication of all parts of build table done to ${cmd.helperReceiverId}"
                  )
                  Future.True
                } else {
                  Future.False
                }
              })
          } else {
            Future.False
          }
        } catch {
          case exception: Exception =>
            logger.error("Reshape exception: ", exception)
            Future.False
        }
      case _ =>
        // This case shouldn't happen
        Future.False
    }
  }
}
