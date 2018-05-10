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
from Patients import Patient_Profile
from random import randint
from kivy.properties import StringProperty, ObjectProperty


class Create_New_Patient_State(State.State):
    def on_enter(self):
        pass

    def execute(self):
        super(Create_New_Patient_State, self).execute()

    def save_patient(self):
        #TODO: validate input
        patient_id = randint(100000, 999999)
        self._current_user = Patient_Profile.Patient(patient_id, self._display.ids['patient_name'].text)
        self._current_user._data = self._display.ids['patient_comments'].text.split('\n')
        self._current_user.save()

    def _init_paths(self):
        return

    def _init_commands(self):
        back_command = coms.Back_Button_Command(self)
        save_command = coms.Save_Patient_Button_Command(self)
        home_command = coms.Home_Button_Command(self)
        self._commands = {"CANCEL": back_command,
                          "SAVE PATIENT": save_command,
                          "HOME":home_command}

    def _build_display(self):
        self._display = displays.Create_New_Patient_Screen(self._commands, name="Create_Patient_Screen")
