from kivy.uix.screenmanager import ScreenManager, Screen, NoTransition
from kivy.app import App
import Commands.Command as coms
"""
    The Display Object class holds a list of command objects to activate on a button press, and maps user input
    to the appropriate command object.
"""

class Display_Object(Screen):
    def __init__(self,commands,name=None):
        super().__init__(name=name)
        self._name = name
        self._background = None
        self._commands = commands

    #Kivy will trigger this function when a button is pressed.
    def button_press(self,button):
        #Check to see if we have a command for the button that was pressed. Execute it if we do.
        command = self._commands.get(button.text.upper(),None)
        if command:
            command.execute()
        else:
            print("COULD NOT RECOGNIZE COMMAND "+button.text)

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
    pass

class Idle_Screen(Display_Object):
    pass