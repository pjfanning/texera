import bdb
from pdb import Pdb
from queue import Queue
from types import FrameType
from typing import Protocol, IO

from core.util import set_one_of
from proto.edu.uci.ics.amber.engine.architecture.worker import ControlCommandV2, \
    PythonConsoleMessageV2
from proto.edu.uci.ics.amber.engine.common import ActorVirtualIdentity


class QueueMixin(Protocol):
    queue: Queue


class QueueIn(IO, QueueMixin):
    def __init__(self, queue=Queue()):
        self.queue = queue

    def readline(self, limit=None):
        return self.queue.get()


class QueueOut(IO, QueueMixin):
    def __init__(self, async_rpc_client):
        self.buf = ""
        self._async_rpc_client = async_rpc_client

    def write(self, s):
        self.buf += s

    def flush(self):
        s, self.buf = self.buf, ""
        import datetime
        control_command = set_one_of(ControlCommandV2,
                                     PythonConsoleMessageV2(datetime.datetime.now(),
                                                            "DEBUGGER", s))
        self._async_rpc_client.send(ActorVirtualIdentity(name="CONTROLLER"),
                                    control_command)


class Tdb(Pdb):
    # a list of all commands that will trigger resume. different from Pdb.commands_resuming
    resume_commands = ['c', 'cont', 'continue', 'n', 'next', 'unt', 'until', 's',
                       'step', 'r', 'return']

    def __init__(self, stdin: QueueIn, stdout: QueueOut, switch_channel):
        super().__init__(stdin=stdin, stdout=stdout)
        self.switch_channel = switch_channel

    def user_call(self, frame, argument_list):
        self.switch_channel()
        return super(Tdb, self).user_call(frame, argument_list)

    def user_line(self, frame: FrameType) -> None:
        self.switch_channel()
        return super(Tdb, self).user_line(frame)

    def user_return(self, frame, return_value):
        self.switch_channel()
        return super(Tdb, self).user_return(frame, return_value)

    def user_exception(self, frame, exc_info):
        self.switch_channel()
        return super(Tdb, self).user_exception(frame, exc_info)

    def do_clear(self, arg):
        if not arg:
            bplist = [bp for bp in bdb.Breakpoint.bpbynumber if bp]
            self.clear_all_breaks()
            for bp in bplist:
                self.message('Deleted %s' % bp)
            return
        if ':' in arg:
            # Make sure it works for "clear C:\foo\bar.py:12"
            i = arg.rfind(':')
            filename = arg[:i]
            arg = arg[i + 1:]
            try:
                lineno = int(arg)
            except ValueError:
                err = "Invalid line number (%s)" % arg
            else:
                bplist = self.get_breaks(filename, lineno)[:]
                err = self.clear_break(filename, lineno)
            if err:
                self.error(err)
            else:
                for bp in bplist:
                    self.message('Deleted %s' % bp)
            return
        numberlist = arg.split()
        for i in numberlist:
            try:
                bp = self.get_bpbynumber(i)
            except ValueError as err:
                self.error(err)
            else:
                self.clear_bpbynumber(i)
                self.message('Deleted %s' % bp)

    do_cl = do_clear  # 'c' is already an abbreviation for 'continue'
