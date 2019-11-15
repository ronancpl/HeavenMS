#include <stdio.h>
#include <stdlib.h>
#include <string.h>

//NOTE: should the HASH_MAXITEM or HASH_NUMBUCK value be too small, program will crash by SIG_SEGV
#define HASH_MAXITEM 4000
#define HASH_NUMBUCK 1340
#define HASH_HIVALUE 2147483647     //32-BIT integer

#define HASH_REHTHRE 3.5
#define HASH_REHRATE 5

typedef struct {
    int list[HASH_MAXITEM];
    int first;

    unsigned int count;
} HastSetIndex;

typedef struct {
    HastSetIndex **table;
    int *list;

    unsigned int threshold;
    unsigned int length;
    unsigned int count;
} HashSet;

void hashset_create_table(HashSet *hs) {
    hs->table = (HastSetIndex **)malloc(hs->length * sizeof(HastSetIndex *));
    hs->threshold = (unsigned int)(HASH_REHTHRE * hs->length);

    unsigned int i;
    for(i = 0; i < hs->length; i++) {
        hs->table[i] = (HastSetIndex *)malloc(sizeof(HastSetIndex));
        hs->table[i]->count = 0;
        hs->table[i]->first = HASH_HIVALUE;
    }
}

HashSet* hashset_create() {
    HashSet *hs = (HashSet *)malloc(sizeof(HashSet));
    hs->count = 0;
    hs->length = HASH_NUMBUCK;
    hs->list = NULL;

    hashset_create_table(hs);
    return(hs);
}

void hashset_destroy(HashSet *hs) {
    if(hs->list != NULL) {
        free(hs->list);
    }

    unsigned int i;
    for(i = 0; i < hs->length; i++)
        free(hs->table[i]);

    free(hs->table);
    free(hs);
}

unsigned int hashset_maptable(HashSet *hs, int item) {
    return(item % hs->length);
}

unsigned int hashset_slot(HashSet *hs, int item, unsigned int *bucket) {
    *bucket = hashset_maptable(hs, item);

    unsigned int i;
    for(i = 0; i < hs->table[*bucket]->count; i++) {
        if(hs->table[*bucket]->list[i] == item)
            return(i);
    }

    return(-1);
}

short hashset_contains(HashSet *hs, int item, unsigned int *bucket) {
    return(hashset_slot(hs, item, bucket) != -1);
}

short hashset_insertinto(HashSet *hs, int item) {
    unsigned int bucket;

    if(!hashset_contains(hs, item, &bucket)) {
        if(hs->table[bucket]->first > item)
            hs->table[bucket]->first = item;

        hs->table[bucket]->list[hs->table[bucket]->count] = item;

        (hs->count)++;
        (hs->table[bucket]->count)++;
        if(hs->table[bucket]->count > hs->threshold) return(1);
    }

    return(0);
}

void hashset_rehash(HashSet *hs) {
    int *temp = (int *)malloc(hs->count * sizeof(int));
    unsigned int temp_cursor = 0, i, j;

    for(i = 0; i < hs->length; i++) {
        for(j = 0; j < hs->table[i]->count; j++) {
            temp[temp_cursor] = hs->table[i]->list[j];
            temp_cursor++;
        }
    }

    for(i = 0; i < hs->length; i++)
        free(hs->table[i]);
    free(hs->table);

    hs->count = 0;
    hs->length *= HASH_REHRATE;
    hashset_create_table(hs);

    for(i = 0; i < temp_cursor; i++)
        hashset_insertinto(hs, temp[i]);

    free(temp);
}

void hashset_insert(HashSet *hs, int item) {
    if(hashset_insertinto(hs, item)) {
        hashset_rehash(hs);
    }
}

int hashset_recalc_first(HashSet *hs, int bucket) {
    int i, val = HASH_HIVALUE;
    for(i = 0; i < hs->table[bucket]->count; i++) {
        if(val > hs->table[bucket]->list[i])
            val = hs->table[bucket]->list[i];
    }

    return(val);
}

void hashset_remove(HashSet *hs, int item) {
    unsigned int bucket;
    unsigned int slot = hashset_slot(hs, item, &bucket);

    if(slot != -1) {
        (hs->count)--;
        (hs->table[bucket]->count)--;
        hs->table[bucket]->list[slot] = hs->table[bucket]->list[hs->table[bucket]->count];

        if(item == hs->table[bucket]->first)
            hs->table[bucket]->first = hashset_recalc_first(hs, bucket);
    }
}

short hashset_is_empty(HashSet *hs) {
    return(hs->count == 0);
}

void hashset_make_empty(HashSet *hs) {
    unsigned int i;
    for(i = 0; i < hs->length; i++) {
        hs->table[i]->first = HASH_HIVALUE;
        hs->table[i]->count = 0;
    }

    hs->count = 0;
}

int hashset_remove_first(HashSet *hs) {
    int i, take = HASH_HIVALUE;
    for(i = 0; i < hs->length; i++) {
        if(take > hs->table[i]->first)
            take = hs->table[i]->first;
    }

    hashset_remove(hs, take);
    return(take);
}

void hashset_merge(HashSet *hs1, HashSet *hs2) {
    //add values from hs2 to hs1

    unsigned int i, j;
    for(i = 0; i < hs2->length; i++) {
        for(j = 0; j < hs2->table[i]->count; j++) {
            hashset_insert(hs1, hs2->table[i]->list[j]);
        }
    }
}

void hashset_dump(HashSet *hs) {
    printf("HASHSET v1.0 -- count: %d, buckets: %d, threshold: %d\n", hs->count, hs->length, hs->threshold);

    unsigned int i, j;
    for(i = 0; i < hs->length; i++) {
        printf("\n%d -> ", i);
        for(j = 0; j < hs->table[i]->count; j++) {
            printf("%d ", hs->table[i]->list[j]);
        }
        printf("$");
    }
    printf("\n");
}

int* hashset_list(HashSet *hs) {
    int *list = hs->list;
    if(list != NULL) {
        free(list);
    }

    list = (int *)malloc(hs->count * sizeof(int));

    unsigned int i, j, k = 0;
    for(i = 0; i < hs->length; i++) {
        for(j = 0; j < hs->table[i]->count; j++) {
            list[k] = hs->table[i]->list[j];
            k++;
        }
    }

    return list;
}

/*
    HASHSET:

    HashSet* hashset_create();
    void hashset_destroy(HashSet *hs);
    short hashset_contains(HashSet *hs, int item, unsigned int *bucket);
    void hashset_insert(HashSet *hs, int item);
    void hashset_remove(HashSet *hs, int item);
    short hashset_is_empty(HashSet *hs);
    void hashset_make_empty(HashSet *hs);
    int hashset_remove_first(HashSet *hs);
    void hashset_merge(HashSet *hs1, HashSet *hs2);
    void hashset_dump(HashSet *hs);
*/
