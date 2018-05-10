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
