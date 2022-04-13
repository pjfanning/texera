from typing import Union, List

from proto.edu.uci.ics.amber.engine.architecture.worker import InitializePortMappingV2PortInfoPair, \
    InitializePortMappingV2PortInfoPairPortInfo
from proto.edu.uci.ics.amber.engine.common import LinkIdentity


class PortMap(dict):
    def __getitem__(self, item: Union[LinkIdentity, int, str]) -> \
            Union[List[LinkIdentity], InitializePortMappingV2PortInfoPairPortInfo]:
        """
        :param item:  Union[LinkIdentity, int, str], could be a LinkIdentity, a port ordinal, or a port name.
        :return:
            - If querying for a LinkIdentity, returns the InitializePortMappingV2PortInfoPairPortInfo(ordinal, name).
            - If querying for a port ordinal or a port name, returns list of all
                LinkIdentities that match.
        """
        if isinstance(item, LinkIdentity):
            return super(PortMap, self).__getitem__(item)
        else:
            return [k for k, v in self if item in v]

    def get_port_info(self, link: LinkIdentity) -> InitializePortMappingV2PortInfoPairPortInfo:
        return self[link]

    def get_links_by_port_index(self, index: int) -> List[LinkIdentity]:
        return self.get(index, [])

    def get_links_by_port_name(self, name: str) -> List[LinkIdentity]:
        return self.get(name, [])


class PortManager:
    def __init__(self):
        self.input_to_ordinal_mapping = PortMap()
        self.output_to_ordinal_mapping = PortMap()

    def set_input_ports(self, port_info_pairs: List[InitializePortMappingV2PortInfoPair]) -> None:
        for pair in port_info_pairs:
            self.input_to_ordinal_mapping[pair.link_identity] = pair.port_info

    def set_output_ports(self, port_ordinal_pairs: List[InitializePortMappingV2PortInfoPair]) -> None:
        for pair in port_ordinal_pairs:
            self.input_to_ordinal_mapping[pair.link_identity] = pair.port_info

    def get_input_port_ordinal(self, link: LinkIdentity) -> int:
        return self.input_to_ordinal_mapping.get_port_info(link).port_ordinal

    def get_output_port_ordinal(self, link: LinkIdentity):
        return self.input_to_ordinal_mapping.get_port_info(link).port_ordinal

    def get_input_port_name(self, link: LinkIdentity):
        return self.input_to_ordinal_mapping.get_port_info(link).port_name

    def get_output_port_name(self, link: LinkIdentity):
        return self.input_to_ordinal_mapping.get_port_info(link).port_name

    def __str__(self):
        return "inputs: " + str(self.input_to_ordinal_mapping) \
               + "\noutputs: " + str(self.output_to_ordinal_mapping)
