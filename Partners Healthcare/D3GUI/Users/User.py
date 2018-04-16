import datetime
import os
class User(object):
    def __init__(self,user_ID,user_name):
        super(object,self).__init__()
        self._ID = user_ID
        self._name = user_name
        self._data = []
        self.file_path = "data/users/"
        if not os.path.exists(self.file_path):
            os.makedirs(self.file_path)

    def save(self):
        t = datetime.datetime.now()
        date_string = "{}_{}_{}".format(t.year,t.month,t.day)
        file_name = "{}_{}_{}_{}.txt".format(self._ID,self._name,date_string,t.microsecond)
        file_directory = ''.join([self.file_path,file_name])

        with open(file_directory,'w') as f:
            f.write("{}\n{}\n".format(self._ID,self._name))
            for entry in self._data:
                f.write("{}\n".format(entry))

    def load(self,file_path):
        file_name = file_path[0][file_path[0].rfind("\\")+1:]
        print("Loading from",file_name)
        dat = file_name.split("_")

        self._data = []
        self._ID = dat[0]
        self._name = dat[1]

        with open(''.join([self.file_path,file_name])) as f:
            lines = f.readlines()
            self._data = [line for line in lines]

        print("ID: {}\nName: {}\nData: {}".format(self._ID,self._name,self._data))