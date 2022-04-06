from proto.edu.uci.ics.amber.engine.architecture.worker import InitializePortMappingV2
from .handler_base import Handler
from ..managers.context import Context


class InitializePortMappingHandler(Handler):
    cmd = InitializePortMappingV2

    def __call__(self, context: Context, command: cmd, *args, **kwargs):
        context.port_manager.set_input_ports(command.input_to_ordinal_mapping)
        context.port_manager.set_input_ports(command.output_to_ordinal_mapping)
        return None
