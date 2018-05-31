"""
Copyright 2017 Matthew W. Allen & David Pacheco

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
"""
from States import State
from Display import Display as displays
from Commands import Command as coms
from kivy.logger import Logger
from kivy.config import Config
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
        except Exception as e:
            Logger.exception("Exception trying to validate login data!\nTIME: {}\nEXCEPTION: {}".
                             format(time.strftime("%m/%d/%Y_%H:%M:%S"), e))
            return False

    #Refer to superclass documentation.
    def _init_paths(self):
        return

    def _init_commands(self):
        login_command = coms.Login_Command(self)
        exit_command = coms.Exit_Button_Command(self)
        self._commands = {"LOGIN": login_command,
                          "EXIT":exit_command}

    def _build_display(self):
        self._display = displays.Login_Screen(self._commands, name="Login_Screen")
