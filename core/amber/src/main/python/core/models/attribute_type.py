import datetime
from enum import Enum

from bidict import bidict
from pyarrow import lib


class AttributeType(Enum):
    STRING = 1
    INT = 2
    LONG = 3
    BOOL = 4
    DOUBLE = 5
    TIMESTAMP = 6
    BINARY = 7


RAW_TYPE_MAPPING = bidict(
    {
        "string": AttributeType.STRING,
        "integer": AttributeType.INT,
        "long": AttributeType.LONG,
        "double": AttributeType.DOUBLE,
        "boolean": AttributeType.BOOL,
        "timestamp": AttributeType.TIMESTAMP,
        "binary": AttributeType.BINARY,
    }
)

ARROW_TYPE_MAPPING = bidict(
    {
        lib.Type_INT32: AttributeType.INT,
        lib.Type_INT64: AttributeType.LONG,
        lib.Type_STRING: AttributeType.STRING,
        lib.Type_DOUBLE: AttributeType.DOUBLE,
        lib.Type_BOOL: AttributeType.BOOL,
        lib.Type_BINARY: AttributeType.BINARY,
        lib.Type_TIMESTAMP: AttributeType.TIMESTAMP,
    }
)

# Only single-directional mapping.
PYOBJECT_TYPE_MAPPING = {
    AttributeType.STRING: str,
    AttributeType.INT: int,
    AttributeType.LONG: int,  # Python3 unifies long into int.
    AttributeType.DOUBLE: float,
    AttributeType.BOOL: bool,
    AttributeType.BINARY: bytes,
    AttributeType.TIMESTAMP: datetime.datetime,
}
