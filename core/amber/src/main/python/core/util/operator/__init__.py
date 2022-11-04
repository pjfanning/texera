import importlib.util
import inspect
import sys

from core.models import Operator
from core.util import gen_uuid, get_root


def load_operator(code: str) -> type(Operator):
    """
    Load the given operator code in string into a class definition
    :param code: str, python code that defines an Operator, should contain one
            and only one Operator definition.
    :return: an Operator sub-class definition
    """
    module_name = gen_uuid("udf")
    file_name = f"{module_name}.py"
    with open(get_root().with_name(file_name), "w") as file:
        file.write(code)
    import importlib

    sys.path.append(str(get_root()))
    operator_module = importlib.import_module(module_name)
    operators = list(filter(is_concrete_operator, operator_module.__dict__.values()))
    assert len(operators) == 1, "There should be one and only one Operator defined"
    return operators[0]


def is_concrete_operator(cls: type) -> bool:
    """
    checks if the class is a non-abstract Operator
    :param cls: a target class to be evaluated
    :return: bool
    """
    return (
        inspect.isclass(cls)
        and issubclass(cls, Operator)
        and not inspect.isabstract(cls)
    )


class Option:
    def __init__(self, val=None):
        self.set(val)

    def set(self, val):
        self.val = val

    def get(self):
        return self.val
