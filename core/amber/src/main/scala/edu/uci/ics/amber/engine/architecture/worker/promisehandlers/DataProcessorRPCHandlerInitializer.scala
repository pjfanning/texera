package edu.uci.ics.amber.engine.architecture.worker.promisehandlers

import edu.uci.ics.amber.engine.architecture.worker.DataProcessor
import edu.uci.ics.amber.engine.common.AmberLogging
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCHandlerInitializer

trait DataProcessorRPCHandlerInitializer
    extends AsyncRPCHandlerInitializer
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
    with SendImmutableStateOrNotifyHelperHandler
    with AcceptImmutableStateHandler
    with SharePartitionHandler
    with PauseSkewMitigationHandler
    with BackpressureHandler
    with SaveSkewedWorkerInfoHandler
    with AcceptMutableStateHandler
    with SchedulerTimeSlotEventHandler
    with GetReplayAlignmentHandler
    with FlushNetworkBufferHandler
    with NoOpHandler
    with ShutdownDPHandler
    with TakeCheckpointHandler {
  val dp:DataProcessor
  var lastReportTime = 0L
}
