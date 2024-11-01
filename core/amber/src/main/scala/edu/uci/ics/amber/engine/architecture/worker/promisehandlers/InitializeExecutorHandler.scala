package edu.uci.ics.amber.engine.architecture.worker.promisehandlers

import com.twitter.util.Future
import edu.uci.ics.amber.engine.architecture.deploysemantics.layer.OpExecInitInfo
import edu.uci.ics.amber.engine.architecture.deploysemantics.layer.OpExecInitInfo.generateJavaOpExec
import edu.uci.ics.amber.engine.architecture.rpc.{AsyncRPCContext, InitializeExecutorRequest}
import edu.uci.ics.amber.engine.architecture.rpc.EmptyReturn
import edu.uci.ics.amber.engine.architecture.worker.DataProcessorRPCHandlerInitializer
import edu.uci.ics.amber.engine.common.{AmberRuntime, VirtualIdentityUtils}

trait InitializeExecutorHandler {
  this: DataProcessorRPCHandlerInitializer =>

  override def initializeExecutor(
      req: InitializeExecutorRequest,
      ctx: AsyncRPCContext
  ): Future[EmptyReturn] = {
    dp.serializationManager.setOpInitialization(req)
    val bytes = req.opExecInitInfo.value.toByteArray
    val opExecInitInfo: OpExecInitInfo =
      AmberRuntime.serde.deserialize(bytes, classOf[OpExecInitInfo]).get
    dp.executor = generateJavaOpExec(
      opExecInitInfo,
      VirtualIdentityUtils.getWorkerIndex(actorId),
      req.totalWorkerCount
    )
    EmptyReturn()
  }

}
