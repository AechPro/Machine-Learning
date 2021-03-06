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
import cv2
from kivy.logger import Logger
import time
try:
    import RPi.GPIO as GPIO
except Exception as e:
    Logger.exception("Exception trying to import RPi GPIO Library!\nTIME: {}\nEXCEPTION: {}".
                     format(time.strftime("%m/%d/%Y_%H:%M:%S"), e))

LED_PIN = 19 # Broadcom Pin 4

class Idle_State(State.State):
    """
        The Idle State should contain the idle screen Display object, handle the camera feed, and handle swapping
        states based on user interaction.
    """
    def __init__(self,patient):
        super(Idle_State,self).__init__(patient)
        try:
            self._LED_state = GPIO.HIGH
            GPIO.setmode(GPIO.BCM)
            GPIO.setup(LED_PIN, GPIO.OUT)
            GPIO.output(LED_PIN,self._LED_state)
        except Exception as e:
            Logger.exception("Exception trying to set up GPIO LED pin!\nTIME: {}\nEXCEPTION: {}".
                             format(time.strftime("%m/%d/%Y_%H:%M:%S"), e))
        self._hist_update_tick = 0
        self._camera = None


    def on_enter(self):
        pass
        #if self._camera is not None:
            #self._camera.start()

    def execute(self):
        super(Idle_State, self).execute()
        try:
            if self._camera is None:
                self._camera = self._display.ids.get('camera',None)
            if self._hist_update_tick >= 60:
                self._hist_update_tick = 0
                frame = self._camera.get_current_frame()
                if frame is not None:
                    self._display.ids["ref_taken_radio_button"].active = \
                        not self._display.ids["ref_taken_radio_button"].active
                    self._display.ids['sample_histogram'].set_data(frame)
            self._hist_update_tick+=1

        except Exception as e:
            Logger.exception("Exception trying to execute IDLE state loop!\nTIME: {}\nEXCEPTION: {}".
                             format(time.strftime("%m/%d/%Y_%H:%M:%S"),e))

    def toggle_LED(self):
        try:
            if self._LED_state == GPIO.LOW:
                self._LED_state = GPIO.HIGH
            else:
                self._LED_state = GPIO.LOW
            GPIO.output(LED_PIN, self._LED_state)
        except Exception as e:
            Logger.exception("Exception trying to toggle LED!\nTIME: {}\nEXCEPTION: {}".
                             format(time.strftime("%m/%d/%Y_%H:%M:%S"),e))

    def capture(self):
        self._display.advance_radio_button_color()
        try:
            img = self._camera.get_current_frame()
            self._commands["TRANSFER IMAGE"].execute(data=img)
            #self._camera.stop()
            print("Captured")
        except Exception as e:
            Logger.exception("Unable to capture hologram!\nTIME: {}\nEXCEPTION: {}".
                             format(time.strftime("%m/%d/%Y_%H:%M:%S"), e))
            return False
        return True

    def advance_marker(self):
        self._display.next_radio_button()

    def previous_marker(self):
        self._display.previous_radio_button()

    def save_patient(self):
        print("patient saved")
        self._current_patient.save()


    #Refer to superclass documentation.
    def _init_paths(self):
        return

    def _init_commands(self):
        capture_command = coms.Camera_Capture_Command(self)
        select_command = coms.Change_Patient_Button_Command(self)
        home_command = coms.Home_Button_Command(self)
        save_command = coms.Save_Patient_Idle_State_Button_Command(self)
        LED_command = coms.Toggle_LED_Button_Command(self)
        next_marker_command = coms.Next_Marker_Button_Command(self)
        prev_marker_commands = coms.Previous_Marker_Button_Command(self)
        self._commands = {"CAPTURE": capture_command,
                          "CHANGE\nPATIENT": select_command,
                          "NEXT PATIENT": select_command,
                          "HOME": home_command,
                          "SAVE\nPATIENT": save_command,
                          "TOGGLE\nLED":LED_command,
                          "NEXT MARKER":next_marker_command,
                          "PREVIOUS MARKER":prev_marker_commands}

    def _build_display(self):
        self._display = displays.Idle_Screen(self._commands, name="Idle_Screen")
