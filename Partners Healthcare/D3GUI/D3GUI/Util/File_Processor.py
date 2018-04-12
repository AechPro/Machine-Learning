import os
import dropbox
from kivy.logger import Logger
import sys
from dropbox.files import WriteMode

class File_Processor():
    def __init__(self, **kwargs):
        self._uploading = False
        self._downloading = False
        self._dropbox = dropbox.Dropbox(kwargs['id'])
        self._remote_path = kwargs['remote_path']
        self._max_size = 1024*150
        self.local_path = kwargs['local_path']

    def upload(self, file, name):
        try:
            total_upload_size = os.path.getsize(file)
            remote_path = self.local_path + name

            with open(file, 'rb') as f:
                if total_upload_size <= self._max_size:
                    self._dropbox.files_upload(f.read(), path=remote_path, mode=WriteMode('overwrite'), mute=True)
                else:
                    try:
                        upload_session_start_result = self._dropbox.files_upload_session_start(f.read(self._max_size))
                        cursor = dropbox.files.UploadSessionCursor(session_id=upload_session_start_result.session_id,
                                               offset=f.tell())
                        commit = dropbox.files.CommitInfo(path=remote_path)

                        while f.tell() < total_upload_size:
                            if (total_upload_size - f.tell()) <= self._max_size:
                                self._dropbox.files_upload_session_finish(f.read(self._max_size), cursor, commit)
                            else:
                                self._dropbox.files_upload_session_append(f.read(self._max_size), cursor.session_id,  cursor.offset)
                    except dropbox.exceptions.ApiError as err:
                        print('*** API error', err)

        except os.error:
            e = sys.exc_info()[0]
            Logger.exception('Exception! %s', e)
        finally:
            self._uploading = False

    def download(self):
        try:
            self._downloading = True

            if not os.path.exists(self.local_path):
                os.makedirs(self.local_path)

            # list the folder contents
            try:
                res = self._dropbox.files_list_folder(self._remote_path)
                for entry in res.entries:
                    local_file = os.path.join(self.local_path, entry.name)
                    if not os.path.exists(local_file):
                        # download and overwrite it
                        md, dl_res = self._dropbox.files_download(entry.path_lower)
                        file = open(local_file, 'w')
                        file.write(dl_res.content)
                        file.close
            except dropbox.exceptions.ApiError as err:
                print('*** API error', err)
                return None
        except os.error:
            e = sys.exc_info()[0]
            Logger.exception('Exception! %s', e)
        finally:
            self._downloading = False


File_Processor(id="Yk7MLEza3NAAAAAAAAABGyzVVQi_3q7CkUoPjSjO6tWId31ogOM0KiBcdowZoB0b", remote_path="/input/D3RaspberryPi/", local_path='/home/pi/recon')