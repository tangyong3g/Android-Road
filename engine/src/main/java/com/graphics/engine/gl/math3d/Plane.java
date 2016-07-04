package com.graphics.engine.gl.math3d;


/**
 * 
 * <br>类描述: 3D平面
 * <br>功能详细描述:
 * <br><em>使用前先看{@link GeometryPools}</em>
 * 过点 P0，且法向量为 N 的平面，其方程为 N*(P-P0)=0，
 * 也即 N*p-N*P0=0，展开得 Nx*Px+Ny*Py+Nz*Pz+(-N*P0)=0，
 * 简写成Ax+By+Cz+D=0，则平面可以认为是一个齐次向量(A,B,C,D)，
 * 其与平面上的点P(x,y,z,1)的点积为0。
 * 
 * @author  dengweiming
 * @date  [2013-7-4]
 */
public final class Plane implements Ray.RayIntersectable {
	//CHECKSTYLE IGNORE 4 LINES
	float x;
	float y;
	float z;
	float w;
	
	public Plane() {
		
	}
	
	public Plane(float a, float b, float c, float d) {
		set(a, b, c, d);
	}
	
	public Plane(Point p, Vector n) {
		set(p, n);
	}
	
	public Plane(Point p0, Point p1, Point p2) {
		set(p0, p1, p2);
	}
	
	public Plane set(float a, float b, float c, float d) {
		x = a;
		y = b;
		z = c;
		w = d;
		return this;
	}
	
	public Plane set(Point p, Vector n) {
		float rLen = 1 / n.length();
		x = n.x * rLen;
		y = n.y * rLen;
		z = n.z * rLen;
		w = -(x * p.x + y * p.y + z * p.z);
		return this;
	}
	
	public Plane set(Point p0, Point p1, Point p2) {
		GeometryPools.saveStack();
		Vector n = p1.sub(p0).cross(p2.sub(p0));
		set(p0, n);
		GeometryPools.restoreStack();
		return this;
	}
	
	public Plane set(Plane plane) {
		x = plane.x;
		y = plane.y;
		z = plane.z;
		w = plane.w;
		return this;
	}
	
	public Plane setTo(Plane plane) {
		plane.x = x;
		plane.y = y;
		plane.z = z;
		plane.w = w;
		return plane;
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
		w = src[offset++];
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
		dst[offset++] = w;
		return offset;
	}
	
	@Override
	public String toString() {
		return "Plane(" + x + ", " + y + ", " + z + ", " + w + ")";
	}
	
	/**
	 * 计算点p到平面的有向距离。
	 * <br>对于法向量指向的半空间中的点，距离为正，否则为负。因此这个方法可以用来判断点相对平面的位置。
	 */
	public float dist(Point p) {
		return x * p.x + y * p.y + z * p.z + w;
	}
	
	/**
	 * 计算点p在平面上的投影
	 */
	public Point proj(Point p) {
		float d = dist(p);
//		return p.sub(GeometryPools.aquireVector().set(x, y, z).mul(d));
		Point q = GeometryPools.acquirePoint();
		q.x = p.x - d * x;
		q.y = p.y - d * y;
		q.z = p.z - d * z;
		return q;
	}
	
	/**
	 * 判断平面是否跟向量v同向（如果向量v是视线，则平面是背向面）
	 */
	public boolean isSameSide(Vector v) {
		return x * v.x + y * v.y + z * v.z > 0;
	}
	
	@Override
	public boolean intersect(Ray ray) {
		float dot1 = x * ray.d.x + y * ray.d.y + z * ray.d.z;
		if (Math3D.fZero(dot1)) {	//射线和平面平行
			return false;
		}
		float dot2 = x * ray.p.x + y * ray.p.y + z * ray.p.z + w;
		return ray.checkHit(-dot2 / dot1);
	}
	
	@Override
	public boolean testIntersect(Ray ray) {
		float dot1 = x * ray.d.x + y * ray.d.y + z * ray.d.z;
		if (Math3D.fZero(dot1)) {	//射线和平面平行
			return false;
		}
		float dot2 = x * ray.p.x + y * ray.p.y + z * ray.p.z + w;
		return dot2 * dot1 <= 0;
	}
}
