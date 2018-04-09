
"""
    The Command class is the interface for a Command in the command software design pattern.
    A Command is a single object that implements a single concrete function that may or may not
    operate on an object.
"""
class Command(object):
    def __init__(self, obj_target):
        self._object = obj_target

    #This is the single concrete function that will be executed by the Command.
    def execute(self):
        raise NotImplementedError

    #This allows Command execution to be callable.
    def __call__(self):
        self.execute()