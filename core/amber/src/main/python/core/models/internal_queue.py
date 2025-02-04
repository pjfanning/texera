from __future__ import annotations

from dataclasses import dataclass
from enum import Enum
from threading import RLock
from typing import TypeVar, Set

from core.models.internal_marker import InternalMarker
from core.models.payload import DataPayload
from core.util.customized_queue.linked_blocking_multi_queue import (
    LinkedBlockingMultiQueue,
)
from core.util.customized_queue.queue_base import IQueue, QueueElement
from proto.edu.uci.ics.amber.core import ActorVirtualIdentity, ChannelIdentity
from proto.edu.uci.ics.amber.engine.architecture.rpc import ChannelMarkerPayload
from proto.edu.uci.ics.amber.engine.common import ControlPayloadV2


@dataclass
class InternalQueueElement(QueueElement):
    tag: ChannelIdentity


@dataclass
class DataElement(InternalQueueElement):
    payload: DataPayload


@dataclass
class ControlElement(InternalQueueElement):
    payload: ControlPayloadV2


@dataclass
class ChannelMarkerElement(InternalQueueElement):
    payload: ChannelMarkerPayload


T = TypeVar("T", bound=InternalQueueElement)


class InternalQueue(IQueue):

    class DisableType(Enum):
        DISABLE_BY_PAUSE = 1
        DISABLE_BY_BACKPRESSURE = 2

    def __init__(self):
        self._queue = LinkedBlockingMultiQueue()
        self._queue.add_sub_queue("SYSTEM", 0)
        self._queue_ids: Set[ChannelIdentity] = set()
        self._queue_state: Set[InternalQueue.DisableType] = set()
        self._lock = RLock()

    def is_empty(self, key=None) -> bool:
        return self._queue.is_empty(key)

    def get(self) -> T:
        return self._queue.get()

    def put(self, item: T) -> None:
        if issubclass(item, InternalQueueElement):
            queue_id = str(item.tag)
            if item.tag not in self._queue_ids:
                self._queue.add_sub_queue(queue_id, 1 if item.tag.is_control else 2)
                self._queue_ids.add(item.tag)
            if isinstance(item, (DataElement, InternalMarker)):
                self._queue.put(queue_id, item)
            elif isinstance(item, ControlElement):
                self._queue.put(queue_id, item)
            else:
                raise ValueError(f"item {item} is not recognized by internal queue")
        else:
            self._queue.put("SYSTEM", item)

    def disable(self, channel_id: ChannelIdentity) -> None:
        self._queue.disable(str(channel_id))

    def enable(self, channel_id: ChannelIdentity) -> None:
        self._queue.enable(str(channel_id))

    def is_control_empty(self) -> bool:
        return all(
            self.is_empty(str(queue_id))
            for queue_id in self._queue_ids
            if queue_id.is_control
        )

    def is_data_empty(self) -> bool:
        return all(
            self.is_empty(str(queue_id))
            for queue_id in self._queue_ids
            if not queue_id.is_control
        )

    def __len__(self) -> int:
        return self.size()

    def size(self) -> int:
        return self._queue.size()

    def size_control(self) -> int:
        return sum(
            self._queue.size(str(queue_id))
            for queue_id in self._queue_ids
            if queue_id.is_control
        )

    def size_data(self) -> int:
        return sum(
            self._queue.size(str(queue_id))
            for queue_id in self._queue_ids
            if not queue_id.is_control
        )

    def enable_data(self, disable_type: DisableType) -> bool:
        with self._lock:
            if disable_type in self._queue_state:
                self._queue_state.remove(disable_type)
            if self._queue_state:
                return False
            for queue_id in self._queue_ids:
                if not queue_id.is_control:
                    self._queue.enable(str(queue_id))
            return True

    def disable_data(self, disable_type: DisableType) -> None:
        with self._lock:
            self._queue_state.add(disable_type)
            for queue_id in self._queue_ids:
                if not queue_id.is_control:
                    self._queue.enable(str(queue_id))

    def in_mem_size(self) -> int:
        return sum(
            self._queue.in_mem_size(str(queue_id))
            for queue_id in self._queue_ids
            if not queue_id.is_control
        )

    def is_data_enabled(self) -> bool:
        return any(
            self._queue.is_enabled(str(queue_id))
            for queue_id in self._queue_ids
            if not queue_id.is_control
        )
