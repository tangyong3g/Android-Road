/*************************************************************/
/*                       QUATERNION.INL                      */
/*                                                           */
/* Purpose: Inlined functions for CQuaternion                */
/*      Evan Pipho (May 30, 2002)                            */
/*                                                           */
/*************************************************************/
#ifndef QUATERNION_INL
#define QUATERNION_INL

//#include <math.h>
//#include <memory.h>
//#include "vector.h"
//#include "matrix.h"
//#include "quaternion.h"
#include "math1.h"

#define PIOver180 0.017453292519938f
#define PIUnder180 57.295779513096787f

//Square a value
#define SQU(x) x * x
//convert degrees to radians
#define Deg2Rad(x) x * PIOver180
#define Rad2Deg(x) x * PIUnder180

//-------------------------------------------------------------
//                       CQuaternion                          -
//-------------------------------------------------------------
//-------------------------------------------------------------
//-                      FUNCTIONS                            -
//-------------------------------------------------------------

//-------------------------------------------------------------
//- Magnitude
//- Returns the magnitude of the stored quaternion
//-------------------------------------------------------------
inline float CQuaternion::Magnitude() const
{
	return sqrtf(SQU(m_fQuat[0]) + SQU(m_fQuat[1]) + SQU(m_fQuat[2]) + SQU(m_fQuat[3]));
}

//-------------------------------------------------------------
//- Normalize
//- Normalizes the quaternion so it has a magnitude of 1
//-------------------------------------------------------------
inline void CQuaternion::Normalize()
{
	float fInvMag = 1/Magnitude();
	if(fInvMag != 1.0f)
	{
		m_fQuat[0] *= fInvMag;
		m_fQuat[1] *= fInvMag;
		m_fQuat[2] *= fInvMag;
		m_fQuat[3] *= fInvMag;
	}
}

//-------------------------------------------------------------
//- Rotate
//- Rotate the stored quaternion by another using the formula
//- p' = q(p)(~q)
//-------------------------------------------------------------
inline void CQuaternion::Rotate(const CQuaternion& rQuat)
{
	CQuaternion tmp(rQuat * (*this) * ~rQuat);
	*this = tmp;
}

//-------------------------------------------------------------
//- GetAngle
//- Retrieves the rotation angle of the quaternion
//-------------------------------------------------------------
inline float CQuaternion::GetAngle() const
{
	return (float)2*acosf(m_fQuat[3]);
}

//-------------------------------------------------------------
//- GetAxis
//- Retrieves the axis of rotation
//-------------------------------------------------------------
inline CVector3 CQuaternion::GetAxis()
{
	float sa = 1/sqrtf(1 - SQU(m_fQuat[4]));
	return CVector3(m_fQuat[0] * sa, m_fQuat[1] * sa, m_fQuat[2] * sa);
}

//-------------------------------------------------------------
//- FromAxisAngle
//- Take an axis of rotation and an angle and convert them into a quaternion
//-------------------------------------------------------------
inline void CQuaternion::FromAxisAngle(CVector3& rAxis, float fAngle)
{
	rAxis.Normalize();
	float sa = sinf(fAngle * 0.5f);
	float ca = cosf(fAngle * 0.5f);
	m_fQuat[0] = rAxis.m_fVec[0] * sa;
	m_fQuat[1] = rAxis.m_fVec[1] * sa;
	m_fQuat[2] = rAxis.m_fVec[2] * sa;
	m_fQuat[3] = ca;
}

//-------------------------------------------------------------
//- From Eulers
//- Create a quaternion from three euler angles (degrees)
//-------------------------------------------------------------
inline void CQuaternion::FromEulers(float * fpAngles)
{
	FromEulers(fpAngles[0], fpAngles[1], fpAngles[2]);
}

inline void CQuaternion::FromEulers(float fX, float fY, float fZ)
{
	double dSY = sin(fZ * 0.5f);
	double dSP = sin(fY * 0.5f);
	double dSR = sin(fX * 0.5f);
	double dCY = cos(fZ * 0.5f);
	double dCP = cos(fY * 0.5f);
	double dCR = cos(fX * 0.5f);

	m_fQuat[0] = dSR * dCP * dCY - dCR * dSP * dSY;
	m_fQuat[1] = dCR * dSP * dCY + dSR * dCP * dSY;
	m_fQuat[2] = dCR * dCP * dSY - dSR * dSP * dCY;
	m_fQuat[3] = dCR * dCP * dCY + dSR * dSP * dSY;
}

//-------------------------------------------------------------
//- Get Eulershow about 
//- Extract Euler angles from the quaternion
//-------------------------------------------------------------
inline CVector3 CQuaternion::GetEulers() const
{
	double m11 = SQU(m_fQuat[3]) + SQU(m_fQuat[0]) - SQU(m_fQuat[1]) - SQU(m_fQuat[2]);
	double m21 = 2 * (m_fQuat[0] * m_fQuat[1] + m_fQuat[2] * m_fQuat[3]);
	double m31 = 2 * (m_fQuat[2] * m_fQuat[0] - m_fQuat[1] * m_fQuat[3]);
	double m32 = 2 * (m_fQuat[2] * m_fQuat[1] + m_fQuat[0] * m_fQuat[3]);
	double m33 = SQU(m_fQuat[3]) - SQU(m_fQuat[0]) - SQU(m_fQuat[1]) + SQU(m_fQuat[2]);

	return CVector3((atan2(m32, m33)),(asinf(-m31)), (atan2(m21, m11)));
}

//-------------------------------------------------------------
//- ToMatrix
//- Convert the quaternion to a 3x3 of 4x4 matrix, ignoring values in the 4x4
//- matrix that are not needed for rotation
//-------------------------------------------------------------
inline void CQuaternion::ToMatrix(CMatrix3X3& rDstMat) const
{
	rDstMat.m_fMat[0] = SQU(m_fQuat[3]) + SQU(m_fQuat[0]) - SQU(m_fQuat[1]) - SQU(m_fQuat[2]);
	rDstMat.m_fMat[1] = 2 * (m_fQuat[0] * m_fQuat[1] - m_fQuat[2] * m_fQuat[3]);
	rDstMat.m_fMat[2] = 2 * (m_fQuat[0] * m_fQuat[2] + m_fQuat[1] * m_fQuat[3]);
	rDstMat.m_fMat[3] = 2 * (m_fQuat[0] * m_fQuat[1] + m_fQuat[2] * m_fQuat[3]);
	rDstMat.m_fMat[4] = SQU(m_fQuat[3]) - SQU(m_fQuat[0]) + SQU(m_fQuat[1]) - SQU(m_fQuat[2]);
	rDstMat.m_fMat[5] = 2 * (m_fQuat[1] * m_fQuat[2] - m_fQuat[0] * m_fQuat[3]);
	rDstMat.m_fMat[6] = 2 * (m_fQuat[2] * m_fQuat[0] - m_fQuat[1] * m_fQuat[3]);
	rDstMat.m_fMat[7] = 2 * (m_fQuat[1] * m_fQuat[2] + m_fQuat[0] * m_fQuat[3]);
	rDstMat.m_fMat[8] = SQU(m_fQuat[3]) - SQU(m_fQuat[0]) - SQU(m_fQuat[1]) + SQU(m_fQuat[2]);
	
}	

inline CMatrix4X4 CQuaternion::ToMatrix4() const
{
	CMatrix4X4 Mat;

	Mat.m_fMat[0] = 1.0f - 2.0f * (SQU(m_fQuat[1]) + SQU(m_fQuat[2])); 
	Mat.m_fMat[1] = 2.0f * m_fQuat[0] * m_fQuat[1] + 2.0f * m_fQuat[2] * m_fQuat[3];
	Mat.m_fMat[2] = 2.0f * m_fQuat[0] * m_fQuat[2] - 2.0f * m_fQuat[1] * m_fQuat[3];

	Mat.m_fMat[4] = 2.0f * m_fQuat[0] * m_fQuat[1] - 2.0f * m_fQuat[2] * m_fQuat[3];
	Mat.m_fMat[5] = 1.0f - 2.0f * (SQU(m_fQuat[0]) + SQU(m_fQuat[2]));
	Mat.m_fMat[6] = 2.0f * m_fQuat[1] * m_fQuat[2] + 2.0f * m_fQuat[0] * m_fQuat[3];
	
	Mat.m_fMat[8] = 2.0f * m_fQuat[2] * m_fQuat[0] + 2.0f * m_fQuat[1] * m_fQuat[3];
	Mat.m_fMat[9] = 2.0f * m_fQuat[1] * m_fQuat[2] - 2.0f * m_fQuat[0] * m_fQuat[3];
	Mat.m_fMat[10] = 1.0f - 2.0f * (SQU(m_fQuat[0]) + SQU(m_fQuat[1]));
	
	Mat.m_fMat[15] = 1.0f;

	return Mat;
}

//-------------------------------------------------------------
//- FromMatrix
//- Build a quaternion from a 3x3 or 4x4 matrix
//-------------------------------------------------------------
inline void CQuaternion::FromMatrix(CMatrix3X3& rMat)
{
	float fTr = rMat.m_fMat[0] + rMat.m_fMat[4] + rMat.m_fMat[8] + 1.0f;
	//if the trace of the matrix is greater than 0, perform an instant calculation
	if(fTr > 0)
	{
		float fS = 0.5f / sqrtf(fTr);

		m_fQuat[3] = 0.25f / fS;
		m_fQuat[0] = (rMat.m_fMat[7] - rMat.m_fMat[5]) * fS;
		m_fQuat[1] = (rMat.m_fMat[2] - rMat.m_fMat[6]) * fS;
		m_fQuat[2] = (rMat.m_fMat[3] - rMat.m_fMat[1]) * fS;
	}
	//if the first diagonal element is the largest
	else if(rMat.m_fMat[0] > rMat.m_fMat[4] && rMat.m_fMat[0] > rMat.m_fMat[8])
	{
		float fS = 1 / (sqrt(rMat.m_fMat[0] - rMat.m_fMat[4] - rMat.m_fMat[8] + 1.0f) * 2);
		m_fQuat[0] = 0.5f * fS;
		m_fQuat[1] = (rMat.m_fMat[1] + rMat.m_fMat[3]) * fS;
		m_fQuat[2] = (rMat.m_fMat[2] + rMat.m_fMat[6]) * fS;
		m_fQuat[3] = (rMat.m_fMat[5] + rMat.m_fMat[7]) * fS;
	}
	//if the second.....
	else if(rMat.m_fMat[4] > rMat.m_fMat[8])
	{
		float fS = 1 / (sqrt(rMat.m_fMat[4] - rMat.m_fMat[0] - rMat.m_fMat[8] + 1.0f) * 2);
		m_fQuat[0] = (rMat.m_fMat[1] + rMat.m_fMat[3]) * fS;
		m_fQuat[1] = 0.5f * fS;
		m_fQuat[2] = (rMat.m_fMat[5] + rMat.m_fMat[7]) * fS;
		m_fQuat[3] = (rMat.m_fMat[2] + rMat.m_fMat[6]) * fS;
	}
	//if the last element is the largest
	else
	{
		float fS = 1 / (sqrt(rMat.m_fMat[8] - rMat.m_fMat[0] - rMat.m_fMat[4] + 1.0f) * 2);
		m_fQuat[0] = (rMat.m_fMat[2] + rMat.m_fMat[6]) * fS;
		m_fQuat[1] = (rMat.m_fMat[5] + rMat.m_fMat[7]) * fS;
		m_fQuat[2] = 0.5f * fS;
		m_fQuat[3] = (rMat.m_fMat[1] + rMat.m_fMat[3]) * fS;
	}
}

inline void CQuaternion::FromMatrix(CMatrix4X4& rMat)
{
	float fTr = rMat.m_fMat[0] + rMat.m_fMat[5] + rMat.m_fMat[9] + 1.0f;
	//if the trace of the matrix is greater than 0, perform an instant calculation
	if(fTr > 0)
	{
		float fS = 0.5f / sqrtf(fTr);

		m_fQuat[3] = 0.25f / fS;
		m_fQuat[0] = (rMat.m_fMat[9] - rMat.m_fMat[6]) * fS;
		m_fQuat[1] = (rMat.m_fMat[2] - rMat.m_fMat[8]) * fS;
		m_fQuat[2] = (rMat.m_fMat[4] - rMat.m_fMat[1]) * fS;
	}
	//if the first diagonal element is the largest
	else if(rMat.m_fMat[0] > rMat.m_fMat[5] && rMat.m_fMat[0] > rMat.m_fMat[10])
	{
		float fS = 1 / (sqrt(rMat.m_fMat[0] - rMat.m_fMat[5] - rMat.m_fMat[10] + 1.0f) * 2);
		m_fQuat[0] = 0.5f * fS;
		m_fQuat[1] = (rMat.m_fMat[1] + rMat.m_fMat[4]) * fS;
		m_fQuat[2] = (rMat.m_fMat[2] + rMat.m_fMat[8]) * fS;
		m_fQuat[3] = (rMat.m_fMat[6] + rMat.m_fMat[9]) * fS;
	}
	//if the second.....
	else if(rMat.m_fMat[5] > rMat.m_fMat[10])
	{
		float fS = 1 / (sqrt(rMat.m_fMat[5] - rMat.m_fMat[0] - rMat.m_fMat[10] + 1.0f) * 2);
		m_fQuat[0] = (rMat.m_fMat[1] + rMat.m_fMat[4]) * fS;
		m_fQuat[1] = 0.5f * fS;
		m_fQuat[2] = (rMat.m_fMat[6] + rMat.m_fMat[9]) * fS;
		m_fQuat[3] = (rMat.m_fMat[2] + rMat.m_fMat[8]) * fS;
	}
	//if the last element is the largest
	else
	{
		float fS = 1 / (sqrt(rMat.m_fMat[10] - rMat.m_fMat[0] - rMat.m_fMat[5] + 1.0f) * 2);
		m_fQuat[0] = (rMat.m_fMat[2] + rMat.m_fMat[8]) * fS;
		m_fQuat[1] = (rMat.m_fMat[6] + rMat.m_fMat[9]) * fS;
		m_fQuat[2] = 0.5f * fS;
		m_fQuat[3] = (rMat.m_fMat[1] + rMat.m_fMat[4]) * fS;
	}
}

//-------------------------------------------------------------
//-                      OPERATORS                            -
//-------------------------------------------------------------
//-------------------------------------------------------------
//- operator[] 
//- Retrive a reference to one of the elements like an array
//-------------------------------------------------------------
inline float& CQuaternion::operator[] (const int iIdx)
{
	return m_fQuat[iIdx];
}

//-------------------------------------------------------------
//- operator+
//- adds a quaternion to the stored one, and returns the result
//-------------------------------------------------------------
inline const CQuaternion CQuaternion::operator +(const CQuaternion& rQuat) const
{
	return CQuaternion(m_fQuat[0] + rQuat.m_fQuat[0], m_fQuat[1] + rQuat.m_fQuat[1],
					   m_fQuat[2] + rQuat.m_fQuat[2], m_fQuat[3] + rQuat.m_fQuat[3]);
}

//-------------------------------------------------------------
//- operator-
//- Subtracts a quaternion from the internal one and returns the result
//-------------------------------------------------------------
inline const CQuaternion CQuaternion::operator -(const CQuaternion& rQuat) const
{
	return CQuaternion(m_fQuat[0] - rQuat.m_fQuat[0], m_fQuat[1] - rQuat.m_fQuat[1],
					   m_fQuat[2] - rQuat.m_fQuat[2], m_fQuat[3] - rQuat.m_fQuat[3]);

}

//-------------------------------------------------------------
//- operator*
//- Multiplies the quaternion by a scalar value, returns the result
//-------------------------------------------------------------
inline const CQuaternion CQuaternion::operator *(const float fScalar) const
{
	return CQuaternion(m_fQuat[0] * fScalar, m_fQuat[1] * fScalar, 
					   m_fQuat[2] * fScalar, m_fQuat[3] * fScalar);  
}	

//-------------------------------------------------------------
//- operator/
//- Divides the quaternion my a scalar, returns the result
//-------------------------------------------------------------
inline const CQuaternion CQuaternion::operator /(const float fScalar) const
{
	float fInvScl = 1/fScalar;
	return CQuaternion(m_fQuat[0] * fInvScl, m_fQuat[1] * fInvScl, 
					   m_fQuat[2] * fInvScl, m_fQuat[3] * fInvScl);  

}

//-------------------------------------------------------------
//- operator += 
//- Add a quaternion to the stored one
//-------------------------------------------------------------
inline const void CQuaternion::operator +=(const CQuaternion& rQuat)
{
	m_fQuat[0] += rQuat.m_fQuat[0];
	m_fQuat[1] += rQuat.m_fQuat[1];
	m_fQuat[2] += rQuat.m_fQuat[2];
	m_fQuat[3] += rQuat.m_fQuat[3];
}

//-------------------------------------------------------------
//- operator -=
//- Subtract a quaternion from the stored one
//-------------------------------------------------------------
inline const void CQuaternion::operator -=(const CQuaternion& rQuat)
{
	m_fQuat[0] -= rQuat.m_fQuat[0];
	m_fQuat[1] -= rQuat.m_fQuat[1];
	m_fQuat[2] -= rQuat.m_fQuat[2];
	m_fQuat[3] -= rQuat.m_fQuat[3];
}

//-------------------------------------------------------------
//- operator *=
//- Multiply the stored quaternion by a scalar
//-------------------------------------------------------------
inline const void CQuaternion::operator *=(const float fScalar)
{
	m_fQuat[0] *= fScalar;
	m_fQuat[1] *= fScalar;
	m_fQuat[2] *= fScalar;
	m_fQuat[3] *= fScalar;
}

//-------------------------------------------------------------
//- operator /=
//- Divide the stored quaternion by a scalar
//-------------------------------------------------------------
inline const void CQuaternion::operator /=(const float fScalar)
{
	float fInvScl = 1/fScalar;
	m_fQuat[0] *= fInvScl;
	m_fQuat[1] *= fInvScl;
	m_fQuat[2] *= fInvScl;
	m_fQuat[3] *= fInvScl;
}

//-------------------------------------------------------------
//- operator*
//- Multiply the quaternion by another, return the result
//-------------------------------------------------------------
inline const CQuaternion CQuaternion::operator *(const CQuaternion& rQuat) const
{
	CVector3 v1(m_fQuat[0], m_fQuat[1], m_fQuat[2]);
	CVector3 v2(rQuat.m_fQuat[0], rQuat.m_fQuat[1], rQuat.m_fQuat[2]);
	CVector3 vFinal = v1 * rQuat.m_fQuat[3] + v2 * m_fQuat[3] + CrossProduct(v1, v2);
	float fScalar = m_fQuat[3] * rQuat.m_fQuat[3] - DotProduct3(v1, v2);
	return CQuaternion(vFinal[0], vFinal[1], vFinal[2], fScalar);
}

//-------------------------------------------------------------
//- operator *=
//- Multiply the stored quaterniopn by another
//-------------------------------------------------------------
inline const void CQuaternion::operator *=(const CQuaternion& rQuat)
{
	float res[4];
	res[0] = m_fQuat[3] * rQuat.m_fQuat[0] + m_fQuat[0] * rQuat.m_fQuat[3] +
						m_fQuat[1] * rQuat.m_fQuat[2] - m_fQuat[2] * rQuat.m_fQuat[1];

	res[1] = m_fQuat[3] * rQuat.m_fQuat[1] + m_fQuat[1] * rQuat.m_fQuat[3] +
						m_fQuat[2] * rQuat.m_fQuat[0] - m_fQuat[0] * rQuat.m_fQuat[2];

	res[2] = m_fQuat[3] * rQuat.m_fQuat[2] + m_fQuat[2] * rQuat.m_fQuat[3] +
						m_fQuat[0] * rQuat.m_fQuat[1] - m_fQuat[1] * rQuat.m_fQuat[0];

	res[3] = m_fQuat[3] * rQuat.m_fQuat[3] - m_fQuat[0] * rQuat.m_fQuat[0] -
						m_fQuat[1] * rQuat.m_fQuat[1] - m_fQuat[2] * rQuat.m_fQuat[2];
	memcpy(m_fQuat, res, sizeof(float[4]));
}

//-------------------------------------------------------------
//- operator*
//- Multiply the quaternion by a vector, return the result
//-------------------------------------------------------------
inline const CQuaternion CQuaternion::operator *(const CVector3& rVec) const
{
	return CQuaternion(-(m_fQuat[0] * rVec.m_fVec[0] + m_fQuat[1] * rVec.m_fVec[1] + m_fQuat[2] * rVec.m_fVec[2]), 
                         m_fQuat[3] * rVec.m_fVec[0] + m_fQuat[1] * rVec.m_fVec[2] + m_fQuat[2] * rVec.m_fVec[1], 
                         m_fQuat[3] * rVec.m_fVec[1] + m_fQuat[2] * rVec.m_fVec[0] + m_fQuat[0] * rVec.m_fVec[2],
                         m_fQuat[3] * rVec.m_fVec[2] + m_fQuat[0] * rVec.m_fVec[1] + m_fQuat[1] * rVec.m_fVec[0] ); 
}

//-------------------------------------------------------------
//- operator *=
//- Multiply the stored quaterniopn by another
//-------------------------------------------------------------
inline const void CQuaternion::operator *=(const CVector3& rVec)
{
	CQuaternion tmp(m_fQuat[3] * rVec.m_fVec[0] + 
						m_fQuat[1] * rVec.m_fVec[2] - m_fQuat[2] * rVec.m_fVec[1],

					   m_fQuat[3] * rVec.m_fVec[1] + 
						m_fQuat[2] * rVec.m_fVec[0] - m_fQuat[0] * rVec.m_fVec[2],
						
					   m_fQuat[3] * rVec.m_fVec[2] + 
						m_fQuat[0] * rVec.m_fVec[1] - m_fQuat[1] * rVec.m_fVec[0],
					   
					   -(m_fQuat[0] * rVec.m_fVec[0] + 
						m_fQuat[1] * rVec.m_fVec[1] + m_fQuat[2] * rVec.m_fVec[2]));
	*this = tmp;
}

//-------------------------------------------------------------
//- operator -
//- Return a negated quaternion
//-------------------------------------------------------------
inline const CQuaternion CQuaternion::operator -(void) const
{
	return CQuaternion(-m_fQuat[0], -m_fQuat[1], -m_fQuat[2], -m_fQuat[3]);
}

//-------------------------------------------------------------
//- operator ~
//- Return the conjugate of the quaternion
//-------------------------------------------------------------
inline const CQuaternion CQuaternion::operator ~(void) const
{
	return CQuaternion(-m_fQuat[0], -m_fQuat[1], -m_fQuat[2], m_fQuat[3]);
}

//-------------------------------------------------------------
//- operator= 
//- Set the stored quaternion equal to another
//-------------------------------------------------------------
inline const void CQuaternion::operator = (const CQuaternion& rQuat)
{
	memcpy(m_fQuat, rQuat.m_fQuat, sizeof(float[4]));
}

//-------------------------------------------------------------
//- operator == 
//- Check for equality
//-------------------------------------------------------------
inline const bool CQuaternion::operator ==(const CQuaternion& rQuat) const
{
	return (m_fQuat[0] == rQuat.m_fQuat[0] && m_fQuat[1] == rQuat.m_fQuat[1] &&
		    m_fQuat[2] == rQuat.m_fQuat[2] && m_fQuat[3] == rQuat.m_fQuat[3]);
}

//-------------------------------------------------------------
//- operator != 
//- Check for inequality
//-------------------------------------------------------------
inline const bool CQuaternion::operator !=(const CQuaternion& rQuat) const
{
	return !(*this == rQuat);
}

//-------------------------------------------------------------
//- Accessors
//- Get - Returns a pointer to the whole array
//- GetVector - Returns a CVector3 of the vector component
//- GetScalar - Returns a scalar
//- Set - Takes an array of for floats
//- Set - Takes four separate floats
//-------------------------------------------------------------
inline float * CQuaternion::Get()
{
	return m_fQuat;
}

inline CVector3 CQuaternion::GetVector()
{
	return CVector3(m_fQuat);
}

inline float CQuaternion::GetScalar()
{
	return m_fQuat[3];
}

inline void CQuaternion::Set(float * fpQuat)
{
	memcpy(m_fQuat, fpQuat, sizeof(float[4]));
}

inline void CQuaternion::Set(float fX, float fY, float fZ, float fW)
{
	m_fQuat[0] = fX;
	m_fQuat[1] = fY;
	m_fQuat[2] = fZ;
	m_fQuat[3] = fW;
}

//-------------------------------------------------------------
//- Constructors
//- 1. Default Constructor
//- 2. Pointer to array with quat values
//- 3. 4 floating point values XYZW
//- 4. Copy constructor
//-------------------------------------------------------------
inline CQuaternion::CQuaternion()
{
	memset(m_fQuat, 0, sizeof(float[4]));
	m_fQuat[3] = 1.0f;
}

inline CQuaternion::CQuaternion(float * fpQuat)
{
	memcpy(m_fQuat, fpQuat, sizeof(float[4]));
}

inline CQuaternion::CQuaternion(float fX, float fY, float fZ, float fW)
{
	m_fQuat[0] = fX;
	m_fQuat[1] = fY;
	m_fQuat[2] = fZ;
	m_fQuat[3] = fW;
}

inline CQuaternion::CQuaternion(const CQuaternion& rQuat)
{
	*this = rQuat;
}

inline void CQuaternion::Inverse()
{
	m_fQuat[0] = -m_fQuat[0];
	m_fQuat[1] = -m_fQuat[1];
	m_fQuat[2] = -m_fQuat[2];
	Normalize();
}

inline bool CQuaternion::IsZero()
{
	return m_fQuat[0] == 0 && m_fQuat[1] == 0 && m_fQuat[2] == 0 && m_fQuat[3] == 1;
}

//-------------------------------------------------------------
//-                  END CQuaternion                          -
//-------------------------------------------------------------

#endif //QUATERNION_INL
