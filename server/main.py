from terminal import *
import listener
import threading
import os

active_agents = []
agent_command = {}
interacted_agent = ""

if __name__ == "__main__":
    # Create data directory
    if not os.path.exists("data"):
        os.makedirs("data")

    # Terminal
    terminal = Terminal()
    terminal_thread = threading.Thread(target=terminal.cmdloop,)
    terminal_thread.start()

    # Web server
    port = 443
    print("Starting HTTPS server on port {}\nPress Ctrl + C after exiting prompt to close the server\n".format(port))
    listener.run(port)