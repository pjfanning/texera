#!/usr/bin/env python
import ast
import json
import os
import subprocess
import sys
from _ast import FunctionDef, arg, Name, Subscript, Yield, Expr, Return, Dict, Tuple, \
    Load, AST
import argparse

from core.models import Schema


class FuncArgsTransformer(ast.NodeTransformer):
    def generic_visit(self, node: AST) -> AST:
        super().generic_visit(node)

        process_tuple_func_cond = (
                isinstance(node, FunctionDef) and node.name == "process_tuple"
        )
        if process_tuple_func_cond:
            global target_line
            target_line = node.lineno
            args = list()
            args.append(node.args.args[0])
            for attr_name, attr_type in schema.items():
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
    def generic_visit(self, node: AST) -> AST:
        super().generic_visit(node)
        if isinstance(node, Subscript) and isinstance(node.value,
                                                      Name) and node.value.id == "tuple_":
            field_name = node.slice.value
            global output_fields
            if field_name not in output_fields:
                output_fields.append(field_name)
            return Name(node.slice.value)
        return node


class YieldTransformer(ast.NodeTransformer):
    def generic_visit(self, node: AST) -> AST:
        super().generic_visit(node)
        if isinstance(node, Expr):

            if isinstance(node.value, Yield):
                global output_fields
                exp = node.value.value
                if isinstance(exp, Name):
                    if exp.id == "tuple_":
                        return Return(Tuple([Name(x) for x in output_fields], Load()))
                if isinstance(exp, Dict):
                    output_fields = [key.n for key in exp.keys]
                    return Return(Tuple(exp.values, Load()))
        return node


if __name__ == '__main__':

    parser = argparse.ArgumentParser()
    parser.add_argument('--json', help='json input')
    parser.add_argument('--stdin', nargs='?', type=bool, default=True,
                        help='stdin input')
    args = parser.parse_args()

    if args.json is not None:
        with open(args.json) as file:
            input_data = json.load(file)

    if args.stdin:
        input_data = json.load(sys.stdin)
    schema = Schema(raw_schema={attr['attributeName']: attr['attributeType'] for attr
                                in \
                                input_data['inputSchema'][
                                    'attributes']}).to_python_object_type_schema()

    code = input_data['code'].replace("from pytexera import *", "import datetime")
    root = ast.parse(code)
    output_fields = [name for name in schema]
    target_line = 0
    updated_root = FuncArgsTransformer().visit(root)
    updated_root = TexeraTupleAccessorTransformer().visit(updated_root)
    updated_root = YieldTransformer().visit(updated_root)
    generated_code = ast.unparse(updated_root)

    # print(generated_code)
    with open(
            '/Users/yicong-huang/IdeaProjects/texera/core/amber/src/main/python/output.py',
            'w') as fp:
        fp.write(generated_code)

    subprocess.call(["/Users/yicong-huang/IdeaProjects/texera/venv/bin/dmypy", "run",
                     f'/Users/yicong-huang/IdeaProjects/texera/core/amber/src/main/python/output.py']
                    , stdout=subprocess.DEVNULL
                    , stderr=subprocess.DEVNULL
                    )

    output = subprocess.run(
        ["/Users/yicong-huang/IdeaProjects/texera/venv/bin/dmypy", "suggest", '--json',
         # '--no-errors',
         f'/Users/yicong-huang/IdeaProjects/texera/core/amber/src/main/python/output.py'
         f':{target_line}'],
        capture_output=True)

    annotations = json.loads(output.stdout.decode("utf-8").strip())

    output_types = annotations[0]['signature']['return_type']
    allowed_types = ['int', 'float', 'datetime', 'str', 'bool', 'bytes']
    raw_types = [type.strip("?") for type in output_types.strip("Tuple[").strip(
        "]").split(", ")]
    final_types = [type if type in allowed_types else 'binary' for type in raw_types]

    output_json_dict = {
        "output_schema": dict(
            zip(output_fields, final_types))
    }
    print(json.dumps(output_json_dict))
    # os.remove("output.py")
