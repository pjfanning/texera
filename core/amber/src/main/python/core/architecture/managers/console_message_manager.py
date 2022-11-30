from core.util.console_message.timed_buffer import TimedBuffer


class ConsoleMessageManager:
    def __init__(self):
        self.print_buf = TimedBuffer()

    def has_message(self):
        return not self.print_buf.is_empty()

    def get_messages(self, force_flush: bool = False):
        return self.print_buf.output(force_flush)
