#!/usr/bin/env python
import ast
import json
import os
import subprocess
import sys
import tempfile
import typing
from _ast import FunctionDef, arg, Name, Subscript, Yield, Expr, Return, Dict, Tuple, \
    Load, AST
import argparse
from pathlib import Path

from core.models import Schema


class FuncArgsTransformer(ast.NodeTransformer):

    def __init__(self, input_schema: Schema):
        super().__init__()
        self.input_schema = input_schema
        self.target_line = 0

    def generic_visit(self, node: AST) -> AST:
        super().generic_visit(node)

        process_tuple_func_cond = (
                isinstance(node, FunctionDef) and node.name == "process_tuple"
        )
        if process_tuple_func_cond:
            self.target_line = node.lineno
            args = list()
            args.append(node.args.args[0])
            for attr_name, attr_type in self.input_schema.to_python_object_type_schema().items():
                new_arg = arg()
                new_arg.arg = attr_name
                new_arg.annotation = Name()
                new_arg.annotation.id = attr_type

                args.append(new_arg)
            # proc = FunctionDef("process", args, node.body, node.decorator_list, node.returns)
            node.args.args = args
            node.decorator_list = list()
            node.returns = None
        return node


class TexeraTupleAccessorTransformer(ast.NodeTransformer):
    def __init__(self, output_fields: typing.List[str]):
        self.output_fields = output_fields

    def generic_visit(self, node: AST) -> AST:
        super().generic_visit(node)
        if isinstance(node, Subscript) and isinstance(node.value,
                                                      Name) and node.value.id == "tuple_":
            field_name = node.slice.value

            if field_name not in self.output_fields:
                self.output_fields.append(field_name)
            return Name(node.slice.value)
        return node


class YieldTransformer(ast.NodeTransformer):

    def __init__(self, output_fields: typing.List[str]):
        self.output_fields = output_fields

    def generic_visit(self, node: AST) -> AST:
        super().generic_visit(node)
        if isinstance(node, Expr):

            if isinstance(node.value, Yield):

                exp = node.value.value
                if isinstance(exp, Name):
                    if exp.id == "tuple_":
                        return Return(Tuple([Name(x) for x in self.output_fields],
                                            Load()))
                if isinstance(exp, Dict):
                    self.output_fields = [key.n for key in exp.keys]
                    return Return(Tuple(exp.values, Load()))
        return node


class FunctionLocator(ast.NodeVisitor):

    def __init__(self):
        self.target_line = 0

    def visit_FunctionDef(self, node: FunctionDef):
        if node.name == "process_tuple":
            self.target_line = node.lineno


class OutputSchemaInferencer:
    def inference_schema(self, input_schema: Schema, code: str):
        root = ast.parse(code)
        output_fields = [name for name in input_schema.to_python_object_type_schema()]
        updated_root = FuncArgsTransformer(input_schema).visit(root)
        updated_root = TexeraTupleAccessorTransformer(output_fields).visit(updated_root)
        updated_root = YieldTransformer(output_fields).visit(updated_root)
        generated_code = ast.unparse(updated_root)
        locator = FunctionLocator()
        locator.visit(ast.parse(generated_code))
        target_line = locator.target_line
        with tempfile.NamedTemporaryFile(mode='w', suffix=".py") as fp:
            fp.write(generated_code)
            fp.flush()
            dmypy_path = Path(os.path.dirname(sys.executable)).joinpath("dmypy")
            subprocess.call(
                [dmypy_path, "run", fp.name]
                , stdout=subprocess.DEVNULL
                , stderr=subprocess.DEVNULL
            )

            output = subprocess.run(
                [dmypy_path, "suggest", '--json',
                 # '--no-errors',
                 f'{fp.name}:{target_line}'],
                capture_output=True
            )
            annotations = json.loads(output.stdout.decode("utf-8").strip())

            output_types = annotations[0]['signature']['return_type']
            allowed_types = ['int', 'float', 'datetime', 'str', 'bool', 'bytes']
            raw_types = [type.strip("?") for type in output_types.strip("Tuple[").strip(
                "]").split(", ")]
            final_types = [type if type in allowed_types else 'binary' for type in
                           raw_types]

            output_json_dict = {
                "outputSchema": dict(zip(output_fields, final_types))
            }
            return output_json_dict


if __name__ == '__main__':

    parser = argparse.ArgumentParser()
    parser.add_argument('--json', help='json input')
    parser.add_argument('--stdin', nargs='?', type=bool, default=True,
                        help='stdin input')
    args = parser.parse_args()

    if args.json is not None:
        with open(args.json) as file:
            input_data = json.load(file)

    elif args.stdin:
        input_data = json.load(sys.stdin)
    schema = Schema(raw_schema={attr['attributeName']: attr['attributeType'] for attr
                                in input_data['inputSchema']['attributes']})

    code = input_data['code'].replace("from pytexera import *", "import datetime")

    output_json_dict = OutputSchemaInferencer().inference_schema(schema, code)

    print(json.dumps(output_json_dict))
    # os.remove("output.py")
