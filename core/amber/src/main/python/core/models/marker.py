from dataclasses import dataclass
from pyarrow import Table
from pandas import DataFrame

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
    def __init__(self):
        self.data = {}

    def add(self, key: str, value: any) -> None:
        self.data[key] = value

    def get(self, key: str) -> any:
        return self.data[key]

    def from_dict(self, dictionary: dict) -> "State":
        for key, value in dictionary.items():
            self.add(key, value)
        return self

    def to_table(self) -> Table:
        return Table.from_pandas(df=DataFrame([self.data]))

    def from_table(self, table: Table) -> "State":
        return self.from_dict(table.to_pandas().iloc[0].to_dict())

    def __setitem__(self, key: str, value: any):
        self.add(key, value)

    def __getitem__(self, key: str) -> any:
        return self.get(key)

    def __str__(self) -> str:
        content = ", ".join(
            [repr(key) + ": " + repr(value) for key, value in self.data.items()]
        )
        return f"State[{content}]"

    __repr__ = __str__