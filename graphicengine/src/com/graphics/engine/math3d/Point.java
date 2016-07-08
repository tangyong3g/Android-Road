package com.graphics.engine.math3d;


/**
 * 
 * <br>类描述: 3D点
 * <br>功能详细描述:
 * <br><em>使用前先看{@link GeometryPools}</em>
 * <br>注意和向量{@link Vector}是不一样的，点的齐次坐标w分量是1，而向量的是0。
 * 
 * @author  dengweiming
 * @date  [2013-7-1]
 */
public final class Point {
	//CHECKSTYLE IGNORE 3 LINES
	public float x;
	public float y;
	public float z;
	
	public Point() {
		
	}
	
	public Point(float x, float y, float z) {
		set(x, y, z);
	}
	
	public Point set(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
		return this;
	}
	
	public Point set(Point p) {
		x = p.x;
		y = p.y;
		z = p.z;
		return this;
	}
	
	public Point setTo(Point p) {
		p.x = x;
		p.y = y;
		p.z = z;
		return p;
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
		return "Point(" + x + ", " + y + ", " + z + ")";
	}
	
	public Point add(Vector v) {
		Point res = GeometryPools.acquirePoint();
		res.x = x + v.x;
		res.y = y + v.y;
		res.z = z + v.z;
		return res;
	}
	
	public Point sub(Vector v) {
		Point res = GeometryPools.acquirePoint();
		res.x = x - v.x;
		res.y = y - v.y;
		res.z = z - v.z;
		return res;
	}
	
	public Vector sub(Point p) {
		Vector res = GeometryPools.acquireVector();
		res.x = x - p.x;
		res.y = y - p.y;
		res.z = z - p.z;
		return res;
	}
	
	/**
	 * 计算与点p的距离
	 * <br>注意：如果只是判断距离，可以使用距离的平方{@link #sqDist(Point)}，会快点。
	 */
	public float dist(Point p) {
		float x = p.x - this.x;
		float y = p.y - this.y;
		float z = p.z - this.z;
		return (float) Math.sqrt(x * x + y * y + z * z);
	}
	
	/**
	 * 计算与点p的距离的平方
	 */
	public float sqDist(Point p) {
		float x = p.x - this.x;
		float y = p.y - this.y;
		float z = p.z - this.z;
		return x * x + y * y + z * z;
	}
	
	/**
	 * 与点p按t插值
	 */
	public Point lerp(Point p, float t) {
		Point res = GeometryPools.acquirePoint();
		res.x = (p.x - x) * t + x;
		res.y = (p.y - y) * t + y;
		res.z = (p.z - z) * t + z;
		return res;
	}
	
	public Point translate(float x, float y, float z) {
		Point res = GeometryPools.acquirePoint();
		res.x = this.x + x;
		res.y = this.y + y;
		res.z = this.z + z;
		return res;
	}
	
	/**
	 * 乘以一个矩阵
	 */
	public Point transform(Matrix matrix) {
		float[] m = matrix.m;
		Point res = GeometryPools.acquirePoint();
		res.x = m[Math3D.M00] * x + m[Math3D.M01] * y + m[Math3D.M02] * z + m[Math3D.M03];
		res.y = m[Math3D.M10] * x + m[Math3D.M11] * y + m[Math3D.M12] * z + m[Math3D.M13];
		res.z = m[Math3D.M20] * x + m[Math3D.M21] * y + m[Math3D.M22] * z + m[Math3D.M23];
		return res;
	}
	
	/**
	 * 乘以一个矩阵的逆矩阵（假设没有缩放变换，否则考虑使用{@link #inverseTRS()}）
	 * @see {@link #transform()}
	 * @see {@link #inverseTRS()}
	 */
	public Point inverseRotateAndTranslate(Matrix matrix) {
		float[] m = matrix.m;
		Point res = GeometryPools.acquirePoint();
		final float x = this.x - m[Math3D.MTX];
		final float y = this.y - m[Math3D.MTY];
		final float z = this.z - m[Math3D.MTZ];
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
	public Point inverseTRS(Matrix matrix) {
		float[] m = matrix.m;
		Point res = GeometryPools.acquirePoint();
		final float x = this.x - m[Math3D.MTX];
		final float y = this.y - m[Math3D.MTY];
		final float z = this.z - m[Math3D.MTZ];
		float xx = m[Math3D.M00] * m[Math3D.M00] + m[Math3D.M10] * m[Math3D.M10] + m[Math3D.M20] * m[Math3D.M20];     
		float yy = m[Math3D.M01] * m[Math3D.M01] + m[Math3D.M11] * m[Math3D.M11] + m[Math3D.M21] * m[Math3D.M21];     
		float zz = m[Math3D.M02] * m[Math3D.M02] + m[Math3D.M12] * m[Math3D.M12] + m[Math3D.M22] * m[Math3D.M22];

		res.x = (m[Math3D.M00] * x + m[Math3D.M10] * y + m[Math3D.M20] * z) / xx;
		res.y = (m[Math3D.M01] * x + m[Math3D.M11] * y + m[Math3D.M21] * z) / yy;
		res.z = (m[Math3D.M02] * x + m[Math3D.M12] * y + m[Math3D.M22] * z) / zz;
		return res;
	}

}
