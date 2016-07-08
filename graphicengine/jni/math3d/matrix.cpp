/*************************************************************/
/*                         MATRIX.INL                        */
/*                                                           */
/* Purpose: Inlined functions for CMatrix3X3 and CMatrix4X4  */
/*      Evan Pipho (May 30, 2002)                            */
/*                                                           */
/*************************************************************/

#ifndef MATRIX_INL
#define MATRIX_INL

//#include <string.h>		// for memset, memcpy, etc
//#include "vector.h"
//#include "matrix.h"
//#include "quaternion.h"


#include "math1.h"

#define SQU(x) x * x

//-------------------------------------------------------------
//                       CMatrix3X3                           -
//-------------------------------------------------------------
//-------------------------------------------------------------
//-                      FUNCTIONS                            -
//-------------------------------------------------------------
//-------------------------------------------------------------
//- Zero
//- Set the matrix to a zero matrix
//-------------------------------------------------------------
inline void CMatrix3X3::Zero()
{
	memset(m_fMat, 0, sizeof(float[9]));
}

//-------------------------------------------------------------
//- Identity
//- Set the matrix to an identity matrix
//-------------------------------------------------------------
inline void CMatrix3X3::Identity()
{
	Zero();
	m_fMat[0] = 1.0f;
	m_fMat[4] = 1.0f;
	m_fMat[8] = 1.0f;
}

//-------------------------------------------------------------
//- Transpose
//- Transpose the matrix (swap rows and columns)
//-------------------------------------------------------------
inline void CMatrix3X3::Transpose()
{
	CMatrix3X3 tmp(m_fMat[0], m_fMat[3], m_fMat[6],
					m_fMat[1], m_fMat[4], m_fMat[7],
					m_fMat[2], m_fMat[5], m_fMat[8]);
	*this = tmp;
}

//-------------------------------------------------------------
//- Invert
//- Invert the stored matrix
//-------------------------------------------------------------
inline void CMatrix3X3::Invert()
{
	float fDet = Determinant();
	float fNewMat[9];
	//If determinant is 0, make it 1 to prevent divide by 0
	if(fDet == 0)
		fDet = 1;
	
	//Calculate the inverse
	float fInvDet = 1/fDet;
	
	fNewMat[0] =  (m_fMat[4] * m_fMat[8] - m_fMat[5] * m_fMat[8]) * fInvDet;
	fNewMat[1] = -(m_fMat[1] * m_fMat[8] - m_fMat[2] * m_fMat[8]) * fInvDet;
	fNewMat[2] =  (m_fMat[1] * m_fMat[5] - m_fMat[2] * m_fMat[4]) * fInvDet;

	fNewMat[3] = -(m_fMat[3] * m_fMat[8] - m_fMat[5] * m_fMat[6]) * fInvDet;
	fNewMat[4] =  (m_fMat[0] * m_fMat[8] - m_fMat[2] * m_fMat[6]) * fInvDet;
	fNewMat[5] = -(m_fMat[0] * m_fMat[5] - m_fMat[2] * m_fMat[3]) * fInvDet;

	fNewMat[6] =  (m_fMat[3] * m_fMat[7] - m_fMat[4] * m_fMat[6]) * fInvDet;
	fNewMat[7] = -(m_fMat[0] * m_fMat[7] - m_fMat[1] * m_fMat[6]) * fInvDet;
	fNewMat[8] =  (m_fMat[0] * m_fMat[4] - m_fMat[1] * m_fMat[3]) * fInvDet;
	
	memcpy(m_fMat, fNewMat, sizeof(float[16]));
}

//-------------------------------------------------------------
//- SetRotation
//- Build a rotation matrix
//-------------------------------------------------------------
inline void CMatrix3X3::SetRotation(float fX, float fY, float fZ)
{
	double cx = cos(fX);
	double sx = sin(fX);
	double cy = cos(fY);
	double sy = sin(fY);
	double cz = cos(fZ);
	double sz = sin(fZ);

	m_fMat[0] = (float)(cy * cz);
	m_fMat[1] = (float)(cy * sz);
	m_fMat[2] = (float)(-sy);

	m_fMat[3] = (float)(sx * sy * cz - cx * sz);
	m_fMat[4] = (float)(sx * sy * sz + cx * cz);
	m_fMat[5] = (float)(sx * cy);

	m_fMat[6] = (float)(cx * sy * cz + sx * sz);
	m_fMat[7] = (float)(cx * sy * sz - sx * cz);
	m_fMat[8] = (float)(cx * cy);
}

inline void CMatrix3X3::SetRotation(float * fpAngles)
{
	SetRotation(fpAngles[0], fpAngles[1], fpAngles[2]);
}

//-------------------------------------------------------------
//- SetInvRotation
//- Build an inverse rotation matrix
//-------------------------------------------------------------
inline void CMatrix3X3::SetInvRotation(float fX, float fY, float fZ)
{
	double cx = cos(fX);
	double sx = sin(fX);
	double cy = cos(fY);
	double sy = sin(fY);
	double cz = cos(fZ);
	double sz = sin(fZ);

	m_fMat[0] = (float)(cy * cz);
	m_fMat[3] = (float)(cy * sz);
	m_fMat[6] = (float)(-sy );

	m_fMat[1] = (float)(sx * sy * cz - cx * sz);
	m_fMat[4] = (float)(sx * sy * sz + cx * cz);
	m_fMat[7] = (float)(sx * cy);

	m_fMat[2] = (float)(cx * sy * cz + sx * sz);
	m_fMat[5] = (float)(cx * sy * sz - sx * cz);
	m_fMat[8] = (float)(cx * cz);
}

inline void CMatrix3X3::SetInvRotation(float * fpAngles)
{
	SetInvRotation(fpAngles[0], fpAngles[1], fpAngles[2]);
}

inline void CMatrix3X3::SetScale2D(float scaleX, float scaleY)
{
	m_fMat[0] = scaleX;
	m_fMat[4] = scaleY;
	m_fMat[8] = 1;
}

inline void CMatrix3X3::SetScale2D(float scaleX, float scaleY, float pivotX, float pivotY)
{
	m_fMat[0] = scaleX;
	m_fMat[4] = scaleY;
	m_fMat[8] = 1;

	m_fMat[6] = scaleX*pivotX - pivotX;
	m_fMat[7] = scaleY*pivotY - pivotY;
}

inline void CMatrix3X3::SetTranslation2D(float translateX, float translateY)
{
	m_fMat[0] = 1;
	m_fMat[4] = 1;
	m_fMat[8] = 1;

	m_fMat[6] = translateX;
	m_fMat[7] = translateY;
}

inline void CMatrix3X3::SetRotation2D(float angleZ)
{
	float cz = cos(angleZ);
	float sz = sin(angleZ);

	m_fMat[0] = cz;
	m_fMat[3] = -sz;

	m_fMat[1] = sz;
	m_fMat[4] = cz;

	m_fMat[8] = 1;
}

inline void CMatrix3X3::SetRotation2D(float angleZ, float pivotX, float pivotY)
{
	float cz = cos(angleZ);
	float sz = sin(angleZ);

	m_fMat[0] = cz;
	m_fMat[3] = -sz;

	m_fMat[1] = sz;
	m_fMat[4] = cz;

	m_fMat[6] = cz * -pivotX + sz * pivotY + pivotX;
	m_fMat[7] = sz * -pivotX - cz * pivotY + pivotY;
	m_fMat[8] = 1;
}

inline void CMatrix3X3::FromAxisAngle(CVector3& vec, float fAngle)
{
	float fCos = cosf(fAngle);
	float fSin = sinf(fAngle);
	float fOMC = 1.0f - fCos;

	m_fMat[0] = fCos + SQU(vec[0]) * fOMC;
	m_fMat[4] = fCos + SQU(vec[1]) * fOMC;
	m_fMat[8] = fCos + SQU(vec[2]) * fOMC;

	m_fMat[1] = vec[0] * vec[1] * fOMC + vec[2] * fSin;
	m_fMat[3] = vec[0] * vec[1] * fOMC - vec[2] * fSin;
	m_fMat[2] = vec[0] * vec[2] * fOMC + vec[1] * fSin;
	m_fMat[6] = vec[0] * vec[2] * fOMC - vec[1] * fSin;
	m_fMat[5] = vec[0] * vec[2] * fOMC + vec[0] * fSin;
	m_fMat[7] = vec[0] * vec[2] * fOMC - vec[0] * fSin;

}

//-------------------------------------------------------------
//- FromQuaternion
//- Build a rotation matrix from a quaternion
//-------------------------------------------------------------
inline void CMatrix3X3::FromQuaternion(const CQuaternion& rQuat)
{
	m_fMat[0] = SQU(rQuat.m_fQuat[3]) + SQU(rQuat.m_fQuat[0]) - SQU(rQuat.m_fQuat[1]) - SQU(rQuat.m_fQuat[2]);
	m_fMat[1] = 2 * (rQuat.m_fQuat[0] * rQuat.m_fQuat[1] - rQuat.m_fQuat[2] * rQuat.m_fQuat[3]);
	m_fMat[2] = 2 * (rQuat.m_fQuat[0] * rQuat.m_fQuat[2] + rQuat.m_fQuat[1] * rQuat.m_fQuat[3]);
	m_fMat[3] = 2 * (rQuat.m_fQuat[0] * rQuat.m_fQuat[1] + rQuat.m_fQuat[2] * rQuat.m_fQuat[3]);
	m_fMat[4] = SQU(rQuat.m_fQuat[3]) - SQU(rQuat.m_fQuat[0]) + SQU(rQuat.m_fQuat[1]) - SQU(rQuat.m_fQuat[2]);
	m_fMat[5] = 2 * (rQuat.m_fQuat[1] * rQuat.m_fQuat[2] - rQuat.m_fQuat[0] * rQuat.m_fQuat[3]);
	m_fMat[6] = 2 * (rQuat.m_fQuat[2] * rQuat.m_fQuat[0] - rQuat.m_fQuat[1] * rQuat.m_fQuat[3]);
	m_fMat[7] = 2 * (rQuat.m_fQuat[1] * rQuat.m_fQuat[2] + rQuat.m_fQuat[0] * rQuat.m_fQuat[3]);
	m_fMat[8] = SQU(rQuat.m_fQuat[3]) - SQU(rQuat.m_fQuat[0]) - SQU(rQuat.m_fQuat[1]) + SQU(rQuat.m_fQuat[2]);
}

//-------------------------------------------------------------
//- Determinant
//- Calculate the determinant of the matrix
//-------------------------------------------------------------
inline float CMatrix3X3::Determinant()
{
	return (m_fMat[0] * (m_fMat[4] * m_fMat[8] - m_fMat[7] * m_fMat[5])) -
		   (m_fMat[1] * (m_fMat[3] * m_fMat[8] - m_fMat[6] * m_fMat[5])) + 
		   (m_fMat[2] * (m_fMat[3] * m_fMat[7] - m_fMat[6] * m_fMat[4]));
}

//-------------------------------------------------------------
//- InvRotateVec
//- Rotate avector using the inverse of the matrix
//-------------------------------------------------------------
inline void CMatrix3X3::InverseRotateVec(float * fpVec)
{
	float tmp[3];

	tmp[0] = fpVec[0] * m_fMat[0] + fpVec[1] * m_fMat[1] + fpVec[2] * m_fMat[2];
	tmp[1] = fpVec[0] * m_fMat[3] + fpVec[1] * m_fMat[4] + fpVec[2] * m_fMat[5];
	tmp[2] = fpVec[0] * m_fMat[6] + fpVec[1] * m_fMat[7] + fpVec[2] * m_fMat[8];

	memcpy(fpVec, tmp, sizeof(float[3]));
}

//-------------------------------------------------------------
//-                      OPERATORS                            -
//-------------------------------------------------------------
//-------------------------------------------------------------
//- operator[]
//- Get a reference to a single element
//-------------------------------------------------------------
inline float& CMatrix3X3::operator [](const int iIdx)
{
	return m_fMat[iIdx];
}

//-------------------------------------------------------------
//- opertator +
//- Add a matrix to the stored matrix, return the result
//-------------------------------------------------------------
inline const CMatrix3X3 CMatrix3X3::operator +(const CMatrix3X3& rMat) const
{
	return CMatrix3X3(m_fMat[0] + rMat.m_fMat[0], m_fMat[1] + rMat.m_fMat[1], m_fMat[2] + rMat.m_fMat[2], 
					m_fMat[3] + rMat.m_fMat[3], m_fMat[4] + rMat.m_fMat[4], m_fMat[5] + rMat.m_fMat[5], 
					m_fMat[6] + rMat.m_fMat[6], m_fMat[7] + rMat.m_fMat[7], m_fMat[8] + rMat.m_fMat[8]);
}

//-------------------------------------------------------------
//- operator -
//- Subtracts a matrix from the stored one, returns the result
//-------------------------------------------------------------
inline const CMatrix3X3 CMatrix3X3::operator -(const CMatrix3X3& rMat) const
{
	return CMatrix3X3(m_fMat[0] - rMat.m_fMat[0], m_fMat[1] - rMat.m_fMat[1], m_fMat[2] - rMat.m_fMat[2], 
					m_fMat[3] - rMat.m_fMat[3], m_fMat[4] - rMat.m_fMat[4], m_fMat[5] - rMat.m_fMat[5], 
					m_fMat[6] - rMat.m_fMat[6], m_fMat[7] - rMat.m_fMat[7], m_fMat[8] - rMat.m_fMat[8]);
}

//-------------------------------------------------------------
//- operator += 
//- Add a matrix to the stored one
//-------------------------------------------------------------
inline const void CMatrix3X3::operator +=(const CMatrix3X3& rMat)
{
	m_fMat[0] += rMat.m_fMat[0];
	m_fMat[1] += rMat.m_fMat[1];
	m_fMat[2] += rMat.m_fMat[2];
	m_fMat[3] += rMat.m_fMat[3];
	m_fMat[4] += rMat.m_fMat[4];
	m_fMat[5] += rMat.m_fMat[5];
	m_fMat[6] += rMat.m_fMat[6];
	m_fMat[7] += rMat.m_fMat[7];
	m_fMat[8] += rMat.m_fMat[8];
}

//-------------------------------------------------------------
//- operator -= 
//- Subtracts a matrix from the stored one
//-------------------------------------------------------------
inline const void CMatrix3X3::operator -=(const CMatrix3X3& rMat)
{
	m_fMat[0] -= rMat.m_fMat[0];
	m_fMat[1] -= rMat.m_fMat[1];
	m_fMat[2] -= rMat.m_fMat[2];
	m_fMat[3] -= rMat.m_fMat[3];
	m_fMat[4] -= rMat.m_fMat[4];
	m_fMat[5] -= rMat.m_fMat[5];
	m_fMat[6] -= rMat.m_fMat[6];
	m_fMat[7] -= rMat.m_fMat[7];
	m_fMat[8] -= rMat.m_fMat[8];
}

//-------------------------------------------------------------
//- operator *
//- Multiplies the matrix by a scalar and returns the result
//-------------------------------------------------------------
inline const CMatrix3X3 CMatrix3X3::operator *(const float fScalar) const
{
	return CMatrix3X3(m_fMat[0] * fScalar, m_fMat[1] * fScalar, m_fMat[2] * fScalar, 
					m_fMat[3] * fScalar, m_fMat[4] * fScalar, m_fMat[5] * fScalar, 
					m_fMat[6] * fScalar, m_fMat[7] * fScalar, m_fMat[8] * fScalar);
}

//-------------------------------------------------------------
//- operator /
//- Divides the current matrix by a scalar and returns the result
//-------------------------------------------------------------
inline const CMatrix3X3 CMatrix3X3::operator /(const float fScalar) const
{
	float fInvScl = 1/fScalar;
	return CMatrix3X3(m_fMat[0] * fInvScl, m_fMat[1] * fInvScl, m_fMat[2] * fInvScl, 
					m_fMat[3] * fInvScl, m_fMat[4] * fInvScl, m_fMat[5] * fInvScl, 
					m_fMat[6] * fInvScl, m_fMat[7] * fInvScl, m_fMat[8] * fInvScl);
}

//-------------------------------------------------------------
//- operator *= 
//- Multiplies the stored matrix by a scalar
//-------------------------------------------------------------
inline const void CMatrix3X3::operator *=(const float fScalar)
{
	m_fMat[0] *= fScalar;
	m_fMat[1] *= fScalar;
	m_fMat[2] *= fScalar;
	m_fMat[3] *= fScalar;
	m_fMat[4] *= fScalar;
	m_fMat[5] *= fScalar;
	m_fMat[6] *= fScalar;
	m_fMat[7] *= fScalar;
	m_fMat[8] *= fScalar;
}

//-------------------------------------------------------------
//- operator /=
//- Divides the stored matrix by a scalar
//-------------------------------------------------------------
inline const void CMatrix3X3::operator /=(const float fScalar)
{
	float fInvScl = 1/fScalar;
	m_fMat[0] *= fInvScl;
	m_fMat[1] *= fInvScl;
	m_fMat[2] *= fInvScl;
	m_fMat[3] *= fInvScl;
	m_fMat[4] *= fInvScl;
	m_fMat[5] *= fInvScl;
	m_fMat[6] *= fInvScl;
	m_fMat[7] *= fInvScl;
	m_fMat[8] *= fInvScl;
}

//-------------------------------------------------------------
//- operator *
//- Multiply the stored matrix by another, return the result
//-------------------------------------------------------------
inline const CMatrix3X3 CMatrix3X3::operator *(const CMatrix3X3& rMat) const
{
	const float * m1 = m_fMat;
	const float * m2 = rMat.m_fMat;

	return CMatrix3X3(

		(m1[0] * m2[0] + m1[3] * m2[1] + m1[6] * m2[2]),
		(m1[1] * m2[0] + m1[4] * m2[1] + m1[7] * m2[2]),
		(m1[2] * m2[0] + m1[5] * m2[1] + m1[8] * m2[2]),
		
		(m1[0] * m2[3] + m1[3] * m2[4] + m1[6] * m2[5]),
		(m1[1] * m2[3] + m1[4] * m2[4] + m1[7] * m2[5]),
		(m1[2] * m2[3] + m1[5] * m2[4] + m1[8] * m2[5]),

		(m1[0] * m2[6] + m1[3] * m2[7] + m1[6] * m2[8]),
		(m1[1] * m2[6] + m1[4] * m2[7] + m1[7] * m2[8]),
		(m1[2] * m2[6] + m1[5] * m2[7] + m1[8] * m2[8])
	
		);
}

//-------------------------------------------------------------
//- operator *=
//- Multiply the stored matrix by another
//-------------------------------------------------------------
inline const void CMatrix3X3::operator *=(const CMatrix3X3& rMat)
{
	float fNewMat[9];

	const float * m1 = m_fMat;
	const float * m2 = rMat.m_fMat;

	fNewMat[0] = (m1[0] * m2[0] + m1[3] * m2[1] + m1[6] * m2[2]);
	fNewMat[1] = (m1[1] * m2[0] + m1[4] * m2[1] + m1[7] * m2[2]);
	fNewMat[2] = (m1[2] * m2[0] + m1[5] * m2[1] + m1[8] * m2[2]);

	fNewMat[3] = (m1[0] * m2[3] + m1[3] * m2[4] + m1[6] * m2[5]);
	fNewMat[4] = (m1[1] * m2[3] + m1[4] * m2[4] + m1[7] * m2[5]);
	fNewMat[5] = (m1[2] * m2[3] + m1[5] * m2[4] + m1[8] * m2[5]);

	fNewMat[6] = (m1[0] * m2[6] + m1[3] * m2[7] + m1[6] * m2[8]);
	fNewMat[7] = (m1[1] * m2[6] + m1[4] * m2[7] + m1[7] * m2[8]);
	fNewMat[8] = (m1[2] * m2[6] + m1[5] * m2[7] + m1[8] * m2[8]);

	memcpy(m_fMat, fNewMat, sizeof(float[9]));
}

//-------------------------------------------------------------
//- operator = 
//- Sets stored matrix equal to another
//-------------------------------------------------------------
inline const void CMatrix3X3::operator =(const CMatrix3X3& rMat)
{
	memcpy(m_fMat, rMat.m_fMat, sizeof(float[9]));
}


//-------------------------------------------------------------
//- operator==
//- Check for equality
//-------------------------------------------------------------
inline const bool CMatrix3X3::operator ==(const CMatrix3X3& rMat) const
{
	return (m_fMat[0] == rMat.m_fMat[0] && m_fMat[1] == rMat.m_fMat[1] && m_fMat[2] == rMat.m_fMat[2] && 
		m_fMat[3] == rMat.m_fMat[3] && m_fMat[4] == rMat.m_fMat[4] && m_fMat[5] == rMat.m_fMat[5] && 
		m_fMat[6] == rMat.m_fMat[6] && m_fMat[7] == rMat.m_fMat[7] && m_fMat[8] == rMat.m_fMat[8]);
}

//-------------------------------------------------------------
//- operator != 
//- Check for inequality
//-------------------------------------------------------------
inline const bool CMatrix3X3::operator != (const CMatrix3X3& rMat) const
{
	return !(*this == rMat);
}
//-------------------------------------------------------------
//- Constuctors
//- 1. Default Constructor (no parameters, initializes to 0)
//- 2. Copy Constructor (takes another CMatrix3X3 and copies it)
//- 3. 9 floats (takes 9 floats one each element)
//- 4. float array (takes a pointer to an array of floats and uses
//     the nine elements to fill in the matrix)
//-------------------------------------------------------------
inline CMatrix3X3::CMatrix3X3()
{
	memset(m_fMat, 0, sizeof(float[9]));
}

inline CMatrix3X3::CMatrix3X3(bool identity)
{
	memset(m_fMat, 0, sizeof(float[9]));
	if(identity){
		m_fMat[0] = 1.0f;
		m_fMat[4] = 1.0f;
		m_fMat[8] = 1.0f;
	}
}

inline CMatrix3X3::CMatrix3X3(const CMatrix3X3& rMat)
{
	*this = rMat;
}

inline CMatrix3X3::CMatrix3X3(float f11, float f12, float f13,
				float f21, float f22, float f23,
				float f31, float f32, float f33)
{
	m_fMat[0] = f11;
	m_fMat[1] = f12;
	m_fMat[2] = f13;
	m_fMat[3] = f21;
	m_fMat[4] = f22;
	m_fMat[5] = f23;
	m_fMat[6] = f31;
	m_fMat[7] = f32;
	m_fMat[8] = f33;
}

inline CMatrix3X3::CMatrix3X3(float * fpMat)
{
	memcpy(m_fMat, fpMat, sizeof(float[9]));
}
//-------------------------------------------------------------
//- Accessors
//- 1. Get (returns a pointer to the array)
//- 2. Set (takes 9 floats, one for each component)
//- 3. Set (takes a pointer to a float array, the first nine 
//     elements are used)
//-------------------------------------------------------------
inline float * CMatrix3X3::Get()
{
	return m_fMat;
}

inline void CMatrix3X3::Set(float f11, float f12, float f13,
				float f21, float f22, float f23,
				float f31, float f32, float f33)
{
	m_fMat[0] = f11;
	m_fMat[1] = f12;
	m_fMat[2] = f13;
	m_fMat[3] = f21;
	m_fMat[4] = f22;
	m_fMat[5] = f23;
	m_fMat[6] = f31;
	m_fMat[7] = f32;
	m_fMat[8] = f33;
}

inline void CMatrix3X3::Set(float * fpMat)
{
	memcpy(m_fMat, fpMat, sizeof(float[9]));
}


//-------------------------------------------------------------
//                    END CMatrix3X3                          -
//-------------------------------------------------------------



//-------------------------------------------------------------
//                       CMatrix4X4                           -
//-------------------------------------------------------------
//-------------------------------------------------------------
//-                      FUNCTIONS                            -
//-------------------------------------------------------------
//-------------------------------------------------------------
//- Zero
//- Set the matrix to a zero matrix
//-------------------------------------------------------------
inline void CMatrix4X4::Zero()
{
	memset(m_fMat, 0, sizeof(float[16]));
}

//-------------------------------------------------------------
//- Identity
//- Set the matrix to an identity matrix
//-------------------------------------------------------------
inline void CMatrix4X4::Identity()
{
	Zero();
	m_fMat[0] = 1.0f;
	m_fMat[5] = 1.0f;
	m_fMat[10] = 1.0f;
	m_fMat[15] = 1.0f;
}

//-------------------------------------------------------------
//- Transpose
//- Transpose the matrix (swap rows and columns)
//-------------------------------------------------------------
inline void CMatrix4X4::Transpose()
{
	CMatrix4X4 tmp(m_fMat[0], m_fMat[4], m_fMat[8], m_fMat[12],
					m_fMat[1], m_fMat[5], m_fMat[9], m_fMat[13],
					m_fMat[2], m_fMat[6], m_fMat[10], m_fMat[14],
					m_fMat[3], m_fMat[7], m_fMat[11], m_fMat[15]);
	*this = tmp;
}

//-------------------------------------------------------------
//- Invert
//- Invert the stored matrix
//- ADAPTED FROM MTXLIB.CPP on the Game Programming Gems CD
//- Original code Copyright (C) Dante Treglia II and Mark A. DeLoura, 2000
//-------------------------------------------------------------
inline void CMatrix4X4::Invert()
{
	CMatrix4X4 matA(*this);
	CMatrix4X4 matB;
	matB.Identity();

	unsigned int uiRow, uiColumn;
	unsigned int uiRowMax; // Points to max abs value row in this column
	float tmp;

  // Go through columns
	for(uiColumn = 0; uiColumn < 4; uiColumn++)
	{

	  // Find the row with max value in this column
		uiRowMax = uiColumn;
		for(uiRow = uiColumn+1; uiRow < 4; uiRow++)
		{
			if(fabs(matA[4 * uiRow + uiColumn]) > fabs(matA[4 * uiRowMax + uiColumn]))
			{
				uiRowMax = uiRow;
			}
		}

	  // If the max value here is 0, we can't invert.  .
		if(matA[4 * uiColumn + uiRowMax] == 0.0F)
		{
			/*
			 * TODO: 这个矩阵求不出逆，在此返回了。用下面的InvertTranslationAndRotation来求。
			0.00    -0.00    -1.00    -0.00
			0.18    -0.98    0.00    10.71
			-0.98    -0.18    0.00    19.34
			0.00    0.00    0.00    1.00
			 */
			Identity();
			return;
		}

	  // Swap row "rowMax" with row "c"
		int cc;
		for(cc = 0; cc < 4; cc++)
		{
			tmp = matA[4 * uiColumn + cc];
			matA[4 * uiColumn + cc] = matA[4 * uiRowMax + cc];
			matA[4 * uiRowMax + cc] = tmp;

			tmp = matB[4 * uiColumn + cc];
			matB[4 * uiColumn + cc] = matB[4 * uiRowMax + cc];
			matB[4 * uiRowMax + cc] = tmp;
		}

	  // Now everything we do is on row "c".
	  // Set the max cell to 1 by dividing the entire row by that value
		tmp = matA[4 * uiColumn + uiColumn];
		for(cc = 0; cc < 4; cc++)
		{
			matA[4 * uiColumn + cc] /= tmp;
			matB[4 * uiColumn + cc] /= tmp;
		}

	  // Now do the other rows, so that this column only has a 1 and 0's
		for(uiRow = 0; uiRow < 4; uiRow++)
		{
			if (uiRow != uiColumn)
			{
				tmp = matA[4 * uiRow + uiColumn];
				for(cc = 0; cc < 4; cc++)
				{
					matA[4 * uiRow + cc] -= matA[4 * uiColumn + cc] * tmp;
					matB[4 * uiRow + cc] -= matB[4 * uiColumn + cc] * tmp;
				}
			}
		}
	}

	*this = matB;

}

inline void CMatrix4X4::InvertTranslationAndRotation()
{
	float tx = m_fMat[0] * m_fMat[12] + m_fMat[1] * m_fMat[13] + m_fMat[2] * m_fMat[14];
	float ty = m_fMat[4] * m_fMat[12] + m_fMat[5] * m_fMat[13] + m_fMat[6] * m_fMat[14];
	float tz = m_fMat[8] * m_fMat[12] + m_fMat[9] * m_fMat[13] + m_fMat[10] * m_fMat[14];
	CMatrix4X4 tmp(	m_fMat[0], m_fMat[4], m_fMat[8], 0,
					m_fMat[1], m_fMat[5], m_fMat[9], 0,
					m_fMat[2], m_fMat[6], m_fMat[10], 0,
					-tx, -ty, -tz, 1);

	*this = tmp;
}

//-------------------------------------------------------------
//- SetRotation
//- Build a rotation matrix
//-------------------------------------------------------------
inline void CMatrix4X4::SetRotation(float fX, float fY, float fZ)
{
	double cx = cos(fX);
	double sx = sin(fX);
	double cy = cos(fY);
	double sy = sin(fY);
	double cz = cos(fZ);
	double sz = sin(fZ);

	m_fMat[0] = (float)(cy * cz);
	m_fMat[1] = (float)(cy * sz);
	m_fMat[2] = (float)(-sy);

	m_fMat[4] = (float)(sx * sy * cz - cx * sz);
	m_fMat[5] = (float)(sx * sy * sz + cx * cz);
	m_fMat[6] = (float)(sx * cy);

	m_fMat[8] = (float)(cx * sy * cz + sx * sz);
	m_fMat[9] = (float)(cx * sy * sz - sx * cz);
	m_fMat[10] = (float)(cx * cy);

	m_fMat[15] = 1.0f;
}

inline void CMatrix4X4::SetRotation(float * fpAngles)
{
	SetRotation(fpAngles[0], fpAngles[1], fpAngles[2]);
}

//-------------------------------------------------------------
//- SetInvRotation
//- Build an inverse rotation matrix
//-------------------------------------------------------------
inline void CMatrix4X4::SetInvRotation(float fX, float fY, float fZ)
{
	double cx = cos(fX);
	double sx = sin(fX);
	double cy = cos(fY);
	double sy = sin(fY);
	double cz = cos(fZ);
	double sz = sin(fZ);

	m_fMat[0] = (float)(cy * cz);
	m_fMat[4] = (float)(cy * sz);
	m_fMat[8] = (float)(-sy );

	m_fMat[1] = (float)(sx * sy * cz - cx * sz);
	m_fMat[5] = (float)(sx * sy * sz + cx * cz);
	m_fMat[9] = (float)(sx * cy);

	m_fMat[2] = (float)(cx * sy * cz + sx * sz);
	m_fMat[6] = (float)(cx * sy * sz - sx * cz);
	m_fMat[10] = (float)(cx * cz);

	m_fMat[15] = 1.0f;
}

inline void CMatrix4X4::SetInvRotation(float * fpAngles)
{
	SetInvRotation(fpAngles[0], fpAngles[1], fpAngles[2]);
}

inline void CMatrix4X4::FromAxisAngle(CVector3& vec, float fAngle)
{
	float fCos = cosf(fAngle);
	float fSin = sinf(fAngle);
	float fOMC = 1.0f - fCos;

	m_fMat[0] = fCos + SQU(vec[0]) * fOMC;
	m_fMat[5] = fCos + SQU(vec[1]) * fOMC;
	m_fMat[10] = fCos + SQU(vec[2]) * fOMC;
	m_fMat[15] = 1.0f;
	m_fMat[4] = vec[0] * vec[1] * fOMC + vec[2] * fSin;
	m_fMat[1] = vec[0] * vec[1] * fOMC - vec[2] * fSin;
	m_fMat[8] = vec[0] * vec[2] * fOMC + vec[1] * fSin;
	m_fMat[2] = vec[0] * vec[2] * fOMC - vec[1] * fSin;
	m_fMat[9] = vec[1] * vec[2] * fOMC + vec[0] * fSin;
	m_fMat[6] = vec[1] * vec[2] * fOMC - vec[0] * fSin;

}

//-------------------------------------------------------------
//- SetTranslation
//- Set the translation values of the matrix
//-------------------------------------------------------------
inline void CMatrix4X4::SetTranslation(float fX, float fY, float fZ)
{
	m_fMat[12] = fX;
	m_fMat[13] = fY;
	m_fMat[14] = fZ;
}

inline void CMatrix4X4::SetTranslation(float * fpValues)
{
	SetTranslation(fpValues[0], fpValues[1], fpValues[2]);
}

//-------------------------------------------------------------
//- SetInvTranslation
//- Set the inverse translation values of the matrix
//-------------------------------------------------------------
inline void CMatrix4X4::SetInvTranslation(float fX, float fY, float fZ)
{
	m_fMat[12] = -fX;
	m_fMat[13] = -fY;
	m_fMat[14] = -fZ;
}

inline void CMatrix4X4::SetInvTranslation(float * fpValues)
{
	SetInvTranslation(fpValues[0], fpValues[1], fpValues[2]);
}

//-------------------------------------------------------------
//- FromQuaternion
//- Build a rotation matrix from a quaternion
//-------------------------------------------------------------
inline void CMatrix4X4::FromQuaternion(CQuaternion& rQuat)
{
	float * fQ = rQuat.Get();
	m_fMat[0] = 1.0f - 2.0f * (fQ[1] * fQ[1] + fQ[2] * fQ[2]); 
	m_fMat[1] = 2.0f * (fQ[0] * fQ[1] - fQ[2] * fQ[3]);
	m_fMat[2] = 2.0f * (fQ[0] * fQ[2] + fQ[1] * fQ[3]);

	m_fMat[4] = 2.0f * (fQ[0] * fQ[1] + fQ[2] * fQ[3]);
	m_fMat[5] = 1.0f - 2.0f * (fQ[0] * fQ[0] + fQ[2] * fQ[2]);
	m_fMat[6] = 2.0f * (fQ[1] * fQ[2] - fQ[0] * fQ[3]);
	
	m_fMat[8] = 2.0f * (fQ[0] * fQ[2] - fQ[1] * fQ[3]);
	m_fMat[9] = 2.0f * (fQ[1] * fQ[2] + fQ[0] * fQ[3]);
	m_fMat[10] = 1.0f - 2.0f * (fQ[0] * fQ[0] + fQ[1] * fQ[1]);


	m_fMat[15] = 1.0f;
}

//-------------------------------------------------------------
//- Determinant
//- Calculate the determinant of the matrix
//-------------------------------------------------------------
inline float CMatrix4X4::Determinant()
{
	return( (m_fMat[0] * m_fMat[5] * m_fMat[10] * m_fMat[15]) +
		    (m_fMat[1] * m_fMat[6] * m_fMat[11] * m_fMat[12]) +
			(m_fMat[2] * m_fMat[7] * m_fMat[8] * m_fMat[13]) +
			(m_fMat[3] * m_fMat[4] * m_fMat[9] * m_fMat[14]) - 
			(m_fMat[3] * m_fMat[6] * m_fMat[9] * m_fMat[12]) - 
			(m_fMat[7] * m_fMat[10] * m_fMat[13] * m_fMat[0]) - 
			(m_fMat[11] * m_fMat[14] * m_fMat[1] * m_fMat[4]) - 
			(m_fMat[15] * m_fMat[2] * m_fMat[5] * m_fMat[8]));

}

//-------------------------------------------------------------
//- InvRotateVec
//- Rotate avector using the inverse of the matrix
//-------------------------------------------------------------
inline void CMatrix4X4::InverseRotateVec(float * fpVec) const
{
	float tmp[3];

	tmp[0] = fpVec[0] * m_fMat[0] + fpVec[1] * m_fMat[1] + fpVec[2] * m_fMat[2];
	tmp[1] = fpVec[0] * m_fMat[4] + fpVec[1] * m_fMat[5] + fpVec[2] * m_fMat[6];
	tmp[2] = fpVec[0] * m_fMat[8] + fpVec[1] * m_fMat[9] + fpVec[2] * m_fMat[10];

	memcpy(fpVec, tmp, sizeof(float[3]));
}

//-------------------------------------------------------------
//- InvTranslateVec
//- Translate a vector based on the inverse matrix
//-------------------------------------------------------------
inline void CMatrix4X4::InverseTranslateVec(float * fpVec) const
{
	fpVec[0] = fpVec[0] - m_fMat[12];
	fpVec[1] = fpVec[1] - m_fMat[13];
	fpVec[2] = fpVec[2] - m_fMat[14];
}

inline void CMatrix4X4::InverseTranslateRotateVec(float *fpVec, float* dst) const
{
	float tmp[3];
	tmp[0] = fpVec[0] - m_fMat[12];
	tmp[1] = fpVec[1] - m_fMat[13];
	tmp[2] = fpVec[2] - m_fMat[14];

	dst[0] = tmp[0] * m_fMat[0] + tmp[1] * m_fMat[1] + tmp[2] * m_fMat[2];
	dst[1] = tmp[0] * m_fMat[4] + tmp[1] * m_fMat[5] + tmp[2] * m_fMat[6];
	dst[2] = tmp[0] * m_fMat[8] + tmp[1] * m_fMat[9] + tmp[2] * m_fMat[10];
}

//-------------------------------------------------------------
//-                      OPERATORS                            -
//-------------------------------------------------------------
//-------------------------------------------------------------
//- operator[]
//- Get a reference to a single element
//-------------------------------------------------------------
inline float& CMatrix4X4::operator [](const int iIdx)
{
	return m_fMat[iIdx];
}

//-------------------------------------------------------------
//- opertator +
//- Add a matrix to the stored matrix, return the result
//-------------------------------------------------------------
inline const CMatrix4X4 CMatrix4X4::operator +(const CMatrix4X4& rMat) const
{
	return CMatrix4X4(m_fMat[0] + rMat.m_fMat[0], m_fMat[1] + rMat.m_fMat[1], m_fMat[2] + rMat.m_fMat[2], m_fMat[3] + rMat.m_fMat[3], 
		              m_fMat[4] + rMat.m_fMat[4], m_fMat[5] + rMat.m_fMat[5], m_fMat[6] + rMat.m_fMat[6], m_fMat[7] + rMat.m_fMat[7], 
					  m_fMat[8] + rMat.m_fMat[8], m_fMat[9] + rMat.m_fMat[9], m_fMat[10] + rMat.m_fMat[10], m_fMat[11] + rMat.m_fMat[11], 
					  m_fMat[12] + rMat.m_fMat[12], m_fMat[13] + rMat.m_fMat[13], m_fMat[14] + rMat.m_fMat[14], m_fMat[15] + rMat.m_fMat[15]);
}

//-------------------------------------------------------------
//- operator -
//- Subtracts a matrix from the stored one, returns the result
//-------------------------------------------------------------
inline const CMatrix4X4 CMatrix4X4::operator -(const CMatrix4X4& rMat) const
{
	return CMatrix4X4(m_fMat[0] - rMat.m_fMat[0], m_fMat[1] - rMat.m_fMat[1], m_fMat[2] - rMat.m_fMat[2], m_fMat[3] - rMat.m_fMat[3], 
		              m_fMat[4] - rMat.m_fMat[4], m_fMat[5] - rMat.m_fMat[5], m_fMat[6] - rMat.m_fMat[6], m_fMat[7] - rMat.m_fMat[7], 
					  m_fMat[8] - rMat.m_fMat[8], m_fMat[9] - rMat.m_fMat[9], m_fMat[10] - rMat.m_fMat[10], m_fMat[11] - rMat.m_fMat[11], 
					  m_fMat[12] - rMat.m_fMat[12], m_fMat[13] - rMat.m_fMat[13], m_fMat[14] - rMat.m_fMat[14], m_fMat[15] - rMat.m_fMat[15]);
}

//-------------------------------------------------------------
//- operator += 
//- Add a matrix to the stored one
//-------------------------------------------------------------
inline const void CMatrix4X4::operator +=(const CMatrix4X4& rMat)
{
	m_fMat[0] += rMat.m_fMat[0];
	m_fMat[1] += rMat.m_fMat[1];
	m_fMat[2] += rMat.m_fMat[2];
	m_fMat[3] += rMat.m_fMat[3];
	m_fMat[4] += rMat.m_fMat[4];
	m_fMat[5] += rMat.m_fMat[5];
	m_fMat[6] += rMat.m_fMat[6];
	m_fMat[7] += rMat.m_fMat[7];
	m_fMat[8] += rMat.m_fMat[8];
	m_fMat[9] += rMat.m_fMat[9];
	m_fMat[10] += rMat.m_fMat[10];
	m_fMat[11] += rMat.m_fMat[11];
	m_fMat[12] += rMat.m_fMat[12];
	m_fMat[13] += rMat.m_fMat[13];
	m_fMat[14] += rMat.m_fMat[14];
	m_fMat[15] += rMat.m_fMat[15];
}

//-------------------------------------------------------------
//- operator -= 
//- Subtracts a matrix from the stored one
//-------------------------------------------------------------
inline const void CMatrix4X4::operator -=(const CMatrix4X4& rMat)
{
	m_fMat[0] -= rMat.m_fMat[0];
	m_fMat[1] -= rMat.m_fMat[1];
	m_fMat[2] -= rMat.m_fMat[2];
	m_fMat[3] -= rMat.m_fMat[3];
	m_fMat[4] -= rMat.m_fMat[4];
	m_fMat[5] -= rMat.m_fMat[5];
	m_fMat[6] -= rMat.m_fMat[6];
	m_fMat[7] -= rMat.m_fMat[7];
	m_fMat[8] -= rMat.m_fMat[8];
	m_fMat[9] -= rMat.m_fMat[9];
	m_fMat[10] -= rMat.m_fMat[10];
	m_fMat[11] -= rMat.m_fMat[11];
	m_fMat[12] -= rMat.m_fMat[12];
	m_fMat[13] -= rMat.m_fMat[13];
	m_fMat[14] -= rMat.m_fMat[14];
	m_fMat[15] -= rMat.m_fMat[15];
}

//-------------------------------------------------------------
//- operator *
//- Multiplies the matrix by a scalar and returns the result
//-------------------------------------------------------------
inline const CMatrix4X4 CMatrix4X4::operator *(const float fScalar) const
{
	return CMatrix4X4(m_fMat[0] * fScalar, m_fMat[1] * fScalar, m_fMat[2] * fScalar, m_fMat[3] * fScalar, 
		              m_fMat[4] * fScalar, m_fMat[5] * fScalar, m_fMat[6] * fScalar, m_fMat[7] * fScalar, 
					  m_fMat[8] * fScalar, m_fMat[9] * fScalar, m_fMat[10] * fScalar, m_fMat[11] * fScalar,
					  m_fMat[12] * fScalar, m_fMat[13] * fScalar, m_fMat[14] * fScalar, m_fMat[15] * fScalar);
}

//-------------------------------------------------------------
//- operator /
//- Divides the current matrix by a scalar and returns the result
//-------------------------------------------------------------
inline const CMatrix4X4 CMatrix4X4::operator /(const float fScalar) const
{
	float fInvScl = 1/fScalar;
	return CMatrix4X4(m_fMat[0] * fInvScl, m_fMat[1] * fInvScl, m_fMat[2] * fInvScl, m_fMat[3] * fInvScl, 
		              m_fMat[4] * fInvScl, m_fMat[5] * fInvScl, m_fMat[6] * fInvScl, m_fMat[7] * fInvScl, 
					  m_fMat[8] * fInvScl, m_fMat[9] * fInvScl, m_fMat[10] * fInvScl, m_fMat[11] * fInvScl,
					  m_fMat[12] * fInvScl, m_fMat[13] * fInvScl, m_fMat[14] * fInvScl, m_fMat[15] * fInvScl);
}

//-------------------------------------------------------------
//- operator *= 
//- Multiplies the stored matrix by a scalar
//-------------------------------------------------------------
inline const void CMatrix4X4::operator *=(const float fScalar)
{
	m_fMat[0] *= fScalar;
	m_fMat[1] *= fScalar;
	m_fMat[2] *= fScalar;
	m_fMat[3] *= fScalar;
	m_fMat[4] *= fScalar;
	m_fMat[5] *= fScalar;
	m_fMat[6] *= fScalar;
	m_fMat[7] *= fScalar;
	m_fMat[8] *= fScalar;
	m_fMat[9] *= fScalar;
	m_fMat[10] *= fScalar;
	m_fMat[11] *= fScalar;
	m_fMat[12] *= fScalar;
	m_fMat[13] *= fScalar;
	m_fMat[14] *= fScalar;
	m_fMat[15] *= fScalar;
}

//-------------------------------------------------------------
//- operator /=
//- Divides the stored matrix by a scalar
//-------------------------------------------------------------
inline const void CMatrix4X4::operator /=(const float fScalar)
{
	float fInvScl = 1/fScalar;
	m_fMat[0] *= fInvScl;
	m_fMat[1] *= fInvScl;
	m_fMat[2] *= fInvScl;
	m_fMat[3] *= fInvScl;
	m_fMat[4] *= fInvScl;
	m_fMat[5] *= fInvScl;
	m_fMat[6] *= fInvScl;
	m_fMat[7] *= fInvScl;
	m_fMat[8] *= fInvScl;
	m_fMat[9] *= fInvScl;
	m_fMat[10] *= fInvScl;
	m_fMat[11] *= fInvScl;
	m_fMat[12] *= fInvScl;
	m_fMat[13] *= fInvScl;
	m_fMat[14] *= fInvScl;
	m_fMat[15] *= fInvScl;
}

//-------------------------------------------------------------
//- operator *
//- Multiply the stored matrix by another, return the result
//-------------------------------------------------------------
inline const CMatrix4X4 CMatrix4X4::operator *(const CMatrix4X4& rMat) const
{
	//Shorten the syntax a bit
	const float * m1 = m_fMat;
	const float * m2 = rMat.m_fMat;

	return CMatrix4X4(

	(m1[0] * m2[0] + m1[4] * m2[1] + m1[8] * m2[2] + m1[12] * m2[3]),
	(m1[1] * m2[0] + m1[5] * m2[1] + m1[9] * m2[2] + m1[13] * m2[3]),
	(m1[2] * m2[0] + m1[6] * m2[1] + m1[10] * m2[2] + m1[14] * m2[3]),
	(m1[3] * m2[0] + m1[7] * m2[1] + m1[11] * m2[2] + m1[15] * m2[3]),

	(m1[0] * m2[4] + m1[4] * m2[5] + m1[8] * m2[6] + m1[12] * m2[7]),
	(m1[1] * m2[4] + m1[5] * m2[5] + m1[9] * m2[6] + m1[13] * m2[7]),
	(m1[2] * m2[4] + m1[6] * m2[5] + m1[10] * m2[6] + m1[14] * m2[7]),
	(m1[3] * m2[4] + m1[7] * m2[5] + m1[11] * m2[6] + m1[15] * m2[7]),

	(m1[0] * m2[8] + m1[4] * m2[9] + m1[8] * m2[10] + m1[12] * m2[11]),
	(m1[1] * m2[8] + m1[5] * m2[9] + m1[9] * m2[10] + m1[13] * m2[11]),
	(m1[2] * m2[8] + m1[6] * m2[9] + m1[10] * m2[10] + m1[14] * m2[11]),
	(m1[3] * m2[8] + m1[7] * m2[9] + m1[11] * m2[10] + m1[15] * m2[11]),

	(m1[0] * m2[12] + m1[4] * m2[13] + m1[8] * m2[14] + m1[12] * m2[15]),
	(m1[1] * m2[12] + m1[5] * m2[13] + m1[9] * m2[14] + m1[13] * m2[15]),
	(m1[2] * m2[12] + m1[6] * m2[13] + m1[10] * m2[14] + m1[14] * m2[15]),
	(m1[3] * m2[12] + m1[7] * m2[13] + m1[11] * m2[14] + m1[15] * m2[15])
	);
}

//-------------------------------------------------------------
//- operator *=
//- Multiply the stored matrix by another
//-------------------------------------------------------------
inline const void CMatrix4X4::operator *=(const CMatrix4X4& rMat)
{
	float fNewMat[16];
	//Shorten the syntax a bit
	const float * m1 = m_fMat;
	const float * m2 = rMat.m_fMat;

	fNewMat[0] = m1[0] * m2[0] + m1[4] * m2[1] + m1[8] * m2[2] + m1[12] * m2[3];
	fNewMat[1] = m1[1] * m2[0] + m1[5] * m2[1] + m1[9] * m2[2] + m1[13] * m2[3];
	fNewMat[2] = m1[2] * m2[0] + m1[6] * m2[1] + m1[10] * m2[2] + m1[14] * m2[3];
	fNewMat[3] = m1[3] * m2[0] + m1[7] * m2[1] + m1[11] * m2[2] + m1[15] * m2[3];

	fNewMat[4] = m1[0] * m2[4] + m1[4] * m2[5] + m1[8] * m2[6] + m1[12] * m2[7];
	fNewMat[5] = m1[1] * m2[4] + m1[5] * m2[5] + m1[9] * m2[6] + m1[13] * m2[7];
	fNewMat[6] = m1[2] * m2[4] + m1[6] * m2[5] + m1[10] * m2[6] + m1[14] * m2[7];
	fNewMat[7] = m1[3] * m2[4] + m1[7] * m2[5] + m1[11] * m2[6] + m1[15] * m2[7];

	fNewMat[8] = m1[0] * m2[8] + m1[4] * m2[9] + m1[8] * m2[10] + m1[12] * m2[11];
	fNewMat[9] = m1[1] * m2[8] + m1[5] * m2[9] + m1[9] * m2[10] + m1[13] * m2[11];
	fNewMat[10] = m1[2] * m2[8] + m1[6] * m2[9] + m1[10] * m2[10] + m1[14] * m2[11];
	fNewMat[11] = m1[3] * m2[8] + m1[7] * m2[9] + m1[11] * m2[10] + m1[15] * m2[11];

	fNewMat[12] = m1[0] * m2[12] + m1[4] * m2[13] + m1[8] * m2[14] + m1[12] * m2[15];
	fNewMat[13] = m1[1] * m2[12] + m1[5] * m2[13] + m1[9] * m2[14] + m1[13] * m2[15];
	fNewMat[14] = m1[2] * m2[12] + m1[6] * m2[13] + m1[10] * m2[14] + m1[14] * m2[15];
	fNewMat[15] = m1[3] * m2[12] + m1[7] * m2[13] + m1[11] * m2[14] + m1[15] * m2[15];

	memcpy(m_fMat, fNewMat, sizeof(float[16]));
}

//-------------------------------------------------------------
//- operator = 
//- Sets stored matrix equal to another
//-------------------------------------------------------------
inline const void CMatrix4X4::operator =(const CMatrix4X4& rMat)
{
	memcpy(m_fMat, rMat.m_fMat, sizeof(float[16]));
}


//-------------------------------------------------------------
//- operator==
//- Check for equality
//-------------------------------------------------------------
inline const bool CMatrix4X4::operator ==(const CMatrix4X4& rMat) const
{
	return (m_fMat[0] == rMat.m_fMat[0] && m_fMat[1] == rMat.m_fMat[1] && m_fMat[2] == rMat.m_fMat[2] && 
		m_fMat[3] == rMat.m_fMat[3] && m_fMat[4] == rMat.m_fMat[4] && m_fMat[5] == rMat.m_fMat[5] && 
		m_fMat[6] == rMat.m_fMat[6] && m_fMat[7] == rMat.m_fMat[7] && m_fMat[8] == rMat.m_fMat[8] &&
		m_fMat[9] == rMat.m_fMat[9] && m_fMat[10] == rMat.m_fMat[10] && m_fMat[11] == rMat.m_fMat[11] &&
		m_fMat[12] == rMat.m_fMat[12] && m_fMat[13] == rMat.m_fMat[13] && m_fMat[14] == rMat.m_fMat[14] && m_fMat[15] == rMat.m_fMat[15]);
}

//-------------------------------------------------------------
//- operator != 
//- Check for inequality
//-------------------------------------------------------------
inline const bool CMatrix4X4::operator != (const CMatrix4X4& rMat) const
{
	return !(*this == rMat);
}

//-------------------------------------------------------------
//- Constuctors
//- 1. Default Constructor (no parameters, initializes to 0)
//- 2. Copy Constructor (takes another CMatrix4X4 and copies it)
//- 3. 16 floats (takes 16 floats one each element)
//- 4. float array (takes a pointer to an array of floats and uses
//     the 16 elements to fill in the matrix)
//-------------------------------------------------------------
inline CMatrix4X4::CMatrix4X4()
{
	memset(m_fMat, 0, sizeof(float[16]));
}

inline CMatrix4X4::CMatrix4X4(bool identity)
{
	memset(m_fMat, 0, sizeof(float[16]));
	if(identity){
		m_fMat[0] = 1.0f;
		m_fMat[5] = 1.0f;
		m_fMat[10] = 1.0f;
		m_fMat[15] = 1.0f;
	}
}

inline CMatrix4X4::CMatrix4X4(const CMatrix4X4& rMat)
{
	*this = rMat;
}

inline CMatrix4X4::CMatrix4X4(float f11, float f12, float f13, float f14,
				float f21, float f22, float f23, float f24, 
				float f31, float f32, float f33, float f34,
				float f41, float f42, float f43, float f44)
{
	m_fMat[0] = f11;
	m_fMat[1] = f12;
	m_fMat[2] = f13;
	m_fMat[3] = f14;
	m_fMat[4] = f21;
	m_fMat[5] = f22;
	m_fMat[6] = f23;
	m_fMat[7] = f24;
	m_fMat[8] = f31;
	m_fMat[9] = f32;
	m_fMat[10] = f33;
	m_fMat[11] = f34;
	m_fMat[12] = f41;
	m_fMat[13] = f42;
	m_fMat[14] = f43;
	m_fMat[15] = f44;
}

inline CMatrix4X4::CMatrix4X4(float * fpMat)
{
	memcpy(m_fMat, fpMat, sizeof(float[16]));
}
//-------------------------------------------------------------
//- Accessors
//- 1. Get (returns a pointer to the array)
//- 2. Set (takes 16 floats, one for each component)
//- 3. Set (takes a pointer to a float array, the first 16 
//     elements are used)
//-------------------------------------------------------------
inline float * CMatrix4X4::Get()
{
	return m_fMat;
}

inline void CMatrix4X4::Set(float f11, float f12, float f13, float f14,
				float f21, float f22, float f23, float f24, 
				float f31, float f32, float f33, float f34,
				float f41, float f42, float f43, float f44)
{
	m_fMat[0] = f11;
	m_fMat[1] = f12;
	m_fMat[2] = f13;
	m_fMat[3] = f14;
	m_fMat[4] = f21;
	m_fMat[5] = f22;
	m_fMat[6] = f23;
	m_fMat[7] = f24;
	m_fMat[8] = f31;
	m_fMat[9] = f32;
	m_fMat[10] = f33;
	m_fMat[11] = f34;
	m_fMat[12] = f41;
	m_fMat[13] = f42;
	m_fMat[14] = f43;
	m_fMat[15] = f44;
}

inline void CMatrix4X4::Set(float * fpMat)
{
	memcpy(m_fMat, fpMat, sizeof(float[16]));
}

void CMatrix4X4::GetTranslation(float* fpTrans)
{
	fpTrans[0] = m_fMat[12];
	fpTrans[1] = m_fMat[13];
	fpTrans[2] = m_fMat[14];
}

//-------------------------------------------------------------
//                    END CMatrix4X4                          -
//-------------------------------------------------------------

#endif //MATRIX_INL
