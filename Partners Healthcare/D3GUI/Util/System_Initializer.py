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
from kivy.uix.screenmanager import ScreenManager
from States import Change_Patient, Browse_Patients, Create_Patient, Idle, Sample_View, Continue, Login
from States import Clean_CCD, Browse_Images
from Patients import Patient_Profile
from Commands import Command as coms
import os


def init(first_state):
    """
    Basic function to initialize all of the states, commands, and UI objects that will be used by Top and Kivy.
    :return: void
    """
    setup_directory_structure()

    patient = Patient_Profile.Patient(123456, "jane_smith")

    change_patient_state = Change_Patient.Change_Patient_State(patient)
    browse_patient_state = Browse_Patients.Browse_Patients_State(patient)
    create_new_patient_state = Create_Patient.Create_New_Patient_State(patient)
    idle_state = Idle.Idle_State(patient)
    sample_view_state = Sample_View.Sample_View_State(patient)
    continue_state = Continue.Continue_State(patient)
    login_state = Login.Login_State(patient)
    browse_images_state = Browse_Images.Browse_Images_State(patient)

    state_dict = {"BROWSE PATIENTS": browse_patient_state, "NEW PATIENT": create_new_patient_state,
                  "IDLE": idle_state, "CONTINUE": continue_state,
                  "CHANGE PATIENT": change_patient_state, "SAMPLE VIEW":sample_view_state,
                  "LOGIN":login_state, "BROWSE IMAGES":browse_images_state}

    setup_state_commands(state_dict)

    #Set up our Kivy screen manager.
    sm = ScreenManager()
    state_to_display_first = state_dict[first_state]
    sm.add_widget(state_to_display_first.get_display_panel())
    del state_dict[first_state]
    for state in state_dict.items():
        sm.add_widget(state[1].get_display_panel())
    state_dict[first_state] = state_to_display_first
    return state_dict, sm

def setup_state_commands(states):
    transfer_image = coms.Transfer_Image_Command(states["SAMPLE VIEW"])
    states["IDLE"].add_command("TRANSFER IMAGE",transfer_image)
    states["BROWSE IMAGES"].add_command("TRANSFER IMAGE",transfer_image)

def setup_directory_structure():
    dirs = ["data/patients","data/img","data"]
    for d in dirs:
        if not os.path.exists(d):
            os.makedirs(d)