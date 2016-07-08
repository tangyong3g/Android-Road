package com.graphics.engine.math3d;


/**
 * 
 * <br>类描述: 球体
 * <br>功能详细描述:
 * <br><em>使用前先看{@link GeometryPools}</em>
 * 
 * @author  dengweiming
 * @date  [2013-7-4]
 */
public final class Sphere implements Ray.RayIntersectable {
	//CHECKSTYLE IGNORE 2 LINES
	/** 圆心 */ final Point c;
	/** 半径 */ float r;
	
	/**
	 * 创建一个球体
	 */
	public Sphere() {
		c = new Point();
	}
	
	/**
	 * 创建一个球体
	 * @param p 球的中心
	 * @param r 球的半径
	 */
	public Sphere(Point p, float r) {
		this();
		set(p, r);
	}
	
	/**
	 * 重设球体
	 * @param p 球的中心
	 * @param r 球的半径
	 */
	public Sphere set(Point p, float r) {
		this.c.set(p);
		this.r = r;
		return this;
	}
	
	/**
	 * 以一个球体重设这个球体
	 */
	public Sphere set(Sphere sphere) {
		c.set(sphere.c);
		r = sphere.r;
		return this;
	}
	
	/**
	 * 以这个球体重设另一个球体
	 */
	public Sphere setTo(Sphere sphere) {
		sphere.c.set(c);
		sphere.r = r;
		return sphere;
	}
	
	@Override
	public String toString() {
		return "Sphere(" + c.x + ", " + c.y + ", " + c.z + " r=" + r + ")";
	}
	
	/**
	 * 获取球的中心
	 */
	public Point center() {
		return GeometryPools.acquirePoint().set(c);
	}
	
	/**
	 * 获取球的半径
	 */
	public float radius() {
		return r;
	}
	
	@Override
	public boolean intersect(Ray ray) {
		//交点方程为 (p + td - c) * (p + td - c) - r * r = 0
		//令 m = p - c，展开后利用 d * d = 1 化简得
		//t^2 + 2(m * d)t + (m * m - r * r) = 0
		//解二次方程 t^2 + 2bt + c = 0
		Vector m = ray.p.sub(c);
		float b = m.dot(ray.d);
		float c = m.dot(m) - r * r;
		if (c > 0 && b > 0) {
			return false;	//射线起点在球外且指向远离球的方向
		}
		float discr = b * b - c;
		if (discr < 0) {
			return false;	//判别式为0,方程无根
		}
		float t = -b - (float) Math.sqrt(discr);
		return ray.checkHit(Math.max(t, 0));
	}
	
	@Override
	public boolean testIntersect(Ray ray) {
		Vector m = ray.p.sub(c);
		float c = m.dot(m) - r * r;
		if (c <= 0) {
			return true;	//射线起点在球内
		}
		float b = m.dot(ray.d);
		if (b > 0) {
			return false;	//射线起点在球外且指向远离球的方向
		}
		float discr = b * b - c;
		if (discr < 0) {
			return false;	//判别式为0,方程无根
		}
		return true;
	}
	
	/**
	 * 平移这个球体，得到新的球体
	 */
	public Sphere translate(float x, float y, float z) {
		Sphere res = GeometryPools.acquireSphere();
		res.c.set(c.x + x, c.y + y, c.z + z);
		res.r = r;
    	return res;
	}
	
	/**
	 * 乘以一个矩阵（假设没有缩放变换），得到新的球体
	 */
	public Sphere rotateAndTranslate(Matrix matrix) {
		Sphere res = GeometryPools.acquireSphere();
		c.transform(matrix).setTo(res.c);
		res.r = r;
    	return res;
	}
	
	/**
	 * 乘以一个矩阵的逆矩阵（假设没有缩放变换），得到新的球体
	 */
	public Sphere inverseRotateAndTranslate(Matrix matrix) {
		Sphere res = GeometryPools.acquireSphere();
		c.inverseRotateAndTranslate(matrix).setTo(res.c);
		res.r = r;
    	return res;
	}

}
