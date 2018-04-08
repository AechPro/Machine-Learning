from Commands import Command
class UISwitch(Command):
    def execute(self,nextUIPath):
        self._object.load_UI(nextUIPath)