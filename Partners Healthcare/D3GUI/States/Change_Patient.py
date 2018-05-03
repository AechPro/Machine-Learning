from States import State
from Display import Display as displays
from Commands import Command as coms


class Change_Patient_State(State.State):
    def on_enter(self):
        pass

    def execute(self):
        super(Change_Patient_State, self).execute()

    def _init_paths(self):
        return

    def _init_commands(self):
        browse_command = coms.Browse_Patients_Button_Command(self)
        create_command = coms.Create_New_Patient_Button_Command(self)
        self._commands = {"BROWSE PATIENTS": browse_command,
                          "NEW PATIENT": create_command}

    def _build_display(self):
        print(self._commands)
        self._display = displays.Change_Patient_Screen(self._commands, name="Select_Patient_Screen")
