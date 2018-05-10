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

"""
    The Command class is the interface for a Command in the command software design pattern.
    A Command is a single object that implements a single concrete function that may or may not
    operate on an object.
"""
class Command(object):
    def __init__(self, obj_target):
        self._object = obj_target

    #This is the single concrete function that will be executed by the Command.
    def execute(self,data=None):
        raise NotImplementedError

    #This allows Command execution to be callable.
    def __call__(self,data=None):
        self.execute(data=data)


class Browse_Patients_Button_Command(Command):
    def execute(self,data=None):
        self._object.set_next_state("BROWSE PATIENTS")

class Change_Patient_Button_Command(Command):
    def execute(self,data=None):
        self._object.set_next_state("CHANGE PATIENT")

class Create_New_Patient_Button_Command(Command):
    def execute(self,data=None):
        self._object.set_next_state("NEW PATIENT")

class Select_Patient_Button_Command(Command):
    def execute(self, data=None):
        self._object.set_next_state("BROWSE IMAGES")
        if data is not None:
            if not self._object.get_patient().load(data):
                self._object.set_next_state(None)

class Select_Image_Button_Command(Command):
    def execute(self,data=None):
        self._object.set_next_state("SAMPLE VIEW")
        if data is not None:
            if not self._object.load_image(data):
                self._object.set_next_state(None)

class Save_Patient_Button_Command(Command):
    def execute(self, data=None):
        self._object.save_patient()
        self._object.set_next_state("IDLE")

class Save_Patient_Idle_State_Button_Command(Command):
    def execute(self,data=None):
        self._object.save_patient()
        self._object.set_next_state("IDLE")

class Save_Sample_Button_Command(Command):
    def execute(self,data=None):
        self._object.save_sample()
        self._object.set_next_state("IDLE")

class Back_Button_Command(Command):
    def execute(self,data=None):
        self._object.set_next_state("BACK")

class Retry_Button_Command(Command):
    def execute(self, data=None):
        self._object.delete_temp_image()
        self._object.set_next_state("IDLE")

class Exit_Button_Command(Command):
    def execute(self,data=None):
        self._object.set_next_state("EXIT")

class Clean_CCD_Ok_Button_Command(Command):
    def execute(self,data=None):
        self._object.set_next_state("IDLE")

class Camera_Capture_Command(Command):
    def execute(self, data=None):
        if not self._object.capture():
            self._object.set_next_state(None)
        self._object.set_next_state("SAMPLE VIEW")

class Continue_With_Same_Patient_Command(Command):
    def execute(self, data=None):
        self._object.set_next_state("IDLE")

class Continue_With_Different_Patient_Command(Command):
    def execute(self, data=None):
        self._object.set_next_state("CHANGE PATIENT")

class Next_Marker_Button_Command(Command):
    def execute(self,data=None):
        self._object.advance_marker()

class Previous_Marker_Button_Command(Command):
    def execute(self,data=None):
        self._object.previous_marker()

class Toggle_LED_Button_Command(Command):
    def execute(self, data=None):
        try:
            self._object.toggle_LED()
        except:
            print("UNABLE TO TOGGLE LED")

class Transfer_Image_Command(Command):
    def execute(self,data=None):
        self._object.set_image(data)

class Logout_Button_Command(Command):
    def execute(self,data=None):
        self._object.set_next_state("LOGIN")

class Home_Button_Command(Command):
    def execute(self,data=None):
        self._object.set_next_state("CHANGE PATIENT")

class Login_Command(Command):
    def execute(self, data=None):
        self._object.set_next_state("CHANGE PATIENT")
        if not self._object.validate_user():
            print("failed")
            self._object.set_next_state(None)
