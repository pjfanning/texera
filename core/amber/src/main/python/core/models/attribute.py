from core.models.attribute_type import AttributeType


class Attribute:
    name: str
    type_: AttributeType

    def __init__(self, name: str, type_: AttributeType):
        self.name = name
        self.type_ = type_
