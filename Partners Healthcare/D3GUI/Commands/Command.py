
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


"""
    The execute() function in State_Command must trigger a state transition to happen on the next clock cycle.
"""
class State_Command(Command):
    def __init__(self,obj_target,state):
        super(State_Command,self).__init__(obj_target)
        self._state = state

class Browse_Users_Button_State_Command(State_Command):
    def execute(self):
        self._state.set_next_state("BROWSE USERS")

class Create_New_User_Button_State_Command(State_Command):
    def execute(self):
        self._state.set_next_state("NEW USER")

class Save_User_Button_State_Command(State_Command):
    def execute(self):
        self._state.save_user()
        self._state.set_next_state("IDLE")

class Back_Button_State_Command(State_Command):
    def execute(self):
        self._state.set_next_state("BACK")

class Camera_Capture_State_Command(State_Command):
    def __init__(self, camera, state):
        super(Camera_Capture_State_Command,self).__init__(camera,state)
    def execute(self):
        self._object.capture()
        self._state.set_next_state("CAPTURE")
