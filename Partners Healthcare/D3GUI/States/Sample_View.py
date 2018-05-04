from States import State
from Display import Display as displays
from Commands import Command as coms
from Util import File_Processor as fp
from kivy.graphics.texture import Texture
import os
import time

class Sample_View_State(State.State):
    def __init__(self, patient):
        super(Sample_View_State, self).__init__(patient)
        self.file_processor = None
        self.setup_dropbox()
        
    def on_enter(self):
        pass

    def execute(self):
        super(Sample_View_State, self).execute()
    def delete_temp_image(self):
        try:
            os.remove(self._display.ids['sample_image'].source)
        except Exception as e:
            #log stuff
            print(e)

    def setup_dropbox(self):
        local_paths = {
            'samples': 'data/img/Samples',
            'reconstructed': 'data/img/Reconstructed'}
        remote_paths = {'samples': "/Unprocessed", 'reconstructed': "/Reconstructed"}
        self.file_processor = fp.File_Processor(
            id="sGDRqJ6j3gkAAAAAAABEJtN4aZshf-Uzo1JLekCjKAt_FPkAOsJsTUuXPcUenH33", remote_paths=remote_paths,
            local_paths=local_paths)

    def save_sample(self):
        try:
            os.rename(self._display.ids['sample_image'].source, 'data/img/'+str(self._current_patient._ID) + "_"+time.strftime("%Y.%m.%d_%H.%M.%S")+".png")
            self.file_processor.sync()
            print("sample saved")
        except OSError as oe:
            #log stuff
            print(oe)

    def set_image(self,img):
        simg = self._display.ids["sample_image"]
        simg.texture = Texture.create(size=(img.shape[1],img.shape[0]),colorfmt='rgb')
        simg.texture.blit_buffer(img.tostring(),colorfmt='rgb',bufferfmt='ubyte')
        self._display.ids['sample_histogram'].set_data(img)

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