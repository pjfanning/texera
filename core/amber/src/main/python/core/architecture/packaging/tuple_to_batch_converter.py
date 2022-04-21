import typing
from collections import OrderedDict
from itertools import chain

import pyarrow
from loguru import logger
from typing import Iterable, Iterator

from pyarrow.lib import Schema

from core.architecture.managers.port_manager import PortManager
from core.architecture.sendsemantics.hash_based_shuffle_partitioner import (
    HashBasedShufflePartitioner,
)
from core.architecture.sendsemantics.one_to_one_partitioner import OneToOnePartitioner
from core.architecture.sendsemantics.partitioner import Partitioner
from core.architecture.sendsemantics.round_robin_partitioner import (
    RoundRobinPartitioner,
)
from core.models import Tuple
from core.models.payload import OutputDataFrame, DataPayload
from core.models.tuple import PortConfig
from core.util import get_one_of
from proto.edu.uci.ics.amber.engine.architecture.sendsemantics import (
    HashBasedShufflePartitioning,
    OneToOnePartitioning,
    Partitioning,
    RoundRobinPartitioning,
)
from proto.edu.uci.ics.amber.engine.common import ActorVirtualIdentity, LinkIdentity


class TupleToBatchConverter:
    def __init__(self, port_manager: PortManager):
        self._port_manager = port_manager
        self._partitioners: OrderedDict[LinkIdentity, Partitioner] = OrderedDict()
        self._partitioning_to_partitioner: dict[
            type(Partitioning), type(Partitioner)
        ] = {
            OneToOnePartitioning: OneToOnePartitioner,
            RoundRobinPartitioning: RoundRobinPartitioner,
            HashBasedShufflePartitioning: HashBasedShufflePartitioner,
        }

    def add_partitioning(self, tag: LinkIdentity, partitioning: Partitioning) -> None:
        """
        Add down stream operator and its transfer policy
        :param tag:
        :param partitioning:
        :return:
        """
        the_partitioning = get_one_of(partitioning)
        logger.debug(f"adding {the_partitioning}")
        partitioner: type = self._partitioning_to_partitioner[type(the_partitioning)]
        self._partitioners.update({tag: partitioner(the_partitioning)})

    def tuple_to_batch(
        self, tuple_: Tuple, config: PortConfig
    ) -> Iterator[typing.Tuple[ActorVirtualIdentity, OutputDataFrame]]:
        schema = self._port_manager.get_output_port_schema(config.port_ordinal)
        self.cast_tuple_to_match_schema(tuple_, schema)

        output_links = self._port_manager.get_output_links(config.port_ordinal)
        schema = self._port_manager.get_output_port_schema(config.port_ordinal)

        def add_schema(batch_with_reciever):
            to, batch = batch_with_reciever
            batch.schema = schema
            return to, batch

        partitioners = map(lambda link: self._partitioners.get(link), output_links)
        yield from chain(
            *(
                map(add_schema, partitioner.add_tuple_to_batch(tuple_))
                for partitioner in partitioners
            )
        )

    def emit_end_of_upstream(
        self,
    ) -> Iterable[typing.Tuple[ActorVirtualIdentity, DataPayload]]:
        for (
            port_ordinal
        ) in self._port_manager.output_to_ordinal_mapping.get_port_ordinals():
            output_links = self._port_manager.get_output_links(port_ordinal)
            schema = self._port_manager.get_output_port_schema(port_ordinal)
            partitioners = map(lambda link: self._partitioners.get(link), output_links)

            def add_schema(batch_with_reciever):
                to, batch = batch_with_reciever
                batch.schema = schema
                return to, batch

            yield from chain(
                *(
                    map(add_schema, partitioner.no_more())
                    for partitioner in partitioners
                )
            )

    @staticmethod
    def cast_tuple_to_match_schema(output_tuple, schema):
        # TODO: find a better place for this function.
        #   right now placed here since a tuple only knows which port to go after
        #   being yield out. Only with the port it can find the corresponding Schema
        #   to cast.
        # right now only support casting ANY to binary.
        import pickle

        for field_name in output_tuple.get_field_names():
            field_value = output_tuple[field_name]
            field = schema.field(field_name)
            field_type = field.type if field is not None else None
            if field_type == pyarrow.binary():
                output_tuple[field_name] = b"pickle    " + pickle.dumps(field_value)
