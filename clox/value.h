#ifndef VALUE_H
#define VALUE_H

#include <stdio.h>

#include "common.h"

// each chunk carries a "constant pool" containing all its literals
typedef double Value;
typedef struct {
    int capacity;
    int count;
    Value* values;
} ValueArray;

void initValueArray(ValueArray* array);
void writeValueArray(ValueArray* array, Value value);
void freeValueArray(ValueArray* array);
void printValue(Value value);

#endif //VALUE_H
