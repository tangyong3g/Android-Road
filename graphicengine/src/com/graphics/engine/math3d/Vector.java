package com.graphics.engine.math3d;


/**
 * 
 * <br>类描述: 3D向量
 * <br>功能详细描述:
 * <br><em>使用前先看{@link GeometryPools}</em>
 * <br>注意和点{@link Point}是不一样的，点的齐次坐标w分量是1，而向量的是0。
 * 
 * @author  dengweiming
 * @date  [2013-7-1]
 */
public final class Vector {
	//CHECKSTYLE IGNORE 3 LINES
	public float x;
	public float y;
	public float z;

	private final static Vector NEG_QUATERNION = new Vector();
	
	public Vector() {
		
	}
	
	public Vector(float x, float y, float z) {
		set(x, y, z);
	}
	
	public Vector(Point p, Point q) {
		set(p, q);
	}
	
	public Vector set(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
		return this;
	}
	
	public Vector set(Point p, Point q) {
		x = q.x - p.x;
		y = q.y - p.y;
		z = q.z - p.z;
		return this;
	}
	
	public Vector set(Vector v) {
		x = v.x;
		y = v.y;
		z = v.z;
		return this;
	}
	
	public Vector setTo(Vector v) {
		v.x = x;
		v.y = y;
		v.z = z;
		return v;
	}
	
	/**
	 * 反序列化
	 * @param src 源数据数组
	 * @param offset 要读的数据在<var>src</var>中的开始索引
	 * @return 新的<var>offset</var>值，用于下一个调用
	 * @see {@link #toArray(float[], int)}
	 */
	public int fromArray(float[] src, int offset) {
		x = src[offset++];
		y = src[offset++];
		z = src[offset++];
		return offset;
	}
	
	/**
	 * 序列化
	 * @param dst 目标数据数组
	 * @param offset 要写的数据在<var>dst</var>的开始索引
	 * @return 新的<var>offset</var>值，用于下一个调用
	 * @see {@link #fromArray(float[], int)}
	 */
	public int toArray(float[] dst, int offset) {
		dst[offset++] = x;
		dst[offset++] = y;
		dst[offset++] = z;
		return offset;
	}
	
	@Override
	public String toString() {
		return "Vector(" + x + ", " + y + ", " + z + ")";
	}
	
	/**
	 * 计算向量长度
	 * <br>注意：有时候只是想比较长度，那么可以使用长度的平方{@link #sqLength()}替代，会快点。
	 */
	public float length() {
		float sqLen = x * x + y * y + z * z;
		if (sqLen > Math3D.EPSILON) {
			return (float) Math.sqrt(sqLen);
		}
		return 0;
	}
	
	/**
	 * 计算向量长度的平方
	 */
	public float sqLength() {
		return x * x + y * y + z * z;
	}
	
	/**
	 * 规范化为单位向量 
	 */
	public Vector normalize() {
		float sqLen = x * x + y * y + z * z;
		if (!Math3D.fZero(sqLen)) {
			float recipLen = (float) (1 / Math.sqrt(sqLen));
			x *= recipLen;
			y *= recipLen;
			z *= recipLen;
		}
		return this;
	}
	
	/**
	 * 判断是否零向量（允许误差{@link Math3D#EPSILON}）
	 */
	public boolean isZero() {
		return Math3D.fZero(x) &&  Math3D.fZero(y) && Math3D.fZero(z);
	}
	
	public Vector add(Vector v) {
		Vector res = GeometryPools.acquireVector();
		res.x = x + v.x;
		res.y = y + v.y;
		res.z = z + v.z;
		return res;
	}
	
	public Vector sub(Vector v) {
		Vector res = GeometryPools.acquireVector();
		res.x = x - v.x;
		res.y = y - v.y;
		res.z = z - v.z;
		return res;
	}
	
	public Vector mul(float t) {
		Vector res = GeometryPools.acquireVector();
		res.x = x * t;
		res.y = y * t;
		res.z = z * t;
		return res;
	}
	
	/**
	 * 计算反方向的向量
	 */
	public Vector neg() {
//		return mul(-1);
		Vector res = GeometryPools.acquireVector();
		res.x = -x;
		res.y = -y;
		res.z = -z;
		return res;
	}
	
	/**
	 * 计算本向量和向量v的点积
	 */
	public float dot(Vector v) {
		return x * v.x + y * v.y + z * v.z;
	}
	
	/**
	 * 计算本向量和向量v的叉积
	 */
	public Vector cross(Vector v) {
		Vector res = GeometryPools.acquireVector();
		res.x = y * v.z - z * v.y;
		res.y = z * v.x - x * v.z;
		res.z = x * v.y - y * v.x;
		return res;
	}
	
	/**
	 * 计算本向量相对于向量v上平行分量（也即投影）
	 */
	public Vector proj(Vector v) {
//		return v.mul(this.dot(v) / v.dot(v));
		Vector res = GeometryPools.acquireVector();
		float dot1 = x * v.x + y * v.y + z * v.z;
		float dot2 = v.x * v.x + v.y * v.y + v.z * v.z;
		float t = dot1 / dot2;
		res.x = v.x * t;
		res.y = v.y * t;
		res.z = v.z * t;
		return res;
	}
	
	/**
	 * 计算本向量相对于向量v上的垂直分量
	 */
	public Vector perp(Vector v) {
//		return sub(proj(v)); 
		Vector res = GeometryPools.acquireVector();
		float dot1 = x * v.x + y * v.y + z * v.z;
		float dot2 = v.x * v.x + v.y * v.y + v.z * v.z;
		float t = dot1 / dot2;
		res.x = x - v.x * t;
		res.y = y - v.y * t;
		res.z = z - v.z * t;
		return res;
	}
	
	/**
	 * <br>功能简述: 两个向量作球形线性插值
	 * <br>功能详细描述: 
	 * 两个向量起始端重合，以及两个末端，可以确定一个圆，并把圆弧分成两段，选取逆时针的那段，
	 * 按照插值参数<var>t</var>，对弧长（或者圆心角）插值，得到圆周上新的点，作为结果
	 * 向量的末端。
	 * <br>注意:
	 * @param q 另一个向量
	 * @param t 插值参数，t=0时结果为"this"向量，t=1时则结果为<var>q</var>
	 * @param nearest 是否选取较短的圆弧，即使其为顺时针的
	 * @return
	 */
	public Vector slerp(Vector q, float t, boolean nearest) {
		Vector res = GeometryPools.acquireVector();
		float dot = x * q.x + y * q.y + z * q.z;
		if (dot < 0 && nearest) {
			dot = -dot;
			NEG_QUATERNION.x = -q.x;
			NEG_QUATERNION.y = -q.y;
			NEG_QUATERNION.z = -q.z;
			q = NEG_QUATERNION;	// q=-q;
		}
		if (dot > 1 - Math3D.EPSILON) {
			res.x = (q.x - x) * t + x;
			res.y = (q.y - y) * t + y;
			res.z = (q.z - z) * t + z;
			return res;
		}
		float theta = (float) Math.acos(dot);
		//q3 = sinθ(1-t)/sinθ * q1 + sinθt/sinθ * q2
		float rSin = (float) (1 / Math.sin(theta));
		float c1 = (float) Math.sin(theta * (1 - t)) * rSin;
		float c2 = (float) Math.sin(theta * t) * rSin;
		res.x = c1 * x + c2 * q.x;
		res.y = c1 * y + c2 * q.y;
		res.z = c1 * z + c2 * q.z;
		return res;
	}
	
	/**
	 * 平移这个向量，得到新的向量
	 */
	public Vector translate(float x, float y, float z) {
		Vector res = GeometryPools.acquireVector();
		res.x = this.x + x;
		res.y = this.y + y;
		res.z = this.z + z;
		return res;
	}
	
	/**
	 * 乘以一个矩阵
	 * <br>注意：如果是法向量，需要乘以矩阵的逆转置矩阵{@link Matrix#invertTranspose()}，以保证法向量和切向量垂直
	 */
	public Vector transform(Matrix matrix) {
		float[] m = matrix.m;
		Vector res = GeometryPools.acquireVector();
    	res.x = m[Math3D.M00] * x + m[Math3D.M01] * y + m[Math3D.M02] * z;
    	res.y = m[Math3D.M10] * x + m[Math3D.M11] * y + m[Math3D.M12] * z;
    	res.z = m[Math3D.M20] * x + m[Math3D.M21] * y + m[Math3D.M22] * z;
    	return res;
	}
	
	/**
	 * 乘以一个矩阵的逆矩阵（假设没有缩放变换，否则考虑使用{@link #inverseTRS()}）
	 * @see {@link #transform()}
	 * @see {@link #inverseTRS()}
	 */
	public Vector inverseRotateAndTranslate(Matrix matrix) {
		float[] m = matrix.m;
		Vector res = GeometryPools.acquireVector();
    	res.x = m[Math3D.M00] * x + m[Math3D.M10] * y + m[Math3D.M20] * z;
    	res.y = m[Math3D.M01] * x + m[Math3D.M11] * y + m[Math3D.M21] * z;
    	res.z = m[Math3D.M02] * x + m[Math3D.M12] * y + m[Math3D.M22] * z;
    	return res;
	}
	
	/**
	 * 乘以一个矩阵的逆矩阵（假设缩放变换右边没有旋转变换，否则考虑使用{@link Matrix#invert()}与{@link #transform()}）
	 * <br>另外，可以有其他缩放变换和平移变换，这样总可以转化为 TRS 形式
	 * @see {@link #transform()}
	 * @see {@link #inverseRotateAndTranslate()}
	 */
	public Vector inverseTRS(Matrix matrix) {
		float[] m = matrix.m;
		Vector res = GeometryPools.acquireVector();
		float xx = m[Math3D.M00] * m[Math3D.M00] + m[Math3D.M10] * m[Math3D.M10] + m[Math3D.M20] * m[Math3D.M20];     
		float yy = m[Math3D.M01] * m[Math3D.M01] + m[Math3D.M11] * m[Math3D.M11] + m[Math3D.M21] * m[Math3D.M21];     
		float zz = m[Math3D.M02] * m[Math3D.M02] + m[Math3D.M12] * m[Math3D.M12] + m[Math3D.M22] * m[Math3D.M22];

		res.x = (m[Math3D.M00] * x + m[Math3D.M10] * y + m[Math3D.M20] * z) / xx;
		res.y = (m[Math3D.M01] * x + m[Math3D.M11] * y + m[Math3D.M21] * z) / yy;
		res.z = (m[Math3D.M02] * x + m[Math3D.M12] * y + m[Math3D.M22] * z) / zz;
		return res;
	}
}
