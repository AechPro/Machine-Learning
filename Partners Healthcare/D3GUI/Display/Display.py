from kivy.uix.screenmanager import ScreenManager, Screen, NoTransition
from kivy.app import App
import Commands.Command as coms
from kivy.properties import StringProperty, ObjectProperty
"""
    The Display Object class holds a list of command objects to activate on a button press, and maps user input
    to the appropriate command object.
"""

class Display_Object(Screen):
    def __init__(self,commands, name=None):
        super(Display_Object, self).__init__(name=name)
        self._name = name
        self._background = None
        self._commands = commands


    def button_press(self, button_text):
        """
        Kivy will trigger this function when a button is pressed
        :param button_text: string identifier for the button which was pressed
        :return:
        """
        #Check to see if we have a command for the button that was pressed. Execute it if we do.
        command = self._commands.get(button_text.upper(), None)
        if command:
            command.execute()
        else:
            print("COULD NOT RECOGNIZE COMMAND "+button_text)

    #ACCESSORS & MUTATORS
    def get_name(self):
        return self._name
    def get_background(self):
        return self._background

    def set_name(self,new_name):
        self._name = new_name
    def set_background(self,new_background):
        self._background=new_background.copy()


# Declare screens
class Start_Screen(Display_Object):
    pass

class Browse_Users_Screen(Display_Object):
    pass

class Create_New_User_Screen(Display_Object):

    patient_id = ObjectProperty(None)

    def update(self):
        print(self.patient_id.text)

class Idle_Screen(Display_Object):
    pass