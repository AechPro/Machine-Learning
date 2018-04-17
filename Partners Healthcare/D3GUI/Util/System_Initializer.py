from kivy.uix.screenmanager import ScreenManager
from States import Change_User, Browse_Users, Create_User, Idle, Sample_View
from States import Clean_CCD
from Users import User
import os

#Basic function to initialize all of the states, commands, and UI objects that will be used by Top and Kivy.
def init():
    setup_directory_structure()

    user = User.User(123456,"jane_smith")

    change_user_state = Change_User.Change_User_State(user)
    browse_users_state = Browse_Users.Browse_Users_State(user)
    create_new_user_state = Create_User.Create_New_User_State(user)
    idle_state = Idle.Idle_State(user,None)
    sample_view_state = Sample_View.Sample_View_State(user)
    clean_ccd_state = Clean_CCD.Clean_CCD_State(user)

    state_dict = {"CHANGE USER": change_user_state,
                  "BROWSE USERS": browse_users_state,
                  "NEW USER": create_new_user_state,
                  "IDLE": idle_state,
                  "SAMPLE VIEW":sample_view_state,
                  "CLEAN CCD":clean_ccd_state}

    #Set up our Kivy screen manager.
    sm = ScreenManager()
    for state in state_dict.items():
        sm.add_widget(state[1].get_display_panel())

    return state_dict, sm
def setup_directory_structure():
    if not os.path.exists("data/users"):
        os.makedirs("data/users")
