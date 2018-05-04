from States import State
from Display import Display as displays
from Commands import Command as coms
import cv2

class Browse_Images_State(State.State):

    def on_enter(self):
        self._display.ids["filechooser2"].path = "data/img/samples"
        self._display.ids["filechooser2"]._update_files()

    def execute(self):
        super(Browse_Images_State, self).execute()

    def load_image(self,file_path):

        img = cv2.imread(file_path)
        self._commands["TRANSFER IMAGE"].execute(img)
    def _init_paths(self):
        return

    def _init_commands(self):
        select_command = coms.Select_Image_Button_Command(self)
        back_command = coms.Back_Button_Command(self)
        self._commands = {"SELECT": select_command,
                          "CANCEL": back_command}

    def _build_display(self):
        self._display = displays.Browse_Images_Screen(self._commands, name="Browse_Images_Screen")
