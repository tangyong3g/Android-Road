/*************************************************************/
/*                          MATRIX.H                         */
/*                                                           */
/* Purpose: Definitions for CMatrix classes, both 3X3 and 4X4*/
/*          matrices.                                        */
/*      Evan Pipho (May 30, 2002)                            */
/*                                                           */
/*************************************************************/
#ifndef MATRIX_H
#define MATRIX_H

class CVector3;
class CQuaternion;

//-------------------------------------------------------------
//                        CMatrix3X3                          -
// author: Evan Pipho (terminate@gdnmail.net)                 -
// date  : May 30, 2002                                       -
//-------------------------------------------------------------
class CMatrix3X3
{
	friend class CVector3;
	friend class CQuaternion;

public:

	//functions
	//Zero the matrix
	void Zero();
	//Set the matrix to be an identity matrix
	void Identity();
	//Transpose the matrix
	void Transpose();
	//calculate the inverse
	void Invert();
	//Set the rotations (or inverse rotations)
	void SetRotation(float * fpRot);
	void SetRotation(float fX, float fY, float fZ);
	void SetInvRotation(float * fpInvRot);
	void SetInvRotation(float fX, float fY, float fZ);

	//Set scale ratio,use in x-y plane
	void SetScale2D(float scaleX, float scaleY);
	void SetScale2D(float scaleX, float scaleY, float pivotX, float pivotY);
	//translation in x-y plane
	void SetTranslation2D(float translateX, float translateY);
	//rotation about z-axis
	void SetRotation2D(float angleZ);
	void SetRotation2D(float angleZ, float pivotX, float pivotY);

	void FromAxisAngle(CVector3& vec, float fAngle);
	//Create a rotation matrix from a quaternion
	void FromQuaternion(const CQuaternion& rQuat);
	//Find the determinant of the stored matrix
	float Determinant();
	//Rotate a vector using the inverse
	void InverseRotateVec(float * m_fVec);

	//operators
	//accessor
	float& operator [] (const int iIdx);
	//Add a matrix to the stored matrix and return the result
	const CMatrix3X3 operator + (const CMatrix3X3& rMat) const;
	//Subtract a matrix from the stored matrix and return the result
	const CMatrix3X3 operator - (const CMatrix3X3& rMat) const;
	//Add a matrix to the stored matrix
	const void operator += (const CMatrix3X3& rMat);
	//Subtract a matrix from the stored matrix
	const void operator -= (const CMatrix3X3& rMat);

	//Multiply the stored matrix by a scalar, return the result
	const CMatrix3X3 operator * (const float fScalar) const;
	//Divide the stored matrix by a scalar, return the result
	const CMatrix3X3 operator / (const float fScalar) const;
	//Multiply the stored matrix by a scalr
	const void operator *= (const float fScalar);
	//Divide the stored matrix by a scalar
	const void operator /= (const float fScalar);
	
	//Multiply the stored matrix by another matrix and return the result
	const CMatrix3X3 operator * (const CMatrix3X3& rMat) const;
	//Multiply the stored matrix by another matrix
	const void operator *= (const CMatrix3X3& rMat);

	//Set the stored matrix equal to another
	const void operator = (const CMatrix3X3& rMat);
	//Check if the stored matrix is equal to another
	const bool operator == (const CMatrix3X3& rMat) const;
	//Check if the stored matrix is not equal to another
	const bool operator != (const CMatrix3X3& rMat) const;

	//constructors
	CMatrix3X3();
	CMatrix3X3(bool identity);
	CMatrix3X3(const CMatrix3X3& rMat);
	CMatrix3X3(float f11, float f12, float f13,
		       float f21, float f22, float f23,
			   float f31, float f32, float f33);
	CMatrix3X3(float * fpMat);

	//accessors
	float * Get();
	void Set(float f11, float f12, float f13,
		     float f21, float f22, float f23,
			 float f31, float f32, float f33);
	void Set(float * fpMat);

private:

	//Matrix data
	float m_fMat[9];
};


//-------------------------------------------------------------
//                        CMatrix4X4                          -
// author: Evan Pipho (terminate@gdnmail.net)                 -
// date  : May 30, 2002                                       -
//-------------------------------------------------------------
class CMatrix4X4
{
	friend class CVector3;
	friend class CQuaternion;

public:

	//Functions
	//Make the stored matrix an Identity matrix
	void Identity();
	//Make the stored matrix a zero matrix
	void Zero();
	//Set the rotations (or inverse rotations)
	void SetRotation(float * fpRot);
	void SetRotation(float fX, float fY, float fZ);
	void SetInvRotation(float * fpInvRot);
	void SetInvRotation(float fX, float fY, float fZ);
	//Set translations (or inverse translations)
	void SetTranslation(float * fpTrans);
	void SetTranslation(float fX, float fY, float fZ);
	void SetInvTranslation(float * fpTrans);
	void SetInvTranslation(float fX, float fY, float fZ);
	void FromAxisAngle(CVector3& vec, float fAngle);
	//Create a rotation matrix from a quaternion
	void FromQuaternion(CQuaternion& quat);
	//Find the determinant of the stored matrix
	float Determinant();
	//Transpose the matrix so rows become columns and columns become rows
	void Transpose();
	//Find the inverse of the matrix
	void Invert();
	//Find the inverse of the matrix, assume the matrix is combined by T mat and R mat
	void InvertTranslationAndRotation();
	//Rotate a vector using the inverse
	void InverseRotateVec(float * m_fVec) const;
	//Translate a vector using the inverse matrix
	void InverseTranslateVec(float * m_fVec) const;
	//Translate and rotate a vector using the inverse matrix
	void InverseTranslateRotateVec(float *fpVec, float* dst) const;

	void GetTranslation(float* fpTrans);

	//Operators
	//Retrivea reference to a specific element
	float& operator[] (const int iIdx);
	//Add a matrix to the stored one, return the result
	const CMatrix4X4 operator+ (const CMatrix4X4& mat) const;
	//Subtract a matrix from the stored one, return the result
	const CMatrix4X4 operator- (const CMatrix4X4& mat) const;
	//Multiply the stored matrix by a scalar, return the result
	const CMatrix4X4 operator* (const float fScalar) const;
	//Divide the stored matrix by a scalar, return the result
	const CMatrix4X4 operator/ (const float fScalar) const;

	//Add a matrix to the stored one
	const void operator += (const CMatrix4X4& mat);
	//Subtract a matrix from the stored one
	const void operator -= (const CMatrix4X4& mat);
	//Multiply the stored matrix by a scalar
	const void operator *= (const float fScalar);
	//Divide the stored matrix by a scalar
	const void operator /= (const float fScalar);

	//Multiply a matrix by the stored matrix, return the results
	const CMatrix4X4 operator * (const CMatrix4X4& mat) const;
	//Multiply the stored matrix times another matrix
	const void operator *= (const CMatrix4X4& mat);

	//Set the stored matrix equal to another
	const void operator= (const CMatrix4X4& mat);
	//Check if a matrix is equal to the stored one
	const bool operator== (const CMatrix4X4& mat) const;
	//Check if a matrix is not equal to the intrenal one
	const bool operator!= (const CMatrix4X4& mat) const;

	//constructors
	CMatrix4X4();
	CMatrix4X4(bool identity);
	CMatrix4X4(const CMatrix4X4& mat);
	CMatrix4X4(float f11, float f12, float f13, float f14,
		       float f21, float f22, float f23, float f24,
			   float f31, float f32, float f33, float f34,
			   float f41, float f42, float f43, float f44);
	CMatrix4X4(float * fpMat);

	//accessors
	float * Get();
	void Set(float f11, float f12, float f13, float f14,
		       float f21, float f22, float f23, float f24,
			   float f31, float f32, float f33, float f34,
			   float f41, float f42, float f43, float f44);
	void Set(float * fpMat);

private:

	//Matrix data
	float m_fMat[16];
};

//Inline functions
//#include "matrix.inl"

#endif
