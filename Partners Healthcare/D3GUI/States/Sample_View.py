from States import State
from Display import Display as displays
from Commands import Command as coms


class Sample_View_State(State.State):
    def execute(self):
        super(Sample_View_State, self).execute()

    def save_sample(self):
        print("sample saved")

    #Refer to superclass documentation.
    def _init_paths(self):
        return

    def _init_commands(self):
        back_command = coms.Back_Button_Command(self)
        save_command = coms.Save_Sample_Button_Command(self)
        self._commands = {"RETRY": back_command,
                          "SAVE": save_command}

    def _build_display(self):
        self._display = displays.Sample_View_Screen(self._commands, name="Sample_View_Screen")
