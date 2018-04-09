"""
    The Display Object class should hold a list of Component objects to display and an optional background texture.
    This class should be responsible for calling the appropriate update and render functions for each of its components,
    as well as rendering all of its components in the appropriate positions on the screen.
"""

class Display_Object(object):
    def __init__(self,name=None):
        self.display_components = []
        self._name = name
        self._build_components()
        self._background = None

    #This function should be responsible for updating any components in the UI that need attention each cycle.
    #In theory, this and render() should be exactly the same for every subclass, so should be implemented here.
    def update(self):
        for comp in self.display_components:
            comp.update()

    #This function should be responsible for ensuring this display object can be drawn in the next function call.
    def render(self):
        if self._background is not None:
            #If Kivy lets us render textures on their own, do that here.
            self._background.kivy_render_function()

        #Call render for each of our components.
        #It may be a good idea to add a render priority system for components to ensure
        #some components get rendered in front of other components.
        for comp in self.display_components:
            comp.render()

    #This function should load and put together everything for this display object.
    def _build_components(self):
        raise NotImplementedError

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
