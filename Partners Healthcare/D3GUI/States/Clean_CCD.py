from States import State
from Display import Display as displays
from Commands import Command as coms


class Clean_CCD_State(State.State):
    def execute(self):
        super(Clean_CCD_State,self).execute()
    def _init_paths(self):
        return
    def _init_commands(self):
        ok_command = coms.Clean_CCD_Ok_Button_Command(self)
        self._commands = {"OK":ok_command}
    def _build_display(self):
        self._display = displays.Clean_CCD_Screen(self._commands,name="Clean_CCD_Screen")
