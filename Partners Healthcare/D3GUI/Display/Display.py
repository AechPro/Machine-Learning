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
    pass

class Sample_View_Screen(Display_Object):
    def __init__(self, commands, name=None):
        super(Sample_View_Screen, self).__init__(commands, name=name)
        self._data = {"Temp Image":"TEMP_1234567.png"}
        print("DATA: ",self._data)

class Clean_CCD_Screen(Display_Object):
    pass

class Continue_Screen(Display_Object):
    pass