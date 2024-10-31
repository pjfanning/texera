# Generated by the protocol buffer compiler.  DO NOT EDIT!
# sources: edu/uci/ics/amber/engine/common/actormessage.proto, edu/uci/ics/amber/engine/common/ambermessage.proto, edu/uci/ics/amber/engine/common/virtualidentity.proto, edu/uci/ics/amber/engine/common/workflow.proto, edu/uci/ics/amber/engine/common/workflowruntimestate.proto
# plugin: python-betterproto
# This file has been @generated

from dataclasses import dataclass
from datetime import datetime
from typing import (
    Dict,
    List,
)

import betterproto

from ..architecture import (
    rpc as _architecture_rpc__,
    worker as _architecture_worker__,
)


class FatalErrorType(betterproto.Enum):
    COMPILATION_ERROR = 0
    EXECUTION_FAILURE = 1


@dataclass(eq=False, repr=False)
class WorkflowIdentity(betterproto.Message):
    id: int = betterproto.int64_field(1)


@dataclass(eq=False, repr=False)
class ExecutionIdentity(betterproto.Message):
    id: int = betterproto.int64_field(1)


@dataclass(eq=False, repr=False)
class ActorVirtualIdentity(betterproto.Message):
    name: str = betterproto.string_field(1)


@dataclass(eq=False, repr=False)
class ChannelIdentity(betterproto.Message):
    from_worker_id: "ActorVirtualIdentity" = betterproto.message_field(1)
    to_worker_id: "ActorVirtualIdentity" = betterproto.message_field(2)
    is_control: bool = betterproto.bool_field(3)


@dataclass(eq=False, repr=False)
class OperatorIdentity(betterproto.Message):
    id: str = betterproto.string_field(1)


@dataclass(eq=False, repr=False)
class PhysicalOpIdentity(betterproto.Message):
    logical_op_id: "OperatorIdentity" = betterproto.message_field(1)
    layer_name: str = betterproto.string_field(2)


@dataclass(eq=False, repr=False)
class ChannelMarkerIdentity(betterproto.Message):
    id: str = betterproto.string_field(1)


@dataclass(eq=False, repr=False)
class PortIdentity(betterproto.Message):
    id: int = betterproto.int32_field(1)
    internal: bool = betterproto.bool_field(2)


@dataclass(eq=False, repr=False)
class InputPort(betterproto.Message):
    id: "PortIdentity" = betterproto.message_field(1)
    display_name: str = betterproto.string_field(2)
    allow_multi_links: bool = betterproto.bool_field(3)
    dependencies: List["PortIdentity"] = betterproto.message_field(4)


@dataclass(eq=False, repr=False)
class OutputPort(betterproto.Message):
    id: "PortIdentity" = betterproto.message_field(1)
    display_name: str = betterproto.string_field(2)
    blocking: bool = betterproto.bool_field(3)


@dataclass(eq=False, repr=False)
class PhysicalLink(betterproto.Message):
    from_op_id: "PhysicalOpIdentity" = betterproto.message_field(1)
    from_port_id: "PortIdentity" = betterproto.message_field(2)
    to_op_id: "PhysicalOpIdentity" = betterproto.message_field(3)
    to_port_id: "PortIdentity" = betterproto.message_field(4)


@dataclass(eq=False, repr=False)
class ControlPayloadV2(betterproto.Message):
    control_invocation: "_architecture_rpc__.ControlInvocation" = (
        betterproto.message_field(1, group="value")
    )
    return_invocation: "_architecture_rpc__.ReturnInvocation" = (
        betterproto.message_field(2, group="value")
    )


@dataclass(eq=False, repr=False)
class PythonDataHeader(betterproto.Message):
    tag: "ActorVirtualIdentity" = betterproto.message_field(1)
    payload_type: str = betterproto.string_field(2)


@dataclass(eq=False, repr=False)
class PythonControlMessage(betterproto.Message):
    tag: "ActorVirtualIdentity" = betterproto.message_field(1)
    payload: "ControlPayloadV2" = betterproto.message_field(2)


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
class ExecutionBreakpointStore(betterproto.Message):
    operator_info: Dict[str, "OperatorBreakpoints"] = betterproto.map_field(
        1, betterproto.TYPE_STRING, betterproto.TYPE_MESSAGE
    )


@dataclass(eq=False, repr=False)
class EvaluatedValueList(betterproto.Message):
    values: List["_architecture_rpc__.EvaluatedValue"] = betterproto.message_field(1)


@dataclass(eq=False, repr=False)
class OperatorConsole(betterproto.Message):
    console_messages: List["_architecture_rpc__.ConsoleMessage"] = (
        betterproto.message_field(1)
    )
    evaluate_expr_results: Dict[str, "EvaluatedValueList"] = betterproto.map_field(
        2, betterproto.TYPE_STRING, betterproto.TYPE_MESSAGE
    )


@dataclass(eq=False, repr=False)
class ExecutionConsoleStore(betterproto.Message):
    operator_console: Dict[str, "OperatorConsole"] = betterproto.map_field(
        1, betterproto.TYPE_STRING, betterproto.TYPE_MESSAGE
    )


@dataclass(eq=False, repr=False)
class OperatorWorkerMapping(betterproto.Message):
    operator_id: str = betterproto.string_field(1)
    worker_ids: List[str] = betterproto.string_field(2)


@dataclass(eq=False, repr=False)
class OperatorStatistics(betterproto.Message):
    input_count: List["_architecture_worker__.PortTupleCountMapping"] = (
        betterproto.message_field(1)
    )
    output_count: List["_architecture_worker__.PortTupleCountMapping"] = (
        betterproto.message_field(2)
    )
    num_workers: int = betterproto.int32_field(3)
    data_processing_time: int = betterproto.int64_field(4)
    control_processing_time: int = betterproto.int64_field(5)
    idle_time: int = betterproto.int64_field(6)


@dataclass(eq=False, repr=False)
class OperatorMetrics(betterproto.Message):
    operator_state: "_architecture_rpc__.WorkflowAggregatedState" = (
        betterproto.enum_field(1)
    )
    operator_statistics: "OperatorStatistics" = betterproto.message_field(2)


@dataclass(eq=False, repr=False)
class ExecutionStatsStore(betterproto.Message):
    start_time_stamp: int = betterproto.int64_field(1)
    end_time_stamp: int = betterproto.int64_field(2)
    operator_info: Dict[str, "OperatorMetrics"] = betterproto.map_field(
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
class ExecutionMetadataStore(betterproto.Message):
    state: "_architecture_rpc__.WorkflowAggregatedState" = betterproto.enum_field(1)
    fatal_errors: List["WorkflowFatalError"] = betterproto.message_field(2)
    execution_id: "ExecutionIdentity" = betterproto.message_field(3)
    is_recovering: bool = betterproto.bool_field(4)


@dataclass(eq=False, repr=False)
class Backpressure(betterproto.Message):
    enable_backpressure: bool = betterproto.bool_field(1)


@dataclass(eq=False, repr=False)
class CreditUpdate(betterproto.Message):
    pass


@dataclass(eq=False, repr=False)
class ActorCommand(betterproto.Message):
    backpressure: "Backpressure" = betterproto.message_field(1, group="sealed_value")
    credit_update: "CreditUpdate" = betterproto.message_field(2, group="sealed_value")


@dataclass(eq=False, repr=False)
class PythonActorMessage(betterproto.Message):
    payload: "ActorCommand" = betterproto.message_field(1)
