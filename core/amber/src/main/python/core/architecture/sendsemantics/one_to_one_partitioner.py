import typing
from typing import Iterator

from overrides import overrides

from copy import deepcopy
from core.architecture.sendsemantics.partitioner import Partitioner
from core.models import Tuple, State
from core.models.payload import OutputDataFrame, DataPayload, EndOfUpstream, StateFrame
from core.util import set_one_of
from proto.edu.uci.ics.amber.engine.architecture.sendsemantics import (
    OneToOnePartitioning,
    Partitioning,
)
from proto.edu.uci.ics.amber.engine.common import ActorVirtualIdentity


class OneToOnePartitioner(Partitioner):
    def __init__(self, partitioning: OneToOnePartitioning, worker_id: str):
        super().__init__(set_one_of(Partitioning, partitioning))
        self.batch_size = partitioning.batch_size
        self.batch: list[Tuple] = list()
        for channel in partitioning.channels:
            if channel.from_worker_id.name == worker_id:
                self.receiver = channel.to_worker_id
                break  # one to one will have only one receiver.

    @overrides
    def add_tuple_to_batch(
        self, tuple_: Tuple
    ) -> Iterator[typing.Tuple[ActorVirtualIdentity, OutputDataFrame]]:
        self.batch.append(tuple_)
        if len(self.batch) == self.batch_size:
            yield self.receiver, OutputDataFrame(frame=self.batch)
            self.reset()

    @overrides
    def add_state_to_batch(self, state: State):
        if len(self.batch) > 0:
            yield self.receiver, OutputDataFrame(frame=deepcopy(self.batch))
            self.batch.clear()

        yield self.receiver, StateFrame(frame=state.to_table())

    @overrides
    def no_more(self) -> Iterator[typing.Tuple[ActorVirtualIdentity, DataPayload]]:
        if len(self.batch) > 0:
            yield self.receiver, OutputDataFrame(frame=self.batch)
        self.reset()
        yield self.receiver, EndOfUpstream()

    @overrides
    def reset(self) -> None:
        self.batch = list()
