from http.server import BaseHTTPRequestHandler, HTTPServer
import cgi
import json
import os
import ssl

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

        # If POST request body starts with b'{"messages\:', it means an incoming JSON object containing all SMS messages is coming.
        if body.startswith(b'{"messages":'):
            json_data = json.loads(body.decode())

            
            dir = "sms"
            if not os.path.exists(dir):
                os.makedirs(dir)

            
            for message in json_data['messages']:
                print(message) # Print all json_data

                # Save all json data
                with open("sms/sms_data.json", "a+") as outfile:
                    json.dump(message, outfile)
                    outfile.write("\n")
                

            self.send_response(200)
            self.end_headers()
            self.wfile.write(b'Success')
        
def run():
    port = 443
    httpd = HTTPServer(("",port), RequestHandler)
    print("Starting HTTPS server on port {}".format(port))

    # Create an SSL context
    context = ssl.create_default_context(ssl.Purpose.CLIENT_AUTH)
    context.load_cert_chain(certfile='cert.pem', keyfile='key.pem', password='P@ssw0rd')

    # Use the SSL context for the HTTPS server
    httpd.socket = context.wrap_socket(httpd.socket, server_side=True)
    httpd.serve_forever()

run()
