from terminal import *
import threading
import os
import app

active_agents = []
agent_command = {}
interacted_agent = ""

if __name__ == "__main__":
    # Create task directory
    if not os.path.exists("task"):
        os.mkdir("task")

    # Terminal
    terminal = Terminal()
    terminal_thread = threading.Thread(target=terminal.cmdloop,)
    terminal_thread.start()

    # Web server
    app.run_server()