from typing import Union, List, Tuple

from proto.edu.uci.ics.amber.engine.architecture.worker import InitializePortMappingV2PortOrdinalPair
from proto.edu.uci.ics.amber.engine.common import LinkIdentity


class PortMap(dict):
    def __getitem__(self, item: Union[LinkIdentity, int, str]) -> Union[List[LinkIdentity], Tuple[int, str]]:
        """
        :param item:  Union[LinkIdentity, int, str], could be a LinkIdentity, a port index, or a port name.
        :return:
            - If querying for a LinkIdentity, returns the (index, name) tuple.
            - If querying for a port index or a port name, returns list of all
                LinkIdentities that match.
        """
        if isinstance(item, LinkIdentity):
            return super(PortMap, self).__getitem__(item)
        else:
            return [k for k, v in self if item in v]


class PortManager:
    def __init__(self):
        self.input_to_ordinal_mapping = PortMap()
        self.output_to_ordinal_mapping = PortMap()

    def set_input_ports(self, port_ordinal_pairs: List[InitializePortMappingV2PortOrdinalPair]):
        for pair in port_ordinal_pairs:
            link = pair.link_identity
            ordinal = pair.ordinal
            self.input_to_ordinal_mapping[link] = ordinal

    def set_output_ports(self, port_ordinal_pairs: List[InitializePortMappingV2PortOrdinalPair]):
        for pair in port_ordinal_pairs:
            link = pair.link_identity
            ordinal = pair.ordinal
            self.output_to_ordinal_mapping[link] = ordinal

    def get_input_port_index(self, link: LinkIdentity):
        return self.input_to_ordinal_mapping[link].port_index

    def get_output_port_index(self, link: LinkIdentity):
        return self.input_to_ordinal_mapping[link].port_index

    def get_input_port_name(self, link: LinkIdentity):
        return self.input_to_ordinal_mapping[link].port_name

    def get_output_port_name(self, link: LinkIdentity):
        return self.input_to_ordinal_mapping[link].port_name

    def __str__(self):
        return "inputs: " + str(self.input_to_ordinal_mapping) \
               + "\noutputs: " + str(self.output_to_ordinal_mapping)
