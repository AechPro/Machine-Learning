class Display_Object(object):
    def __init__(self,name=None):
        self.display_components = []
        self.name = None
        self._build_components()

    #this function should be responsible for updating any components in the UI that need attention each cycle
    #in theory, this and render should be exactly the same for every subclass, so should be implemented here
    def update(self):
        return

    #this function should be responsible for ensuring this display object can be drawn in the next function call
    def render(self):
        return

    #this function should load and put together everything for this display object
    def _build_components(self):
        raise NotImplementedError