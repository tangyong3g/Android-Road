package com.graphics.engine.gl.view;


import android.view.MotionEvent;

import com.graphics.engine.gl.graphics.GLCanvas;
import com.graphics.engine.gl.math3d.GeometryPools;
import com.graphics.engine.gl.math3d.Matrix;
import com.graphics.engine.gl.math3d.Plane;
import com.graphics.engine.gl.math3d.Point;
import com.graphics.engine.gl.math3d.Quaternion;
import com.graphics.engine.gl.math3d.Ray;
import com.graphics.engine.gl.math3d.Vector;

/**
 * 
 * <br>类描述: 虚拟跟踪球工具类
 * <br>功能详细描述:
 * 基于四元数，支持对物体进行任意轴的旋转。
 * 
 * <br>TODO：增加复位以及自动对齐到坐标轴的动画
 * @author  dengweiming
 * @date  [2013-7-10]
 */
public class VirtualTrackBall {
	//算法参见 Game Programming Gems 1 的 2.10 章节样例

	/** 要旋转的物体的中心位置 */
	private Point mPos = new Point();
	/** 要旋转的物体的包围球大小 */
	private float mRadius;
	private Plane mPlane = new Plane();
	private Vector mDirection = new Vector();
	private boolean mTouched = false;
	private Quaternion mQuaternion = new Quaternion();
	

	/**
	 * 设置物体的包围球的位置和半径
	 */
	public void setBoundingSphere(float x, float y, float z, float r) {
		mPos.set(x, y, z);
		mRadius = r;
	}

	/**
	 * <br>功能简述: 触摸事件响应
	 * <br>功能详细描述:
	 * <br>注意: 
	 * @param ray 从视点到触摸点的射线，可以使用 {@link GLView#getTouchRay(Ray, boolean)} 获得
	 * @param action {@link MotionEvent#getAction()}
	 */
	public void onTouch(Ray ray, int action) {
		GeometryPools.saveStack();
		
		Point eyePos = ray.getPoint(0);
		Vector normal = mPos.sub(eyePos);
		mPlane.set(mPos, normal);
		mPlane.intersect(ray);	// assume to be intersected
		Point hitPoint = ray.getHitPoint();
		Vector d = hitPoint.sub(mPos);

		float len = d.length();
		if (len < mRadius) {
			normal.normalize();
			// let d point at sphere surface
			Vector v = normal.mul((float) Math.sqrt(mRadius * mRadius - len * len));
			d.sub(v).setTo(d);
		}

		d.normalize();
		// touch down 的时候需要重置 mDirection，但是考虑到有可能会收不到 down 事件，
		// 所以在 touch up 的时候要求下一次触摸时重置
		if (!mTouched) {
			mTouched = true;
			mDirection.set(d);
		}
		if ((action & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP) {
			mTouched = false;
		}

		Quaternion q = Quaternion.rotateArc(mDirection, d);
		q.mul(mQuaternion).setTo(mQuaternion);
		mDirection.set(d);

		GeometryPools.restoreStack();
	}

	/**
	 * 获取旋转矩阵作用到画布上
	 * @param center 是否移动到中心位置再旋转并且移动回来，否则只是旋转
	 */
	public void applyRotation(GLCanvas canvas, boolean center) {
		GeometryPools.saveStack();
		Matrix matrix = mQuaternion.toMatrix();
		if (center) {
			canvas.translate(mPos.x, mPos.y, mPos.z);
			canvas.concat(matrix.getValues(), 0);
			canvas.translate(-mPos.x, -mPos.y, -mPos.z);
		} else {
			canvas.concat(matrix.getValues(), 0);
		}
		GeometryPools.restoreStack();
	}
	
	/**
	 * 清除旋转
	 */
	public void clearRotation() {
		mQuaternion.set(0, 0, 0, 1);
	}

}
