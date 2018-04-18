from States import State
from Display import Display as displays
from Commands import Command as coms
import time
import os


class Idle_State(State.State):
    """
        The Idle State should contain the idle screen Display object, handle the camera feed, and handle swapping
        states based on user interaction.
    """

    def execute(self):
        super(Idle_State, self).execute()

    def capture(self):
        '''
        Function to capture the images and give them the names
        according to their captured time and date.
        '''
        camera = self._display.ids['camera']
        timestr = time.strftime("%Y%m%d_%H%M%S")
        filename = 'TEMP_'+self._current_user._ID+".png"
        path = '\data\img\\'
        camera.export_to_png(filename)
        #os.rename(filename, path+filename) this doesn't work because the .png file is not placed fast enough for it to be there when this is executed
        # doing export_to_png(path+filename) also doesn't work for some reason
        print("Captured")
        return True

    def save_user(self):
        print("user saved")
        self._current_user.save()

    #Refer to superclass documentation.
    def _init_paths(self):
        return

    def _init_commands(self):
        capture_command = coms.Camera_Capture_Command(self)
        select_command = coms.Change_User_Button_Command(self)
        exit_command = coms.Exit_Button_Command(self)
        save_command = coms.Save_User_Idle_State_Button_Command(self)

        self._commands = {"CAPTURE": capture_command,
                          "CHANGE\nUSER": select_command,
                          "EXIT": exit_command,
                          "SAVE\nUSER": save_command}

    def _build_display(self):
        self._display = displays.Idle_Screen(self._commands,name="Idle_Screen")
