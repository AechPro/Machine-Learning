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
from kivy.logger import Logger
import time
import cv2
import numpy as np

class Browse_Images_State(State.State):

    def on_enter(self):
        self._display.ids["filechooser"].path = "data/img/samples"
        self._display.ids["filechooser"]._update_files()

    def execute(self):
        super(Browse_Images_State, self).execute()

    def load_image(self,file_path):
        img_name = None
        file_path = file_path[0]
        if "\\" in file_path:
            img_name = file_path.split("\\")[-1]
        elif "/" in file_path:
            img_name = file_path.split("/")[-1]
        ref_name = img_name[:img_name.rfind(".png")]
        try:
            img = cv2.imread(''.join(["data/img/samples/",img_name]),cv2.IMREAD_ANYDEPTH)
            ref = cv2.imread(''.join(["data/img/samples/",ref_name,"_ref.png"]),cv2.IMREAD_ANYDEPTH)
            print(ref_name)
            if img is None or ref is None:
                return False
            self._commands["TRANSFER IMAGE"].execute(np.array([img,ref]))
            return True
        except Exception as e:
            Logger.exception("Exception trying to load image!\nTIME: {}\nEXCEPTION: {}".format(time.strftime
                                                                                             ("%m/%d/%Y_%H:%M:%S"), e))
            return False
    def _init_paths(self):
        return

    def _init_commands(self):
        select_command = coms.Select_Image_Button_Command(self)
        back_command = coms.Back_Button_Command(self)
        home_command = coms.Home_Button_Command(self)
        self._commands = {"SELECT": select_command,
                          "CANCEL": back_command,
                          "HOME":home_command}

    def _build_display(self):
        self._display = displays.Browse_Images_Screen(self._commands, name="Browse_Images_Screen")
