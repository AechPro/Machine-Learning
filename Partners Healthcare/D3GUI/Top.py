import kivy
from Util import System_Initializer as initializer
from Display.Display import Display_Object
from kivy.clock import Clock
from kivy.app import App
from kivy.uix.screenmanager import ScreenManager
from States import State as states
import sys
"""
    The Main class will be the entry point for the program. This class will be responsible for the primary state machine
    functionality, ensuring key system functionality, performing the screen draw calls for the current display panel,
    and attempting system recovery when possible.
"""
class MainApp(App):
    def __init__(self):
        super(MainApp,self).__init__()
        print("init")
        #Set up instance variables only.
        self.states = None
        self.manager = None
        self.state_history = ["START"]
        self.system_failure = False
        self.current_state = "START"
        self.next_state = "START"
        self.running = True
        #Clock.schedule_interval(lambda f: self.state_machine(), 1./60.)

    #This function will be the clock and main loop for the system.
    def state_machine(self):
        #print("state machine")
        if self.running and not self.system_failure:
            #Execute the current state.
            self.execute_state()

            #Swap to the next state if available.
            if self.next_state != None and self.next_state != self.current_state:
                self.swap_states()

            #Ping critical system components.
            self.ping()

        #If the system has failed, attempt recovery.
        if self.system_failure:
            try:
                self.recover()
            except Exception as e:
                print("CRITICAL FAILURE")
                #log things
        if not self.running:
            self.exit()

    #This function gets and executes the current state when available.
    def execute_state(self):
        state = self.states.get(self.current_state,None)
        if state:
            state.execute()
            self.next_state = state.get_next_state()

    #This function swaps the current state and display panel to the next state and display panel.
    def swap_states(self):
        #Special logic to go back a state.
        if(self.next_state == "BACK"):
            self.next_state = self.state_history[-1]

        #Swap states.
        self.current_state = self.next_state

        #Add our new state to the global history of states.
        self.state_history.append(self.current_state)

        #Set the content pane of our screen manager to the screen associated with our new state.
        self.manager.current = self.states[self.current_state].get_display_panel().get_name()

    #This function will be responsible for checking all critical systems and determining if a failure has happened.
    def ping(self):
        #Ping all of our hardware.
        #Ping all of our scripts.
        #Perform other necessary actions to ensure smooth running.
        self.system_failure = False

    def recover(self):
        #Attempt to reconnect hardware.
        #Attempt to re-launch scripts.
        #Attempt to operate the UI components.
        #Call system exit if anything cannot be recovered.

        #If these lines execute it means system exit has not been called and we have recovered.
        self.running = True
        self.system_failure = False

    #This function is the entry point for Kivy, I think.
    def build(self):
        start_state = states.Start_State()
        browse_users_state = states.Browse_Users_State()
        state_dict = {"START": start_state, "BROWSE USERS": browse_users_state}

        # Set up our Kivy screen manager.
        sm = ScreenManager()
        for state in state_dict.items():
            sm.add_widget(state[1].get_display_panel())
        self.states = state_dict
        self.manager = sm
        return self.manager
    def start(self):
        Clock.schedule_interval(lambda f: self.state_machine(), 1. / 60.)

    #This function is used to close our app if it is running and exit the application.
    def exit(self):
        if App.get_running_app() is not None:
            App.get_running_app().stop()
        sys.exit(0)


if __name__ == "__main__":
    app = MainApp()
    app.start()
    app.run()
    #app.exit()