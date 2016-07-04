package com.graphics.engine.gl.math3d;


/**
 * 
 * <br>类描述: 圆柱体
 * <br>功能详细描述:
 * 使用底面和顶面的中心，以及半径来定义的圆柱体，不限制对齐到坐标轴。
 * <br><em>使用前先看{@link GeometryPools}</em>
 * 
 * @author  dengweiming
 * @date  [2013-7-1]
 */
public final class Cylinder implements Ray.RayIntersectable {
	private static float sTRes;
	
	//CHECKSTYLE IGNORE 3 LINES
	/** 底面圆心 */ final Point p;
	/** 顶面圆心 */ final Point q;
	/** 圆柱半径 */ public float r;
	
	/**
	 * 创建一个圆柱体
	 */
	public Cylinder() {
		p = new Point();
		q = new Point();
	}
	
	/**
	 * 创建一个圆柱体
	 * @param p 底面圆心
	 * @param q 顶面圆心
	 * @param r 圆柱半径
	 */
	public Cylinder(Point p, Point q, float r) {
		this();
		set(p, q, r);
	}
	
	/**
	 * 重设圆柱体
	 * @param p 底面圆心
	 * @param q 顶面圆心
	 * @param r 圆柱半径
	 */
	public void set(Point p, Point q, float r) {
		this.p.set(p);
		this.q.set(q);
		this.r = r;
	}
	
	/**
	 * 以一个圆柱体重设这个圆柱体
	 */
	public Cylinder set(Cylinder cylinder) {
		p.set(cylinder.p);
		q.set(cylinder.q);
		r = cylinder.r;
		return this;
	}
	
	/**
	 * 以这个圆柱体重设另一个圆柱体
	 */
	public Cylinder setTo(Cylinder cylinder) {
		cylinder.p.set(p);
		cylinder.q.set(q);
		cylinder.r = r;
		return cylinder;
	}
	
	@Override
	public boolean intersect(Ray ray) {
		GeometryPools.saveStack();
		boolean res = intersectSegmentCylinder(ray);
		GeometryPools.restoreStack();
		return res && ray.checkHit(sTRes);
	}
	
	@Override
	public boolean testIntersect(Ray ray) {
		return intersect(ray);	//TODO：可以优化
	}

	private boolean intersectSegmentCylinder(Ray ray) {
		// Real-Time Collision Detection 5.3.7 p197
		// errata: http://realtimecollisiondetection.net/books/rtcd/errata/
		Vector d = q.sub(p);
		Vector m = ray.p.sub(p);
		Vector n = ray.d;
		final float rayLen = ray.tMax;
		final boolean isSegment = rayLen != Math3D.MAX_VALUE;
		
		float md = m.dot(d);
		float nd = n.dot(d);
		float dd = d.dot(d);

		// Test if segment fully outside either endcap of cylinder
		if (isSegment) {
			if (md < 0 && md + nd * rayLen < 0) {
				return false; 			// Segment outside ’p’ side of cylinder
			}
			if (md > dd && md + nd * rayLen > dd) {
				return false; 			// Segment outside ’q’ side of cylinder
			}
		} else {
			if ((md < 0 && nd <= 0) || (md > dd && nd >= 0)) {
				return false;
			}
		}

		// Solve at^2+2bt+c=0
		float nn = n.dot(n);
		float mn = m.dot(n);
		float a = dd * nn - nd * nd;
		float k = m.dot(m) - r * r;
		float c = dd * k - md * md;

		if (Math.abs(a) < Math3D.EPSILON) {
			// Segment runs parallel to cylinder axis
			if (c > 0) {
				return false;		// ’a’ and thus the segment lie outside cylinder
			}

			// Now known that segment intersects cylinder; figure out how it intersects
			if (md < 0) {
				sTRes = -mn / nn;		// Intersect segment against ’p’ endcap
			} else if (md > dd) {
				sTRes = (nd - mn) / nn; // Intersect segment against ’q’ endcap
			} else {
				sTRes = 0;				// ’a’ lies inside cylinder
			}
			return false;
		}

		float b = dd * mn - nd * md;
		float discr = b * b - a * c;
		if (discr < 0) {
			return false;			// No real roots; no intersection
		}

		sTRes = (-b - (float) Math.sqrt(discr)) / a;
		sTRes = Math.max(sTRes, 0);
		if (md + sTRes * nd < 0) {
			// Intersection outside cylinder on ‘p’ side
			if (nd <= 0) {
				return false; 		// Segment pointing away from endcap
			}
			sTRes = -md / nd;
			// Keep intersection if Dot(S(t) - p, S(t) - p) <= r^2
			return k + sTRes * (2 * mn + sTRes * nn) <= 0;
		} else if (md + sTRes * nd > dd) {
			// Intersection outside cylinder on ‘q’ side
			if (nd >= 0) {
				return false; 		// Segment pointing away from endcap
			}
			sTRes = (dd - md) / nd;
			// Keep intersection if Dot(S(t) - q, S(t) - q) <= r^2
			return k + dd - 2 * md + sTRes * (2 * (mn - nd) + sTRes * nn) <= 0;
		}
		// Intersection if segment intersects cylinder between the end-caps
		return true;
	}

}
