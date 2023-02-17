# Android C2 Server

# Running the Attack Scenario
1. Open the source code in Android Studio and navigate to `/app/src/main/java/com/example/teamchat/Constants.java`
  - Edit the SERVER to your own IP address
2. Generate the APK using our Teamchat key (Note: This step is important as the Teamchat key provides a SHA-1 hash that is used for the Google Drive API)
  - Android Studio > Build > Generate Signed Bundle / APK
  - Select APK and click Next
  - Under Key store path > Click “Choose Existing” and select the keystore.jks that we provide in \key folder
  - Fill in the rest of the fields as follows:

    | Fields              | Value     |
    |--------------       |-----------|
    | Key store password  | android      |
    | Key alias           | teamchat_key  |
    | Key password        | android  |

    ![image](https://user-images.githubusercontent.com/37981072/219643179-81fecd4c-62c6-4365-b647-a31ad96d8864.png)
    - Click Next and select Release
    - Click Finish
3. Install the APK provided on a emulator or physical android phone
  - You can find the generated signed APK in `app\release` folder
4. Run the Teamchat application and login using the follow gmail account

    | Gmail              | Password     |
    |--------------       |-----------|
    | t3sting121@gmail.com  | DR6^UXq&Z5!U#f3$URAko6S7S&^D5K6p      |
5. After logging the gmail account, open terminal / command prompt and run the following commands:
  - `python main.py`
  - main.py is in the server folder


# Outline
## Server
The C2 Server consists of the c2 listener and the terminal 
- **app.py** 
  - This is the c2 listener build on flask
  - Create your command handlers at the `Command Handler` section and add on to `receiveResult` function for it to be handled
  - Add on to `init_database` to create a table for the data you want to exfiltrate
  - It saves the data into an sqlite file `database.db`, you can check out `sqltest.py` on how to use sqlite3 on python. Alternatively, visit https://sqliteonline.com/ and upload `database.db` to view all tables.
  - The logs are saved into `log`, I used `tail -f log` to read the logs

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

## Database
The sqlite database: `database.db`

### Tables
-  agents(id TEXT)
-  sms(id TEXT, address TEXT, body TEXT, formatted_date TEXT, type INT)
-  app(id TEXT, package TEXT, sourceDir TEXT, launchActivity TEXT)
-  images (id TEXT, path TEXT)
-  contacts (id TEXT, name TEXT, number TEXT, email TEXT)
-  devicedetails(id TEXT, model TEXT, manufacturer TEXT, brand TEXT, product TEXT, device TEXT, board TEXT, display TEXT, hardware TEXT, id_num TEXT, serial TEXT, type TEXT, user TEXT)
-  osdetails(id TEXT, androidVersion TEXT,androidVersion TEXT)
-  displaydetails(id TEXT, width TEXT, height TEXT, PPI TEXT)
-  batterydetails(id TEXT, level TEXT, status TEXT, isCharging TEXT)
-  networkdetails(id TEXT, networkType TEXT, isNetworkAvailable TEXT, isWifiConnected TEXT, isMobileConnected TEXT, ssid TEXT, bssid TEXT, linkSpeed TEXT,ipAddress TEXT,networkId TEXT,signalStrenth TEXT,mobileType TEXT,State TEXT,detailedState TEXT,operatorName TEXT, operatorCode TEXT, roaming TEXT, strength TEXT)
-  telephonydetails(id TEXT, phoneNumber TEXT, imei TEXT)
-  location(id TEXT, latitude TEXT, longitude TEXT)
-  callLog(id TEXT, number TEXT, type TEXT, date TEXT, duration TEXT)
-  googledata(id TEXT, path TEXT)

  
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
  - This contains the constant variables. Set your Server IP here.
- **YourStuff**
  - Add your own class to do your malicious stuff and make a handler in `ChatService` to call it


### Terminal commands

- **Main**
  - list -> lists agents.
  - use [number] -> interact with selected agent
  - clear -> resets the data directory 
- **Agent**
  - task -> list the current task
  - do [task] -> writes the task to /task/<id> 
    - Available tasks: `sms`, `app`, `images`, `phonedetails`, `contacts`, `location`, `callLog`, `googledata`

### View extracted information on Flask

1. Inside the `server` directory, start Flask: `flask --app app run`
2. Open a browser and go to `https://localhost:5000/[target]"`, where target is the type of information to view (e.g. sms, images).
3. A table with the desired information and the corresponding agent ID is displayed.

### Release build

Signed APK can be found in `app/release`.
