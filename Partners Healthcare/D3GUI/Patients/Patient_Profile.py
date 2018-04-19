import datetime


class Patient(object):
    def __init__(self, user_ID, user_name):
        super(Patient, self).__init__()
        self._ID = user_ID
        self._name = user_name
        self._data = []
        self.file_path = "data/patients/"

    def save(self):
        print(self._ID, self._name)
        t = datetime.datetime.now()
        date_string = "{}{}{}{}{}{}".format(t.year, t.month, t.day, t.hour, t.minute, t.second)
        file_name = "{}_{}_{}.txt".format(self._ID, self._name, date_string)
        file_directory = ''.join([self.file_path, file_name])

        with open(file_directory, 'w') as f:
            f.write("{}\n{}\n".format(self._ID, self._name))
            for entry in self._data:
                f.write("{}\n".format(entry))

    def load(self, file_path):
        """
        :param file_path: the file path returned by Kivy of the patient data to be loadaed.
        :return bool:
        """
        if len(file_path) == 0:
            print("Unable to load patient, invalid selection!")
            return False
        try:
            name_data = file_path[0]
            if "\\" in name_data:
                name_data = name_data.split("\\")
            elif "/" in name_data:
                name_data = name_data.split("/")
            name_data = name_data[-1].split("_")
            self.ID = name_data[-4]
            self._name = ''.join([name_data[-3],'_',name_data[-2]])
            file_name = "{}_{}_{}_{}".format(name_data[-4],name_data[-3],name_data[-2],name_data[-1])
            print("Loading patient from", file_name)
            self._data = []
            with open(''.join([self.file_path,file_name])) as f:
                lines = f.readlines()
                self._data = [line for line in lines]
        except:
            return False
        print("PATIENT NAME:", self._name)
        return True
