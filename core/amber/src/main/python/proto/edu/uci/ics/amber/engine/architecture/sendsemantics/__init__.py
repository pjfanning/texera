# Generated by the protocol buffer compiler.  DO NOT EDIT!
# sources: edu/uci/ics/amber/engine/architecture/sendsemantics/partitionings.proto
# plugin: python-betterproto
# This file has been @generated

from dataclasses import dataclass
from typing import List

import betterproto

from ... import common as __common__


@dataclass(eq=False, repr=False)
class Partitioning(betterproto.Message):
    one_to_one_partitioning: "OneToOnePartitioning" = betterproto.message_field(
        1, group="sealed_value"
    )
    round_robin_partitioning: "RoundRobinPartitioning" = betterproto.message_field(
        2, group="sealed_value"
    )
    hash_based_shuffle_partitioning: "HashBasedShufflePartitioning" = (
        betterproto.message_field(3, group="sealed_value")
    )
    range_based_shuffle_partitioning: "RangeBasedShufflePartitioning" = (
        betterproto.message_field(4, group="sealed_value")
    )
    broadcast_partitioning: "BroadcastPartitioning" = betterproto.message_field(
        5, group="sealed_value"
    )


@dataclass(eq=False, repr=False)
class OneToOnePartitioning(betterproto.Message):
    batch_size: int = betterproto.int32_field(1)
    receivers: List["__common__.ActorVirtualIdentity"] = betterproto.message_field(2)


@dataclass(eq=False, repr=False)
class RoundRobinPartitioning(betterproto.Message):
    batch_size: int = betterproto.int32_field(1)
    receivers: List["__common__.ActorVirtualIdentity"] = betterproto.message_field(2)


@dataclass(eq=False, repr=False)
class HashBasedShufflePartitioning(betterproto.Message):
    batch_size: int = betterproto.int32_field(1)
    receivers: List["__common__.ActorVirtualIdentity"] = betterproto.message_field(2)
    hash_column_indices: List[int] = betterproto.int32_field(3)


@dataclass(eq=False, repr=False)
class RangeBasedShufflePartitioning(betterproto.Message):
    batch_size: int = betterproto.int32_field(1)
    receivers: List["__common__.ActorVirtualIdentity"] = betterproto.message_field(2)
    range_column_indices: List[int] = betterproto.int32_field(3)
    range_min: int = betterproto.int64_field(4)
    range_max: int = betterproto.int64_field(5)


@dataclass(eq=False, repr=False)
class BroadcastPartitioning(betterproto.Message):
    batch_size: int = betterproto.int32_field(1)
    receivers: List["__common__.ActorVirtualIdentity"] = betterproto.message_field(2)
