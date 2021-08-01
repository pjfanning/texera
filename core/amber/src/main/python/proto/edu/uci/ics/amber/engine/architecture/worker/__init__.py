# Generated by the protocol buffer compiler.  DO NOT EDIT!
# sources: edu/uci/ics/amber/engine/architecture/worker/controlcommands.proto, edu/uci/ics/amber/engine/architecture/worker/controlreturns.proto, edu/uci/ics/amber/engine/architecture/worker/statistics.proto
# plugin: python-betterproto
from dataclasses import dataclass

import betterproto
from betterproto.grpc.grpclib_server import ServiceBase


class WorkerState(betterproto.Enum):
    UNINITIALIZED = 0
    READY = 1
    RUNNING = 2
    PAUSED = 3
    COMPLETED = 4


@dataclass(eq=False, repr=False)
class StartWorkerV2(betterproto.Message):
    pass


@dataclass(eq=False, repr=False)
class PauseWorkerV2(betterproto.Message):
    pass


@dataclass(eq=False, repr=False)
class ResumeWorkerV2(betterproto.Message):
    pass


@dataclass(eq=False, repr=False)
class UpdateInputLinkingV2(betterproto.Message):
    identifier: "__common__.ActorVirtualIdentity" = betterproto.message_field(1)
    input_link: "__common__.LinkIdentity" = betterproto.message_field(2)


@dataclass(eq=False, repr=False)
class AddPartitioningV2(betterproto.Message):
    tag: "__common__.LinkIdentity" = betterproto.message_field(1)
    partitioning: "_sendsemantics__.Partitioning" = betterproto.message_field(2)


@dataclass(eq=False, repr=False)
class WorkerExecutionCompletedV2(betterproto.Message):
    pass


@dataclass(eq=False, repr=False)
class QueryStatisticsV2(betterproto.Message):
    pass


@dataclass(eq=False, repr=False)
class ControlCommandV2(betterproto.Message):
    start_worker: "StartWorkerV2" = betterproto.message_field(1, group="sealed_value")
    pause_worker: "PauseWorkerV2" = betterproto.message_field(2, group="sealed_value")
    resume_worker: "ResumeWorkerV2" = betterproto.message_field(3, group="sealed_value")
    add_partitioning: "AddPartitioningV2" = betterproto.message_field(
        4, group="sealed_value"
    )
    update_input_linking: "UpdateInputLinkingV2" = betterproto.message_field(
        5, group="sealed_value"
    )
    query_statistics: "QueryStatisticsV2" = betterproto.message_field(
        6, group="sealed_value"
    )
    worker_execution_completed: "WorkerExecutionCompletedV2" = (
        betterproto.message_field(101, group="sealed_value")
    )


@dataclass(eq=False, repr=False)
class WorkerStatistics(betterproto.Message):
    worker_state: "WorkerState" = betterproto.enum_field(1)
    input_tuple_count: int = betterproto.int64_field(2)
    output_tuple_count: int = betterproto.int64_field(3)


@dataclass(eq=False, repr=False)
class ControlReturnV2(betterproto.Message):
    worker_statistics: "WorkerStatistics" = betterproto.message_field(1, group="value")
    worker_state: "WorkerState" = betterproto.enum_field(2, group="value")


from .. import sendsemantics as _sendsemantics__
from ... import common as __common__
