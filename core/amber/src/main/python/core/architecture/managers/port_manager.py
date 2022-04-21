from typing import Union, List

from core.util.arrow_utils import to_arrow_schema
from proto.edu.uci.ics.amber.engine.architecture.worker import (
    InitializePortMappingV2PortInfoPair,
    InitializePortMappingV2PortInfoPairPortInfo,
)
from proto.edu.uci.ics.amber.engine.common import LinkIdentity


class PortMap(dict):
    def __getitem__(
        self, item: Union[LinkIdentity, int, str]
    ) -> Union[List[LinkIdentity], InitializePortMappingV2PortInfoPairPortInfo]:
        """
        :param item:  Union[LinkIdentity, int, str], could be a LinkIdentity,
            a port ordinal, or a port name.
        :return:
            - If querying for a LinkIdentity, returns the
                InitializePortMappingV2PortInfoPairPortInfo(ordinal, name).
            - If querying for a port ordinal or a port name, returns list of all
                LinkIdentities that match.
        """
        if isinstance(item, LinkIdentity):
            return super(PortMap, self).__getitem__(item)
        else:
            return [
                k
                for k, pair_info in self.items()
                if item == pair_info.port_ordinal or item == pair_info.port_name
            ]

    def get_port_info(
        self, link: LinkIdentity
    ) -> InitializePortMappingV2PortInfoPairPortInfo:

        return self[link]

    def get_links_by_port_index(self, index: int) -> List[LinkIdentity]:
        return self.__getitem__(index)

    def get_links_by_port_name(self, name: str) -> List[LinkIdentity]:
        return self.__getitem__(name)

    def get_port_ordinals(self):
        return (pair.port_ordinal for pair in self.values())


class PortManager:
    def __init__(self):
        self.input_to_ordinal_mapping = PortMap()
        self.output_to_ordinal_mapping = PortMap()
        self.output_schema_mapping = dict()

    def set_input_ports(
        self, port_info_pairs: List[InitializePortMappingV2PortInfoPair]
    ) -> None:
        for pair in port_info_pairs:
            self.input_to_ordinal_mapping[pair.link_identity] = pair.port_info

    def set_output_ports(
        self, port_ordinal_pairs: List[InitializePortMappingV2PortInfoPair]
    ) -> None:
        for pair in port_ordinal_pairs:
            self.output_to_ordinal_mapping[pair.link_identity] = pair.port_info

    def get_input_port_ordinal(self, link: LinkIdentity) -> int:
        return self.input_to_ordinal_mapping.get_port_info(link).port_ordinal

    def get_output_port_ordinal(self, link: LinkIdentity) -> int:
        return self.input_to_ordinal_mapping.get_port_info(link).port_ordinal

    def get_input_port_name(self, link: LinkIdentity) -> str:
        return self.input_to_ordinal_mapping.get_port_info(link).port_name

    def get_output_port_name(self, link: LinkIdentity) -> str:
        return self.input_to_ordinal_mapping.get_port_info(link).port_name

    def get_output_port_schema(self, ordinal: int) -> str:
        return self.output_schema_mapping[ordinal]

    def set_output_port_schema(self, ordinal: int, raw_output_schema) -> None:
        self.output_schema_mapping[ordinal] = to_arrow_schema(raw_output_schema)

    def get_output_links(self, ordinal: int) -> List[LinkIdentity]:
        print(self.output_to_ordinal_mapping)
        return self.output_to_ordinal_mapping.get_links_by_port_index(ordinal)

    def get_output_links_by_port_name(self, name: str) -> List[LinkIdentity]:
        return self.output_to_ordinal_mapping.get_links_by_port_name(name)

    def __str__(self):
        return (
            "inputs: "
            + str(self.input_to_ordinal_mapping)
            + "\noutputs: "
            + str(self.output_to_ordinal_mapping)
        )
