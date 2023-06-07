import datetime
from typing import TypeVar, Optional

Field = TypeVar(
    "Field",
    Optional[str],
    Optional[int],
    Optional[float],
    Optional[bool],
    Optional[datetime.datetime],
    Optional[bytes],
)
