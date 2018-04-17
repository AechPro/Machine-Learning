from States import State
from Display import Display as displays
from Commands import Command as coms


class Change_User_State(State.State):
    def execute(self):
        super(Change_User_State, self).execute()

    def _init_paths(self):
        return

    def _init_commands(self):
        browse_command = coms.Browse_Users_Button_Command(self)
        create_command = coms.Create_New_User_Button_Command(self)
        self._commands = {"BROWSE USERS": browse_command,
                          "NEW USER": create_command}

    def _build_display(self):
        print(self._commands)
        self._display = displays.Change_User_Screen(self._commands, name="Select_User_Screen")
