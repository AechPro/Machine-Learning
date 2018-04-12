from kivy.uix.screenmanager import ScreenManager
from Display.Display import Display_Object
from Commands import Command as coms
from States import State as states
def init():
    sm = ScreenManager()
    start_state = states.Start_State()
    browse_users_state = states.Browse_Users_State()
    state_dict = {"START":start_state,"BROWSE USERS":browse_users_state}

    for state in state_dict.items():
        sm.add_widget(state[1].get_display_panel())

    return state_dict, sm



"""
    sm.add_widget(IdleScreen(name='idle'))
    sm.add_widget(BrowseUserScreen(name='browse_user'))
    sm.add_widget(CreateUserScreen(name='create_user'))
    sm.add_widget(MainSampleScreen(name='main_sample'))
    sm.add_widget(SampleViewScreen(name='sample_view'))
"""