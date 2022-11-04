import inspect

from loguru import logger

from proto.edu.uci.ics.amber.engine.architecture.worker import ModifyOperatorLogicV2
from .handler_base import Handler
from ..managers.context import Context
from ...models import Operator
from ...util.operator import load_operator


class ModifyOperatorLogicHandler(Handler):
    cmd = ModifyOperatorLogicV2

    def __call__(self, context: Context, command: cmd, *args, **kwargs):
        original_internal_state = context.dp._operator.get().__dict__
        operator: type(Operator) = load_operator(command.code)
        context.dp._operator.set(operator())
        context.dp._operator.get().is_source = command.is_source
        # overwrite the internal state
        context.dp._operator.get().__dict__ = original_internal_state

        logger.add(
            context.dp._print_log_handler,
            level="PRINT",
            filter=dict(inspect.getmembers(operator))["__module__"],
            format="{message}",
        )
        return None
