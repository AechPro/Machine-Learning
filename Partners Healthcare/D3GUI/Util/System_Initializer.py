from kivy.uix.screenmanager import ScreenManager
from Display.Display import Display_Object
from Commands import Command as coms
from States import State as states
def init():
    start_state = states.Start_State()
    browse_users_state = states.Browse_Users_State()
    create_user_state = states.Create_User_State()
    idle_state = states.Idle_State(None)
    capture_state = states.Sample_Capture_State()
    sample_view_state = states.Sample_View_State()



    """
        sm.add_widget(IdleScreen(name='idle'))
        sm.add_widget(BrowseUserScreen(name='browse_user'))
        sm.add_widget(CreateUserScreen(name='create_user'))
        sm.add_widget(MainSampleScreen(name='main_sample'))
        sm.add_widget(SampleViewScreen(name='sample_view'))
    """