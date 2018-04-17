import datetime


class User(object):
    def __init__(self, user_ID, user_name):
        super(User, self).__init__()
        self._ID = user_ID
        self._name = user_name
        self._data = []
        self.file_path = "data/users/"

    def save(self):
        t = datetime.datetime.now()
        date_string = "{}{}{}{}{}{}".format(t.year, t.month, t.day, t.hour, t.minute, t.second)
        file_name = "{}_{}_{}.txt".format(self._ID, self._name, date_string)
        file_directory = ''.join([self.file_path, file_name])

        with open(file_directory, 'w') as f:
            f.write("{}\n{}\n".format(self._ID, self._name))
            for entry in self._data:
                f.write("{}\n".format(entry))

    def load(self, file_path):

        if len(file_path) == 0:
            print("Unable to load user, invalid selection!")
            return False

        file_name = file_path[0][file_path[0].rfind("\\")+1:]
        print("Loading user from", file_name)

        dat = file_name.split("_")
        self._data = []
        self._ID = dat[0]
        self._name = ''.join([dat[1], '_', dat[2]])

        with open(''.join([self.file_path, file_name])) as f:
            lines = f.readlines()
            self._data = [line for line in lines]
        print("USER NAME:", self._name)
        return True
