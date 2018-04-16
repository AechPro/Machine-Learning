
"""
    The Command class is the interface for a Command in the command software design pattern.
    A Command is a single object that implements a single concrete function that may or may not
    operate on an object.
"""
class Command(object):
    def __init__(self, obj_target):
        self._object = obj_target

    #This is the single concrete function that will be executed by the Command.
    def execute(self,data=None):
        raise NotImplementedError

    #This allows Command execution to be callable.
    def __call__(self,data=None):
        self.execute(data=data)

"""
    A State_Command is a Command that oper
"""
class State_Command(Command):
    def __init__(self, obj_target, state):
        super(State_Command, self).__init__(obj_target)
        self._state = state

class Browse_Users_Button_State_Command(State_Command):
    def execute(self,data=None):
        self._state.set_next_state("BROWSE USERS")

class Change_User_Button_State_Command(State_Command):
    def execute(self,data=None):
        self._state.set_next_state("CHANGE USER")

class Create_New_User_Button_State_Command(State_Command):
    def execute(self,data=None):
        self._state.set_next_state("NEW USER")

class Select_User_Button_State_Command(State_Command):
    def execute(self,data=None):
        if data is not None:
            self._state.get_user().load(data)
        self._state.set_next_state("IDLE")

class Save_User_Button_State_Command(State_Command):
    def execute(self,data=None):
        self._state.save_user()
        self._state.set_next_state("IDLE")

class Save_Sample_Button_State_Command(State_Command):
    def execute(self,data=None):
        self._state.save_sample()
        self._state.set_next_state("IDLE")

class Back_Button_State_Command(State_Command):
    def execute(self,data=None):
        self._state.set_next_state("BACK")

class Exit_Button_State_Command(State_Command):
    def execute(self,data=None):
        self._state.set_next_state("EXIT")

class Camera_Capture_State_Command(State_Command):
    def __init__(self, camera, state):
        super(Camera_Capture_State_Command,self).__init__(camera, state)
    def execute(self,data=None):
        self._state.set_next_state("SAMPLE VIEW")
        try:
            self._object.capture()
        except Exception as e:
            print("Error trying to capture!\n",type(e).__name__,"\n",e.args)

