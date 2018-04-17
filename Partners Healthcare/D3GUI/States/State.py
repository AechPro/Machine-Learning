"""
    The State class is meant to be an abstract class with protected local variables that each State must have
    and the functions each state must implement.
"""
class State(object):
    def __init__(self,user):
        self._current_user = user
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

    def get_user(self):
        return self._current_user

    def set_current_user(self,user):
        self._current_user = user

    def set_next_state(self,state):
        self._next_state = state

    #This makes a State object callable with the same effect as State.execute().
    def __call__(self, *args, **kwargs):
        self.execute()