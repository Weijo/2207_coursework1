import sqlite3
from sqlite3 import Error


def create_connection(db_file):
    """ create a database connection to a SQLite database """
    conn = None
    try:
        conn = sqlite3.connect(db_file)
        print(sqlite3.version)

        # sql = "CREATE TABLE IF NOT EXISTS sms(id TEXT, address TEXT, body TEXT, formatted_date TEXT, type INT)"
        # # args = (var1,var2)
        # conn.execute(sql)

        # sql = "INSERT INTO sms VALUES (?, ?, ?, ?, ?)"
        # args = ("abc", "6505551212", "Android is always a sweet treat!", "06/01/2023 15:26:16", "1")
        # conn.execute(sql, args)

        # print(json_data["address"])
        # print(json_data["body"])
        # print(json_data["formatted_date"])
        # print(json_data["type"])

        rows = conn.execute("SELECT id, address, body, formatted_date, type FROM sms").fetchall()
        print(rows)
        conn.commit()

    except Error as e:
        print(e)
    finally:
        if conn:
            conn.close()


if __name__ == '__main__':
    create_connection("database.db")