import kivy
from States import State
class Main(object):
    def __init__(self):
        #set up instance variables only
        self.states = {}
        self.load_states()
        self.running = False
        self.system_failure = False
        self.current_state = "INIT"
        self.next_state = "IDLE"
        self.display_panel = None

    def run(self):
        while self.running and not self.system_failure:
            #execute the current state function
            self.execute_state()

            #swap to the next state if available
            if self.next_state != None and self.next_state != self.current_state:
                self.swap_states()

            #update the current UI panel
            self.update()
            self.render()

            #ping critical system components
            self.ping()

        if self.system_failure:
            try:
                self.recover()
            except Exception as e:
                print("CRITICAL FAILURE")
                #log things

    def execute_state(self):
        for key,state in self.states:
            if self.current_state == key:
                state.execute()
                break

    def swap_states(self):
        self.current_state = self.next_state
        self.display_panel = self.states[self.current_state].get_display_panel()

    def update(self):
        self.display_panel.update()
    def render(self):
        self.display_panel.render()
    def get_input(self):
        #this will only be necessary if kivy doesn't multi-thread the UI components
        return

    def load_states(self):
        #we will need a way to pull necessary global data out of the INITState object
        self.states["INIT"] = State.INITState()
        self.states["IDLE"] = State.IDLEState(None)

    def ping(self):
        #ping all of our hardware
        #ping all of our scripts
        #perform other necessary actions to ensure smooth running
        #return false if any system has failed
        self.system_failure = False
    def recover(self):
        #attempt to reconnect hardware
        #attempt to re-launch scripts
        #attempt to operate the UI components
        #call system exit if anything cannot be recovered

        #if these lines execute it means system exit has not been called and we have recovered
        self.running = True
        self.system_failure = False
        self.run()