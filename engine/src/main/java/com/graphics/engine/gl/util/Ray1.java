package com.graphics.engine.gl.util;

import com.go.gl.animation.Transformation3D;

/**
 * 
 * <br>类描述: 简单的射线类，可以检测一个3D的平面的触摸情况
 * <br>功能详细描述:
 */
public class Ray1 {
	private static final float EPSILON = 1e-6f;

	final float[] mOrigin = new float[4];		// CHECKSTYLE IGNORE
	final float[] mDirection = new float[4];	// CHECKSTYLE IGNORE
	
	public Ray1() {
		mOrigin[3] = 1;		// CHECKSTYLE IGNORE
		//初始化为负z轴单位向量
		mDirection[2] = -1;
	}
	
	/**
	 * 设置起点
	 * @param x
	 * @param y
	 * @param z
	 */
	public void setOrigin(float x, float y, float z) {
		mOrigin[0] = x;
		mOrigin[1] = y;
		mOrigin[2] = z;
	}
	
	/**
	 * 设置方向，会自动归一化
	 * @param x
	 * @param y
	 * @param z
	 */
	public void setDirection(float x, float y, float z) {
		final float square = x * x + y * y + z * z;
		if (Math.abs(square - 1) > EPSILON) {
			final float rLen = (float) (1 / Math.sqrt(square));
			x *= rLen;
			y *= rLen;
			z *= rLen;
		}
		mDirection[0] = x;
		mDirection[1] = y;
		mDirection[2] = z;
	}
	
	/**
	 * 设置终点，实际上会减掉起点位置并使用{@link #setDirection(float, float, float)} 来设置方向，
	 * 因此必须在{@link #setOrigin(float, float, float)之后调用
	 * 
	 * @param x
	 * @param y
	 * @param z
	 */
	public void setEndPoint(float x, float y, float z) {
		setDirection(x - mOrigin[0], y - mOrigin[1],  z - mOrigin[2]);
	}
	
	/**
	 * 获取射线 P = Q + t * V 上参数 t 对应的点的位置（Q为起点，V为方向单位向量）
	 * @param t
	 * @param res
	 * @param offset
	 */
	public void getPoint(float t, float[] res, int offset) {
		res[offset++] = mDirection[0] * t + mOrigin[0];
		res[offset++] = mDirection[1] * t + mOrigin[1];
		res[offset++] = mDirection[2] * t + mOrigin[2];
	}
	
	/**
	 * 将射线转换到局部坐标系
	 * 
	 * @param t
	 * @param outRay
	 */
	public void transformToLocal(Transformation3D t, Ray1 outRay) {
		t.inverseTransform(mOrigin, 0, outRay.mOrigin, 0, 1);
		t.inverseTransform(mDirection, 0, outRay.mDirection, 0, 0);
	}
	
	/**
	 * 计算和xOy平面的交点
	 * 
	 * @param xy 计算的交点结果,y轴是向 <b>上</b> 的
	 * @return 是否相交，如果为false，那么交点是未定义的
	 */
	public boolean getPointInSurface(float[] xy) {
		if (Math.abs(mDirection[2]) < EPSILON) {
			return false; //射线与xOy平面平行，无交点
		}
		final float t = -mOrigin[2] / mDirection[2];
		xy[0] = mDirection[0] * t + mOrigin[0];
		xy[1] = mDirection[1] * t + mOrigin[1];
		return true;
	}
	
}
