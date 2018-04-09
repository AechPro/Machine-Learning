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

    #This function should look into the _file_path list for any necessary textures or data files for building the
    #display object contained by this State and load/configure the display object.
    def _build_display(self):
        raise NotImplementedError

    #This function should fill the _commands list with any commands that will be exclusive to this state.
    #Keep in mind that *ALL* UI related commands should be inside the components of the _display object.
    def _init_commands(self):
        raise NotImplementedError

    #This is a basic accessor for the display object contained by this State.
    def get_display_panel(self):
        return self._display

    def get_next_state(self):
        return self._next_state
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

    #The idle state should update the live camera feed and nothing else each cycle.
    def execute(self):
        self._camera.update_feed()

    #Refer to superclass documentation.
    def _init_paths(self):
        return
    def _build_display(self):
        return

"""
    The Init State should load all variables and build all objects that will be necessary for the 
    smooth execution of the program. Everything will be pulled out of this class by Top after loading
    is complete.
"""
class Init_State(State):
    def __init__(self):
        super().__init__()
        self.objs = []
        self.vars = []

    #The init state should set up variables and load components that will be needed by the Top class.
    def execute(self):
        return

    #This is an accessor for Top to pull all of the objects and variables out of this object.
    def get_initialized_data(self):
        return (self.objs, self.vars)

    #Refer to superclass documentation.
    def _init_paths(self):
        return
    def _build_display(self):
        return

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