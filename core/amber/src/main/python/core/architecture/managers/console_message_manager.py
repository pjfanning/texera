from core.util.buffer.timed_buffer import TimedBuffer
from proto.edu.uci.ics.amber.engine.architecture.worker import ConsoleMessage


class ConsoleMessageManager:
    def __init__(self):
        self.msg_buf = TimedBuffer()

    def get_messages(self, force_flush: bool = False):
        return self.msg_buf.get(force_flush)

    def put_message(self, msg: ConsoleMessage) -> None:
        self.msg_buf.put(msg)
