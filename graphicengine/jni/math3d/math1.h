/*************************************************************/
/*                           MATH.H                          */
/*                                                           */
/* Purpose: Header file to include in order to get full math */
/*          support for 2 and 3 element vectors, 3x3 and 4x4 */
/*          matrices and quaternions                         */
/*      Evan Pipho (May 27, 2002)                            */
/*                                                           */
/*************************************************************/
#ifndef MATH_H
#define MATH_H

//#pragma warning(push)
//#pragma warning(disable : 4244)
#include <math.h>
#include <stdlib.h>
#include <string.h>		// for memset, memcpy, etc

#define INF ((float)(1e10))
#define EPSILON ((float)(1e-6))
#define PI (acosf(-1.0f))
#define RAD2DEG(x) ((x) * (180 / PI))
#define DEG2RAD(x) ((x) * (PI / 180))
#define RANDOM(a, b) \
	( rand() * (1.0f / RAND_MAX) * ((b) - (a)) + (a) )
#define RANDOM_TENT(a, b) \
	( ( (rand() - rand()) * (0.5f / RAND_MAX) + 0.5f ) * ((b) - (a)) + (a) )
#define MAX(a, b) ((a) > (b) ? (a) : (b))
#define MIN(a, b) ((a) < (b) ? (a) : (b))
#define TOMAX(x, a) \
	do{ if((x) < (a)) (x) = (a); } while(0)
#define TOMIN(x, a) \
	do{ if((x) > (a)) (x) = (a); } while(0)
#define CLAMP(x, a, b) \
	do{ if((x) < (a)) (x) = (a); if((x) > (b)) (x) = (b); } while(0)

#define HYPOT(x, y) (sqrtf((x) * (x) + (y) * (y)))
#define HYPOT3(x, y, z) (sqrtf((x) * (x) + (y) * (y) + (z) * (z)))
#define SQUARE(x, y) ((x) * (x) + (y) * (y))

#define FEQUAL(x, y) (fabs((x) - (y)) < EPSILON)
#define FZERO(x) ((x) > -EPSILON && (x) < EPSILON)

inline float EaseInOut2(float t)
{
	t *= 2;
	return (t < 1 ? t * t : 2 - (2 - t) * (2 - t)) * 0.5f;
}

inline float Interpolate(float t, float begin, float end)
{
	return (end - begin) * t + begin;
}

#include "vector.h"
#include "matrix.h"
#include "quaternion.h"
#include "boundingObject.h"

//#pragma warning(pop)

#endif //MATH_H
