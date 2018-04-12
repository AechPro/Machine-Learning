import Display
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
        raise NotImplementedError

    #This function should fill the _file_paths list with every static file path that will ever be needed by this object.
    def _init_paths(self):
        raise NotImplementedError

    #This function should look into the _file_paths list for any necessary textures or data files for building the
    #display object contained by this State and load/configure the display object.
    def _build_display(self):
        raise NotImplementedError

    #This function should fill the _commands list with any commands that will be exclusive to this state.
    #Keep in mind that *ALL* UI related commands should be inside the components of the _display object.
    def _init_commands(self):
        raise NotImplementedError

    def get_display_panel(self):
        return self._display

    def get_next_state(self):
        return self._next_state

    def set_next_state(self,state):
        self._next_state = state
    #This makes a State object callable with the same effect as State.execute().
    def __call__(self, *args, **kwargs):
        self.execute()


"""
    The Idle State should contain the idle screen Display object, handle the camera feed, and handle swapping
    states based on user interaction.
"""
class Idle_State(State):
    def __init__(self, cameraObject):
        super().__init__()
        self._camera = cameraObject
        self._display.set_camera(cameraObject)
        self._next_state = "IDLE"

    #The idle state should update the live camera feed and check for user action each cycle.
    def execute(self):
        self._camera.update_feed()

    #Refer to superclass documentation.

    def _init_paths(self):
        raise NotImplementedError

    def _build_display(self):
        raise NotImplementedError

    def _init_commands(self):
        raise NotImplementedError


"""
    The View Image State should contain the display objects necessary for our image viewing screen,
    and handle swapping states based on user interaction.
"""
class View_Image_State(State):
    def __init__(self):
        super().__init__()

    def execute(self):
        return

    #Refer to superclass documentation.
    def _init_paths(self):
        return
    def _build_display(self):
        return