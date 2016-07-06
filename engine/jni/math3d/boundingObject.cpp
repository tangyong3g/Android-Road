
#include "boundingObject.h"

CBoundingBox::CBoundingBox()
{
	m_LowerCorner.Set(0, 0, 0);
	m_UpperCorner.Set(0, 0, 0);
}

CBoundingBox::CBoundingBox(const CVector3& lowerCorner, const CVector3& upperCorner)
{
	m_LowerCorner = lowerCorner;
	m_UpperCorner = upperCorner;
}

CBoundingBox::CBoundingBox(float minX, float minY, float minZ, float maxX, float maxY, float maxZ)
{
	m_LowerCorner.Set(minX, minY, minZ);
	m_UpperCorner.Set(maxX, maxY, maxZ);
}

CBoundingBox::CBoundingBox(const CBoundingBox& box)
{
	m_LowerCorner = box.m_LowerCorner;
	m_UpperCorner = box.m_UpperCorner;
}

const CBoundingBox& CBoundingBox::operator = (const CBoundingBox& box)
{
	m_LowerCorner = box.m_LowerCorner;
	m_UpperCorner = box.m_UpperCorner;
	return *this;
}

bool CBoundingBox::Intersect(const CVector3& point) const
{
	return m_LowerCorner[0] < point[0] && point[0] < m_UpperCorner[0]
	    && m_LowerCorner[1] < point[1] && point[1] < m_UpperCorner[1]
	    && m_LowerCorner[2] < point[2] && point[2] < m_UpperCorner[2];
}

CBoundingSphere::CBoundingSphere()
{
	m_Center.Set(0, 0, 0);
	m_CurCenter.Set(0, 0, 0);
	m_Radius = 0;
}

CBoundingSphere::CBoundingSphere(const CVector3& center, float radius)
{
	m_Center = center;
	m_CurCenter = center;
	m_Radius = radius > 0 ? radius : 0;
}

CBoundingSphere::CBoundingSphere(float minX, float minY, float minZ, float maxX, float maxY, float maxZ)
{
	float dx = maxX - minX;
	float dy = maxY - minY;
	float dz = maxZ - minZ;
	m_Radius = sqrt(dx * dx + dy * dy + dz * dz) / 2;
	m_Center.Set((minX + maxX) / 2, (minY + maxY) / 2, (minZ + maxZ) / 2);
	m_CurCenter = m_Center;
}

CBoundingSphere::CBoundingSphere(const CBoundingSphere& sphere)
{
	m_Center = sphere.m_Center;
	m_CurCenter = sphere.m_CurCenter;
	m_Radius = sphere.m_Radius;
}

const CBoundingSphere& CBoundingSphere::operator = (const CBoundingSphere& sphere)
{
	m_Center = sphere.m_Center;
	m_CurCenter = sphere.m_CurCenter;
	m_Radius = sphere.m_Radius;
	return *this;
}

void CBoundingSphere::SetTransform(const CMatrix4X4& mat)
{
	m_CurCenter = m_Center;
	m_CurCenter.Transform4(mat);
}

void CBoundingSphere::Transform(const CMatrix4X4& mat)
{
	m_CurCenter.Transform4(mat);
}

float CBoundingSphere::GetRadius() const
{
	return m_Radius;
}

void CBoundingSphere::SetRadius(float radius)
{
	m_Radius = radius > 0 ? radius : 0;
}

const CVector3& CBoundingSphere::GetCenter() const
{
	return m_CurCenter;
}

bool CBoundingSphere::Intersect(CBoundingBox& box)
{
	float* pCenter = m_CurCenter.Get();
	float* pLower = box.m_LowerCorner.Get();
	float* pUpper = box.m_UpperCorner.Get();
	return !(
		pCenter[0] < pLower[0] - m_Radius ||
		pCenter[1] < pLower[1] - m_Radius ||
		pCenter[2] < pLower[2] - m_Radius ||
		pCenter[0] > pUpper[0] + m_Radius ||
		pCenter[1] > pUpper[1] + m_Radius ||
		pCenter[2] > pUpper[2] + m_Radius);
}

CRay::CRay()
{
	mDirection.Set(0, 0, -1);
}

CRay::CRay(const CVector3& p1, const CVector3& p2)
{
	mOrigin = p1;
	mDirection = p2 - p1;
	mInitLength = mDirection.Magnitude();
	if(mInitLength <= 0){
		mDirection.Set(0, 0, -1);
	}
	else{
		mDirection.Normalize();
	}
}

void CRay::Set(const CVector3& p1, const CVector3& p2)
{
	mOrigin = p1;
	mDirection = p2 - p1;
	mInitLength = mDirection.Magnitude();
	if(mInitLength <= 0){
		mDirection.Set(0, 0, -1);
	}
	else{
		mDirection.Normalize();
	}
}

void CRay::SetOrigin(const CVector3& origin)
{
	mOrigin = origin;
}

void CRay::SetDirection(const CVector3& dir, bool normalized)
{
	mDirection = dir;
	if(!normalized){
		if(mDirection.Magnitude() <= 0){
			mDirection.Set(0, 0, -1);
		}
		else{
			mDirection.Normalize();
		}
	}
	mInitLength = 1;
}

bool CRay::Intersect(CBoundingSphere& sphere, bool useCurrentCenter) const
{
	/*
	 * 射线方程为 p(t) = mOrigin + t * mDirection,
	 * 带入球的方程中得 ||p(t) - center|| = radius
	 * 化为 t 的一元二次方程 A * t^2 + B * t + C = 0
	 * 其中 A = mDirection * mDirection = 1，
	 * B = 2 * mDirection * (mOrigin - center)，
	 * C = (mOrigin - center) * (mOrigin - center) - radius * radius,
	 * 在此方程有非负实根的时候射线和球相交
	 */
	CVector3 v = mOrigin - (useCurrentCenter ? sphere.m_CurCenter : sphere.m_Center);
	float B = DotProduct3(mDirection, v) * 2;
	float C = DotProduct3(v, v) - sphere.m_Radius * sphere.m_Radius;
	float discriminant = B * B - 4 * C;
	if(discriminant < 0){
		return false;
	}
//	discriminant = sqrt(discriminant);
//	float t1 = (-B - discriminant) / 2;
//	float t2 = (-B + discriminant) / 2;
//	return t1 >= 0 || t2 >= 0;
	return B <= 0 || C <= 0;
}

CVector3 CRay::Intersect(CPlane& plane) const
{
	float t = -(DotProduct3(plane.mNormal, mOrigin) + plane.mDistance)
			/ DotProduct3(plane.mNormal, mDirection);
	return mDirection * t + mOrigin;
}

void CRay::SetTransform(const CMatrix4X4& mat)
{
	mOrigin.Transform4(mat);
	mDirection.Transform3(mat);
}

void CRay::SetInverseTransform(const CMatrix4X4& mat)
{
	mat.InverseTranslateVec(mOrigin.Get());
	mat.InverseRotateVec(mOrigin.Get());
	mat.InverseRotateVec(mDirection.Get());
}

bool CRay::Intersect(CBoundingBox& box, float& tmin, float& tmax, int axisMask) const
{
	// Ray-Box Intersection Algorithm
	// http://www.cs.utah.edu/~awilliam/box/
	float txmin, txmax, tymin, tymax, tzmin, tzmax;

	if(axisMask & AxisMaskX){
		float divX = 1.0f / mDirection[0];
		if(divX >= 0){
			txmin = (box.m_LowerCorner[0] - mOrigin[0]) * divX;
			txmax = (box.m_UpperCorner[0] - mOrigin[0]) * divX;
		}
		else{
			txmin = (box.m_UpperCorner[0] - mOrigin[0]) * divX;
			txmax = (box.m_LowerCorner[0] - mOrigin[0]) * divX;
		}

		if( (tmin > txmax) || (txmin > tmax) )
			return false;
		if (txmin > tmin)
			tmin = txmin;
		if (txmax < tmax)
			tmax = txmax;
	}

	if(axisMask & AxisMaskY){
		float divY = 1.0f / mDirection[1];
		if(divY >= 0){
			tymin = (box.m_LowerCorner[1] - mOrigin[1]) * divY;
			tymax = (box.m_UpperCorner[1] - mOrigin[1]) * divY;
		}
		else{
			tymin = (box.m_UpperCorner[1] - mOrigin[1]) * divY;
			tymax = (box.m_LowerCorner[1] - mOrigin[1]) * divY;
		}

		if( (tmin > tymax) || (tymin > tmax) )
			return false;
		if (tymin > tmin)
			tmin = tymin;
		if (tymax < tmax)
			tmax = tymax;
	}

	if(axisMask & AxisMaskZ){
		float divZ = 1.0f / mDirection[2];
		if(divZ >= 0){
			tzmin = (box.m_LowerCorner[2] - mOrigin[2]) * divZ;
			tzmax = (box.m_UpperCorner[2] - mOrigin[2]) * divZ;
		}
		else{
			tzmin = (box.m_UpperCorner[2] - mOrigin[2]) * divZ;
			tzmax = (box.m_LowerCorner[2] - mOrigin[2]) * divZ;
		}

		if ( (tmin > tzmax) || (tzmin > tmax) )
			return false;
		if (tzmin > tmin)
			tmin = tzmin;
		if (tzmax < tmax)
			tmax = tzmax;
	}

	return tmin <= tmax;
}

CVector3 CRay::GetPosition(float t) const
{
	return mDirection * t + mOrigin;
}

CPlane::CPlane()
{
	mDistance = 0;
}

void CPlane::Set(const CVector3& point, const CVector3& normal)
{
	mNormal = normal;
	if(mNormal.Magnitude() <= 0){
		mNormal.Set(0, 0, -1);
	}
	else{
		mNormal.Normalize();
	}
	mDistance = -DotProduct3(mNormal, point);
}

static bool SameSide(const CVector3& a, const CVector3& b, const CVector3& c, const CVector3& p)
{
	CVector3 b1 = b - a;
	CVector3 c1 = c - a;
	CVector3 p1 = p - a;
	b1.Set(-b1[2], 0, b1[0]);	// 投影到zOx平面上 TODO:投影到法向量最大分量对应的平面
	return DotProduct3(b1, c1) * DotProduct3(b1, p1) >= 0;
}

bool InsideTriangle(const CVector3& a, const CVector3& b, const CVector3& c, const CVector3& p)
{
	return SameSide(a, b, c, p) && SameSide(b, c, a, p) && SameSide(c, a, b, p);
}

bool InsidePolygon(const CVector3 points[], int n, const CVector3& p)
{
	/**
	 * http://paulbourke.net/geometry/insidepoly/
	 * 适合二维平面上任意多边形和点的包含判断，主要是从测试点发出一条水平向右的射线，
	 * 判断射线和多边形的交点个数，如果为奇数则点在内部，偶数则点在外部。
	 * 注意当点在多边形的顶点上判断会出错，有需要可以增加一轮判断
	 */
	bool inside = false;
//TODO：目前只支持zOx平面
#define X_ 0
#define Y_ 2
	for(int i = 0, j = n - 1; i < n; j = i++){
		if((points[i][Y_] > p[Y_]) != (points[j][Y_] > p[Y_]) // 射线的y值位于边的两个端点之间，但不和边重合
			&& p[Y_] != points[j][Y_]	// 如和边的起点相交，则会忽略，因为在前一条边的终点已经计算过
			&& p[X_] < (points[i][X_] - points[j][X_]) // 射线和边的交点在测试点的右边
					   * (p[Y_] - points[j][Y_])
					   / (points[i][Y_] - points[j][Y_])
					   + points[j][X_]){
			inside = !inside;
		}
	}
#undef X_
#undef Y_
	return inside;
}


CLine2D::CLine2D(float a, float b, float c)
{
	A = a;
	B = b;
	C = c;
}

CLine2D::CLine2D(float x1, float y1, float x2, float y2)
{
	A = y1 - y2;
	B = x2 - x1;
	C = x1 * y2 - x2 * y1;
}

bool CLine2D::Intersect(const CLine2D& other, float& x, float& y) const
{
	float crossProduct = A * other.B - other.A * B;
	if(fabsf(crossProduct) < EPSILON){
		return false;
	}
	crossProduct = 1.0f / crossProduct;
	x = (other.B * C - B * other.C) * crossProduct;
	y = (other.A * C - A * other.C) * crossProduct;
}

