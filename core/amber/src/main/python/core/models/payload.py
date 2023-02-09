from dataclasses import dataclass
from pyarrow.lib import Table
from typing import List

from pyarrow.lib import Schema

from core.models.tuple import Tuple

from proto.edu.uci.ics.amber.engine.common import LayerIdentity, ControlInvocationV2


@dataclass
class DataPayload:
    pass


@dataclass
class InputDataFrame(DataPayload):
    frame: Table


@dataclass
class OutputDataFrame(DataPayload):
    frame: List[Tuple]
    schema: Schema = None


@dataclass
class EndOfUpstream(DataPayload):
    pass


@dataclass
class EpochMarker(DataPayload):
    id: int
    destination: LayerIdentity
    command: ControlInvocationV2
