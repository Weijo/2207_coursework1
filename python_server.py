from http.server import BaseHTTPRequestHandler, HTTPServer
import cgi
import json
import os

class RequestHandler(BaseHTTPRequestHandler):

    def _send_response(self, message):
        self.send_response(200)
        self.send_header("Content-type", "text/html")
        self.end_headers()
        self.wfile.write(bytes(message, "utf8"))

    def do_GET(self):
        self._send_response("This is a GET request")

    def do_POST(self):
        content_length = int(self.headers['Content-Length'])
        body = self.rfile.read(content_length)
        json_data = json.loads(body.decode())

        # Print all json_data
        print(json_data["address"])
        print(json_data["body"])
        print(json_data["formatted_date"])
        print(json_data["type"])

        dir = "sms"

        if not os.path.exists(dir):
            os.makedirs(dir)

        # Save all json data
        with open("sms/sms_data.json", "a+") as outfile:
            json.dump(json_data, outfile)
            outfile.write("\n")
            

        self.send_response(200)
        self.end_headers()
        self.wfile.write(b'Success')
        
def run(server_class=HTTPServer, handler_class=RequestHandler, port=80):
    server_address = ("", port)
    httpd = server_class(server_address, handler_class)
    print("Starting HTTP server on port {}".format(port))
    httpd.serve_forever()

run()
