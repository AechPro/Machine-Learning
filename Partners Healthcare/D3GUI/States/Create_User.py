from States import State
from Display import Display as displays
from Commands import Command as coms
from Users import User
from random import randint
from kivy.properties import StringProperty, ObjectProperty


class Create_New_User_State(State.State):

    def execute(self):
        super(Create_New_User_State, self).execute()

    def save_user(self):
        #TODO: validate input
        patient_id = randint(100000, 999999)
        self._current_user = User.User(patient_id, self._display.ids['patient_name'].text)
        self._current_user._data = self._display.ids['patient_comments'].text.split('\n')
        self._current_user.save()

    def _init_paths(self):
        return

    def _init_commands(self):
        back_command = coms.Back_Button_Command(self)
        save_command = coms.Save_User_Button_Command(self)
        self._commands = {"CANCEL": back_command,
                          "SAVE USER": save_command}

    def _build_display(self):
        self._display = displays.Create_New_User_Screen(self._commands, name="Create_User_Screen")
