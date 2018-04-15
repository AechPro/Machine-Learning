import datetime
class User(object):
    def __init__(self):
        super(object,self).__init__()
        self.ID = ""
        self.name = ""
        self.data = []

    def save_user(self):
        t = datetime.datetime.now()
        date_string = "{}_{}_{}".format(t.year,t.month,t.day)
        file_name = "{}_{}_{}_{}.txt".format(self.ID,self.name,date_string,t.microsecond)
        file_path = "data/users/"
        file_directory = ''.join([file_path,file_name])
        with open(file_directory,'w') as f:
            f.write("{}\n{}\n".format(self.ID,self.name))
            for entry in self.data:
                f.write("{}\n".format(entry))
