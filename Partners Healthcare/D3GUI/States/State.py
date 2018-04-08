class State(object):
    def __init__(self):
        self._file_paths = []
        self._commands = []
        self._display = None
        self._name = None
        self._init_paths()
        self._init_commands()
        self._build_display()

    #this function will be called inside Top and should execute everything this state needs to do each cycle
    def execute(self):
        raise NotImplementedError

    #this function should fill the _file_paths list with every static file path that will ever be needed by this object
    def _init_paths(self):
        raise NotImplementedError

    #this function should look into the _file_path list for any necessary textures or data files for building the
    #display object contained by this State and load/configure the display object
    def _build_display(self):
        raise NotImplementedError

    #this function should fill the _commands list with any commands that will be exclusive to this state
    #keep in mind that *ALL* UI related commands should be inside the _display object
    def _init_commands(self):
        raise NotImplementedError

    #this is a basic getter for the display object contained by this State
    def get_display_panel(self):
        return self._display

    #this makes a State object callable with the same effect as State.execute()
    def __call__(self, *args, **kwargs):
        self.execute()

class IDLEState(State):
    def __init__(self, cameraObject):
        super().__init__()
        self._camera = cameraObject
        self._display.set_camera(cameraObject)

    def execute(self):
        #the idle state should update the live camera feed and nothing else
        self._camera.update_feed()
        return

    #refer to superclass documentation
    def _init_paths(self):
        return
    def _build_display(self):
        return

class INITState(State):
    def __init__(self):
        super().__init__()
    def execute(self):
        return
    def _init_paths(self):
        return
    def _build_display(self):
        return