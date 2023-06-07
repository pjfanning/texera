from typing import MutableMapping, Optional, Mapping, List, Tuple

import pyarrow as pa

from core.models.attribute_type import AttributeType, ARROW_TYPE_MAPPING, \
    RAW_TYPE_MAPPING


class Schema:

    def __init__(self, pyarrow_schema: Optional[pa.Schema] = None):
        if pyarrow_schema is not None:
            pass
        self._name_type_mapping: MutableMapping[str, AttributeType] = dict()

    def add(self, attribute_name: str, attribute_type: AttributeType) -> None:
        self._name_type_mapping[attribute_name] = attribute_type

    @classmethod
    def from_raw_schema(cls, raw_schema: Mapping[str, str]) -> "Schema":
        s = Schema()
        for attr_name, raw_type in raw_schema.items():
            attr_type = RAW_TYPE_MAPPING[raw_type]
            s.add(attr_name, attr_type)
        return s

    @classmethod
    def from_arrow_schema(cls, arrow_schema: pa.Schema) -> "Schema":
        s = Schema()
        for attr_name in arrow_schema.names:
            arrow_type = arrow_schema.field(attr_name).type
            attr_type = ARROW_TYPE_MAPPING[arrow_type]
            s.add(attr_name, attr_type)
        return s

    def to_arrow_schema(self) -> pa.Schema:
        return pa.schema(
            [
                pa.field(attr_name, ARROW_TYPE_MAPPING.inverse[attr_type])
                for attr_name, attr_type in self._name_type_mapping.items()
            ]
        )

    def get_attr_names(self) -> List[str]:
        return list(self._name_type_mapping.keys())

    def get_attr_type(self, attr_name: str) -> AttributeType:
        return self._name_type_mapping[attr_name]

    def to_key_value_pairs(self) -> List[Tuple[str, AttributeType]]:
        return [(k, v) for k, v in self._name_type_mapping.items()]

    def __eq__(self, other: "Schema") -> bool:
        if not isinstance(other, Schema):
            return False
        left_pairs = self.to_key_value_pairs()
        right_pairs = other.to_key_value_pairs()
        return left_pairs == right_pairs
