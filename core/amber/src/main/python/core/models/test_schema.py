import pytest

from core.models.attribute_type import AttributeType
from core.models.schema import Schema


class TestSchema:
    @pytest.fixture
    def raw_schema(self):
        return {
            "field-1": "string",
            "field-2": "integer",
            "field-3": "long",
            "field-4": "double",
            "field-5": "boolean",
            "field-6": "timestamp",
            "field-7": "binary",
        }

    @pytest.fixture
    def schema(self):
        s = Schema()
        s.add("field-1", AttributeType.STRING)
        s.add("field-2", AttributeType.INT)
        s.add("field-3", AttributeType.LONG)
        s.add("field-4", AttributeType.DOUBLE)
        s.add("field-5", AttributeType.BOOL)
        s.add("field-6", AttributeType.TIMESTAMP)
        s.add("field-7", AttributeType.BINARY)
        return s

    def test_convert_from_raw_schema(self, raw_schema, schema):
        assert schema == Schema(raw_schema=raw_schema)
