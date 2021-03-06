"""
Copyright 2017 Matthew W. Allen

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
from Tiscamera.list_formats import list_formats, get_frame_rate_list
from kivy.graphics.texture import Texture
from kivy.core.camera import CameraBase
from threading import Thread
from kivy.logger import Logger
import sys
import numpy as np
import cv2
import gi

gi.require_version("Tcam", "0.1")
gi.require_version("Gst", "1.0")
from gi.repository import Tcam, Gst, GLib

class D3_Camera(CameraBase):
    def __init__(self,**kwargs):
        kwargs.setdefault('fourcc', 'GRAY')
        self._user_buffer = None
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
        self._pipeline = None
        self._loop = None
        self._capture_width, self._capture_height = 2592, 1944
        self._num_frames_to_buffer = 1
        self._frame_buffer = [None for _ in range(self._num_frames_to_buffer)]
        self._frame_ptr = 0
        self.display_pipeline = None
        self._resolution = kwargs.get('resolution')

        print("RES: ",self._resolution)
        super(D3_Camera, self).__init__(**kwargs)
        self.stopped = False

    def init_camera(self):
        Gst.init(sys.argv)  # init gstreamer
        # We create a source element to retrieve a device list through it
        source = Gst.ElementFactory.make("tcambin")
        source.get_device_serials()
        # Define the format of the video buffers that will get passed to opencv
        TARGET_FORMAT = "video/x-raw,width={},height={},format=GRAY8".format(self._capture_width, self._capture_height)
        formats = list_formats(source)
        fmt = None
        for format in formats:
            if format.get_value("format") == "GRAY8" \
                    and int(format.get_value("width")) == self._capture_width \
                    and int(format.get_value("height")) == self._capture_height:
                fmt = format
            print(format.get_value("format"), format.get_value("width"), format.get_value("height"))

        frame_rates = get_frame_rate_list(fmt)
        numerator, denominator = frame_rates[0].split("/")
        numerator = int(numerator)
        denominator = int(denominator)
        for i in range(len(frame_rates)):
            n, d = frame_rates[i].split("/")
            n = int(n)
            d = int(d)
            if n / d > numerator / denominator:
                numerator = n
                denominator = d

        fmt.set_value("framerate", Gst.Fraction(int(numerator), int(denominator)))
        print("----VIDEO FORMAT SPECIFICATIONS----\nWidth: {}\nHeight: {}\nFPS: {}\nFormat: {}"
              .format(fmt.get_value("width"), fmt.get_value("height"), fmt.get_value("framerate"),
                      fmt.get_value("format")))
        # Ask the user for the format that should be used for capturing
        # If the user selected a bayer format, we change it to BGRx so that the
        # tcambin will decode the bayer pattern to a color image
        if fmt.get_name() == "video/x-bayer":
            fmt.set_name("video/x-raw")
            fmt.set_value("format", "BGRx")
        # Use a capsfilter to determine the video format of the camera source
        capsfilter = Gst.ElementFactory.make("capsfilter")
        capsfilter.set_property("caps", Gst.Caps.from_string(fmt.to_string()))
        # Add a small queue. Everything behind this queue will run in a separate
        # thread.
        queue = Gst.ElementFactory.make("queue")
        queue.set_property("leaky", True)
        queue.set_property("max-size-buffers", 2)
        # Add a videoconvert and a videoscale element to convert the format of the
        # camera to the target format for opencv
        convert = Gst.ElementFactory.make("videoconvert")
        scale = Gst.ElementFactory.make("videoscale")
        # Add an appsink. This element will receive the converted video buffers and
        # pass them to opencv
        output = Gst.ElementFactory.make("appsink")
        output.set_property("caps", Gst.Caps.from_string(TARGET_FORMAT))
        output.set_property("emit-signals", True)
        pipeline = Gst.Pipeline.new()

        # Add all elements
        pipeline.add(source)
        pipeline.add(capsfilter)
        pipeline.add(queue)
        pipeline.add(convert)
        pipeline.add(scale)
        pipeline.add(output)

        # Link the elements
        source.link(capsfilter)
        capsfilter.link(queue)
        queue.link(convert)
        convert.link(scale)
        scale.link(output)

        # Usually one would use cv2.imgshow(...) to display an image but this is
        # tends to hang in threaded environments. So we create a small display
        # pipeline which we could use to display the opencv buffers.
        output.connect("new-sample", self.camera_callback)
        pipeline.set_state(Gst.State.PLAYING)

        self._loop = GLib.MainLoop()
        #source.set_property("gain",1)
        """tbin = source
        if tbin is None:
            print("No property source available.")
            return
        else:
            # use introspection to get a property list
            prop_names = tbin.get_tcam_property_names()
            # we use this list to iterate all properties and create a widget list to allow
            # user interaction
            # each property is contained in a vbox
            for name in prop_names:
                print("name:",name)
                prop_type = tbin.get_tcam_property_type(name)
                print("type:",prop_type)
                args = tbin.get_tcam_property(name)
                print("args:")
                for arg in args:
                    print(arg)"""
    def camera_callback(self, sink):
        if self.stopped:
            return False
        sample = sink.emit("pull-sample")
        if sample:
            buf = sample.get_buffer()

            caps = sample.get_caps()
            width = caps[0].get_value("width")
            height = caps[0].get_value("height")

            try:
                res, mapinfo = buf.map(Gst.MapFlags.READ)

                img_array = np.asarray(bytearray(mapinfo.data), dtype=np.uint8)
                img = img_array.reshape((height, width))
                img = cv2.cvtColor(img,cv2.COLOR_GRAY2RGB)

                self._frame_buffer[self._frame_ptr] = img
                self._frame_ptr+=1

                if self._frame_ptr >= self._num_frames_to_buffer:
                    self._frame_ptr = 0

                if self._frame_buffer[-1] is not None:
                    self._buffer = cv2.resize(self._frame_buffer[-1], (self._resolution[0], self._resolution[1]),
                                              interpolation=cv2.INTER_CUBIC)
                Clock.schedule_once(self._update)
            finally:
                buf.unmap(mapinfo)

        return Gst.FlowReturn.OK

    def _update(self, dt):
        if self.stopped:
            return
        if self._texture is None:
            # Create the texture
            self._texture = Texture.create(self._resolution,colorfmt='rgb')
            self._texture.flip_vertical()
            self.dispatch('on_load')
        try:
            if self._buffer is not None:
                self._copy_to_gpu()
        except:
            Logger.exception('Error attempting to copy image buffer to GPU!')

    def _copy_to_gpu(self):
        '''Copy the the buffer into the texture'''
        if self._texture is None:
            Logger.debug('Camera: copy_to_gpu() failed, _texture is None !')
            return
        self._texture.blit_buffer(self._buffer.tostring(), colorfmt='rgb', bufferfmt='ubyte')
        self._buffer = None
        self.dispatch('on_texture')

    def start(self):
        t = Thread(name='video_thread', target=self._loop.run)
        t.start()
        super(D3_Camera, self).start()

    def stop(self):
        self._loop.quit()
        self._pipeline.set_state(Gst.State.NULL)
        self.stopped = True
        super(D3_Camera, self).stop()

    def get_current_frame(self):
        return self._frame_buffer[-1]