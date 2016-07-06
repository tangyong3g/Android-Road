/*************************************************************/
/*                         QUATERNION.H                      */
/*                                                           */
/* Purpose: Definitions for CQuaternion class and related    */
/*          functions                                        */
/*      Evan Pipho (May 30, 2002)                            */
/*                                                           */
/*************************************************************/
#ifndef QUATERNION_H
#define QUATERNION_H

#include <math.h>

class CVector3;
class CMatrix3X3;
class CMatrix4X4;

//-------------------------------------------------------------
//                       CQUATERNION                          -
// author: Evan Pipho (terminate@gdnmail.net)                 -
// date  : May 30, 2002                                       -
//-------------------------------------------------------------
class CQuaternion
{
	friend class CVector3;
	friend class CMatrix3X3;
	friend class CMatrix4X4;

public:
	
	//Functions
	//Calculate the magnitude
	float Magnitude() const;
	//Normalize the quaternion
	void Normalize();

	//Rotate the quaternion by another
	void Rotate(const CQuaternion& rQuat);

	//Retrieve the axis and angle of rotation
	float GetAngle() const;
	CVector3 GetAxis();
	//Create a quaternion from an axis and angle
	void FromAxisAngle(CVector3& rAxis, float fAngle);

	//Build a quat from euler angles
	void FromEulers(float * fpAngles);
	void FromEulers(float fX, float fY, float fZ);
	//Retrieve Eulers 
	CVector3 GetEulers() const;

	//Build a 3x3 or 4x4 matrix from the quaternion
	void ToMatrix(CMatrix3X3& rDstMat) const;
	CMatrix4X4 ToMatrix4() const;
	//Create a quaternion from the matrix
	void FromMatrix(CMatrix3X3& rMat);
	void FromMatrix(CMatrix4X4& rMat);

	//operators
	//Accessor
	float& operator[](const int iIdx);
	//Add two quaternions, return the result
	const CQuaternion operator+(const CQuaternion& rQuat) const;
	//Subtract two quaternions, return the result
	const CQuaternion operator-(const CQuaternion& rQuar) const;
	//Multiply the quaternion by a scalar, return the result
	const CQuaternion operator*(const float fScalar) const;
	//Divide a quaternion by a scalar, return the result
	const CQuaternion operator/(const float fScalar) const;

	//Add a quaternion to the stored quat
	const void operator+=(const CQuaternion& rQuat);
	//Subtract a quaternion from the stored quat
	const void operator-=(const CQuaternion& rQuat);
	//Multiply the stored quat by a scalar
	const void operator*=(const float fScalar);
	//Divide the stored quat by a scalar
	const void operator/=(const float fScalar);

	//Multiply quaternions, return the result
	const CQuaternion operator*(const CQuaternion& rQuat) const;
	//Multiply the stored quat by anotehr, store the result
	const void operator*=(const CQuaternion& rQuat);

	//Multiply a quaternion by a vector, return the result
	const CQuaternion operator*(const CVector3& rVec) const;
	//Multiply the quaternion by a vector, store the result
	const void operator*=(const CVector3& rVec);

	//Negate the quaternion
	const CQuaternion operator-(void) const;
	//Return the conjugate of the quaternion
	const CQuaternion operator~(void) const;

	//Set the stored quaternion equal to another
	const void operator=(const CQuaternion& rQuat);
	//Check for equality
	const bool operator==(const CQuaternion& rQuat) const;
	//Check for inequality
	const bool operator!=(const CQuaternion& rQuat) const;

	//Accessors
	float * Get();
	CVector3 GetVector();
	float GetScalar();
	void Set(float * fpQuat);
	void Set(float fX, float fY, float fZ, float fW);

	//Constructors
	CQuaternion();
	CQuaternion(float * fpQuat);
	CQuaternion(float fX, float fY, float fZ, float fW);
	CQuaternion(const CQuaternion& rQuat);
	
	void Inverse();
	bool IsZero();

private:

	//Quaternion Data stored in the order <x,y,z> w
	float m_fQuat[4];
};

//Functions
CQuaternion LERP(CQuaternion& rQuat0, CQuaternion& rQuat1, float fInterp);
//-------------------------------------------------------------
//- SLERP
//- Spherical Linear Interpolation between two Quaternions
//-------------------------------------------------------------
inline CQuaternion SLERP(CQuaternion& rQuat0, CQuaternion& rQuat1, float fInterp)
{
	float * q0 = rQuat0.Get();
	float * q1 = rQuat1.Get();

	//Calculate the dot product
	float fDot = q0[0] * q1[0] + q0[1] * q1[1] + q0[2] * q1[2] + q0[3] * q1[3];

	if(fDot < 0.0f)
	{
		rQuat1 = -rQuat1;
		fDot = -fDot;
	}


	if(fDot < 1.00001f && fDot > 0.99999f)
	{
		return LERP(rQuat0, rQuat1, fInterp);
	}

	//calculate the angle between the quaternions 
	float fTheta = acosf(fDot);

	return (rQuat0 * sinf(fTheta * (1 - fInterp)) + rQuat1 * sinf(fTheta * fInterp))/sinf(fTheta);
}

//-------------------------------------------------------------
//- LERP
//- Linear Interpolation between two Quaternions
//-------------------------------------------------------------
inline CQuaternion LERP(CQuaternion& rQuat0, CQuaternion& rQuat1, float fInterp)
{
	CQuaternion ret(((rQuat1 - rQuat0) * fInterp) + rQuat0);
	ret.Normalize();
	return ret;
}

//Inlined functions
//#include "quaternion.inl"

#endif //QUATERNION_H
