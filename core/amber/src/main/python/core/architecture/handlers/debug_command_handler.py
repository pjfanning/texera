import inspect


from loguru import logger

from proto.edu.uci.ics.amber.engine.architecture.worker import DebugCommandV2
from .handler_base import Handler
from ..managers.context import Context
from ...models.tdb import Tdb


class DebugCommandHandler(Handler):
    cmd = DebugCommandV2

    def __call__(self, context: Context, command: cmd, *args, **kwargs):
        debug_input_queue = context.dp.data_processor.debug_input_queue
        old_notifiable = context.dp.data_processor.notifiable.is_set()
        command, *args = command.cmd.split()
        if command in Tdb.resume_commands:
            context.dp._resume(mode=command)
            return

        # pause the execution, switch to communication with pdb
        if context.dp.data_processor.notifiable.is_set():
            context.dp._pause()

        # re-format the command with the context
        module_name = dict(inspect.getmembers(context.dp._operator.get()))['__module__']
        formatted_command = f"{command} {' '.join(args)}"
        if command in ["b", "break"] and len(args) > 0:
            formatted_command = f"b {module_name}:{args[0]}"

        # send to pdb
        logger.info(f"sending command to pdb: {formatted_command}")
        debug_input_queue.put(f"{formatted_command}\n")

        # reverse back to the original communication channel
        if old_notifiable:
            context.dp._resume()

        return None
