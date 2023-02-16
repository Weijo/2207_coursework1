from terminal import *
import threading
import os
import app

active_agents = []
agent_command = {}
interacted_agent = ""

if __name__ == "__main__":
    # Create directories
    folders = ["task", "static/img", "static/googledata"]

    for folder in folders:
        if not os.path.exists(folder):
            os.mkdir(folder)
            

    # Terminal
    terminal = Terminal()
    terminal_thread = threading.Thread(target=terminal.cmdloop,)
    terminal_thread.start()

    # Web server
    app.run_server()