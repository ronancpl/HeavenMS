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

JavaMethod* createJavaMethod(const char *name) {
    JavaMethod* method = (JavaMethod *)malloc(sizeof(JavaMethod));
    method->name = (char *)malloc((strlen(name) + 1) * sizeof(char));
    strcpy(method->name, name);
    return method;
}

void freeJavaMethod(JavaMethod *method) {
    free(method->name);
    free(method);
}

JavaMethodList createJavaMethodList() {
    JavaMethodList list;
    list.size = 0;

    JavaMethodListItem *item = (JavaMethodListItem *)malloc(sizeof(JavaMethodListItem));
    item->prox = NULL;

    list.last = item;
    list.first = list.last;

    return list;
}

void insertJavaMethod(JavaMethodList *list, JavaMethod *method) {
    JavaMethodListItem *item = (JavaMethodListItem *)malloc(sizeof(JavaMethodListItem));
    item->prox = NULL;

    list->last->method = method;
    list->last->prox = item;

    list->last = item;
    list->size++;
}

void freeJavaMethodList(JavaMethodList *list) {
    JavaMethodListItem *aux = list->first;

    list->first = list->last;
    list->size = 0;

    while (aux->prox != NULL) {
        JavaMethodListItem *aux2 = aux;
        aux = aux->prox;

        freeJavaMethod(aux2->method);
        free(aux2);
    }
    free(aux);
}

void resetJavaMethodCursor(JavaMethodList *list) {
    list->cursor = list->first;
}

JavaMethod* readJavaMethod(JavaMethodList *list) {
    JavaMethodListItem *aux = list->cursor;
    if (aux->prox == NULL) {
        return NULL;
    }

    list->cursor = aux->prox;
    return aux->method;
}
