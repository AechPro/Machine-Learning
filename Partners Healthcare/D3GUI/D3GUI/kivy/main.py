from kivy.uix.screenmanager import ScreenManager, Screen, NoTransition
from kivy.app import App
from kivy.event import EventDispatcher
from kivy.properties import StringProperty, DictProperty, ObjectProperty
from kivy.uix.button import Button
import time


# Declare screens
class IdleScreen(Screen):
    def button_press(self, button):
        if button.text == "New User":
            self.parent.current = 'create_user'
        else:
            self.parent.current = 'browse_user'


class BrowseUserScreen(Screen):
    def button_press(self, button):
        if button.text == "Back":
            self.parent.current = 'idle'


class CreateUserScreen(Screen):
    def button_press(self, button):
        if button.text == "Back":
            self.parent.current = 'idle'
        if button.text == "Save User":
            self.parent.current = 'main_sample'


class MainSampleScreen(Screen):
    def capture(self):
        camera = self.ids['camera']
        timestr = time.strftime("%Y%m%d_%H%M%S")
        img_name = "IMG_{}.png".format(timestr)
        camera.export_to_png(img_name)
        print("Captured")
        self.parent.image_src = img_name
        self.parent.current = 'sample_view'


class SampleViewScreen(Screen):
    pass
    '''
    def __init__(self, *kwargs):
        super(SampleViewScreen, self).__init__(*kwargs)
        self.btn = Button(size_hint=(1, 0.1), pos_hint={'x': 0.1, 'y': 0.1})

    def show_popup(self):
        self.add_widget(self.btn)

    def hide_popup(self):
        self.remove_widget(self.btn)


class StateManager(ScreenManager):
    def __init__(self, *kwargs):
        super(StateManager, self).__init__(*kwargs)
        self.patient_state = StringProperty()
        self.image_src = StringProperty()
'''


class D3App(App):
    def build(self):
        sm = ScreenManager(transition=NoTransition())
        sm.add_widget(IdleScreen(name='idle'))
        sm.add_widget(BrowseUserScreen(name='browse_user'))
        sm.add_widget(CreateUserScreen(name='create_user'))
        sm.add_widget(MainSampleScreen(name='main_sample'))
        sm.add_widget(SampleViewScreen(name='sample_view'))
        return sm


if __name__ == '__main__':
    app = D3App()
    app.run()