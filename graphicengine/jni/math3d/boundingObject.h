
#ifndef BOUNDING_OBJECT_H
#define BOUNDING_OBJECT_H

#include "math1.h"

#define AxisMaskX 1
#define AxisMaskY 2
#define AxisMaskZ 4
#define AxisMaskXYZ 7

class CBoundingSphere;
class CBoundingBox;
class CRay;
class CPlane;


class CBoundingBox
{
	friend class CBoundingSphere;
	friend class CRay;

public:
	CBoundingBox();
	CBoundingBox(const CVector3& lowerCorner, const CVector3& upperCorner);
	CBoundingBox(float minX, float minY, float minZ, float maxX, float maxY, float maxZ);
	CBoundingBox(const CBoundingBox& box);

	bool Intersect(const CVector3& point) const;

	const CBoundingBox& operator = (const CBoundingBox& box);

	CVector3 m_LowerCorner;
	CVector3 m_UpperCorner;
};

class CBoundingSphere
{
	friend class CBoundingBox;
	friend class CRay;

public:
	CBoundingSphere();
	CBoundingSphere(const CVector3& center, float radius);
	CBoundingSphere(float minX, float minY, float minZ, float maxX, float maxY, float maxZ);
	CBoundingSphere(const CBoundingSphere& sphere);

	const CBoundingSphere& operator = (const CBoundingSphere& sphere);

	void SetTransform(const CMatrix4X4& mat);

	void Transform(const CMatrix4X4& mat);

	float GetRadius() const;

	void SetRadius(float radius);

	const CVector3& GetCenter() const;

	bool Intersect(CBoundingBox& box);

private:
	CVector3 m_Center;
	CVector3 m_CurCenter;
	float m_Radius;
};

class CRay
{
public:
	CRay();
	CRay(const CVector3& p1, const CVector3& p2);

	void Set(const CVector3& p1, const CVector3& p2);

	void SetOrigin(const CVector3& origin);

	void SetDirection(const CVector3& dir, bool normalized = false);

	void SetTransform(const CMatrix4X4& mat);

	void SetInverseTransform(const CMatrix4X4& mat);

	bool Intersect(CBoundingBox& box, float& tmin, float& tmax, int axisMask = AxisMaskXYZ) const;

	bool Intersect(CBoundingSphere& sphere, bool useCurrentCenter = true) const;

	CVector3 Intersect(CPlane& plane) const;

	CVector3 GetPosition(float t) const;

	CVector3 mOrigin;
	CVector3 mDirection;
	float mInitLength;
};

class CPlane
{
	friend class CRay;

public:
	CPlane();
	void Set(const CVector3& point, const CVector3& normal);

	CVector3 mNormal;
	float mDistance;
};

class CLine2D
{
public:
	CLine2D(float a = 0, float b = 0, float c = 0);
	CLine2D(float x1, float y1, float x2, float y2);
	bool Intersect(const CLine2D& other, float& x, float& y) const;
private:
	float A, B, C;	// Ax+By+C=0
};

bool InsideTriangle(const CVector3& a, const CVector3& b, const CVector3& c, const CVector3& p);

bool InsidePolygon(const CVector3 points[], int n, const CVector3& p);

#endif //BOUNDING_OBJECT_H
