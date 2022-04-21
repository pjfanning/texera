from typing import Mapping
import pyarrow as pa

from proto.edu.uci.ics.amber.engine.architecture.worker import (
    InitializeOperatorLogicV2Schema,
)

"""
The definitions are mapped and following the AttributeType.java
(src/main/scala/edu/uci/ics/texera/workflow/common/tuple/schema/AttributeType.java)
"""

ARROW_TYPE_MAPPING = {
    "string": pa.string(),
    "integer": pa.int32(),
    "long": pa.int64(),
    "double": pa.float64(),
    "boolean": pa.bool_(),
    "timestamp": pa.timestamp("ms", tz="UTC"),
    "binary": pa.binary(),
    "ANY": pa.string(),
}


def to_arrow_schema(raw_schema: InitializeOperatorLogicV2Schema) -> pa.Schema:
    return pa.schema(
        [
            pa.field(attr.attribute_name, ARROW_TYPE_MAPPING[attr.attribute_type])
            for attr in raw_schema.attributes
        ]
    )
