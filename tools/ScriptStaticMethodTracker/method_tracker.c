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

#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <stdbool.h>
#include <pcre.h>

#include "method_list.h"
#include "script_path.h"

JavaMethodList getBestSubstringsFromStringList(char *aStrRegex, JavaMethodList *lines, int lines_size) {
    JavaMethodList ret = createJavaMethodList();

    // ------------ an adaptation from Mitch Richling's https://www.mitchr.me/SS/exampleCode/AUPG/pcre_example.c.html -----------

    int subStrVec[30];
    int subStrVecLength = 30;
    const char *pcreErrorStr;
    int pcreErrorOffset;

    pcre *reCompiled = pcre_compile(aStrRegex, 0, &pcreErrorStr, &pcreErrorOffset, NULL);
    if(reCompiled == NULL) {
        printf("ERROR: Could not compile '%s': %s\n", aStrRegex, pcreErrorStr);
        return ret;
    }

    pcre_extra *pcreExtra = pcre_study(reCompiled, 0, &pcreErrorStr);
    if(pcreErrorStr != NULL) {
        printf("ERROR: Could not study '%s': %s\n", aStrRegex, pcreErrorStr);
        return ret;
    }

    int i;
    for (i = 0; i < lines_size; i++) {
        JavaMethodList list = lines[i];

        resetJavaMethodCursor(&list);
        while(true) {
            JavaMethod *method = readJavaMethod(&list);
            if (method == NULL) {
                break;
            }

            char *str = method->name;
            int st = 0, en = strlen(str);
            while (st < en) {
                int pcreExecRet = pcre_exec(reCompiled, pcreExtra, str, en, st, 0, subStrVec, subStrVecLength);
                if(pcreExecRet < 0) {
                    switch(pcreExecRet) {
                        //case PCRE_ERROR_NOMATCH      : printf("String did not match the pattern\n");        break;
                        case PCRE_ERROR_NULL         : printf("Something was null\n");                      break;
                        case PCRE_ERROR_BADOPTION    : printf("A bad option was passed\n");                 break;
                        case PCRE_ERROR_BADMAGIC     : printf("Magic number bad (compiled re corrupt?)\n"); break;
                        case PCRE_ERROR_UNKNOWN_NODE : printf("Something kooky in the compiled re\n");      break;
                        case PCRE_ERROR_NOMEMORY     : printf("Ran out of memory\n");                       break;
                        //default                      : printf("Unknown error\n");                           break;
                    }

                    break;  // no more matches found
                } else {
                    if(pcreExecRet == 0) {
                        printf("But too many substrings were found to fit in subStrVec!\n");
                        // Set rc to the max number of substring matches possible.
                        pcreExecRet = 30 / 3;
                    }

                    const char *psubStrMatchStr;
                    pcre_get_substring(str, subStrVec, pcreExecRet, 0, &(psubStrMatchStr));

                    insertJavaMethod(&ret, createJavaMethod(psubStrMatchStr));
                    pcre_free_substring(psubStrMatchStr);

                    st = subStrVec[1];
                }
            }
        }
    }

    pcre_free(reCompiled);

    if(pcreExtra != NULL) {
        pcre_free(pcreExtra);
    }

    return ret;
}

char* extractStaticMethodName(const char *method_line) {
    char *aStrRegex = "([A-Za-z0-9])+(\\s)*\\(";

    int lines_size = 1;
    JavaMethodList *lines = (JavaMethodList *)malloc(lines_size * sizeof(JavaMethodList));

    int i;
    for (i = 0; i < lines_size; i++) {
        lines[i] = createJavaMethodList();
        insertJavaMethod(&(lines[i]), createJavaMethod(method_line));
    }

    JavaMethodList subs = getBestSubstringsFromStringList(aStrRegex, lines, lines_size);

    char *ret;
    if (subs.size > 0) {
        resetJavaMethodCursor(&subs);
        JavaMethod *method = readJavaMethod(&subs);

        char *method_scoop = method->name;
        int i;
        for (i = 0; i < strlen(method_scoop) - 1; i++) {
            char ch = method_scoop[i];
            if (ch == '(' || ch == ' ' || ch == '\t') {
                break;
            }
        }
        method->name[i] = 0;

        ret = (char *)malloc((strlen(method->name) + 1) * sizeof(char));
        strcpy(ret, method->name);
    } else {
        ret = NULL;
    }

    freeJavaMethodList(&subs);

    for (i = 0; i < lines_size; i++) {
        freeJavaMethodList(&(lines[i]));
    }

    free(lines);

    return ret;
}

JavaMethodList getStaticJavaMethodNames(char *aStrRegex, JavaMethodList *lines, int lines_size) {
    JavaMethodList subs = getBestSubstringsFromStringList(aStrRegex, lines, lines_size);
    JavaMethodList ret = createJavaMethodList();

    resetJavaMethodCursor(&subs);
    while (true) {
        JavaMethod *method = readJavaMethod(&subs);
        if (method == NULL) {
            break;
        }

        char *method_name = extractStaticMethodName(method->name);
        if (method_name != NULL) {
            insertJavaMethod(&ret, createJavaMethod(method_name));
            free(method_name);
        }
    }

    freeJavaMethodList(&subs);

    return ret;
}

bool isIgnoreMethod(char *method_name) {
    const char * ignoreMethods[] = {"getInstance", "toString", NULL};

    int i = 0;
    while(true) {
        const char *ign = ignoreMethods[i];
        if (ign == NULL) {
            break;
        }

        if (!strcmp(method_name, ign)) {
            return true;
        }

        i++;
    }

    return false;
}

JavaMethodList trackerFindSourceStaticMethods(JavaMethodList *lines, int lines_size) {
    char *aStrRegex = "(public static\\s).*([A-Za-z0-9])+(\\s)*\\(.*\\{";
    JavaMethodList ret = createJavaMethodList();

    JavaMethodList list = getStaticJavaMethodNames(aStrRegex, lines, lines_size);
    resetJavaMethodCursor(&list);
    while(true) {
        JavaMethod *method = readJavaMethod(&list);
        if (method == NULL) {
            break;
        }

        if (isIgnoreMethod(method->name)) {
            continue;
        }

        insertJavaMethod(&ret, createJavaMethod(method->name));
    }

    freeJavaMethodList(&list);

    return ret;
}

char *getContentFromFile(FILE *f) {
    fseek(f, 0, SEEK_END);  // implemented by user529758 @ StackOverflow
    long fsize = ftell(f);
    fseek(f, 0, SEEK_SET);  /* same as rewind(f); */

    char *string = malloc(fsize + 1);
    fread(string, 1, fsize, f);

    string[fsize] = 0;
    return string;
}

bool locateMethodCall(const char *method_name, char *file_path) {
    FILE *f = fopen(file_path, "r+t");

    char aStrRegex[1000];
    strcpy(aStrRegex, method_name);
    strcat(aStrRegex, "(\\s)*\\(");

    JavaMethodList *file_content = (JavaMethodList *)malloc(sizeof(JavaMethodList));
    file_content[0] = createJavaMethodList();

    char *content = getContentFromFile(f);

    JavaMethod *method = createJavaMethod(content);
    insertJavaMethod(&(file_content[0]), method);
    free(content);

    JavaMethodList list = getBestSubstringsFromStringList(aStrRegex, file_content, 1);
    bool found = (list.size > 0);

    freeJavaMethodList(&(file_content[0]));
    free(file_content);
    fclose(f);

    return found;
}

void locateMethodCalls(const char *method_name, char **file_paths, int file_paths_size) {
    int i;
    for (i = 0; i < file_paths_size; i++) {
        char *path = file_paths[i];
        if (locateMethodCall(method_name, path)) {
            printf("  %s : \'%s\'\n", path, method_name);
        }
    }
}

int trackerLocateScriptsStaticCalls(JavaMethodList method_names) {
    ScriptFiles *files = createScriptFiles("../../scripts");
    if (files == NULL) {
        printf("ERROR: Could not initialize script files.\n");
        return -1;
    }

    resetJavaMethodCursor(&method_names);
    while (true) {
        JavaMethod *method = readJavaMethod(&method_names);
        if (method == NULL) {
            break;
        }

        locateMethodCalls(method->name, files->file_paths, files->file_paths_size);
    }

    freeScriptFiles(files);
    return 0;
}

typedef struct {
    JavaMethodList *file_content;
    int size;
} SourceFilesContent;

SourceFilesContent* readSourceFileContents() {
    ScriptFiles *srcFilePaths = createScriptFiles("../../src");

    SourceFilesContent *files = (SourceFilesContent *)malloc(sizeof(SourceFilesContent));
    files->file_content = (JavaMethodList *)malloc(srcFilePaths->file_paths_size * sizeof(JavaMethodList));
    files->size = srcFilePaths->file_paths_size;

    //int max_len = 0;
    int i;
    for (i = 0; i < srcFilePaths->file_paths_size; i++) {
        files->file_content[i] = createJavaMethodList();

        FILE *f = fopen(srcFilePaths->file_paths[i], "r+t");
        char *content = getContentFromFile(f);

        //int this_len = strlen(content);
        //if (max_len < this_len) max_len = this_len;

        fclose(f);

        insertJavaMethod(&(files->file_content[i]), createJavaMethod(content));
    }

    freeScriptFiles(srcFilePaths);

    return files;
}

void freeSourceFileContents(SourceFilesContent *files) {
    int i;
    for (i = 0; i < files->size; i++) {
        freeJavaMethodList(&(files->file_content[i]));
    }

    free(files->file_content);
    free(files);
}

int main() {
    printf("Loading source files...\n");
    SourceFilesContent *src_contents = readSourceFileContents();

    int lines_size = src_contents->size;
    JavaMethodList *lines = src_contents->file_content;

    printf("Tracking static methods on source...\n");
    JavaMethodList method_names = trackerFindSourceStaticMethods(lines, lines_size);
    printf("Finding static methods calls on scripts...\n");
    trackerLocateScriptsStaticCalls(method_names);
    printf("Track complete!\n");

    freeSourceFileContents(src_contents);
    freeJavaMethodList(&method_names);

    return 0;
}
