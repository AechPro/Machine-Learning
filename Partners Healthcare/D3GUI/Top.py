import kivy
from Display.Display import Display_Object
from kivy.clock import Clock
from kivy.uix.screenmanager import ScreenManager
from States import State

"""
    The Main class will be the entry point for the program. This class will be responsible for the primary state machine
    functionality, ensuring key system functionality, performing the screen draw calls for the current display panel,
    and attempting system recovery when possible.
"""
class Main(object):
    def __init__(self):
        #Set up instance variables only.
        self.states = {}
        self.load_states()
        self.running = False
        self.system_failure = False
        self.current_state = "INIT"
        self.next_state = "IDLE"
        self.manager = ScreenManager()
        self.init_UI()
        Clock.Schedule(self.state_machine(),1./60.)

    #This function will be the clock and main loop for the system.
    def state_machine(self):
        while self.running and not self.system_failure:
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

    #This function gets and executes the current state when available.
    def execute_state(self):
        state = self.states.get(self.current_state,None)
        if state:
            state.execute()
            self.next_state = state.get_next_state()

    #This function swaps the current state and display panel to the next state and display panel.
    def swap_states(self):
        self.current_state = self.next_state
        self.display_panel = self.states[self.current_state].get_display_panel()

    def init_UI(self):
        IDLE_SCREEN = Display_Object()
        self.manager.add_widget()

    """
        This function should load all of the State objects the system may ever need. It is probably a better idea
        to load each State object dynamically when they are needed, because it would give us the ability to use
        the __init__() method of each state as a single-call function that does not need to be checked every cycle
        This is useful if any state needs to perform a one-time process to data that is made available after the device
        has been running for some time.
    """
    def load_states(self):
        self.states["IDLE"] = State.Idle_State(None)

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
        self.run()