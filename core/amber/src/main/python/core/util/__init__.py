import uuid
from pathlib import Path

from .proto import get_one_of, set_one_of
from .customized_queue import DoubleBlockingQueue, IQueue
from .stoppable import Stoppable, StoppableQueueBlockingRunnable

__all__ = [
    "get_one_of",
    "set_one_of",
    "DoubleBlockingQueue",
    "IQueue",
    "StoppableQueueBlockingRunnable",
    "Stoppable",
]


def gen_uuid(prefix=""):
    return f"{prefix}{'-' if prefix else ''}{uuid.uuid4().hex}"


def get_root() -> Path:
    """
    hardcorded to src/main/python for now.
    :return:
    """
    path = Path(__file__)
    return path.parent.parent
