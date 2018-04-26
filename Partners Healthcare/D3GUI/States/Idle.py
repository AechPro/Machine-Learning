from States import State
from Display import Display as displays
from Commands import Command as coms
import RPi.GPIO as GPIO

LED_PIN = 19 # Broadcom Pin 4

class Idle_State(State.State):
    """
        The Idle State should contain the idle screen Display object, handle the camera feed, and handle swapping
        states based on user interaction.
    """
    def __init__(self,patient):
        super(Idle_State,self).__init__(patient)
        self._LED_state = GPIO.HIGH
        GPIO.setmode(GPIO.BCM)
        GPIO.setup(LED_PIN, GPIO.OUT)
        GPIO.output(LED_PIN,self._LED_state)

    def execute(self):
        super(Idle_State, self).execute()

    def toggle_LED(self):
        if self._LED_state == GPIO.LOW:
            self._LED_state = GPIO.HIGH
        else:
            self._LED_state = GPIO.LOW
        GPIO.output(LED_PIN, self._LED_state)

    def capture(self):
        '''
        Function to capture the images and give them the names
        according to their captured time and date.
        '''
        try:
            camera = self._display.ids['camera']
            filename = 'TEMP_'+str(self._current_patient._ID)+".png"
            path = '/data/img/'
            camera.export_to_png(filename)
            print("Captured")
        except:
            return False
        return True

    def save_patient(self):
        print("patient saved")
        self._current_patient.save()

    def set_camera(self,camera):
        self._camera = camera

    #Refer to superclass documentation.
    def _init_paths(self):
        return

    def _init_commands(self):
        capture_command = coms.Camera_Capture_Command(self)
        select_command = coms.Change_Patient_Button_Command(self)
        exit_command = coms.Exit_Button_Command(self)
        save_command = coms.Save_Patient_Idle_State_Button_Command(self)
        LED_command = coms.Toggle_LED_Button_Command(self)

        self._commands = {"CAPTURE": capture_command,
                          "CHANGE\nPATIENT": select_command,
                          "EXIT": exit_command,
                          "SAVE\nPATIENT": save_command,
                          "TOGGLE\nLED":LED_command}

    def _build_display(self):
        self._display = displays.Idle_Screen(self._commands, name="Idle_Screen")
