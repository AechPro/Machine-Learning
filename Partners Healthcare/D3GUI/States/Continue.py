from States import State
from Display import Display as displays
from Commands import Command as coms

class Continue_State(State.State):
    def on_enter(self):
        pass

    def execute(self):
        super(Continue_State, self).execute()

    #Refer to superclass documentation.
    def _init_paths(self):
        return

    def _init_commands(self):
        continue_same_patient_command = coms.Continue_With_Same_Patient_Command(self)
        continue_different_patient_command = coms.Continue_With_Different_Patient_Command(self)
        self._commands = {"CONTINUE WITH SAME\nPATIENT": continue_same_patient_command,
                          "CONTINUE WITH DIFFERENT\nPATIENT": continue_different_patient_command}

    def _build_display(self):
        self._display = displays.Continue_Screen(self._commands, name="Continue_Screen")
