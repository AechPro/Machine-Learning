class Command(object):
    def __init__(self, objTarget):
        self._object = objTarget
    def execute(self):
        raise NotImplementedError
    def __call__(self):
        self.execute()
class UISwitch(Command):
    def execute(self,nextUIPath=None):
        self._object.load_UI(nextUIPath)