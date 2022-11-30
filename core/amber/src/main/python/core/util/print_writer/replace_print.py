import builtins
from contextlib import redirect_stdout
from io import StringIO


class replace_print:
    def __init__(self, callback, *args, **kwargs):
        self.callback = callback
        self.args = args
        self.kwargs = kwargs
        self.builtins_print = builtins.print

    def __enter__(self):
        def wrapped_print(*args, **kwargs):
            with StringIO() as buf, redirect_stdout(buf):
                self.builtins_print(*args, **kwargs)
                message = buf.getvalue()
                self.callback(message, *self.args, **self.kwargs)

        builtins.print = wrapped_print

    def __exit__(self, exc_type, exc_val, exc_tb):
        builtins.print = self.builtins_print


