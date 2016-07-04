package com.graphics.engine.gl.util;

/**
 * 
 * <br>类描述: 齐次向量类，4个分量
 * <br>功能详细描述:
 * 
 * @deprecated
 */
public class Vector4f {

	public float x, y, z, w;

	public Vector4f() {

	}

	public Vector4f(float x, float y, float z, float w) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
	}

	public void set(float x, float y, float z, float w) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
	}

	public void set(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public void set(Vector4f v) {
		set(v.x, v.y, v.z, v.w);
	}

	public void set(Vector3f v) {
		set(v.x, v.y, v.z, 0.0f);
	}

	public void add(float x, float y, float z) {
		this.x += x;
		this.y += y;
		this.z += z;
	}

}
