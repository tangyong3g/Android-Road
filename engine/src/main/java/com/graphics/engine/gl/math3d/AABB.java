package com.graphics.engine.gl.math3d;

/**
 * 
 * <br>类描述:轴对称包围盒类（Axis-aligned Bounding Box）
 * <br>功能详细描述:
 * <br><em>使用前先看{@link GeometryPools}</em>
 * 
 * @author  dengweiming
 * @date  [2013-7-4]
 */
public final class AABB implements Ray.RayIntersectable {
	//CHECKSTYLE IGNORE 2 LINES
	/** lower点 */
	final Point min;
	/** upper点 */
	final Point max;

	/**
	 * 创建一个包围盒
	 */
	public AABB() {
		min = new Point();
		max = new Point();
	}

	/**
	 * 以指定边界范围创建一个包围盒
	 */
	public AABB(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
		this();
		set(minX, minY, minZ, maxX, maxY, maxZ);
	}

	/**
	 * 重设边界范围
	 */
	public AABB set(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
		min.set(minX, minY, minZ);
		max.set(maxX, maxY, maxZ);
		return this;
	}

	/**
	 * 重设边界范围，以边界范围的较小值和盒子的大小
	 */
	public AABB setWithLower(float minX, float minY, float minZ, float w, float h, float l) {
		min.set(minX, minY, minZ);
		max.set(minX + w, minY + h, minZ + l);
		return this;
	}

	/**
	 * 重设边界范围，以盒子的中心和盒子的大小
	 */
	public AABB setWithCenter(float cx, float cy, float cz, float w, float h, float l) {
		min.set(cx - w * 0.5f, cy - h * 0.5f, cz - l * 0.5f);
		max.set(cx + w * 0.5f, cy + h * 0.5f, cz + l * 0.5f);
		return this;
	}

	/**
	 * 以一个盒子重设这个盒子
	 */
	public AABB set(AABB box) {
		min.set(box.min);
		max.set(box.max);
		return this;
	}

	/**
	 * 以这个盒子重设另一个盒子
	 */
	public AABB setTo(AABB box) {
		box.min.set(min);
		box.max.set(max);
		return box;
	}

	/**
	 * 反序列化
	 * @param src 源数据数组
	 * @param offset 要读的数据在<var>src</var>中的开始索引
	 * @return 新的<var>offset</var>值，用于下一个调用
	 * @see {@link #toArray(float[], int)}
	 */
	public int fromArray(float[] src, int offset) {
		min.x = src[offset++];
		min.y = src[offset++];
		min.z = src[offset++];
		max.x = src[offset++];
		max.y = src[offset++];
		max.z = src[offset++];
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
		dst[offset++] = min.x;
		dst[offset++] = min.y;
		dst[offset++] = min.z;
		dst[offset++] = max.x;
		dst[offset++] = max.y;
		dst[offset++] = max.z;
		return offset;
	}

	@Override
	public String toString() {
		return "Box(" + min.x + ", " + min.y + ", " + min.z + " - " + max.x + ", " + max.y + ", " + max.z + ")";
	}

	/**
	 * 获取中心点
	 */
	public Point center() {
		Point p = GeometryPools.acquirePoint();
		p.set((min.x + max.x) * 0.5f, (min.y + max.y) * 0.5f, (min.z + max.z) * 0.5f);
		return p;
	}

	/**
	 * 获取宽度，X轴的范围
	 */
	public float width() {
		return max.x - min.x;
	}

	/**
	 * 获取高度，Y轴的范围
	 */
	public float height() {
		return max.y - min.y;
	}
	
	/**
	 * 获取长度，Z轴的范围
	 */
	public float length() {
		return max.z - min.z;
	}

	@Override
	public boolean intersect(Ray ray) {
		float tMin = ray.tMin;
		float tMax = ray.tMax;
		final Point p = ray.p;
		final Vector d = ray.d;

		if (Math3D.fZero(d.x)) {
			if (p.x < min.x || p.x > max.x) {
				return false;	//射线与yOz平面平行并且在x轴区间外
			}
		} else {
			float rd = 1 / d.x;
			float t1 = (min.x - p.x) * rd;
			float t2 = (max.x - p.x) * rd;
			if (t1 > t2) {
				float tmp = t1;
				t1 = t2;
				t2 = tmp;
			}
			// [t1, t2] 和 [tMin, tMax] 求交
			tMin = Math.max(tMin, t1);
			tMax = Math.min(tMax, t2);
			if (tMin > tMax) {
				return false;
			}
		}

		if (Math3D.fZero(d.y)) {
			if (p.y < min.y || p.y > max.y) {
				return false;
			}
		} else {
			float rd = 1 / d.y;
			float t1 = (min.y - p.y) * rd;
			float t2 = (max.y - p.y) * rd;
			if (t1 > t2) {
				float tmp = t1;
				t1 = t2;
				t2 = tmp;
			}
			tMin = Math.max(tMin, t1);
			tMax = Math.min(tMax, t2);
			if (tMin > tMax) {
				return false;
			}
		}
		
		if (Math3D.fZero(d.z)) {
			if (p.z < min.z || p.z > max.z) {
				return false;
			}
		} else {
			float rd = 1 / d.z;
			float t1 = (min.z - p.z) * rd;
			float t2 = (max.z - p.z) * rd;
			if (t1 > t2) {
				float tmp = t1;
				t1 = t2;
				t2 = tmp;
			}
			tMin = Math.max(tMin, t1);
			tMax = Math.min(tMax, t2);
			if (tMin > tMax) {
				return false;
			}
		}
		ray.checkHit(tMin);
		return false;
	}

	@Override
	public boolean testIntersect(Ray ray) {
		return intersect(ray);
	}
	
	/**
	 * 将盒子平移，得到新的盒子
	 */
	public AABB translate(float x, float y, float z) {
		AABB res = GeometryPools.acquireAABB();
		res.min.set(min.x + x, min.y + y, min.z + z);
		res.max.set(max.x + x, max.y + y, max.z + z);
    	return res;
	}
}
