import json
import os
import zipfile
import urllib.parse
import urllib.request

def get_working_branch(default):
    env = os.getenv('cleanroomDownloadBranch')
    if not env:
        print(f'No branch was found. Use default branch: {default}')
        env = default
    else:
        print('Download branch: ' + env)
    return urllib.parse.quote(env, safe='')


# function to add to JSON
def write_json(filepath, new_data):
    with open(filepath, 'r+') as file:
        # First we load existing data into a dict.
        file_data = json.load(file)
        # Join new_data with file_data
        file_data.update(new_data)
        # Sets file's current position at offset.
        file.seek(0)
        # convert back to json.
        json.dump(file_data, file, indent=4)


# function to extract archive with relevant path and name pattern
def extractArchive(relevant_path: str, name_pattern: str, extract_path: str = None):
    file_name = findFileName(relevant_path, name_pattern)

    if not file_name:
        raise FileNotFoundError(f'No such file: {relevant_path}{os.sep}{name_pattern}')

    with MyZipFile(os.path.join(relevant_path, file_name)) as z:
        z.extractall(extract_path)


# function to find file via pattern
def findFileName(relevant_path, name_pattern):
    print(os.listdir(relevant_path))
    for fn in os.listdir(relevant_path):
        if fn.startswith(name_pattern):
            return fn


class MyZipFile(zipfile.ZipFile):

    def extract(self, member, path=None, pwd=None):
        if not isinstance(member, zipfile.ZipInfo):
            member = self.getinfo(member)

        if path is None:
            path = os.getcwd()

        ret_val = self._extract_member(member, path, pwd)
        attr = member.external_attr >> 16
        if attr != 0:
            os.chmod(ret_val, attr)
        return ret_val

    def extractall(self, path=None, members=None, pwd=None):
        if members is None:
            members = self.namelist()

        if path is None:
            path = os.getcwd()
        else:
            path = os.fspath(path)

        for zipinfo in members:
            self.extract(zipinfo, path, pwd)

def download_file(url: str, path: str) -> None:

    dir_path = os.path.dirname(path)
    if dir_path and not os.path.exists(dir_path):
        os.makedirs(dir_path)
    
    try:

        with urllib.request.urlopen(url) as response:
            if response.status != 200:
                raise ValueError(f"HTTP Error: {response.status}")
            
            with open(path, 'wb') as out_file:
                while True:
                    chunk = response.read(16384)  # 16KB
                    if not chunk:
                        break
                    out_file.write(chunk)
    
    except urllib.error.URLError as e:
        raise ValueError(f"URL Error: {e.reason}") from e
    except urllib.error.HTTPError as e:
        raise ValueError(f"HTTP Error: {e.code} {e.reason}") from e