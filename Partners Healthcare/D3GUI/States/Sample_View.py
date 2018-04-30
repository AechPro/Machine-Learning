from States import State
from Display import Display as displays
from Commands import Command as coms
from Util import File_Processor as fp
import os
import time


class Sample_View_State(State.State):
    def __init__(self, patient):
        super(Sample_View_State, self).__init__(patient)
        print(os.getcwd())
        local_paths = {
            'samples': '/Users/davidmv74/PycharmProjects/Machine-Learning/Partners Healthcare/D3GUI/data/img/Samples',
            'reconstructed': '/Users/davidmv74/PycharmProjects/Machine-Learning/Partners Healthcare/D3GUI/data/img/Reconstructed'}
        remote_paths = {'samples': "/Unprocessed", 'reconstructed': "/Reconstructed"}
        self.file_processor = fp.File_Processor(
            id="wUn7ipvWMdAAAAAAAAABzsThrL9GONrFVbP80dhCXKg39xQXrx78iBS5--pnMfU0", remote_paths=remote_paths,
            local_paths=local_paths)
    def execute(self):
        super(Sample_View_State, self).execute()
        self._display.ids['sample_image'].source = 'TEMP_'+str(self._current_patient._ID)+".png"

    def delete_temp_image(self):
        try:
            os.remove(self._display.ids['sample_image'].source)
        except OSError as oe:
            #log stuff
            print(oe)

    def save_sample(self):
        try:
            os.rename(self._display.ids['sample_image'].source, 'data/img/'+str(self._current_patient._ID) + "_"+time.strftime("%Y.%m.%d_%H.%M.%S")+".png")
            self.file_processor.sync()
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
