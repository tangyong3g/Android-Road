/*
 * unreal.cpp
 *
 *  Created on: 2011-10-14
 *      Author: dengweiming
 */

#include "unreal.h"
#include "math3d/math1.h"

void ax2_FreeAnimation(axiAnimation_t *anim)
{
	if(!anim) return;

	if(anim->sequences){
		for(int i = 0; i < anim->numSequences; ++i)
			if(anim->sequences[i].poses) delete [] anim->sequences[i].poses;

		delete [] anim->sequences;
	}
	delete anim;
}

#define LOADFAIL ax2_FreeAnimation(anim); return 0

#include "util/io.h"

#define GET_axVector_t(dest) \
	GETFLOAT((dest)[0]); \
	GETFLOAT((dest)[1]); \
	GETFLOAT((dest)[2]);

#define GET_axQuat_t(dest) \
	GETFLOAT((dest)[0]); \
	GETFLOAT((dest)[1]); \
	GETFLOAT((dest)[2]); \
	GETFLOAT((dest)[3]);

#define GET_axChunkHeader_t(dest) \
	GETSTRING(20, (dest).id); \
	GETLONG((dest).type); \
	GETLONG((dest).size); \
	GETLONG((dest).nitems);

#define GET_axBonePose_t(dest) \
	GET_axQuat_t((dest).orientation) \
	GET_axVector_t((dest).position) \
	GETLONG((dest).unknown); \
	GET_axVector_t((dest).size)

#define GET_axReferenceBone_t(dest) \
	GETSTRING(64, (dest).name); \
	GETLONG((dest).unusedA); \
	GETLONG((dest).unusedB); \
	GETLONG((dest).parent); \
	GET_axBonePose_t((dest).pose)

#define GET_axAnimationInfo_t(dest) \
	GETSTRING(64, (dest).name); \
	GETSTRING(64, (dest).group); \
	GETLONG((dest).numBones); \
	GETBYTES(16, (dest).unknownA); \
	GETFLOAT((dest).duration); \
	GETFLOAT((dest).framerate); \
	GETBYTES(4, (dest).unknownB); \
	GETLONG((dest).firstFrame); \
	GETLONG((dest).numFrames);

#define GET_axAnimationKey_t(dest) \
	GET_axVector_t((dest).position); \
	GET_axQuat_t((dest).orientation); \
	GETFLOAT((dest).time);

axiAnimation_t *ax2_ReadPSA(const unsigned char *data, unsigned long numBytes)
{
	LOADVARS

	axiAnimation_t *anim;
	const unsigned char *cbuffer;

	axChunkHeader_t chunkHeader;

	axReferenceBone_t dummyBone;

	unsigned long i;
	unsigned long j, numKeys;

	axAnimationKey_t tempKey;
	axAnimationInfo_t tempInfo;

	cbuffer = data;


	anim = new axiAnimation_t;
	LOADASSERT(anim);

	memset(anim, 0, sizeof(axiAnimation_t));

	// Read the header
	GET_axChunkHeader_t(chunkHeader);
	LOADASSERT(!memcmp(chunkHeader.id, "ANIMHEAD", 8));

	// Load points
	GET_axChunkHeader_t(chunkHeader);
	LOADASSERT(!memcmp(chunkHeader.id, "BONENAMES", 9));
	LOADASSERT(chunkHeader.size == 120);
	anim->numBones = chunkHeader.nitems;

	// Ignore the bones - They're going to be stored by the PSA
	for(i=0;i<chunkHeader.nitems;i++)
	{
		GET_axReferenceBone_t(dummyBone);
	}

	GET_axChunkHeader_t(chunkHeader);
	LOADASSERT(!memcmp(chunkHeader.id, "ANIMINFO", 8));
	LOADASSERT(chunkHeader.size == 168);

	anim->numSequences = chunkHeader.nitems;
	LOADASSERT(anim->numSequences);		// There had better be sequences! :)
	anim->sequences = new axiSequence_t[anim->numSequences];
	LOADASSERT(anim->sequences);

	memset(anim->sequences, 0, sizeof(axiSequence_t) * anim->numSequences);

	for(i=0;i<chunkHeader.nitems;i++)
	{
		GET_axAnimationInfo_t(tempInfo);

		LOADASSERT(tempInfo.framerate);
		LOADASSERT(tempInfo.numBones == anim->numBones);
		LOADASSERT(tempInfo.numFrames);

		// Convert it to an internal animation
		anim->sequences[i].framerate = tempInfo.framerate;
		anim->sequences[i].numFrames = tempInfo.numFrames;
		anim->sequences[i].numBones = anim->numBones;
		strcpy(anim->sequences[i].name, tempInfo.name);

		anim->sequences[i].poses = new axiBonePose_t[tempInfo.numBones * tempInfo.numFrames];
		LOADASSERT(anim->sequences[i].poses);
	}

	GET_axChunkHeader_t(chunkHeader);
	LOADASSERT(!memcmp(chunkHeader.id, "ANIMKEYS", 8));
	LOADASSERT(chunkHeader.size == 32);

	CQuaternion remapCoodQuat(sinf(-PI / 4), 0, 0, cosf(PI / 4));	// rotate -90 degrees about X-axis

	// Load it up...
	for(i=0;i<anim->numSequences;i++)
	{
		numKeys = anim->numBones * anim->sequences[i].numFrames;

		for(j=0;j<numKeys;j++)
		{
			GET_axAnimationKey_t(tempKey);


			memcpy(anim->sequences[i].poses[j].quat, tempKey.orientation, sizeof(axQuat_t));
			memcpy(anim->sequences[i].poses[j].position, tempKey.position, sizeof(axVector_t));

			if(j % anim->numBones)
			{
				// 将旋转方向反向
				anim->sequences[i].poses[j].quat[3] = -anim->sequences[i].poses[j].quat[3];
			}
			else
			{
				/*
				 * need to preMultiple this matrix to the root bone's animation key,
				 * to rotate -90 degrees about X-axis
					1  0 0 0
					0  0 1 0
					0 -1 0 0
					0  0 0 1
				*/
				CQuaternion q(anim->sequences[i].poses[j].quat);
				q = remapCoodQuat * q;
				memcpy(anim->sequences[i].poses[j].quat, q.Get(), sizeof(axQuat_t));

				float y = anim->sequences[i].poses[j].position[1];
				anim->sequences[i].poses[j].position[1] = anim->sequences[i].poses[j].position[2];
				anim->sequences[i].poses[j].position[2] = -y;
			}
		}
	}

	return anim;
}
