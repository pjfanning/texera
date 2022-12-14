import sys
from bdb import Breakpoint

from pdb import Pdb
from threading import Condition

from core.architecture.managers.operator_manager import OperatorManager
from core.models.single_blocking_io import SingleBlockingIO


class DebugManager:
    DEBUGGER = None
    def __init__(self, condition: Condition, operator_manager: OperatorManager):
        self._debug_in = SingleBlockingIO(condition)
        self._debug_out = SingleBlockingIO(condition)
        self._operator_manager = operator_manager
        DebugManager.DEBUGGER = Pdb(stdin=self._debug_in, stdout=self._debug_out,
                                    nosigint=True)
        self.debugger = DebugManager.DEBUGGER

        # Customized prompt, we can design our prompt for the debugger.
        self.debugger.prompt = ""
        self.breakpoints_managed = set()
        self.line_mapping = dict()

    def has_debug_command(self) -> bool:
        return self._debug_in.value is not None

    def has_debug_event(self) -> bool:
        return self._debug_out.value is not None

    def get_debug_event(self) -> str:
        """
        Blocking gets for the next debug event.
        :return str: the fetched event, in string format.
        """
        return self._debug_out.readline()

    def put_debug_command(self, command: str) -> None:
        """
        Puts a debug command.
        :param command: the command to be put, in string format.
        :return:
        """
        self._debug_in.write(command)
        self._debug_in.flush()

    def check_and_swap_for_static_breakpoints(self):
        if self.debugger.breaks:
            # there are dynamic breakpoints, do the swap
            code = None
            for ref, bps in list(Breakpoint.bplist.items()):
                for bp in bps:
                    code = self._operator_manager.add_breakpoint(bp)
                    self.breakpoints_managed.add(bp)
                    # TODO: change line mapping
                    self.debugger.clear_bpbynumber(bp.number)
            self._operator_manager.update_operator(code,
                                                   is_source=self._operator_manager.operator.is_source)


def breakpoint():
    DebugManager.DEBUGGER.set_trace()