from threading import Event, Condition
from typing import Optional, Union, Tuple, Iterator

from core.models.marker import State, Marker
from proto.edu.uci.ics.amber.engine.common import PortIdentity


class TupleProcessingManager:
    def __init__(self):
        self.current_input_marker: Optional[Marker] = None
        self.current_input_tuple: Optional[Tuple] = None
        self.current_input_port_id: Optional[PortIdentity] = None
        self.current_input_iter: Optional[Iterator[Union[Tuple, Marker]]] = None
        self.current_output_state: Optional[State] = None
        self.current_output_tuple: Optional[Tuple] = None
        self.context_switch_condition: Condition = Condition()
        self.finished_current: Event = Event()

    def get_output_tuple(self) -> Optional[Tuple]:
        ret, self.current_output_tuple = self.current_output_tuple, None
        return ret

    def get_output_state(self) -> Optional[State]:
        ret, self.current_output_state = self.current_output_state, None
        return ret

    def get_input_marker(self) -> Optional[State]:
        ret, self.current_input_marker = self.current_input_marker, None
        return ret

    def get_input_port(self) -> int:
        port_id = self.current_input_port_id
        port: int
        if port_id is None:
            # no upstream, special case for source executor.
            port = 0
        else:
            port = port_id.id
        return port
