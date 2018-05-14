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
from Util import File_Processor as fp
from kivy.graphics.texture import Texture
#from Machine_Learning.Lymphoma import Region_Detector as detector
#from Machine_Learning.Lymphoma import Region_Filter as rFilter
import os
import time
import cv2
import numpy as np

class Sample_View_State(State.State):
    def __init__(self, patient):
        super(Sample_View_State, self).__init__(patient)
        self._img = None
        #self._ref = cv2.imread("data/img/reference_image.png", cv2.IMREAD_ANYDEPTH)
        self._camera_center = (1080 / 2., 1920 / 2)
        self._display_area = (1920, 1080)
        #self._filter = rFilter.filter_CNN()
        self._cell_count = 0
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
            print("sample saved")
        except Exception as e:
            #log stuff
            print(e)
        try:
            self.file_processor.sync()
        except Exception as e:
            print("ERROR SYNCING TO DROPBOX\n",e)

    def set_image(self,img):
        self._img = img.copy()
        self.show_image(img)

    def show_image(self,img):
        self._cell_count = 0
        try:
            display_image = self._get_area_to_show(img)
            if display_image.shape != (720, 1280):
                display_image = cv2.resize(display_image, (1280, 720), interpolation=cv2.INTER_CUBIC)

            if display_image.dtype == 'uint16':
                if display_image.dtype == 'uint16':
                    display_image = np.divide(display_image, 256)
                    display_image = display_image.astype('uint8')

            if len(display_image.shape) == 2:
                display_image = cv2.cvtColor(display_image, cv2.COLOR_GRAY2RGB)

            simg = self._display.ids["sample_image"]
            simg.texture = Texture.create(size=(display_image.shape[1], display_image.shape[0]), colorfmt='rgb')
            simg.texture.blit_buffer(display_image.tostring(), colorfmt='rgb', bufferfmt='ubyte')
        except Exception as e:
            print("Error trying to show image!\n",e)

        try:
            self._display.ids['sample_histogram'].set_data(img)

        except:
            print("Error trying to set sample image histogram!")
    def _get_area_to_show(self,img):
        if (img.shape[0],img.shape[1]) != (self._ref.shape[0],self._ref.shape[1]):
            return img
        w, h = self._display_area
        y, x = self._camera_center
        y1 = y - h // 2
        y2 = y + h // 2
        x1 = x - w // 2
        x2 = x + w // 2
        box = [x1, y1, x2, y2]
        x1, y1, x2, y2, cx, cy = self.reshapeBox(box, (w, h), (img.shape[1],img.shape[0]))
        if len(img.shape) == 3:
            return img[y1:y2,x1:x2,:]
        return img[y1:y2,x1:x2]

    def reshapeBox(self, box, shape, boundaryShape):
        # Unpack the box.
        x1, y1, x2, y2 = box
        # h & w are the maximum x,y coordinates that the box is allowed to attain.
        h, w = boundaryShape
        # dw & dh are the desired width and height of the box.
        dw, dh = shape

        # Calculated the amount that the dimensions of the box will need to be changed
        heightExpansion = dh - abs(y1 - y2)
        widthExpansion = dw - abs(x1 - x2)

        # Force y1 to be the smallest of the y values. Shift y1 by half of the necessary expansion. Bound y1 to a minimum of 0.
        y1 = max(int(round(min(y1, y2) - heightExpansion / 2.)), 0)

        # Force y2 to be the largest of the y values. Shift y2 by half the necessary expansion. Bound y2 to a maximum of h.
        y2 = min(int(round(max(y1, y2) + heightExpansion / 2.)), h)

        # These two lines are a repeat of the above y1,y2 calculations but with x and w instead of y and h.
        x1 = max(int(round(min(x1, x2) - widthExpansion / 2.)), 0)
        x2 = min(int(round(max(x1, x2) + widthExpansion / 2.)), w)

        # Calculate the center point of the newly reshaped box, truncated.
        cx = x1 + abs(x1 - x2) // 2
        cy = y1 + abs(y1 - y2) // 2

        return x1, y1, x2, y2, cx, cy
    #Refer to superclass documentation.
    def _init_paths(self):
        return

    def _init_commands(self):
        retry_command = coms.Retry_Button_Command(self)
        save_command = coms.Save_Sample_Button_Command(self)
        home_command = coms.Home_Button_Command(self)
        self._commands = {"RETRY": retry_command,
                          "SAVE": save_command,
                          "HOME":home_command}

    def _build_display(self):
        self._display = displays.Sample_View_Screen(self._commands, name="Sample_View_Screen")