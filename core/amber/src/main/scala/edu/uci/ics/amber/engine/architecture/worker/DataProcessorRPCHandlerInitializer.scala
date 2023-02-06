package edu.uci.ics.amber.engine.architecture.worker

import edu.uci.ics.amber.engine.architecture.worker.promisehandlers._
import edu.uci.ics.amber.engine.common.{AmberLogging, IOperatorExecutor}
import edu.uci.ics.amber.engine.common.rpc.{
  AsyncRPCClient,
  AsyncRPCHandlerInitializer,
  AsyncRPCServer
}

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
    with FlushNetworkBufferHandler {
  this: DataProcessor => // Force it to be a data processor
  var lastReportTime = 0L
}
