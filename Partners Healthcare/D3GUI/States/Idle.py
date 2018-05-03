from States import State
from Display import Display as displays
from Commands import Command as coms
import cv2
#import RPi.GPIO as GPIO

LED_PIN = 19 # Broadcom Pin 4

class Idle_State(State.State):
    """
        The Idle State should contain the idle screen Display object, handle the camera feed, and handle swapping
        states based on user interaction.
    """
    def __init__(self,patient):
        super(Idle_State,self).__init__(patient)
        '''self._LED_state = GPIO.HIGH
        GPIO.setmode(GPIO.BCM)
        GPIO.setup(LED_PIN, GPIO.OUT)
        GPIO.output(LED_PIN,self._LED_state)'''
        self._hist_update_tick = 0
        self._camera = None


    def on_enter(self):
        if self._camera is not None:
            self._camera.start()

    def execute(self):
        super(Idle_State, self).execute()
        try:
            if self._camera is None:
                self._camera = self._display.ids.get('camera',None)

            if self._hist_update_tick >= 60:
                self._hist_update_tick = 0
                frame = self._camera.get_current_frame()
                if frame is not None:
                    self._display.ids["ref_taken_radio_button"].active = not self._display.ids[
                        "ref_taken_radio_button"].active
                    self._display.ids['hist'].set_data(frame)
            self._hist_update_tick+=1
        except:
            print("Unable to execute Idle loop!")
    def toggle_LED(self):
        try:
            if self._LED_state == GPIO.LOW:
                self._LED_state = GPIO.HIGH
            else:
                self._LED_state = GPIO.LOW
            GPIO.output(LED_PIN, self._LED_state)
        except:
            print("Unable to toggle LED!")

    def capture(self):
        '''
        Function to capture the images and give them the names
        according to their captured time and date.
        '''
        self._display.advance_radio_button_color()
        img = cv2.imread("Machine_Learning/Lymphoma/test_hologram.png")
        img = cv2.resize(img,(1280,720),interpolation=cv2.INTER_CUBIC)
        self._commands["TRANSFER IMAGE"].execute(data=img)
        try:
            filename = 'TEMP_'+str(self._current_patient._ID)+".png"
            img = self._camera.get_current_frame(filename)
            img = cv2.resize(img, (1280, 720), interpolation=cv2.INTER_CUBIC)
            self._commands["TRANSFER IMAGE"].execute(data=img)
            self._camera.stop()
            print("Captured")
        except:
            print("Unable to capture!")
            return False
        return True

    def advance_marker(self):
        self._display.next_radio_button()

    def save_patient(self):
        print("patient saved")
        self._current_patient.save()


    #Refer to superclass documentation.
    def _init_paths(self):
        return

    def _init_commands(self):
        capture_command = coms.Camera_Capture_Command(self)
        select_command = coms.Change_Patient_Button_Command(self)
        exit_command = coms.Exit_Button_Command(self)
        save_command = coms.Save_Patient_Idle_State_Button_Command(self)
        LED_command = coms.Toggle_LED_Button_Command(self)
        next_marker_command = coms.Next_Marker_Button_Command(self)
        self._commands = {"CAPTURE": capture_command,
                          "CHANGE\nPATIENT": select_command,
                          "NEXT PATIENT": select_command,
                          "EXIT": exit_command,
                          "SAVE\nPATIENT": save_command,
                          "TOGGLE\nLED":LED_command,
                          "NEXT MARKER":next_marker_command}

    def _build_display(self):
        self._display = displays.Idle_Screen(self._commands, name="Idle_Screen")
