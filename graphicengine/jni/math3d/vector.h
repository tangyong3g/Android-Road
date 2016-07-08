/*************************************************************/
/*                          VECTOR.H                         */
/*                                                           */
/* Purpose: Definitions for CVector class, both 2 and 3      */
/*          element types and related functions.             */
/*      Evan Pipho (May 27, 2002)                            */
/*                                                           */
/*************************************************************/
#ifndef VECTOR_H
#define VECTOR_H


class CQuaternion;
class CMatrix3X3;
class CMatrix4X4;

//-------------------------------------------------------------
//                        CVector2                            -
// author: Evan Pipho (terminate@gdnmail.net)                 -
// date  : May 27, 2002                                       -
//-------------------------------------------------------------
class CVector2
{
public:

	//calculate magnitude of the vector
	float Magnitude() const;
	//normalize the vector so it has a magnitude of 1
	void Normalize();
	//negate all the components of the vector
	void Negate();
	
	//calculate the dot product
	float Dot(const CVector2& vec) const;

	//Operators
	//Accessor
	float& operator[](const int iIdx);

	//add vector
	const CVector2 operator+ (const CVector2& vec) const;
	//subtract vector
	const CVector2 operator- (const CVector2& vec) const;
	//add vector to stored vector
	const void operator += (const CVector2& vec);
	//subtract vector from stored vector
	const void operator -= (const CVector2& vec);
	//negate the vector
	const CVector2 operator-();
	
	//Scalar multiplication
	const CVector2 operator *  (const float fScalar) const;
	//Scalar division
	const CVector2 operator /  (const float fScalar) const;
	//Multiply the stored vector by a scalar
	const void operator *= (const float fScalar);
	//Divide the stored vector by a scalar
	const void operator /= (const float fScalar);

	//Set the stored vector equal to another
	const void operator =  (const CVector2& vec);
	//See if the stored vector and another are equal
	const bool	  operator == (const CVector2& vec)  const;
	//See if the stored vector and another are not equal
	const bool	  operator != (const CVector2& vec)  const;

	//constructors
	CVector2();
	CVector2(const CVector2& vecCopy);
	CVector2(float * fpVec);
	CVector2(float fX, float fY);

	//accessors
	float * Get();
	void Set(float fX, float fY);
	void Set(float * fpVec);

private:

	//vector elements
	float m_fVec[2];

};

//-------------------------------------------------------------
//                        CVector3                            -
// author: Evan Pipho (terminate@gdnmail.net)                 -
// date  : May 27, 2002                                       -
//-------------------------------------------------------------
class CVector3
{
	friend class CQuaternion;

public:

	//calculate magnitude of the vector
	float Magnitude() const;
	//normalize the vector so it has a magnitude of 1
	void Normalize();
	//negate all the components of the vector
	void Negate();
	
	//calculate the dot product
	float Dot(const CVector3& vec) const;
	//calculate the cross product
	CVector3 Cross(const CVector3& vec) const;

	//transform the vector by a 3x3 matrix
	void Transform3(const CMatrix3X3& mat);
	//transform the vector by a 4x4 matrix, using only the firs 3x3
	void Transform3(const CMatrix4X4& mat);
	//transform the vector by a 4x4 matrix
	void Transform4(const CMatrix4X4& mat);
	//transform the vector by a 4x4 matrix
	void Transform4To(const CMatrix4X4& mat, float* dst);
	//Transform the vector by a Quaternion
	void TransformQ(const CQuaternion& quat);

	//Operators
	//Accessor
	float& operator[](const int iIdx);

	float operator[](const int iIdx) const;

	//add vector
	const CVector3 operator+ (const CVector3& vec) const;
	//subtract vector
	const CVector3 operator- (const CVector3& vec) const;
	//add vector to stored vector
	const void operator += (const CVector3& vec);
	//subtract vector from stored vector
	const void operator -= (const CVector3& vec);
	//negate the vector
	const CVector3 operator-();
	
	//Scalar multiplication
	const CVector3 operator *  (const float fScalar) const;
	//Scalar division
	const CVector3 operator /  (const float fScalar) const;
	//Multiply the stored vector by a scalar
	const void operator *= (const float fScalar);
	//Divide the stored vector by a scalar
	const void operator /= (const float fScalar);

	//Set the stored vector equal to another
	const void operator =  (const CVector3& vec);
	//See if the stored vector and another are equal
	const bool	  operator == (const CVector3& vec)  const;
	//See if the stored vector and another are not equal
	const bool	  operator != (const CVector3& vec)  const;

	//Multiply the vector by a quaternion (rotate it)
	const CVector3 operator * (const CQuaternion& quat) const;

	//constructors
	CVector3();
	CVector3(const CVector3& vecCopy);
	CVector3(float * fpVec);
	CVector3(float fX, float fY, float fZ);

	//accessors
	float * Get();
	void Set(float fX, float fY, float fZ);
	void Set(float * fpVec);

	bool IsZero() const;

private:

	//vector elements
	float m_fVec[3];

};

//-------------------------------------------------------------
//                      FUNCTIONS                             -
//-------------------------------------------------------------
//-------------------------------------------------------------
//- DotProduct
//- Returns the dot product of two vectors
//-------------------------------------------------------------
inline float DotProduct2(CVector2& vec0, CVector2& vec1)
{
	return (
		(vec0[0] * vec1[0]) +
		(vec0[1] * vec1[1]));
}

inline float DotProduct3(const CVector3& vec0, const CVector3& vec1)
{
	return (
		(vec0[0] * vec1[0])+
		(vec0[1] * vec1[1])+
		(vec0[2] * vec1[2]));
}

//-------------------------------------------------------------
//- CrossProduct
//- Returns the dot product of two vectors
//-------------------------------------------------------------
inline CVector3 CrossProduct(CVector3& vec0, CVector3& vec1)
{
		return ( CVector3(
			(vec0[1] * vec1[2] - 
	         vec0[2] * vec1[1]),
            (vec0[2] * vec1[0] - 
	         vec0[0] * vec1[2]),
            (vec0[0] * vec1[1] - 
	         vec0[1] * vec1[0])));
}

inline CVector3 Lerp(CVector3& vec0, CVector3& vec1, float fInterp)
{
	return CVector3(
			vec0[0] + (vec1[0] - vec0[0]) * fInterp,
			vec0[1] + (vec1[1] - vec0[1]) * fInterp,
			vec0[2] + (vec1[2] - vec0[2]) * fInterp);
}

inline void AccumulateVertex(CVector3& vec0, CVector3& vec1, float factor)
{
	vec0[0] += vec1[0] * factor;
	vec0[1] += vec1[1] * factor;
	vec0[2] += vec1[2] * factor;
}

//inline functions
//#include "vector.inl"

#endif //VECTOR_H
