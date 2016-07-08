package com.graphics.engine.scroller.effector.subscreeneffector;

import android.graphics.Matrix;

import com.graphics.engine.animation.Transformation3D;
import com.graphics.engine.graphics.GLCanvas;
import com.graphics.engine.scroller.ScreenScroller;

/**
 * 
 * 类描述:长方体特效(内部)
 * 功能详细描述:
 * 
 * @author  songsiyu
 * @date  [2012-9-3]
 */
public class CuboidOutsideEffector extends FlipEffector {

	final static int POINTS_COUNT = 4;
	final static float SQRT2 = (float) Math.sqrt(2);
	//	static final float HALF_PI = (float)Math.PI / 2;
	float mCullPassAngle; // 旋转小于该角度时，视图是正面的
	float mCullFailAngle; // 旋转大于该角度时，视图是反面的
	static float[] sPoints = new float[POINTS_COUNT];
	Matrix mMatrix = new Matrix();
	Transformation3D mTransformation = new Transformation3D();

	public CuboidOutsideEffector() {
		mCombineBackground = false;
	}

	@Override
	public void onSizeChanged() {
		super.onSizeChanged();
		mRadRatio = PI2 / mScreenSize;
		mAngleRatio = RIGHT_ANGLE / mScreenSize;
		mInnerRadius = mScreenSize * HALF;
		mOuterRadius = mInnerRadius * SQRT2;
		final float maxZ = computeCurrentDepthZ(PI4);
		// 角度的绝对值增大时，在透视作用下会逐渐从正面变为反面，临界角的方程为
		// cos(angle)=mInnerRadius/(mCenterZ+CameraZ), mCenterZ=[mInnerRadius, maxZ]
		// 深度增大时，解出来的角度的绝对值也增大，因此mCullPassAngle一定还是正面的，
		// mCullFailAngle一定已经是反面了
		mCullPassAngle = (float) Math.toDegrees(Math.acos(mInnerRadius / (mInnerRadius + CAMERAZ)));
		mCullFailAngle = (float) Math.toDegrees(Math.acos(mInnerRadius / (maxZ + CAMERAZ)));
		mOvershootPercent = (int) ((1 - mCullFailAngle / RIGHT_ANGLE) * 100); // CHECKSTYLE IGNORE THIS LINE
		mScroller.setOvershootPercent(mOvershootPercent);
		// 当mScreenSize=480时，mCullPassAngle=72.89536，
		// mCullFailAngle=74.800705，mOvershootPercent=16
		//		mCenterZ = mWidth/SQRT2;
	}

	float computeCurrentDepthZ(float rad) {
		// 对于盒子来说，b=pi/2, 1/sin(b/2)=sqrt(2)
//		return (float) Math.cos(rad - PI4) * mOuterRadius;
		return (float) mWidth / 2;
	}

	@Override
	protected boolean onDrawScreen(GLCanvas canvas, int screen, int offset1, boolean first) {
		float offset = mScroller.getCurrentScreenDrawingOffset(first);
		
		final float angle = offset * mAngleRatio;
		final float angleAbs = Math.abs(angle);
		if (angleAbs > mCullFailAngle) {
			return false;
		}
		transform(canvas, angle);
		if (angleAbs < mCullPassAngle) {
			return true;
		}
		return false;
		//TODO:3D的矩阵不能直接用到2D Matrix实例
//		throw new RuntimeException("3D的矩阵不能直接用到2D Matrix实例");
		//		canvas.getMatrix(mTransformation);
		//		mMatrix.setValues(mTransformation.getMatrix());
		//		return frontFaceTest(mMatrix,mPoints,this);
	}
	
	static boolean frontFaceTest(Matrix matrix, float[] points, MSubScreenEffector effector) {
		points[0] = 0;
		points[1] = 0;
		points[2] = effector.mWidth;
		points[3] = effector.mHeight;			// CHECKSTYLE IGNORE
		matrix.mapPoints(points);
		if (effector.mOrientation == ScreenScroller.HORIZONTAL) {
			// 透视投影后，左边界和右边界未穿越，表示还是正面
			return points[0] + 1 < points[2];
		}
		return points[1] + 1 < points[3];		// CHECKSTYLE IGNORE
	}
}
