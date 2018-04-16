from States import State
from Display import Display as displays
from Commands import Command as coms
class Idle_State(State.State):
    """
        The Idle State should contain the idle screen Display object, handle the camera feed, and handle swapping
        states based on user interaction.
    """
    def __init__(self, user, camera_object):
        self._camera = camera_object
        super(Idle_State,self).__init__(user)

    def execute(self):
        super(Idle_State, self).execute()
        if self._camera is not None:
            self._camera.update_feed()

    def capture(self):
        return False

    #Refer to superclass documentation.
    def _init_paths(self):
        return

    def _init_commands(self):
        capture_command = coms.Camera_Capture_Command(self)
        select_command = coms.Change_User_Button_Command(self)
        exit_command = coms.Exit_Button_Command(self)

        self._commands = {"CAPTURE": capture_command,
                          "CHANGE\nUSER": select_command,
                          "EXIT": exit_command}

    def _build_display(self):
        self._display = displays.Idle_Screen(self._commands,name="Idle_Screen")
