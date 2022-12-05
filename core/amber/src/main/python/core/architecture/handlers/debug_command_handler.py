from loguru import logger

from proto.edu.uci.ics.amber.engine.architecture.worker import (
    DebugCommandV2,
)
from .handler_base import Handler
from ..managers.context import Context


class DebugCommandHandler(Handler):
    cmd = DebugCommandV2

    def __call__(self, context: Context, command: cmd, *args, **kwargs):
        logger.info(f"Got DebugCommand {command}")

        # translate the command with the context
        translated_command = self.translate_debug_command(command, context)

        if not context.debug_manager.talk_with_debugger:

            # initialize debugger
            logger.info("sending an emtpy string to invoke pdb")
            context.debug_manager.put_debug_command("")

            # ignore the initial message sent by pdb.
            context.debug_manager.get_debug_event()
            logger.info("pdb is up, ready to send command")

        else:
            context.main_loop._resume_dp()

        context.debug_manager.put_debug_command(translated_command)

    @staticmethod
    def translate_debug_command(command: cmd, context: Context) -> str:
        """
        This method cleans up, reformats, and then translates a debug command into
        a command that can be understood by the debugger.

        For example, it adds the UDF code context.

        :param command:
        :param context:
        :return:
        """
        debug_command, *debug_args = command.cmd.strip().split()
        module_name = context.operator_manager.operator_module_name
        if debug_command in ["b", "break"] and len(debug_args) > 0:
            # b(reak) ([filename:]lineno | function) [, condition]¶
            translated_command = f"{debug_command} {module_name}:{debug_args[0]}"
        else:
            translated_command = f"{debug_command} {' '.join(debug_args)}"
        logger.info("translated debug command " + translated_command)

        translated_command = translated_command.strip()
        return translated_command
