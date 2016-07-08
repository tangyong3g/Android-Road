/*
 * unreal.h
 *
 *  Created on: 2011-10-14
 *      Author: dengweiming
 */

#ifndef UNREAL_H_
#define UNREAL_H_

typedef float axVector_t[3];
typedef float axQuat_t[4];

struct axChunkHeader_t
{
	char			id[21];
	unsigned long	type;
	unsigned long	size;
	unsigned long	nitems;
};

struct axAnimationInfo_t
{
	char				name[65];
	char				group[65];

	unsigned long		numBones;

	char				unknownA[16];

	float				duration;
	float				framerate;

	char				unknownB[4];

	unsigned long		firstFrame;
	unsigned long		numFrames;
};

struct axAnimationKey_t
{
	axVector_t			position;
	axQuat_t			orientation;
	float				time;
};

struct axBonePose_t
{
	axQuat_t			orientation;
	axVector_t			position;

	unsigned long		unknown;

	axVector_t			size;		// For collision?
};

struct axReferenceBone_t
{
	char				name[65];
	unsigned long		unusedA;
	unsigned long		unusedB;
	unsigned long		parent;			// Root has 0 as parent, not -1 like DML

	axBonePose_t		pose;
};

struct axiBonePose_t
{
	axQuat_t quat;
	axVector_t position;
};

struct axiSequence_t
{
	char			name[65];

	float			framerate;

	unsigned int	numFrames;

	axiBonePose_t	*poses;

	unsigned int	numBones;
};

struct axiAnimation_t
{
	unsigned int numBones;

	unsigned int numSequences;
	axiSequence_t *sequences;

};

void ax2_FreeAnimation(axiAnimation_t *anim);
axiAnimation_t* ax2_ReadPSA(const unsigned char *data, unsigned long numBytes);

#endif /* UNREAL_H_ */
