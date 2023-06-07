import datetime
from typing import TypeVar

Field = TypeVar("Field", str, int, float, bool, datetime.datetime, bytes, type(None))
