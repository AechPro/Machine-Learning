"""
Copyright 2017 Ismail Deganii

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
from kivy.clock import Clock
from kivy.graphics.texture import Texture
from kivy.core.camera import CameraBase
import select
import v4l2capture
from PIL import Image
from threading import Thread
from kivy.logger import Logger
import sys
from time import sleep
import time
import datetime
import numpy as np

import cv2
import cv2.cv as cv
import os
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
        self._device = '/dev/video%d' % self._index
        if not self.stopped:
            self.start()
    def _do_capture(self, is_ref):
        try:
            device = self._device
            video = v4l2capture.Video_device(device)
            (res_x, res_y) = self._capture_resolution
            fourcc = self._capture_fourcc
            (size_x, size_y) = video.set_format(res_x, res_y, fourcc=fourcc)
            capture_texture_size = (size_x, size_y)
            video.create_buffers(1)
            video.queue_all_buffers()
            video.start()
            select.select((video,), (), ())
            image_data = video.read_and_queue()
            Logger.debug("Obtained a frame of size %d", len(image_data))
            image = Image.frombytes(self._get_mode_from_fourcc(fourcc),
                                    capture_texture_size, image_data)
            ts = time.time()
            st = datetime.datetime.fromtimestamp(ts).strftime('%Y-%m-%d-%Hh-%Mm-%Ss')
            if is_ref:
                file = '/home/pi/d3-captures/reference-%s.tiff' % st
            else:
                file = '/home/pi/d3-captures/capture-%s.tiff' % st
            image.save(file, format='PNG')
            video.close()
        except:
            e = sys.exc_info()[0]
            Logger.exception('Exception! %s', e)
            Clock.schedule_once(self.stop)

    def _v4l_init_video(self):
        device = self._device
        (res_x, res_y) = self.resolution
        fourcc = self._fourcc
        Logger.info("video_thread started")
        video = v4l2capture.Video_device(device)
        (size_x, size_y) = video.set_format(res_x, res_y, fourcc=fourcc)
        self._texture_size = (size_x, size_y)
        Logger.info("Received resolution: %d,%d", size_x, size_y)
        video.create_buffers(1)
        video.queue_all_buffers()
        video.start()
        self._reset_fps()
        return video

    def _v4l_loop(self):
        video = None
        while True:
            try:
                video = self._v4l_init_video()
                # set to the auto on startup
                # video.set_exposure_absolute(400)
            except:
                e = sys.exc_info()[0]
                Logger.exception('Exception on video thread startup! %s', e)
                try:
                    if video is not None:
                        video.close()
                except:
                    e2 = sys.exc_info()[0]
                    Logger.info("Exception while trying to close video stream for retry... %s", e2)
                Logger.info("Trying to restart video stream")
                # Try again in a second...
                sleep(2.0)
                os.system("sudo ./usbreset /dev/bus/usb/001/007")
                sleep(5.0)
            break # get out of the loop once this works...

        while not self.stopped:
            try:

                # Logger.debug("Obtaining a frame...")
                select.select((video,), (), ())
                image_data = video.read_and_queue()
                # Logger.debug("Obtained a frame of size %d", len(image_data))
                image = Image.frombytes(self._mode, self._texture_size, image_data)
                self._user_buffer = image

                # convert to rgb for display on-screen
                while (self._buffer is not None):
                    # make this an event object?
                    sleep(0.02)



                #self._buffer = image.convert('RGB').tobytes("raw", "RGB")
                image = image.convert('RGB')

                # draw some hough circles on the RGB buffer as an overlay
                if self._object_detection:
                    # convert from PIL RGB colorspace to opencv's BGR
                    color_imcv = cv2.cvtColor(np.asarray(image), cv2.COLOR_RGB2BGR)
                    gray_imcv = np.asarray(self._user_buffer)
                    circles = cv2.HoughCircles(gray_imcv, cv.CV_HOUGH_GRADIENT, 1, 2, np.array([]), 100, 10,0,10)
                    if circles is not None:
                        a, b, c = circles.shape
                        for i in range(b):
                                cv2.circle(color_imcv, (circles[0][i][0], circles[0][i][1]), circles[0][i][2], (0, 0, 255), 3, cv2.CV_AA)
                                cv2.circle(color_imcv, (circles[0][i][0], circles[0][i][1]), 2, (0, 255, 0), 3, cv2.CV_AA)  # draw center of circle
                        # convert back from opencv's BGR colorspace to PIL RGB
                        image = Image.fromarray(cv2.cvtColor(color_imcv,cv2.COLOR_BGR2RGB))

                # convert to RGB in order to display on-screen
                self._buffer = image.tobytes("raw", "RGB")
                self._fps_tick()

                Clock.schedule_once(self._update)

                self._exposure = video.get_exposure_absolute()

                if(self._exposure_requested):
                    video.set_exposure_absolute(self._requested_exposure)
                    self._exposure_requested = False

                if(self.capture_requested or self.ref_requested):
                    # need to switch to high res mode
                    video.close()
                    self._do_capture(self.ref_requested)
                    self.capture_requested = False
                    self.ref_requested = False
                    # reinitialize
                    video = self._v4l_init_video()
            except:
                e = sys.exc_info()[0]
                Logger.exception('Exception! %s', e)
                video.close()
                Logger.info("Trying to restart video stream")
                # Try again...
                sleep(1.0)
                video = self._v4l_init_video()

                #Clock.schedule_once(self.stop)
        Logger.info("closing video object")
        video.close()
        Logger.info("video_thread exiting")

    def _reset_fps(self):
        self.TICK_SAMPLES = 25
        self._ticksum = 0
        self._tickindex = 0
        self._tick_samples = np.zeros(self.TICK_SAMPLES)
        self._lasttime = time.time()
        self._fps = 0

    def _fps_tick(self):
        newtime = time.time()
        newtick = newtime - self._lasttime
        self._ticksum -= self._tick_samples[self._tickindex]
        self._ticksum += newtick
        self._tick_samples[self._tickindex] = newtick
        self._tickindex = (self._tickindex + 1) % self.TICK_SAMPLES
        self._fps = self.TICK_SAMPLES / self._ticksum
        self._lasttime = newtime

    def start(self):
        print("Starting camera")
        Logger.info("d3 camera start() called")
        super(Camera_Object, self).start()
        t = Thread(name='video_thread',
                   target=self._v4l_loop)
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

    #def _capture_complete(self):
    #    self.dispatch('on_capture_complete')

    def on_texture(self):
        pass

    def on_load(self):
        pass
