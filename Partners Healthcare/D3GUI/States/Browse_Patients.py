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


class Browse_Patients_State(State.State):

    def on_enter(self):
        self._display.ids["filechooser"]._update_files()

    def execute(self):
        super(Browse_Patients_State, self).execute()

    def _init_paths(self):
        return

    def _init_commands(self):
        select_command = coms.Select_Patient_Button_Command(self)
        home_command = coms.Home_Button_Command(self)
        self._commands = {"SELECT": select_command,
                          "CANCEL": home_command,
                          "HOME":home_command}

    def _build_display(self):
        self._display = displays.Browse_Patients_Screen(self._commands, name="Browse_Patient_Screen")
