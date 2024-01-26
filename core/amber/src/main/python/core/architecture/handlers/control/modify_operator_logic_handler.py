from proto.edu.uci.ics.amber.engine.architecture.worker import ModifyOperatorLogicV2
from core.architecture.handlers.control.control_handler_base import ControlHandler
from core.architecture.managers.context import Context

from loguru import logger
class ModifyOperatorLogicHandler(ControlHandler):
    cmd = ModifyOperatorLogicV2

    def __call__(self, context: Context, command: cmd, *args, **kwargs):
        logger.info("updating!!!!")
        context.operator_manager.update_operator(command.code, command.is_source)
        return None
