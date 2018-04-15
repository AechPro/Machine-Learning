from kivy.uix.screenmanager import ScreenManager
from States import State as states

#Basic function to initialize all of the states, commands, and UI objects that will be used by Top and Kivy.
def init():

    start_state = states.Start_State()
    browse_users_state = states.Browse_Users_State()
    create_new_user_state = states.Create_New_User_State()
    idle_state = states.Idle_State(None)
    sample_view_state = states.Sample_View_State()

    state_dict = {"START": start_state,
                  "BROWSE USERS": browse_users_state,
                  "NEW USER": create_new_user_state,
                  "IDLE": idle_state,
                  "SAMPLE VIEW":sample_view_state}

    #Set up our Kivy screen manager.
    sm = ScreenManager()
    for state in state_dict.items():
        print(state)
        sm.add_widget(state[1].get_display_panel())

    return state_dict, sm
