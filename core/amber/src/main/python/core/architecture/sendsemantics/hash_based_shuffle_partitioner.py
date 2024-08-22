import typing
from typing import Iterator

from loguru import logger
from overrides import overrides
from copy import deepcopy
from core.architecture.sendsemantics.partitioner import Partitioner
from core.models import Tuple, State
from core.models.marker import EndOfUpstream
from core.util import set_one_of
from proto.edu.uci.ics.amber.engine.architecture.sendsemantics import (
    HashBasedShufflePartitioning,
    Partitioning,
)
from proto.edu.uci.ics.amber.engine.common import ActorVirtualIdentity


class HashBasedShufflePartitioner(Partitioner):
    def __init__(self, partitioning: HashBasedShufflePartitioning):
        super().__init__(set_one_of(Partitioning, partitioning))
        logger.debug(f"got {partitioning}")
        self.batch_size = partitioning.batch_size
        self.receivers = [
            (receiver, [])
            for receiver in {channel.to_worker_id for channel in partitioning.channels}
        ]
        self.hash_attribute_names = partitioning.hash_attribute_names

    @overrides
    def add_tuple_to_batch(
        self, tuple_: Tuple
    ) -> Iterator[typing.Tuple[ActorVirtualIdentity, typing.List[Tuple]]]:
        partial_tuple = (
            tuple_
            if not self.hash_attribute_names
            else tuple_.get_partial_tuple(self.hash_attribute_names)
        )
        hash_code = hash(partial_tuple) % len(self.receivers)
        receiver, batch = self.receivers[hash_code]
        batch.append(tuple_)
        if len(batch) == self.batch_size:
            yield receiver, batch
            self.receivers[hash_code] = (receiver, list())

    @overrides
    def add_state_to_batch(self, state: State):
        for receiver, batch in self.receivers:
            if len(batch) > 0:
                yield receiver, deepcopy(batch)
            yield receiver, deepcopy(batch)
            batch.clear()
            yield receiver, state

    @overrides
    def no_more(
        self,
    ) -> Iterator[
        typing.Tuple[
            ActorVirtualIdentity, typing.Union[EndOfUpstream, typing.List[Tuple]]
        ]
    ]:
        for receiver, batch in self.receivers:
            if len(batch) > 0:
                yield receiver, batch
            yield receiver, EndOfUpstream()
