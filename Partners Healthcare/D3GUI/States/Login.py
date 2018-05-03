from States import State
from Display import Display as displays
from Commands import Command as coms
from kivy.uix.popup import Popup
import time
import os


class Login_State(State.State):
    """
        The Idle State should contain the idle screen Display object, handle the camera feed, and handle swapping
        states based on user interaction.
    """
    def on_enter(self):
        pass

    def execute(self):
        super(Login_State, self).execute()

    def validate_user(self):
        tried_username = self._display.ids['username'].text
        tried_password = self._display.ids['password'].text
        try:
            login_file = open("data/login_info.txt", "r")
            user_data = login_file.readlines()
            for line in user_data:
                username, password = line.split(" ")
                if username.strip() == str(tried_username) and password.strip() == str(tried_password):
                    print("Match Found")
                    return True
        except IOError as ioe:
            #log error
            print(ioe)
            return False

    #Refer to superclass documentation.
    def _init_paths(self):
        return

    def _init_commands(self):
        login_command = coms.Login_Command(self)
        self._commands = {"LOGIN": login_command}

    def _build_display(self):
        self._display = displays.Login_Screen(self._commands, name="Login_Screen")
