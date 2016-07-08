#ifndef IO_H
#define IO_H

#include <string.h>

#define LOADASSERT(x)   if(!(x)) { LOADFAIL; }

#define LOADVARS unsigned char tempBuffer[64];\
	unsigned long tempULong;\
	unsigned short tempUShort;\
	union { unsigned long l; float f; } tempFloatUnion;


#define GETBYTES(x,dest) \
	if((x) > numBytes)\
	{\
		LOADFAIL;\
	}\
	else\
	{\
		memcpy(dest, cbuffer, (x));\
		cbuffer+=(x);\
		numBytes-=(x);\
	}

#define GETSTRING(x, dest) \
	GETBYTES(x, dest); \
	(dest)[x] = '\0';

#define GETLONG(dest) \
	GETBYTES(4, tempBuffer);\
	tempULong = tempBuffer[0] | (tempBuffer[1] << 8) | (tempBuffer[2] << 16) | (tempBuffer[3] << 24);\
	(dest) = tempULong;

#define GETSHORT(dest) \
	GETBYTES(2, tempBuffer);\
	tempUShort = tempBuffer[0] | (tempBuffer[1] << 8);\
	(dest) = tempUShort;

#define GETFLOAT(dest) \
	GETLONG(tempFloatUnion.l)\
	(dest) = tempFloatUnion.f;

#define GETBYTE(dest) \
	GETBYTES(1, tempBuffer);\
	(dest) = tempBuffer[0];

#endif /*IO_H*/
