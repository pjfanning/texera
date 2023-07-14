package edu.uci.ics.amber.engine.architecture.worker

import akka.actor.ActorContext
import com.twitter.util.Future
import edu.uci.ics.amber.engine.architecture.messaginglayer.{NetworkInputPort, NetworkOutputPort, OutputManager}
import edu.uci.ics.amber.engine.architecture.worker.WorkerAsyncRPCService.methodMapping
import edu.uci.ics.amber.engine.architecture.worker.promisehandlers._
import edu.uci.ics.amber.engine.architecture.worker.workercallservice.WorkerCallServiceGrpc
import edu.uci.ics.amber.engine.architecture.worker.workercallservice.WorkerCallServiceGrpc.WorkerCallService
import edu.uci.ics.amber.engine.common.ambermessage.{ControlPayload, DataPayload}
import edu.uci.ics.amber.engine.common.rpc.{AsyncRPCClient, AsyncRPCServer, AsyncRPCService, CurrentThreadExecutionContext}
import edu.uci.ics.amber.engine.common.rpcwrapper.ControlPayload
import edu.uci.ics.amber.engine.common.statetransition.WorkerStateManager
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity
import edu.uci.ics.amber.engine.common.{AmberLogging, IOperatorExecutor}
import io.grpc.Channel

class WorkerAsyncRPCService(
                             val actorId: ActorVirtualIdentity,
                             val controlInputPort: NetworkInputPort[ControlPayload],
                             val dataInputPort: NetworkInputPort[DataPayload],
                             val controlOutputPort: NetworkOutputPort[ControlPayload],
                             val dataOutputPort: NetworkOutputPort[DataPayload],
                             val outputManager: OutputManager,
                             val upstreamLinkStatus: UpstreamLinkStatus,
                             val pauseManager: PauseManager,
                             val dataProcessor: DataProcessor,
                             val internalQueue: WorkerInternalQueue,
                             var operator: IOperatorExecutor,
                             val breakpointManager: BreakpointManager,
                             val stateManager: WorkerStateManager,
                             val actorContext: ActorContext,
                             val epochManager: EpochManager
) extends AsyncRPCService[WorkerCallServiceGrpc.WorkerCallService](actorId, controlOutputPort)
    with AmberLogging
    with WorkerCallServiceGrpc.WorkerCallService
    with OpenOperatorHandler
    with PauseHandler
    with AddPartitioningHandler
    with QueryAndRemoveBreakpointsHandler
    with QueryCurrentInputTupleHandler
    with QueryStatisticsHandler
    with ResumeHandler
    with StartHandler
    with UpdateInputLinkingHandler
    with AssignLocalBreakpointHandler
    with ShutdownDPThreadHandler
    with MonitoringHandler
    with SendImmutableStateHandler
    with AcceptImmutableStateHandler
    with SharePartitionHandler
    with PauseSkewMitigationHandler
    with BackpressureHandler
    with SchedulerTimeSlotEventHandler
    with FlushNetworkBufferHandler
    with WorkerEpochMarkerHandler
    with ModifyOperatorLogicHandler {
  var lastReportTime = 0L

  implicit val executionContext: CurrentThreadExecutionContext = new CurrentThreadExecutionContext()

  override def serviceMethodMapping: Map[String, Any => Future[_]] = AsyncRPCServer.getMethodMapping(this,  WorkerCallService.bindService(this, executionContext))

  override def serviceStubGen: Channel => WorkerCallService = c => WorkerCallServiceGrpc.stub(c)
}
