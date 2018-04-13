import os
import dropbox
from kivy.logger import Logger
import sys
from dropbox.files import WriteMode


class FileProcessor():
    """
    Class used to handle all file processing including syncing of local and remote directories, uploading, and downloading.
    """
    def __init__(self, **kwargs):
        self._uploading = False
        self._downloading = False
        self._dbx = dropbox.Dropbox(kwargs['id'])
        self._remote_path = kwargs['remote_path']
        self._max_size = 1024*150
        self.local_path = kwargs['local_path']
        self._flag = "{flag}"

    def upload(self, file):
        """
        :param file: file to be uploaded to the remote directory.
        :return: void
        """
        try:
            total_upload_size = os.path.getsize(file)
            remote_path = self.local_path + file

            with open(file, 'rb') as f:
                if total_upload_size <= self._max_size:
                    self._dbx.files_upload(f.read(), path=remote_path, mode=WriteMode('overwrite'), mute=True)
                else:
                    try:
                        upload_session_start_result = self._dbx.files_upload_session_start(f.read(self._max_size))
                        cursor = dropbox.files.UploadSessionCursor(session_id=upload_session_start_result.session_id,
                                               offset=f.tell())
                        commit = dropbox.files.CommitInfo(path=remote_path)

                        while f.tell() < total_upload_size:
                            if (total_upload_size - f.tell()) <= self._max_size:
                                self._dbx.files_upload_session_finish(f.read(self._max_size), cursor, commit)
                            else:
                                self._dbx.files_upload_session_append_v2(f.read(self._max_size), cursor.session_id,  cursor.offset)
                    except dropbox.exceptions.ApiError as err:
                        self.flag_file(file)
                        print('*** API error', err)

        except os.error:
            e = sys.exc_info()[0]
            Logger.exception('Exception! %s', e)
        finally:
            self._uploading = False

    def download(self):
        """
        Downloads a file from the remote directory into the local directory
        :return:
        """
        try:
            self._downloading = True

            if not os.path.exists(self.local_path):
                os.makedirs(self.local_path)

            # list the folder contents
            try:
                res = self._dbx.files_list_folder(self._remote_path)
                for entry in res.entries:
                    local_file = os.path.join(self.local_path, entry.name)
                    if not os.path.exists(local_file):
                        # download and overwrite it
                        md, dl_res = self._dbx.files_download(entry.path_lower)
                        file = open(local_file, 'w')
                        file.write(dl_res.content)
                        file.close()
            except dropbox.exceptions.ApiError as err:
                print('*** API error', err)
                return None
        except os.error:
            e = sys.exc_info()[0]
            Logger.exception('Exception! %s', e)
        finally:
            self._downloading = False

    def sync(self):
        """
        This function should find all the flagged files in the local directory, upload them to the remote directory, and deflag them
        :return: void
        """

        # look for flagged files
        try:
            for root, dirs, files in os.walk(self.local_path):
                for file in files:
                    if file.contains('{flag}'):
                        self.upload(file)
                        self.deflag(file)
        except os.error:
            e = sys.exec_info()[0]
            Logger.exception('Exception! %s', e)

    def flag(self, file):
        """

        :param file: file to be flagged locally, should only be flagged if there was an issue during an upload involving this file
        :return: void
        """
        current_path_name = self._local_path+file
        flagged_name = current_path_name[:-4]+self._flag+".txt"
        os.rename(current_path_name, flagged_name)

    def deflag(self, file):
        """
        :param file: file to be deflagged locally, should only be deflagged if a syncing operation successfully uploaded the file remotely
        :return: void
        """
        current_path_name = self._local_path + file
        deflagged_name = current_path_name[:-10] + ".txt"
        os.rename(current_path_name, deflagged_name)


FileProcessor(id="Yk7MLEza3NAAAAAAAAABGyzVVQi_3q7CkUoPjSjO6tWId31ogOM0KiBcdowZoB0b", remote_path="/input/D3RaspberryPi/", local_path='/home/pi/recon')