
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


class Browse_Patients_Button_Command(Command):
    def execute(self,data=None):
        self._object.set_next_state("BROWSE USERS")

class Change_Patient_Button_Command(Command):
    def execute(self,data=None):
        self._object.set_next_state("CHANGE USER")

class Create_New_Patient_Button_Command(Command):
    def execute(self,data=None):
        self._object.set_next_state("NEW USER")

class Select_Patient_Button_Command(Command):
    def execute(self, data=None):
        self._object.set_next_state("CLEAN CCD")
        if data is not None:
            if not self._object.get_user().load(data):
                self._object.set_next_state("BROWSE USERS")

class Save_Patient_Button_Command(Command):
    def execute(self,data=None):
        self._object.save_user()
        self._object.set_next_state("CLEAN CCD")

class Save_Patient_Idle_State_Button_Command(Command):
    def execute(self,data=None):
        self._object.save_user()
        self._object.set_next_state("IDLE")

class Save_Sample_Button_Command(Command):
    def execute(self,data=None):
        self._object.save_sample()
        self._object.set_next_state("CONTINUE")

class Back_Button_Command(Command):
    def execute(self,data=None):
        self._object.set_next_state("BACK")

class Exit_Button_Command(Command):
    def execute(self,data=None):
        self._object.set_next_state("EXIT")

class Clean_CCD_Ok_Button_Command(Command):
    def execute(self,data=None):
        self._object.set_next_state("IDLE")

class Camera_Capture_Command(Command):
    def execute(self, data=None):
        self._object.set_next_state("SAMPLE VIEW")
        if not self._object.capture():
            self._object.set_next_state(None)

class Continue_With_Same_Patient_Command(Command):
    def execute(self, data=None):
        self._object.set_next_state("CLEAN CCD")

class Continue_With_Different_Patient_Command(Command):
    def execute(self, data=None):
        self._object.set_next_state("CHANGE USER")

