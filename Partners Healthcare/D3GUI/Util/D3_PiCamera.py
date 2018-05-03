from kivy.clock import Clock
from kivy.graphics.texture import Texture
from kivy.core.camera import CameraBase
import select
from PIL import Image
from threading import Thread
from kivy.logger import Logger
import sys
from time import sleep
import time
import datetime
import numpy as np
from picamera.array import PiRGBArray
from picamera import PiCamera
import time

import cv2
import cv2.cv as cv


class Camera_Object(CameraBase):
    def __init__(self, **kwargs):
        kwargs.setdefault('fourcc', 'GRAY')
        self._user_buffer = None
        self._format = 'rgb'
        self._video_src = 'v4l'
        self._device = None
        self._texture_size = None
        self._fourcc = kwargs.get('fourcc')
        self._mode = kwargs.get('mode')
        self._capture_resolution = kwargs.get('capture_resolution')
        self._capture_fourcc = kwargs.get('capture_fourcc')
        self.capture_requested = False
        self.ref_requested = False
        self._exposure_requested = False
        self._requested_exposure = 0
        self._exposure = 0
        self._object_detection = False
        self._fps = 0

        if self._mode is None:
            self._mode = self._get_mode_from_fourcc(self._fourcc)

        super(Camera_Object, self).__init__(**kwargs)

    def _get_mode_from_fourcc(self, fourcc):
        return "I;16" if fourcc == "Y16 " else "L"

    def init_camera(self):
        self._device = PiCamera()
        if not self.stopped:
            self.start()

    def _do_capture(self, is_ref):
        try:
            device = self._device
            rawCapture = PiRGBArray(device)
            image = device.capture(rawCapture, format="bgr")
            image = rawCapture.array
            cv2.imwrite("test.png",image)
        except:
            e = sys.exc_info()[0]
            Logger.exception('Exception! %s', e)
            Clock.schedule_once(self.stop)

    def _video_loop(self):
        camera = PiCamera()
        camera.resolution = (640, 480)
        camera.framerate = 32
        rawCapture = PiRGBArray(camera, size=(640, 480))
        for frame in camera.capture_continuous(rawCapture, format="bgr", use_video_port=True):
            # grab the raw NumPy array representing the image, then initialize the timestamp
            # and occupied/unoccupied text
            image = frame.array

    def start(self):
        print("Starting camera")
        Logger.info("d3 camera start() called")
        super(Camera_Object, self).start()
        t = Thread(name='video_thread',
                   target=self._video_loop)
        t.start()

    def stop(self, dt=None):
        super(Camera_Object, self).stop()

    def get_current_frame(self):
        return self._user_buffer

    def capture__full_res_frame(self):
        self.capture_requested = True

    def capture__full_res_ref(self):
        self.ref_requested = True

    def get_fps(self):
        return self._fps

    def set_exposure(self, val):
        self._requested_exposure = val
        self._exposure_requested = True

    def get_exposure(self):
        return self._exposure

    def set_object_detection(self, val):
        self._object_detection = val

    def get_object_detection(self):
        return self._object_detection

    def _update(self, dt):
        if self._buffer is None:
            return
        Logger.debug("Rendering a frame...")
        if self._texture is None and self._texture_size is not None:
            Logger.debug("Creating a new texture...")
            self._texture = Texture.create(
                size=self._texture_size, colorfmt='rgb')
            self._texture.flip_vertical()
            self.dispatch('on_load')
        self._copy_to_gpu()

    # def _capture_complete(self):
    #    self.dispatch('on_capture_complete')

    def on_texture(self):
        pass

    def on_load(self):
        pass
