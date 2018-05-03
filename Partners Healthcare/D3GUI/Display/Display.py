from kivy.uix.screenmanager import Screen
from kivy.uix.screenmanager import ScreenManager, Screen, NoTransition
from kivy.app import App
from kivy.properties import DictProperty
import Commands.Command as coms
from kivy.properties import StringProperty, ObjectProperty
"""
    The Display Object class holds a list of command objects to activate on a button press, and maps user input
    to the appropriate command object.
"""


class Display_Object(Screen):
    _data = DictProperty(None)

    def __init__(self, commands, name=None):
        super(Display_Object, self).__init__(name=name)
        self._name = name
        self._background = None
        self._commands = commands
        self._data = {}


    def button_press(self, button_text, data=None):
        """
        Kivy will trigger this function when a button is pressed
        :param button_text: string identifier for the button which was pressed
        :return:
        """
        #Check to see if we have a command for the button that was pressed. Execute it if we do.
        command = self._commands.get(button_text.upper(), None)
        if command:
            command.execute(data=data)
        else:
            print("COULD NOT RECOGNIZE COMMAND "+button_text+"\n"
                  "DID YOU FORGET TO ADD THAT COMMAND TO THIS DISPLAY OBJECT?")

    def add_data(self, key, value):
        self._data[key] = value

    def get_data(self, key):
        print("RETURNING",self._data.get(key),"FROM",key)
        return self._data.get(key, None)

    #ACCESSORS & MUTATORS
    def get_name(self):
        return self._name

    def get_background(self):
        return self._background

    def set_name(self,new_name):
        self._name = new_name

    def set_background(self, new_background):
        self._background = new_background.copy()


# Declare screens
class Change_Patient_Screen(Display_Object):
    pass

class Browse_Patients_Screen(Display_Object):
    pass
    #TODO: make sure to filter the user files shown live

class Create_New_Patient_Screen(Display_Object):
    pass

class Idle_Screen(Display_Object):
    def __init__(self,commands, name=None):
        super(Idle_Screen,self).__init__(commands,name=name)
        self._current_radio_button = "kappa"

    def advance_radio_button_color(self):
        button_name = ''.join([self._current_radio_button, "_radio_button"])
        color = self.ids[button_name].color
        active = self.ids[button_name].active
        if not active:
            self.ids[button_name].active = True

        if color == [3,1,0,2]:
            color = [0,1,0,2]

        if self._current_radio_button == 'ki67':
            if self.ids['next_marker_button'].text == "Next Marker":
                self.ids['next_marker_button'].text = "Next Patient"

        self.ids[button_name].color = color

    def next_radio_button(self):
        b = self._current_radio_button
        if b == 'kappa':
            b = 'lambda'

        elif b == 'lambda':
            b = 'ki67'

        button_name = ''.join([b, "_radio_button"])
        self.ids[button_name].color = [3,1,0,2]
        self.ids[button_name].active = True
        self._current_radio_button = b
class Sample_View_Screen(Display_Object):
    pass

class Clean_CCD_Screen(Display_Object):
    pass

class Continue_Screen(Display_Object):
    pass

class Login_Screen(Display_Object):
    pass