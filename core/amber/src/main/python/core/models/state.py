import pyarrow as pa

class State:
    def __init__(self, data):
        self.data = data

    def __setitem__(self, key, value):
        self.data[key] = value

    def __getitem__(self, key):
        return self.data[key]

    def __str__(self) -> str:
        content = ", ".join(
            [repr(key) + ": " + repr(value) for key, value in self.data.items()]
        )
        return f"State[{content}]"

    __repr__ = __str__

    def to_table(self):
        return pa.Table.from_pydict(self.data)