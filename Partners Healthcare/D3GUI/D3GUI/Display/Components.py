import Commands.Command
import kivy


"""
    The Component class should contain a single Kivy object to display and interact with.
    Upon user interaction with the Kivy object, the Component should execute a Command 
    associated with the interaction.
"""
class Component(object):
    def __init__(self):
        self._commands = {}
        self._texture = None
        self._pos = [0,0]

        #This should be some Kivy component, e.g. a button or frame of some kind to display.
        self._ui_component = None

    def update(self):
        """
            I have no idea if this is the correct way to access a Kivy object.
            This method will be called every cycle, and should check if a user has interacted
            with the Kivy object contained in this Component. If an action has happened,
            this method should all the Command associated with that action.
        """
        return self._ui_component.get_input()

    def render(self):
        #If Kivy renders components per cycle, call that here.
        self._ui_component.kivy_render_function_call()

    def load_texture(self,file_path):
        #Load a texture with Kivy here and associate it with the Kivy object contained in this class.
        self._ui_component.load_texture(file_path)
        self.texture = self._ui_component.get_texture()

    #ACCESSORS & MUTATORS
    def get_pos(self):
        return self._pos
    def get_texture(self):
        return self._texture
    def set_pos(self, new_pos):
        self._pos = [new_pos[0], new_pos[1]]
    def set_texture(self,new_texture):
        self.texture = new_texture.copy()