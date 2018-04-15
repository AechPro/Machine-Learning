from Commands import Command as coms
from Display import Display as displays

"""
    The State class is meant to be an abstract class with protected local variables that each State must have
    and the functions each state must implement.
"""
class State(object):
    def __init__(self):
        self._file_paths = []
        self._commands = []
        self._display = None
        self._name = None
        self._next_state = None
        self._init_paths()
        self._init_commands()
        self._build_display()

    #This function should execute everything this state will need to do each cycle.
    def execute(self):
        self._next_state = None

    #This function should fill the _file_paths list with every static file path that will ever be needed by this object.
    def _init_paths(self):
        raise NotImplementedError

    #This function should look into the _file_paths list for any necessary textures or data files for building the
    #display object contained by this State and load/configure the display object.
    def _build_display(self):
        raise NotImplementedError

    #This function should fill the _commands list with any commands that will be exclusive to this state.
    def _init_commands(self):
        raise NotImplementedError


    #ACCESSORS & MUTATORS.
    def get_display_panel(self):
        return self._display

    def get_next_state(self):
        return self._next_state

    def set_next_state(self,state):
        self._next_state = state

    #This makes a State object callable with the same effect as State.execute().
    def __call__(self, *args, **kwargs):
        self.execute()


class Idle_State(State):
    """
        The Idle State should contain the idle screen Display object, handle the camera feed, and handle swapping
        states based on user interaction.
    """
    def __init__(self, camera_object):
        self._camera = camera_object
        super(Idle_State,self).__init__()

    def execute(self):
        super(Idle_State, self).execute()
        if self._camera is not None:
            self._camera.update_feed()

    #Refer to superclass documentation.

    def _init_paths(self):
        return

    def _init_commands(self):
        capture_command = coms.Camera_Capture_State_Command(self._camera, self)
        browse_command = coms.Browse_Users_Button_State_Command(None, self)
        exit_command = coms.Exit_Button_State_Command(None, self)

        self._commands = {"CAPTURE": capture_command,
                          "CHANGE\nUSER": browse_command,
                          "EXIT": exit_command}
        return

    def _build_display(self):
        self._display = displays.Idle_Screen(self._commands,name="Idle_Screen")
        return



"""
    The following classes are generic incomplete state objects for basic Kivy testing.
"""

class Browse_Users_State(State):
    def execute(self):
        super(Browse_Users_State, self).execute()
    def _init_paths(self):
        return
    def _init_commands(self):
        select_command = coms.Select_User_Button_State_Command(None, self)
        back_command = coms.Back_Button_State_Command(None, self)
        self._commands = {"SELECT": select_command,
                          "CANCEL": back_command}
    def _build_display(self):
        self._display = displays.Browse_Users_Screen(self._commands,name="Browse_Users_Screen")

class Create_New_User_State(State):
    def execute(self):
        super(Create_New_User_State, self).execute()

    def save_user(self):
        return

    def _init_paths(self):
        return

    def _init_commands(self):
        back_command = coms.Back_Button_State_Command(None, self)
        save_command = coms.Save_User_Button_State_Command(None, self)
        self._commands = {"CANCEL": back_command,
                          "SAVE USER": save_command}

    def _build_display(self):
        self._display = displays.Create_New_User_Screen(self._commands, name="Create_User_Screen")

class Start_State(State):
    def execute(self):
        super(Start_State, self).execute()

    def _init_paths(self):
        return

    def _init_commands(self):
        browse_command = coms.Browse_Users_Button_State_Command(None, self)
        create_command = coms.Create_New_User_Button_State_Command(None, self)
        self._commands = {"BROWSE USERS": browse_command,
                          "NEW USER": create_command}

    def _build_display(self):
        self._display = displays.Start_Screen(self._commands, name="Start_Screen")

class Sample_Capture_State(State):
    pass

class Sample_View_State(State):
    def execute(self):
        super(Sample_View_State, self).execute()

    #Refer to superclass documentation.
    def _init_paths(self):
        return

    def _init_commands(self):
        back_command = coms.Back_Button_State_Command(None, self)
        save_command = coms.Save_Sample_Button_State_Command(None, self)
        self._commands = {"RETRY": back_command,
                          "SAVE SAMPLE": save_command}

    def _build_display(self):
        self._display = displays.Sample_View_Screen(self._commands, name="Sample_View_Screen")
