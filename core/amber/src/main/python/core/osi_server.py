import http.server
import json
import socketserver

from core.models import Schema
from osi_cli import OutputSchemaInferencer

PORT = 8000


class OSIHandler(http.server.BaseHTTPRequestHandler):
    def do_POST(self):
        content_length = int(self.headers.get("Content-Length"))
        # read that many bytes from the body of the request
        body = json.loads(self.rfile.read(content_length))
        schema = Schema(
            raw_schema={attr['attributeName']: attr['attributeType'] for attr
                        in body['inputSchema']['attributes']})

        code = body['code'].replace("from pytexera import *", "import datetime")
        output_json_dict = OutputSchemaInferencer().inference_schema(schema, code)
        # send 200 response
        self.send_response(200)
        # send response headers
        self.end_headers()
        # send the body of the response
        self.wfile.write(bytes(json.dumps(output_json_dict), "utf-8"))


if __name__ == '__main__':
    with socketserver.TCPServer(("", PORT), OSIHandler) as httpd:
        httpd.serve_forever()
