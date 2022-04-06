from typing import Union, List

from proto.edu.uci.ics.amber.engine.architecture.worker import InitializePortMappingV2PortOrdinalPair
from proto.edu.uci.ics.amber.engine.common import LinkIdentity


class PortMap(dict):
    def __getitem__(self, item: Union[LinkIdentity, int, str]):
        if isinstance(item, LinkIdentity):
            return super(PortMap, self).__getitem__(item)
        else:
            return [k for k, v in self if item in v]


class PortManager:
    def __init__(self):
        self.input_ports = PortMap()
        self.output_ports = PortMap()

    def set_input_ports(self, port_ordinal_pairs: List[InitializePortMappingV2PortOrdinalPair]):
        for pair in port_ordinal_pairs:
            link = pair.link_identity
            ordinal = pair.ordinal
            self.input_ports[link] = ordinal

    def set_output_ports(self, port_ordinal_pairs: List[InitializePortMappingV2PortOrdinalPair]):
        for pair in port_ordinal_pairs:
            link = pair.link_identity
            ordinal = pair.ordinal
            self.output_ports[link] = ordinal

    def __str__(self):
        return "inputs: " + str(self.input_ports) + "\noutputs: " + str(self.output_ports)
