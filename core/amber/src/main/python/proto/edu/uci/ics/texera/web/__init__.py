# Generated by the protocol buffer compiler.  DO NOT EDIT!
# sources: edu/uci/ics/texera/workflowruntimestate.proto
# plugin: python-betterproto
from dataclasses import dataclass
from datetime import datetime
from typing import Dict, List

import betterproto
from betterproto.grpc.grpclib_server import ServiceBase


class FatalErrorType(betterproto.Enum):
    COMPILATION_ERROR = 0
    EXECUTION_FAILURE = 1


class WorkflowAggregatedState(betterproto.Enum):
    UNINITIALIZED = 0
    READY = 1
    RUNNING = 2
    PAUSING = 3
    PAUSED = 4
    RESUMING = 5
    COMPLETED = 6
    FAILED = 7
    UNKNOWN = 8
    KILLED = 9


@dataclass(eq=False, repr=False)
class BreakpointFault(betterproto.Message):
    worker_name: str = betterproto.string_field(1)
    faulted_tuple: "BreakpointFaultBreakpointTuple" = betterproto.message_field(2)


@dataclass(eq=False, repr=False)
class BreakpointFaultBreakpointTuple(betterproto.Message):
    id: int = betterproto.int64_field(1)
    is_input: bool = betterproto.bool_field(2)
    tuple: List[str] = betterproto.string_field(3)


@dataclass(eq=False, repr=False)
class OperatorBreakpoints(betterproto.Message):
    unresolved_breakpoints: List["BreakpointFault"] = betterproto.message_field(1)


@dataclass(eq=False, repr=False)
class JobBreakpointStore(betterproto.Message):
    operator_info: Dict[str, "OperatorBreakpoints"] = betterproto.map_field(
        1, betterproto.TYPE_STRING, betterproto.TYPE_MESSAGE
    )


@dataclass(eq=False, repr=False)
class EvaluatedValueList(betterproto.Message):
    values: List[
        "__amber_engine_architecture_worker__.EvaluatedValue"
    ] = betterproto.message_field(1)


@dataclass(eq=False, repr=False)
class OperatorConsole(betterproto.Message):
    console_messages: List[
        "__amber_engine_architecture_worker__.ConsoleMessage"
    ] = betterproto.message_field(1)
    evaluate_expr_results: Dict[str, "EvaluatedValueList"] = betterproto.map_field(
        2, betterproto.TYPE_STRING, betterproto.TYPE_MESSAGE
    )


@dataclass(eq=False, repr=False)
class JobConsoleStore(betterproto.Message):
    operator_console: Dict[str, "OperatorConsole"] = betterproto.map_field(
        1, betterproto.TYPE_STRING, betterproto.TYPE_MESSAGE
    )


@dataclass(eq=False, repr=False)
class OperatorWorkerMapping(betterproto.Message):
    operator_id: str = betterproto.string_field(1)
    worker_ids: List[str] = betterproto.string_field(2)


@dataclass(eq=False, repr=False)
class OperatorRuntimeStats(betterproto.Message):
    state: "WorkflowAggregatedState" = betterproto.enum_field(1)
    input_count: int = betterproto.int64_field(2)
    output_count: int = betterproto.int64_field(3)


@dataclass(eq=False, repr=False)
class JobStatsStore(betterproto.Message):
    start_time_stamp: int = betterproto.int64_field(1)
    end_time_stamp: int = betterproto.int64_field(2)
    operator_info: Dict[str, "OperatorRuntimeStats"] = betterproto.map_field(
        3, betterproto.TYPE_STRING, betterproto.TYPE_MESSAGE
    )
    operator_worker_mapping: List["OperatorWorkerMapping"] = betterproto.message_field(
        4
    )


@dataclass(eq=False, repr=False)
class WorkflowFatalError(betterproto.Message):
    type: "FatalErrorType" = betterproto.enum_field(1)
    timestamp: datetime = betterproto.message_field(2)
    message: str = betterproto.string_field(3)
    details: str = betterproto.string_field(4)
    operator_id: str = betterproto.string_field(5)
    worker_id: str = betterproto.string_field(6)


@dataclass(eq=False, repr=False)
class JobMetadataStore(betterproto.Message):
    state: "WorkflowAggregatedState" = betterproto.enum_field(1)
    fatal_errors: List["WorkflowFatalError"] = betterproto.message_field(2)
    eid: int = betterproto.int64_field(3)
    is_recovering: bool = betterproto.bool_field(4)


from ...amber.engine.architecture import worker as __amber_engine_architecture_worker__
