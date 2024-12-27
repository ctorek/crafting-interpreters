#ifndef CHUNK_H
#define CHUNK_H

#include "common.h"
#include "value.h"

// each bytecode instruction is a one-byte operation code
typedef enum {
    OP_CONSTANT,
    OP_RETURN
} OpCode;

// each "chunk" of bytecode contains a series of instructions
typedef struct {
    int capacity; // number of elements allocated
    int count; // number of elements in use
    uint8_t* code; // the opcode
    int* lines; // the line numbers corresponding to this chunk
    ValueArray constants; // constants associated with this chunk
} Chunk;

void initChunk(Chunk* chunk);
void freeChunk(Chunk* chunk);
void writeChunk(Chunk* chunk, uint8_t byte, int line);
int addConstant(Chunk* chunk, Value value);

#endif //CHUNK_H
