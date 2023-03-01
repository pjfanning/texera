package edu.uci.ics.amber.engine.architecture.worker.processing

import edu.uci.ics.amber.engine.architecture.worker.processing.promisehandlers._
import edu.uci.ics.amber.engine.common.AmberLogging
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCHandlerInitializer
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity

class DataProcessorRPCHandlerInitializer(val dp: DataProcessor)
    extends AsyncRPCHandlerInitializer(dp.asyncRPCClient, dp.asyncRPCServer)
    with AmberLogging
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
    with MonitoringHandler
    with AcceptImmutableStateHandler
    with SharePartitionHandler
    with PauseSkewMitigationHandler
    with BackpressureHandler
    with SchedulerTimeSlotEventHandler
    with GetReplayAlignmentHandler
    with FlushNetworkBufferHandler
    with NoOpHandler
    with ShutdownDPHandler
    with TakeCheckpointHandler
      with WorkerEpochMarkerHandler
      with ModifyOperatorLogicHandler{
  val actorId: ActorVirtualIdentity = dp.actorId
  var lastReportTime = 0L
}
