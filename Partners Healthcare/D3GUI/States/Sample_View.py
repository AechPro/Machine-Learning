from States import State
from Display import Display as displays
from Commands import Command as coms
import os


class Sample_View_State(State.State):
    def execute(self):
        super(Sample_View_State, self).execute()
        self._display.ids['temp_image'].source = 'TEMP_'+str(self._current_patient._ID)+".png"

    def delete_temp_image(self):
        try:
            os.remove(self._display.ids['temp_image'].source)
        except OSError as oe:
            #log stuff
            print(oe)

    def save_sample(self):
        try:
            os.rename(self._display.ids['temp_image'].source, 'data/img/'+self._display.ids['temp_image'].source)
            print("sample saved")
        except OSError as oe:
            #log stuff
            print(oe)


    #Refer to superclass documentation.
    def _init_paths(self):
        return

    def _init_commands(self):
        retry_command = coms.Retry_Button_Command(self)
        save_command = coms.Save_Sample_Button_Command(self)
        self._commands = {"RETRY": retry_command,
                          "SAVE": save_command}

    def _build_display(self):
        self._display = displays.Sample_View_Screen(self._commands, name="Sample_View_Screen")
