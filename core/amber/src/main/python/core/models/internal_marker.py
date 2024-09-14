from dataclasses import dataclass


from proto.edu.uci.ics.amber.engine.common import ChannelIdentity


@dataclass
class InternalMarker:
    """
    A special Data Message, only being generated in un-packaging a batch into Tuples.
    Markers retain the order information and served as a indicator of data state.
    """

    pass


@dataclass
class SenderChange(InternalMarker):
    channel_id: ChannelIdentity

@dataclass
class StartOfAny(InternalMarker):
    pass

@dataclass
class EndOfAll(InternalMarker):
    pass

@dataclass
class InputInitialized(InternalMarker):
    pass

@dataclass
class InputExhausted(InternalMarker):
    pass


