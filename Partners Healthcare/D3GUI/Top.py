import kivy
from Util import System_Initializer as sys_init
from Display.Display import Display_Object
from kivy.clock import Clock
from kivy.app import App
from kivy.uix.screenmanager import ScreenManager
from States import State as states
import sys
import os

class MainApp(App):
    """
        The Main class will be the entry point for the program. This class will be responsible for the primary state machine
        functionality, ensuring key system functionality, performing the screen draw calls for the current display panel,
        and attempting system recovery when possible.
    """
    def __init__(self):
        super(MainApp,self).__init__()
        print("MAIN INIT")
        
        #Set up instance variables only.
        self.current_state = "LOGIN"
        self.states = None
        self.manager = None
        self.state_history = [self.current_state]
        self.system_failure = False
        self.next_state = self.current_state
        self.running = True
        self.fps = 60.
        self.clk = None

    def state_machine(self):
        """
        This function will be the clock and main loop for the system.
        :return: void
        """
        if self.running and not self.system_failure:
            #Execute the current state.
            self.execute_state()
            #Swap to the next state if available.
            if self.next_state != None and self.next_state != self.current_state:
                self.swap_states()

            #Ping critical system components.
            self.ping()

        #If the system has failed, attempt recovery.
        elif self.system_failure:
            try:
                self.recover()
            except Exception as e:
                print("CRITICAL FAILURE")
                #log things

        elif not self.running:
            self.exit()

    def execute_state(self):
        """
        This function gets and executes the current state when available.
        :return: void
        """
        state = self.states.get(self.current_state, None)
        if state:
            self.next_state = state.get_next_state()
            state.execute()

    def swap_states(self):
        """
        This function swaps the current state and display panel to the next state and display panel.
        :return: void
        """
        if(self.next_state == "EXIT"):
            self.exit()

        #Special logic to go back a state.
        if(self.next_state == "BACK"):
            self.next_state = self.state_history[-2]

        #Swap states.
        self.current_state = self.next_state

        #Add our new state to the global history of states.
        self.state_history.append(self.current_state)

        print("CHANGE PATIENT" in self.states)
        print(self.current_state)
        #Set the content pane of our screen manager to the screen associated with our new state.
        self.manager.current = self.states[self.current_state].get_display_panel().get_name()

        #Call the on_enter() function for our new state.
        state = self.states.get(self.current_state, None)
        if state:
            state.on_enter()

    def ping(self):
        """
        This function will be responsible for checking all critical systems and determining if a failure has happened.
        :return: void
        """
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
        self.states, self.manager = sys_init.init(self.current_state)
        
        return self.manager

    def start(self):
        self.clk = Clock.schedule_interval(lambda f: self.state_machine(), 1. / self.fps)
        
    def on_stop(self, *largs):
        self.root_window.close()

        try:
            stupid_camera_object = self.states["IDLE"].get_display_panel().ids["camera"]
            stupid_camera_object._camera.stop()
            stupid_camera_object._camera._device.release()
        except:
            print("Camera device not detected. Release successful.")
        if self.clk is not None:
            self.clk.cancel()
            self.clk = None

    def exit(self):
        """
        This function is used to close our app if it is running and exit the application.
        :return: void
        """
        print("Shutting down...")
        if App.get_running_app() is not None:
            App.get_running_app().stop()
        sys.exit(0)


if __name__ == "__main__":
    
    app = MainApp()
    
    app.start()
    
    app.run()
    
    #app.exit()