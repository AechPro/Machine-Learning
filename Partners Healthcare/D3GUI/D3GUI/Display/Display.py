from kivy.uix.screenmanager import ScreenManager, Screen, NoTransition
from kivy.app import App
import Commands.Command as coms
"""
    The Display Object class holds a list of command objects to activate on a button press, as well as a hash map
    for screens to transition through on button press.
"""

class Display_Object(Screen):
    def __init__(self,commands,screen_map,name=None):
        super().__init__()
        self._name = name
        self._background = None
        self._commands = commands
        self._screen_map = screen_map

    def button_press(self,button):
        hash = button.text.upper()
        command = self._commands.get(hash,None)
        if command:
            command.execute()
            next_screen = self._screen_map.get(hash,None)
            if next_screen:
                self.parent.current = next_screen

    def exit(self):
        App.get_running_app().stop()

    def add_component(self,new_component):
        self.display_components.append(new_component.copy())

    def get_name(self):
        return self._name
    def get_background(self):
        return self._background

    def set_name(self,new_name):
        self._name = new_name
    def set_background(self,new_background):
        self._background=new_background.copy()