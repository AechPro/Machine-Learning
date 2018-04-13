from kivy.uix.screenmanager import ScreenManager
from States import State as states

#Basic function to initialize all of the states, commands, and UI objects that will be used by Top and Kivy.
def init():

    #Set up our states.
    start_state = states.Start_State()
    browse_users_state = states.Browse_Users_State()
    state_dict = {"START":start_state,"BROWSE USERS":browse_users_state}

    #Set up our Kivy screen manager.
    sm = ScreenManager()
    for state in state_dict.items():
        sm.add_widget(state[1].get_display_panel())

    return state_dict, sm
