from dataclasses import dataclass
from pyarrow import Table
from pandas import DataFrame
from typing import Optional
from .schema import Schema, AttributeType
from .schema.attribute_type import FROM_PYOBJECT_MAPPING


@dataclass
class Marker:
    pass

@dataclass
class StartOfUpstream(Marker):
    pass

@dataclass
class EndOfUpstream(Marker):
    pass

@dataclass
class State(Marker):
    def __init__(self, table: Optional[Table] = None):
        if table is None:
            self.data = {}
            self.schema = Schema()
        else:
            self.data = table.to_pandas().iloc[0].to_dict()
            self.schema = table.schema

    def add(self, key: str, value: any, value_type: Optional[AttributeType] = None) -> None:
        self.data[key] = value
        if value_type is not None:
            self.schema.add(key, value_type)
        else:
            self.schema.add(key, FROM_PYOBJECT_MAPPING[type(value)])

    def get(self, key: str) -> any:
        return self.data[key]

    def to_table(self) -> Table:
        return Table.from_pandas(df=DataFrame([self.data]), schema=self.schema.as_arrow_schema(),)

    def __setitem__(self, key: str, value: any, value_type: AttributeType) -> None:
        self.add(key, value, value_type)

    def __getitem__(self, key: str) -> any:
        return self.get(key)

    def __str__(self) -> str:
        content = ", ".join(
            [repr(key) + ": " + repr(value) for key, value in self.data.items()]
        )
        return f"State[{content}]"

    __repr__ = __str__