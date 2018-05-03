"""
    The State class is meant to be an abstract class with protected local variables that each State must have
    and the functions each state must implement.
"""
class State(object):
    def __init__(self, patient):
        self._current_patient = patient
        self._file_paths = {}
        self._commands = []
        self._display = None
        self._name = None
        self._next_state = None
        self._init_paths()
        self._init_commands()
        self._build_display()
    def on_enter(self):
        """
        This function should be called every time this state becomes the active state.
        :return: void
        """
        raise NotImplementedError

    def execute(self):
        """
        This function should execute everything this state will need to do each cycle.
        :return: void
        """
        self._next_state = None

    def _init_paths(self):
        """
        This function should fill the _file_paths list with every static file path that will ever be needed by this object.
        :return: void
        """
        raise NotImplementedError

    def _build_display(self):
        """
        This function should look into the _file_paths list for any necessary textures or data files for building the
        display object contained by this State and load/configure the display object.
        :return: void
        """
        raise NotImplementedError

    def _init_commands(self):
        """
        This function should fill the _commands list with any commands that will be exclusive to this state.
        :return: void
        """
        raise NotImplementedError


    #ACCESSORS & MUTATORS.
    def get_display_panel(self):
        return self._display

    def get_next_state(self):
        return self._next_state

    def get_patient(self):
        return self._current_patient

    def set_current_patient(self,patient):
        self._current_patient = patient

    def set_next_state(self,state):
        self._next_state = state
    def add_command(self,key,com):
        self._commands[key] = com
    def __call__(self, *args, **kwargs):
        """
        This makes a State object callable with the same effect as State.execute().
        :param args: ...
        :param kwargs: ...
        :return: void
        """
        self.execute()