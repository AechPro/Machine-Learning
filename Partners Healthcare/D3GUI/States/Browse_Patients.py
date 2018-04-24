from States import State
from Display import Display as displays
from Commands import Command as coms


class Browse_Patients_State(State.State):
    def execute(self):
        super(Browse_Patients_State, self).execute()

    def _init_paths(self):
        return

    def _init_commands(self):
        select_command = coms.Select_Patient_Button_Command(self)
        back_command = coms.Back_Button_Command(self)
        self._commands = {"SELECT": select_command,
                          "CANCEL": back_command}

    def _build_display(self):
        self._display = displays.Browse_Patients_Screen(self._commands, name="Browse_Patient_Screen")