/*************************************************************/
/*                         VECTOR.INL                        */
/*                                                           */
/* Purpose: Inlined functions for CVector2 and CVector3      */
/*      Evan Pipho (May 27, 2002)                            */
/*                                                           */
/*************************************************************/
//#ifndef VECTOR_INL
//#define VECTOR_INL

//-------------------------------------------------------------
//                        INCLUES                             -
//-------------------------------------------------------------
//#include <math.h>
//#include "vector.h"
//#include "matrix.h"
//#include "quaternion.h"

#include "math1.h"

//-------------------------------------------------------------
//                       CVector2                             -
//-------------------------------------------------------------
//-------------------------------------------------------------
//-                      FUNCTIONS                            -
//-------------------------------------------------------------
//-------------------------------------------------------------
//- Magnitude
//- Returns the magnitude of the vector
//-------------------------------------------------------------
inline float CVector2::Magnitude() const
{
	return (sqrtf( (m_fVec[0] * m_fVec[0]) + (m_fVec[1] * m_fVec[1]) ));
}

//-------------------------------------------------------------
//- Normalize
//- Scales the vector so it has a magnitude of one
//-------------------------------------------------------------
inline void CVector2::Normalize()
{
	float fInvMag = 1/Magnitude();
	m_fVec[0] *= fInvMag;
	m_fVec[1] *= fInvMag;
}

//-------------------------------------------------------------
//- Negate
//- Negates all vector components, equivalent to v *= -1
//-------------------------------------------------------------
inline void CVector2::Negate()
{
	m_fVec[0] = -m_fVec[0];
	m_fVec[1] = -m_fVec[1];
}

//-------------------------------------------------------------
//- Dot
//- Calculate the dot product of a vector and the stored vector
//-------------------------------------------------------------
inline float CVector2::Dot(const CVector2& vec) const
{
	return( (vec.m_fVec[0] * m_fVec[0]) + (vec.m_fVec[1] * m_fVec[1]) );
}


//-------------------------------------------------------------
//-                   OPERATORS                               -
//-------------------------------------------------------------
//-------------------------------------------------------------
//- operator[]
//- Returns a vector element, use as with an array
//-------------------------------------------------------------
inline float& CVector2::operator [](const int iIdx)
{
	return m_fVec[iIdx];
}

//-------------------------------------------------------------
//- operator+
//- Add vectors together (vectorC = vectorA + vectorB
//-------------------------------------------------------------
inline const CVector2 CVector2::operator +(const CVector2& vec) const
{
	return( CVector2(
		( vec.m_fVec[0] + m_fVec[0] ),
		( vec.m_fVec[1] + m_fVec[1] )));
}

//-------------------------------------------------------------
//- operator -
//- Subtracts two vectors (vectorC = vectorA - vectorB)
//-------------------------------------------------------------
inline const CVector2 CVector2::operator -(const CVector2& vec) const
{
	return( CVector2(
		( m_fVec[0] - vec.m_fVec[0] ),
		( m_fVec[1] - vec.m_fVec[1] )));
}

//-------------------------------------------------------------
//- operator += 
//- Add a vector to the stored vector
//-------------------------------------------------------------
inline const void CVector2::operator += (const CVector2& vec)
{
	m_fVec[0] += vec.m_fVec[0];
	m_fVec[1] += vec.m_fVec[1];
}

//-------------------------------------------------------------
//- operator -=
//- Subtract a vector from the stored vector
//-------------------------------------------------------------
inline const void CVector2::operator -= (const CVector2& vec)
{
	m_fVec[0] -= vec.m_fVec[0];
	m_fVec[1] -= vec.m_fVec[1];
}

//-------------------------------------------------------------
//- operator -
//- Negate the stored vector
//-------------------------------------------------------------
inline const CVector2 CVector2::operator -()
{
	return( CVector2(-m_fVec[0], -m_fVec[1]) );
}

//-------------------------------------------------------------
//- operator *
//- Multiply a vector by a scalar
//-------------------------------------------------------------
inline const CVector2 CVector2::operator *(const float fScalar) const
{
	return( CVector2(
		( m_fVec[0] * fScalar ),
		( m_fVec[1] * fScalar )));
}

//-------------------------------------------------------------
//- operator /
//- Divide a vector by a scalar
//-------------------------------------------------------------
inline const CVector2 CVector2::operator /(const float fScalar) const
{
	float fInvScale = 1/fScalar;

	return( CVector2(
		( m_fVec[0] * fInvScale ),
		( m_fVec[1] * fInvScale )));
}

//-------------------------------------------------------------
//- operator *=
//- Multiply the stored vector by a scalar
//-------------------------------------------------------------
inline const void CVector2::operator *=(const float fScalar)
{
	m_fVec[0] *= fScalar;
	m_fVec[1] *= fScalar;
}

//-------------------------------------------------------------
//- operator *=
//- Divide the stored vector by a scalar
//-------------------------------------------------------------
inline const void CVector2::operator /=(const float fScalar)
{
	float fInvScale = 1/fScalar;

	m_fVec[0] *= fInvScale;
	m_fVec[1] *= fInvScale;
}

//-------------------------------------------------------------
//- operator =
//- Set the stored vector equal to another
//-------------------------------------------------------------
inline const void CVector2::operator =(const CVector2& vec)
{
	m_fVec[0] = vec.m_fVec[0];
	m_fVec[1] = vec.m_fVec[1];
}

//-------------------------------------------------------------
//- operator ==
//- Determines if a vector is equal to another
//-------------------------------------------------------------
inline const bool CVector2::operator ==(const CVector2& vec) const
{
	return ( 
		(m_fVec[0] == vec.m_fVec[0]) &&
		(m_fVec[1] == vec.m_fVec[1]));
}

//-------------------------------------------------------------
//- operator !=
//- Determines if two vectors are NOT equal to each other
//-------------------------------------------------------------
inline const bool CVector2::operator !=(const CVector2& vec) const
{
	return (!(*this == vec));
}

//-------------------------------------------------------------
//- Constuctors
//- 1. Default Constructor (no parameters, initializes to 0)
//- 2. Copy Constructor (takes another CVector2 and copies it)
//- 3. 2 floats (takes 2 floats one for X and one for Y components)
//- 4. float array (takes a pointer to an array of floats and uses
//     the first and second element for the X and Y components)
//-------------------------------------------------------------
inline CVector2::CVector2()
{
	m_fVec[0] = 0;
	m_fVec[1] = 0;
}

inline CVector2::CVector2(const CVector2& vec)
{
	m_fVec[0] = vec.m_fVec[0];
	m_fVec[1] = vec.m_fVec[1];
}

inline CVector2::CVector2(float fX, float fY)
{
	m_fVec[0] = fX;
	m_fVec[1] = fY;
}

inline CVector2::CVector2(float * vec)
{
	m_fVec[0] = vec[0];
	m_fVec[1] = vec[1];
}

//-------------------------------------------------------------
//- Accessors
//- 1. Get (returns a pointer to the array)
//- 2. Set (takes 2 floats, one for each component)
//- 3. Set (takes a pointer to a float array, the first two 
//     elements are used)
//-------------------------------------------------------------
inline float * CVector2::Get()
{
	return m_fVec;
}

inline void CVector2::Set(float fX, float fY)
{
	m_fVec[0] = fX;
	m_fVec[1] = fY;
}

inline void CVector2::Set(float * vec)
{
	m_fVec[0] = vec[0];
	m_fVec[1] = vec[1];
}

//-------------------------------------------------------------
//                     END CVector2                           -
//-------------------------------------------------------------


//-------------------------------------------------------------
//                       CVector3                             -
//-------------------------------------------------------------
//-------------------------------------------------------------
//-                      FUNCTIONS                            -
//-------------------------------------------------------------
//-------------------------------------------------------------
//- Magnitude
//- Returns the magnitude of the vector
//-------------------------------------------------------------
inline float CVector3::Magnitude() const
{
	return (sqrtf( (m_fVec[0] * m_fVec[0]) + (m_fVec[1] * m_fVec[1]) + (m_fVec[2] * m_fVec[2])));
}

//-------------------------------------------------------------
//- Normalize
//- Scales the vector so it has a magnitude of one
//-------------------------------------------------------------
inline void CVector3::Normalize()
{
	float fInvMag = 1/Magnitude();
	m_fVec[0] *= fInvMag;
	m_fVec[1] *= fInvMag;
	m_fVec[2] *= fInvMag;
}

//-------------------------------------------------------------
//- Negate
//- Negates all vector components, equivalent to v *= -1
//-------------------------------------------------------------
inline void CVector3::Negate()
{
	m_fVec[0] = -m_fVec[0];
	m_fVec[1] = -m_fVec[1];
	m_fVec[2] = -m_fVec[2];
}

//-------------------------------------------------------------
//- Dot
//- Calculate the dot product of a vector and the stored vector
//-------------------------------------------------------------
inline float CVector3::Dot(const CVector3& vec) const
{
	return( (vec.m_fVec[0] * m_fVec[0]) + (vec.m_fVec[1] * m_fVec[1]) + (vec.m_fVec[2] * m_fVec[2]) );
}

//-------------------------------------------------------------
//- Cross
//- Calculates the cross product of a vector and the stored vector
//-------------------------------------------------------------
inline CVector3 CVector3::Cross(const CVector3& vec) const
{
		return ( CVector3(
			(m_fVec[1] * vec.m_fVec[2] - 
	         m_fVec[2] * vec.m_fVec[1]),
            (m_fVec[2] * vec.m_fVec[0] - 
	         m_fVec[0] * vec.m_fVec[2]),
            (m_fVec[0] * vec.m_fVec[1] - 
	         m_fVec[1] * vec.m_fVec[0])));
}

//-------------------------------------------------------------
//- Transform3
//- Transforms the stored vector by a matrix, either 3x3 or 4x4
//- but uses only the 3x3 matrix if it is a 4x4
//-------------------------------------------------------------
inline void CVector3::Transform3(const CMatrix3X3& mat)
{
	CVector3 temp (
		(m_fVec[0] * mat.m_fMat[0] + 
	     m_fVec[1] * mat.m_fMat[3] + 
	     m_fVec[2] * mat.m_fMat[6]),
	
	    (m_fVec[0] * mat.m_fMat[1] +
	     m_fVec[1] * mat.m_fMat[4] + 
         m_fVec[2] * mat.m_fMat[7]),
	
	    (m_fVec[0] * mat.m_fMat[2] +
	     m_fVec[1] * mat.m_fMat[5] +
	     m_fVec[2] * mat.m_fMat[8]));
	
	*this = temp;
}

inline void CVector3::Transform3(const CMatrix4X4& mat)
{
	CVector3 temp( 
		(m_fVec[0] * mat.m_fMat[0] + 
	    m_fVec[1] * mat.m_fMat[4] +
	    m_fVec[2] * mat.m_fMat[8]),

		(m_fVec[0] * mat.m_fMat[1] + 
	    m_fVec[1] * mat.m_fMat[5] + 
	    m_fVec[2] * mat.m_fMat[9]),

	    (m_fVec[0] * mat.m_fMat[2] + 
	    m_fVec[1] * mat.m_fMat[6] + 
	    m_fVec[2] * mat.m_fMat[10]) );

	*this = temp;
}

//-------------------------------------------------------------
//- Transform4
//- Transforms the stored vector by a 4x4 matrix
//-------------------------------------------------------------
inline void CVector3::Transform4(const CMatrix4X4& mat)
{
	CVector3 temp( 
		(m_fVec[0] * mat.m_fMat[0] + 
	    m_fVec[1] * mat.m_fMat[4] +
	    m_fVec[2] * mat.m_fMat[8] + 
	    mat.m_fMat[12] ),

		(m_fVec[0] * mat.m_fMat[1] + 
	    m_fVec[1] * mat.m_fMat[5] + 
	    m_fVec[2] * mat.m_fMat[9] + 
	    mat.m_fMat[13] ),

	    (m_fVec[0] * mat.m_fMat[2] + 
	    m_fVec[1] * mat.m_fMat[6] + 
	    m_fVec[2] * mat.m_fMat[10]+
	    mat.m_fMat[14]) );

	*this = temp;
}

inline void CVector3::Transform4To(const CMatrix4X4& mat, float* dst)
{
	dst[0] = m_fVec[0] * mat.m_fMat[0] + m_fVec[1] * mat.m_fMat[4] +
			 m_fVec[2] * mat.m_fMat[8] + mat.m_fMat[12];
	dst[1] = m_fVec[0] * mat.m_fMat[1] + m_fVec[1] * mat.m_fMat[5] +
			 m_fVec[2] * mat.m_fMat[9] + mat.m_fMat[13];
    dst[2] = m_fVec[0] * mat.m_fMat[2] + m_fVec[1] * mat.m_fMat[6] +
    		 m_fVec[2] * mat.m_fMat[10]+ mat.m_fMat[14];
}

//-------------------------------------------------------------
//- TransformQ
//- Transform the vector by a rotation quaternion
//-------------------------------------------------------------
inline void CVector3::TransformQ(const CQuaternion& quat)
{
	CQuaternion qv(m_fVec[0], m_fVec[1], m_fVec[2], 0);
	CQuaternion qF = quat * qv * (~quat);
	*this = qF.GetVector();
}

//-------------------------------------------------------------
//-                   OPERATORS                               -
//-------------------------------------------------------------
//-------------------------------------------------------------
//- operator[]
//- Returns a vector element, use as with an array
//-------------------------------------------------------------
inline float& CVector3::operator [](const int iIdx)
{
	return m_fVec[iIdx];
}

inline float CVector3::operator[](const int iIdx) const
{
	return m_fVec[iIdx];
}

//-------------------------------------------------------------
//- operator+
//- Add vectors together (vectorC = vectorA + vectorB
//-------------------------------------------------------------
inline const CVector3 CVector3::operator +(const CVector3& vec) const
{
	return( CVector3(
		( vec.m_fVec[0] + m_fVec[0] ),
		( vec.m_fVec[1] + m_fVec[1] ),
		( vec.m_fVec[2] + m_fVec[2] )));
}

//-------------------------------------------------------------
//- operator -
//- Subtracts two vectors (vectorC = vectorA - vectorB)
//-------------------------------------------------------------
inline const CVector3 CVector3::operator -(const CVector3& vec) const
{
	return( CVector3(
		( m_fVec[0] - vec.m_fVec[0] ),
		( m_fVec[1] - vec.m_fVec[1] ),
		( m_fVec[2] - vec.m_fVec[2] )));
}

//-------------------------------------------------------------
//- operator += 
//- Add a vector to the stored vector
//-------------------------------------------------------------
inline const void CVector3::operator += (const CVector3& vec)
{
	m_fVec[0] += vec.m_fVec[0];
	m_fVec[1] += vec.m_fVec[1];
	m_fVec[2] += vec.m_fVec[2];
}

//-------------------------------------------------------------
//- operator -=
//- Subtract a vector from the stored vector
//-------------------------------------------------------------
inline const void CVector3::operator -= (const CVector3& vec)
{
	m_fVec[0] -= vec.m_fVec[0];
	m_fVec[1] -= vec.m_fVec[1];
	m_fVec[2] -= vec.m_fVec[2];
}

//-------------------------------------------------------------
//- operator -
//- Negate the stored vector
//-------------------------------------------------------------
inline const CVector3 CVector3::operator -()
{
	return( CVector3(-m_fVec[0], -m_fVec[1], -m_fVec[2]) );
}

//-------------------------------------------------------------
//- operator *
//- Multiply a vector by a scalar
//-------------------------------------------------------------
inline const CVector3 CVector3::operator *(const float fScalar) const
{
	return( CVector3(
		( m_fVec[0] * fScalar ),
		( m_fVec[1] * fScalar ),
		( m_fVec[2] * fScalar )));
}

//-------------------------------------------------------------
//- operator /
//- Divide a vector by a scalar
//-------------------------------------------------------------
inline const CVector3 CVector3::operator /(const float fScalar) const
{
	float fInvScale = 1/fScalar;

	return( CVector3(
		( m_fVec[0] * fInvScale ),
		( m_fVec[1] * fInvScale ),
		( m_fVec[2] * fInvScale )));
}

//-------------------------------------------------------------
//- operator *=
//- Multiply the stored vector by a scalar
//-------------------------------------------------------------
inline const void CVector3::operator *=(const float fScalar)
{
	m_fVec[0] *= fScalar;
	m_fVec[1] *= fScalar;
	m_fVec[2] *= fScalar;
}

//-------------------------------------------------------------
//- operator *=
//- Divide the stored vector by a scalar
//-------------------------------------------------------------
inline const void CVector3::operator /=(const float fScalar)
{
	float fInvScale = 1/fScalar;

	m_fVec[0] *= fInvScale;
	m_fVec[1] *= fInvScale;
	m_fVec[2] *= fInvScale;
}

//-------------------------------------------------------------
//- operator =
//- Set the stored vector equal to another
//-------------------------------------------------------------
inline const void CVector3::operator =(const CVector3& vec)
{
	m_fVec[0] = vec.m_fVec[0];
	m_fVec[1] = vec.m_fVec[1];
	m_fVec[2] = vec.m_fVec[2];
}

//-------------------------------------------------------------
//- operator ==
//- Determines if a vector is equal to another
//-------------------------------------------------------------
inline const bool CVector3::operator ==(const CVector3& vec) const
{
	return ( 
		(m_fVec[0] == vec.m_fVec[0]) &&
		(m_fVec[1] == vec.m_fVec[1]) &&
		(m_fVec[2] == vec.m_fVec[2]));
}

//-------------------------------------------------------------
//- operator !=
//- Determines if two vectors are NOT equal to each other
//-------------------------------------------------------------
inline const bool CVector3::operator !=(const CVector3& vec) const
{
	return (!(*this == vec));
}

//-------------------------------------------------------------
//- Constuctors
//- 1. Default Constructor (no parameters, initializes to 0)
//- 2. Copy Constructor (takes another CVector2 and copies it)
//- 3. 3 floats (takes 3 floats one for X one for Y and one for Z components)
//- 4. float array (takes a pointer to an array of floats and uses
//     the first - thired element for the X, Y and Z components)
//-------------------------------------------------------------
inline CVector3::CVector3()
{
	m_fVec[0] = 0;
	m_fVec[1] = 0;
	m_fVec[2] = 0;
}

inline CVector3::CVector3(const CVector3& vec)
{
	m_fVec[0] = vec.m_fVec[0];
	m_fVec[1] = vec.m_fVec[1];
	m_fVec[2] = vec.m_fVec[2];
}

inline CVector3::CVector3(float fX, float fY, float fZ)
{
	m_fVec[0] = fX;
	m_fVec[1] = fY;
	m_fVec[2] = fZ;
}

inline CVector3::CVector3(float * vec)
{
	m_fVec[0] = vec[0];
	m_fVec[1] = vec[1];
	m_fVec[2] = vec[2];
}

//-------------------------------------------------------------
//- Accessors
//- 1. Get (returns a pointer to the array)
//- 2. Set (takes 3 floats, one for each component)
//- 3. Set (takes a pointer to a float array, the first three 
//     elements are used)
//-------------------------------------------------------------
inline float * CVector3::Get()
{
	return m_fVec;
}

inline void CVector3::Set(float fX, float fY, float fZ)
{
	m_fVec[0] = fX;
	m_fVec[1] = fY;
	m_fVec[2] = fZ;
}

inline void CVector3::Set(float * vec)
{
	m_fVec[0] = vec[0];
	m_fVec[1] = vec[1];
	m_fVec[2] = vec[2];
}

inline bool CVector3::IsZero() const
{
	return m_fVec[0] == 0 && m_fVec[1] == 0 && m_fVec[2] == 0;
}

//-------------------------------------------------------------
//                     END CVector3                           -
//-------------------------------------------------------------

//#endif //VECTOR_INL
