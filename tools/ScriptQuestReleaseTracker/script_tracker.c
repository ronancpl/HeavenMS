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

#include "strmap.c"
#include "quest_list.h"
#include "quest_diff.h"

ScriptedQuestList getBestSubstringsFromStringList(char *aStrRegex, ScriptedQuestList *lines, int lines_size) {
    ScriptedQuestList ret = createScriptedQuestList();

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
        ScriptedQuestList list = lines[i];

        resetScriptedQuestCursor(&list);
        while(true) {
            ScriptedQuest *method = readScriptedQuest(&list);
            if (method == NULL) {
                break;
            }

            char *str = method->name;
            int st = 0, en = strlen(str);
            while(st < en) {
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

                    break;
                } else {
                    if(pcreExecRet == 0) {
                        printf("But too many substrings were found to fit in subStrVec!\n");
                        // Set rc to the max number of substring matches possible.
                        pcreExecRet = 30 / 3;
                    }

                    const char *psubStrMatchStr;
                    pcre_get_substring(str, subStrVec, pcreExecRet, 1, &(psubStrMatchStr));

                    insertScriptedQuest(&ret, createScriptedQuest(psubStrMatchStr));
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

char *getContentFromFile(FILE *f) {
    fseek(f, 0, SEEK_END);  // implemented by user529758 @ StackOverflow
    long fsize = ftell(f);
    fseek(f, 0, SEEK_SET);  /* same as rewind(f); */

    char *string = malloc(fsize + 1);
    fread(string, 1, fsize, f);

    string[fsize] = 0;
    return string;
}

ScriptedQuestList readQuestXml(char *file_path) {
    ScriptedQuestList *file_content = (ScriptedQuestList *)malloc(sizeof(ScriptedQuestList));
    file_content[0] = createScriptedQuestList();

    FILE *f = fopen(file_path, "r+t");
    char *content = getContentFromFile(f);

    char *tok = strtok(content, "\n");
    int i = 0;
    while (tok != NULL) {
        insertScriptedQuest(&(file_content[0]), createScriptedQuest(tok));
        tok = strtok(NULL, "\n");
        i++;
    }

    free(content);
    fclose(f);

    ScriptedQuestList ret = getBestSubstringsFromStringList("script\" value=\"(.+)\"", file_content, 1);

    freeScriptedQuestList(&file_content[0]);
    free(file_content);

    return ret;
}

void trackScriptQuestReleases() {
    ScriptedQuestList quests_then = readQuestXml("Check2.img.xml");
    ScriptedQuestList quests_curr = readQuestXml("Check.img.xml");

    performQuestDiff(&quests_then, &quests_curr);

    freeScriptedQuestList(&quests_curr);
    freeScriptedQuestList(&quests_then);
}

int main() {
    trackScriptQuestReleases();
    return 0;
}
