from kivy.uix.screenmanager import ScreenManager
from States import Change_Patient, Browse_Patients, Create_Patient, Idle, Sample_View, Continue, Login
from States import Clean_CCD
from Patients import Patient_Profile
from Util import D3_Camera_Image_Canvas as cam
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
    clean_ccd_state = Clean_CCD.Clean_CCD_State(patient)
    continue_state = Continue.Continue_State(patient)
    login_state = Login.Login_State(patient)
    state_dict = {"BROWSE PATIENTS": browse_patient_state, "NEW PATIENT": create_new_patient_state,
                  "IDLE": idle_state, "CLEAN CCD": clean_ccd_state, "CONTINUE": continue_state,
                  "CHANGE PATIENT": change_patient_state, "SAMPLE VIEW":sample_view_state, "LOGIN":login_state}

    #Set up our Kivy screen manager.
    sm = ScreenManager()
    state_to_display_first = state_dict[first_state]
    sm.add_widget(state_to_display_first.get_display_panel())
    del state_dict[first_state]
    for state in state_dict.items():
        sm.add_widget(state[1].get_display_panel())
    state_dict[first_state] = state_to_display_first
    return state_dict, sm

def setup_directory_structure():
    if not os.path.exists("data/patients"):
        os.makedirs("data/patients")
