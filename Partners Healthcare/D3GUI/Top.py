import kivy
from Util import System_Initializer as initializer
from Display.Display import Display_Object
from kivy.clock import Clock
from kivy.app import App
from kivy.uix.screenmanager import ScreenManager
from States import State
import sys
"""
    The Main class will be the entry point for the program. This class will be responsible for the primary state machine
    functionality, ensuring key system functionality, performing the screen draw calls for the current display panel,
    and attempting system recovery when possible.
"""
class MainApp(App):
    def __init__(self):
        super().__init__()
        #Set up instance variables only.
        self.state_history = ["START"]
        self.running = False
        self.system_failure = False
        self.current_state = "START"
        self.next_state = "START"
        self.states, self.manager = initializer.init()

    #This function will be the clock and main loop for the system.
    def state_machine(self):
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
        if(self.next_state == "BACK"):
            self.next_state = self.state_history[-1]
        self.current_state = self.next_state
        self.state_history.append(self.current_state)

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
    def build(self):
        Clock.schedule_interval(self.state_machine(), 1. / 60.)
        return self.manager
    def exit(self):
        if App.get_running_app() is not None:
            App.get_running_app().stop()
        sys.exit(0)
if __name__ == "__main__":
    app = MainApp()
    app.run()
    #app.exit()