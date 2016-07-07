package com.graphics.engine.gl.math3d;


/**
 * <br>类描述: 3D射线
 * <br>功能详细描述:
 * <br><em>使用前先看{@link GeometryPools}</em>
 * <br>其使用一个起点 p 和方向向量 d 来表示，射线上的点 q 可以表示为参数形式 q = p + td，
 * 其中 t >= 0。
 * 在开始做求交检测前，需要调用{@link #startCast()}
 * 或者{@link #startCast(float)}来初始化可求交部分的范围
 * 
 * @author  dengweiming
 * @date  [2013-7-1]
 */
public final class Ray {
	
	/**
	 * <br>接口描述: 可以和射线相交的物体
	 */
	public interface RayIntersectable {
		
		/**
		 * <br>功能简述: 检测是否和射线/线段相交
		 * <br>功能详细描述:
		 * <br>注意: 实现者需要使用{@link Ray#checkHit(float)} 更新相交位置
		 * @param ray
		 * @return
		 */
		boolean intersect(Ray ray);
		
		/**
		 * <br>功能简述:  检测是否和射线/线段相交
		 * <br>功能详细描述:
		 * <br>注意: 实现者可以不用更新相交位置，以加速检测。例如对包围体的检测，
		 * 如果通过检测，才再对物体本身检测，因此包围体与射线的相交位置没有实际用处。
		 * @param ray
		 * @return
		 */
		boolean testIntersect(Ray ray);
	}
	
	//CHECKSTYLE IGNORE 6 LINES
	/** 起点 */ final Point p;
	/** 方向 */ final Vector d;
	float tMin = 0;	//目前限定为0,不允许改变起点
	float tMax;		//允许临时改变终点，作为线段处理
	/** 长度 */ float len;
	
	public Ray() {
		p = new Point();
		d = new Vector();
	}
	
	public Ray(Point p, Point q) {
		this();
		set(p, q);
	}
	
	public Ray(Point p, Vector d) {
		this();
		set(p, d);
	}
	
	public Ray set(Point p, Point q) {
		this.p.set(p);
		this.d.set(p, q);
		len = this.d.length();
		this.d.normalize();
		return this;
	}
	
	public Ray set(Point p, Vector d) {
		this.p.set(p);
		this.d.set(d);
		len = this.d.length();
		this.d.normalize();
		return this;
	}
	
	public Ray set(Ray ray) {
		p.set(ray.p);
		d.set(ray.d);
		tMin = ray.tMin;
		tMax = ray.tMax;
		len = ray.len;
		return this;
	}
	
	public Ray setTo(Ray ray) {
		ray.p.set(p);
		ray.d.set(d);
		ray.tMin = tMin;
		ray.tMax = tMax;
		ray.len = len;
		return ray;
	}
	
	@Override
	public String toString() {
		return "Ray(" + p.x + ", " + p.y + ", " + p.z + " => " + d.x + ", " + d.y + ", " + d.z + ")";
	}
	
	public float length() {
		return len;
	}
	
	/**
	 * 获取参数为t对应的点
	 */
	public Point getPoint(float t) {
		Point res = GeometryPools.acquirePoint();
		res.x = d.x * t + p.x;
		res.y = d.y * t + p.y;
		res.z = d.z * t + p.z;
		return res;
	}
	
	/**
	 * 获取射线方向
	 */
	public Vector getDirection() {
		return GeometryPools.acquireVector().set(d);
	}
	
	/**
	 * 以射线方式开始投射
	 * @see {@link #startCast(float, float)}
	 */
	public void startCast() {
		tMin = 0;
		tMax = Math3D.MAX_VALUE;
	}
	
	/**
	 * 以线段方式开始投射。可以使用原始长度{@link #length()}作为 <var>max</var>。
	 * @see {@link #startCast()}
	 */
	public void startCast(float max) {
		if (max < tMin) {
			throw new IllegalArgumentException("max=" + max + " should greater than min=" + tMin);
		}
		tMax = max;
	}
	
	public boolean checkHit(float t) {
		if (tMin <= t && t < tMax) {
			tMax = t;
			return true;
		}
		return false;
	}
	
	public boolean checkHit(float t1, float t2) {
//		return checkHit(t1) && checkHit(t2);
		if (tMin <= t1 && t1 < tMax) {
			tMax = t1;
			return true;
		} else if (tMin <= t2 && t2 < tMax) {
			tMax = t2;
			return true;
		}
		return false;
	}
	
	public float getHitResult() {
		return tMax;
	}
	
	public Point getHitPoint() {
		return getPoint(tMax);
	}
	
	public Ray translate(float x, float y, float z) {
		Ray ray = GeometryPools.acquireRay();
		GeometryPools.saveStack();
		Vector v = GeometryPools.acquireVector().set(x, y, z);
		Point p1 = p.add(v);
		ray.set(p1, this.d);
		GeometryPools.restoreStack();
		return ray;
	}
	
	/**
	 * 乘以一个矩阵
	 */
	public Ray transform(Matrix matrix) {
		Ray ray = GeometryPools.acquireRay();
		GeometryPools.saveStack();
		Point p1 = p.transform(matrix);
		Vector v1 = d.transform(matrix);
		ray.set(p1, v1);
		GeometryPools.restoreStack();
		return ray;
	}
	
	/**
	 * 乘以一个矩阵的逆矩阵（假设没有缩放变换，否则考虑使用{@link #inverseTRS()}）
	 */
	public Ray inverseRotateAndTranslate(Matrix matrix) {
		Ray ray = GeometryPools.acquireRay();
		GeometryPools.saveStack();
		Point p1 = p.inverseRotateAndTranslate(matrix);
		Vector v1 = d.inverseRotateAndTranslate(matrix);
		ray.set(p1, v1);
		GeometryPools.restoreStack();
		return ray;
	}

	/**
	 * 乘以一个矩阵的逆矩阵（假设缩放变换右边没有旋转变换，否则考虑使用{@link Matrix#invert()}与{@link #transform()}）
	 * <br>另外，可以有其他缩放变换和平移变换，这样总可以转化为 TRS 形式
	 * @see {@link #transform()}
	 * @see {@link #inverseRotateAndTranslate()}
	 */
	public Ray inverseTRS(Matrix matrix) {
		Ray ray = GeometryPools.acquireRay();
		GeometryPools.saveStack();
		Point p1 = p.inverseTRS(matrix);
		Vector v1 = d.inverseTRS(matrix);
		ray.set(p1, v1);
		GeometryPools.restoreStack();
		return ray;
	}
}
