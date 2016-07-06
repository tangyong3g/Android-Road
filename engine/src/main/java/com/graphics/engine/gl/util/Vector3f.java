package com.graphics.engine.gl.util;

/**
 * 
 * <br>类描述:3D向量类
 * <br>功能详细描述:
 * @deprecated 使用{@link com.graphics.engine.gl.math3d.Vector}代替
 */
public class Vector3f {

	public static final Vector3f UP = new Vector3f(0, 1, 0);
	public static final Vector3f ZERO = new Vector3f(0, 0, 0);

	public static Vector3f TEMP = new Vector3f();		//CHECKSTYLE IGNORE
	public static Vector3f TEMP1 = new Vector3f();		//CHECKSTYLE IGNORE

	public float x, y, z;

	public Vector3f() {

	}

	public Vector3f(float x, float y, float z) {
		set(x, y, z);
	}

	public void set(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public void set(Vector3f v) {
		set(v.x, v.y, v.z);
	}

	public final void interpolate(Vector3f t1, Vector3f t2, float alpha) {
		this.x = (1 - alpha) * t1.x + alpha * t2.x;
		this.y = (1 - alpha) * t1.y + alpha * t2.y;
		this.z = (1 - alpha) * t1.z + alpha * t2.z;
	}

	public final void add(Vector3f t1) {
		this.x += t1.x;
		this.y += t1.y;
		this.z += t1.z;
	}

	public final void add(float x, float y, float z) {
		this.x += x;
		this.y += y;
		this.z += z;
	}

	public final void sub(Vector3f t1) {
		sub(this, t1);
	}

	public final void sub(Vector3f t1, Vector3f t2) {
		this.x = t1.x - t2.x;
		this.y = t1.y - t2.y;
		this.z = t1.z - t2.z;
	}

	public final void scale(float s) {
		this.x *= s;
		this.y *= s;
		this.z *= s;
	}

	public final float dot(Vector3f v1) {
		return this.x * v1.x + this.y * v1.y + this.z * v1.z;
	}

	public final void cross(Vector3f v1, Vector3f v2) {
		float x, y;

		x = v1.y * v2.z - v1.z * v2.y;
		y = v2.x * v1.z - v2.z * v1.x;
		this.z = v1.x * v2.y - v1.y * v2.x;
		this.x = x;
		this.y = y;
	}

	public void zero() {
		x = y = z = 0.0f;
	}

	public void normalize() {
		float len = (float) Math.sqrt(x * x + y * y + z * z);
		len = 1.0f / len;
		x *= len;
		y *= len;
		z *= len;
	}

	public static float distance(Vector3f v0, Vector3f v1) {
		float dx = v0.x - v1.x;
		float dy = v0.y - v1.y;
		float dz = v0.z - v1.z;

		return (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
	}
	
	/**
	 * 两个向量相加
	 * @param u
	 * @param v
	 * @param res	可以和u或者v相同
	 */
	public static void add(float[] u, float[] v, float[] res) {
		res[0] = u[0] + v[0];
		res[1] = u[1] + v[1];
		res[2] = u[2] + v[2];
	}
	
	/**
	 * 两个向量相减
	 * @param u
	 * @param v
	 * @param res	可以和u或者v相同
	 */
	public static void sub(float[] u, float[] v, float[] res) {
		res[0] = u[0] - v[0];
		res[1] = u[1] - v[1];
		res[2] = u[2] - v[2];
	}
	
	/**
	 * 两个向量的点积
	 * @param u
	 * @param v
	 * @return
	 */
	public static float dot(float[] u, float[] v) {
		return u[0] * v[0] + u[1] * v[1] + u[2] * v[2];
	}
	
	/**
	 * 设置向量u的值
	 * @param u
	 * @param offsetU
	 * @param x
	 * @param y
	 * @param z
	 */
	public static void set(float[] u, int offsetU, float x, float y, float z) {
		u[offsetU++] = x;
		u[offsetU++] = y;
		u[offsetU++] = z;
	}

	/**
	 * 将向量u拷贝到v中，如果u和v是同一个数组，那么其实际范围不能重叠
	 */
	public static void copy(float[] u, int offsetU, float[] v, int offsetV) {
		v[offsetV++] = u[offsetU++];
		v[offsetV++] = u[offsetU++];
		v[offsetV++] = u[offsetU++];
	}
	
	/**
	 * 计算向量的模
	 */
	public float magnitude() {
		return (float) Math.sqrt(x * x + y * y + z * z);
	}
	
}