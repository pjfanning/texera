import socket
from threading import Event


class DebugManager:
    def __init__(self):
        self._set_breakpoint_event = Event()
        self._hit_breakpoint_event = Event()
        self._debugger_client = socket.socket(socket.AF_INET, socket.SOCK_STREAM)