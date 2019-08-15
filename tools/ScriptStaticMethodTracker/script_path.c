/*
    This file is part of the HeavenMS MapleStory Server
    Copyleft (L) 2016 - 2019 RonanLana

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation version 3 as published by
    the Free Software Foundation. You may not use, modify or distribute
    this program under any other version of the GNU Affero General Public
    License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

#include <stdio.h>
#include <dirent.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <unistd.h>

void fetchScriptFilesFromDirectory(ScriptFiles *files, char *root_dir) {
    struct dirent *de;
    struct stat info;
    DIR *dir = opendir(root_dir);
    if (dir != NULL) {
        de = readdir(dir);  // skip .
        de = readdir(dir);  // skip ..

        while ((de = readdir(dir)) != NULL) {
            char file_path[SCRIPT_FILES_MAX_PATH_SIZE];
            strcpy(file_path, root_dir);
            strcat(file_path, "/");
            strcat(file_path, de->d_name);

            stat(file_path, &info);
            if (S_ISREG(info.st_mode)) {
                files->file_paths[files->file_paths_size] = (char *)malloc(SCRIPT_FILES_MAX_PATH_SIZE * sizeof(char));
                strcpy(files->file_paths[files->file_paths_size], file_path);
                files->file_paths_size++;
            } else {
                fetchScriptFilesFromDirectory(files, file_path);
            }
        }
    }
}

ScriptFiles* createScriptFiles(char *root_dir) {
    ScriptFiles *files = (ScriptFiles *)malloc(sizeof(ScriptFiles));

    files->file_paths = (char **)malloc(SCRIPT_FILES_MAX_COUNT * sizeof(char *));
    files->file_paths_size = 0;

    fetchScriptFilesFromDirectory(files, root_dir);

    return files;
}

void freeScriptFiles(ScriptFiles *files) {
    int i;
    for (i = 0; i < files->file_paths_size; i++) {
        free(files->file_paths[i]);
    }

    free(files->file_paths);
    free(files);
}
