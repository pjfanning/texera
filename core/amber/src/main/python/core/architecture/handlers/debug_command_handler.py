from loguru import logger

from proto.edu.uci.ics.amber.engine.architecture.worker import DebugCommandV2
from .handler_base import Handler
from ..managers.context import Context


class DebugCommandHandler(Handler):
    cmd = DebugCommandV2

    def __call__(self, context: Context, command: cmd, *args, **kwargs):
        logger.error(command)
        return None
