# Android C2 Server

# Outline
## Server
The C2 Server consists of the c2 listener and the terminal 
- **listener.py** 
  - This is the c2 listener
  - Add on to the `RequestHandler` class to handle your various commands 
- **terminal.py**
  - This is the command line portion
  - There are two Terminals
    - Terminal (main)
    - Agent_Terminal (agent's terminal)
  - Add on to the `Agent_Terminal` to include your commands
    - you can do so by defining a method as such:
    ```python
    def do_yourcommand(self, arg):
        "This will be shown in the help command"
        print(f"Arguments are here {arg}")
    ```
- **main.py**
  - This runs both servers

The data should be stored in `data/<uuid>/`

## Client
Source code in /app/src/main/java/com/example/teamchat/
- **MainActivity**
  - This is the main activity, all the code are called here
  - The main part is `onCreate()`
  - Add on to `checkPermission` for any extra permissions you need
- **ChatService**
  - This is the C2 client service
  - Add on to the code in `handleConnection` and add on your handler below
- **HttpConnection**
  - This is a helper class that connects to a server
    - You just need to call HttpConnection.connect(<Server>, <GET | POST>, <null | String of JSON data >)
    - This will return a ReturnResponse object which contains:
      ```java
      public final String body;
      public final int responseCode;
      ```
  - There's a lot of IO Exception that I do not know how to fix
- **Constants**
  - This contains the constant variables
- **YourStuff**
  - Add your own class to do your malicious stuff and make a handler in `ChatService` to call it


### Terminal commands
This probably be updated frequently

- **Main**
  - list -> lists agents
  - use <number> -> interact with agent
  - clear -> resets the data directory 
- **Agent**
  - task -> list the current task
  - sms -> gather sms

# Extra notes
I have not cleaned up the code, it has a bunch of prints and logs here and there to make me sane
