
import dropbox
from kivy.logger import Logger
from dropbox.files import WriteMode
import datetime
import os
import six
import sys
import time
import unicodedata

class File_Processor():
    def __init__(self, **kwargs):
        self._uploading = False
        self._downloading = False
        self._dropbox = dropbox.Dropbox(kwargs['id'])
        self.remote_paths = kwargs['remote_paths']
        self._max_size = 1024*150
        self.local_paths = kwargs['local_paths']

    def list_folder(self, dbx, folder):
        """List a folder.

        Return a dict mapping unicode filenames to
        FileMetadata|FolderMetadata entries.
        """
        path = '/%s' % (folder)
        while '//' in path:
            path = path.replace('//', '/')
        path = path.rstrip('/')
        try:
            res = dbx.files_list_folder(path)
        except dropbox.exceptions.ApiError as err:
            print('Folder listing failed for', path, '-- assumed empty:', err)
            return {}
        else:
            rv = {}
            for entry in res.entries:
                rv[entry.name] = entry
            return rv

    def download(self, dbx, folder, name):
        """Download a file.

        Return the bytes of the file, or None if it doesn't exist.
        """
        self._downloading = True
        path = '/%s/%s/%s' % (folder, name)
        while '//' in path:
            path = path.replace('//', '/')
        try:
            md, res = dbx.files_download(path)
        except dropbox.exceptions.HttpError as err:
            print('*** HTTP error', err)
            return None
        data = res.content
        print(len(data), 'bytes; md:', md)
        self._downloading = False
        return data

    def upload(self, dbx, fullname, folder, name, overwrite=False):
        """Upload a file.

        Return the request response, or None in case of error.
        """
        self._uploading = True
        path = '/%s/%s' % (folder, name)
        while '//' in path:
            path = path.replace('//', '/')
        mode = (dropbox.files.WriteMode.overwrite
                if overwrite
                else dropbox.files.WriteMode.add)
        mtime = os.path.getmtime(fullname)
        with open(fullname, 'rb') as f:
            data = f.read()
        try:
            res = dbx.files_upload( data, path, mode, client_modified=datetime.datetime(*time.gmtime(mtime)[:6]), mute=True)
        except dropbox.exceptions.ApiError as err:
            print('*** API error', err)
            return None
        print('uploaded as', res.name.encode('utf8'))
        self._uploading = False
        return res

    def sync(self):
        """
        Sync
        local and remote
        """
        for dn, dirs, files in os.walk(self.local_paths['samples']):
            listing = self.list_folder(self._dropbox, self.remote_paths['samples'])
            print(files)
            for name in files:
                print(name)
                fullname = os.path.join(dn, name)
                if not isinstance(name, six.text_type):
                    name = name.decode('utf-8')
                nname = unicodedata.normalize('NFC', name)
                if nname in listing:
                    md = listing[nname]
                    mtime = os.path.getmtime(fullname)
                    mtime_dt = datetime.datetime(*time.gmtime(mtime)[:6])
                    size = os.path.getsize(fullname)
                    if (isinstance(md, dropbox.files.FileMetadata) and
                            mtime_dt == md.client_modified and size == md.size):
                        print(name, 'is already synced [stats match]')
                    else:
                        print(name, 'exists with different stats, downloading')
                        res = self.download(self._dropbox, self.remote_paths['samples'], name)
                        with open(fullname) as f:
                            data = f.read()
                        if res == data:
                            print(name, 'is already synced [content match]')
                        else:
                            print(name, 'has changed since last sync')
                            self.upload(self._dropbox, fullname, self.remote_paths['samples'], name,
                                       overwrite=True)
                self.upload(self._dropbox, fullname, self.remote_paths['samples'], name)