#include <limits.h>

// string hash version by chqrlie - https://stackoverflow.com/questions/20462826/hash-function-for-strings-in-c
unsigned int strhash(const char *word) {
    unsigned int hash = 0, c;

    size_t i = 0;
    for (i = 0; word[i] != '\0'; i++) {
        c = (unsigned char)word[i];
        hash = (hash << 3) + (hash >> (sizeof(hash) * CHAR_BIT - 3)) + c;
    }
    return hash % UINT_MAX;
}

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

#include "strmap.h"
#include "hashset.c"

void performQuestDiff(ScriptedQuestList *quests_then, ScriptedQuestList *quests_curr) {
    char buf[100], bufhash[100];
    HashSet *script_quests = hashset_create();

    // bookkeep quest-script hash
    StrMap *sm = sm_new(2000);

    // insert ongoing scripts
    resetScriptedQuestCursor(quests_curr);
    while(true) {
        ScriptedQuest *method = readScriptedQuest(quests_curr);
        if (method == NULL) {
            break;
        }

        int hash_quest = strhash(method->name);
        sprintf(bufhash, "%d", hash_quest);

        sm_put(sm, bufhash, method->name);
        hashset_insert(script_quests, hash_quest);
    }

    // remove initial scripts
    resetScriptedQuestCursor(quests_then);
    while(true) {
        ScriptedQuest *method = readScriptedQuest(quests_then);
        if (method == NULL) {
            break;
        }

        int hash_quest = strhash(method->name);
        hashset_remove(script_quests, hash_quest);
    }

    int *list = hashset_list(script_quests);
    int i;
    for (i = 0; i < script_quests->count; i++) {
        int hash_quest = list[i];
        sprintf(bufhash, "%d", hash_quest);

        // dump ongoing script releases
        sm_get(sm, bufhash, buf, sizeof(buf));
        printf("%s\n", buf);
    }

    sm_delete(sm);
    hashset_destroy(script_quests);
}
