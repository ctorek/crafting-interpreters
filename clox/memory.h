#ifndef MEMORY_H
#define MEMORY_H

#include "common.h"

#define GROW_CAPACITY(capacity) \
    ((capacity) < 8 ? 8 : (capacity) * 2)

#define GROW_ARRAY(type, ptr, oldCount, newCount) \
    reallocate(ptr, sizeof(type) * (oldCount), sizeof(type) * (newCount))

#define FREE_ARRAY(type, ptr, oldCount) \
    reallocate(ptr, sizeof(type) * (oldCount), 0)

// passing all memory allocations through one function is important for implementing gc
void* reallocate(void* ptr, size_t oldSize, size_t newSize);

#endif //MEMORY_H
