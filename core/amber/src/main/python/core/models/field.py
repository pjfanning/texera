import datetime
from typing import TypeVar

Field = TypeVar(
    "Field", int, float, str, datetime.datetime, bytes, bool, type(None)
)
