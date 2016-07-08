package com.graphics.engine.scroller.effector.subscreeneffector;

import android.graphics.Matrix;

import com.graphics.engine.animation.Transformation3D;
import com.graphics.engine.graphics.GLCanvas;
import com.graphics.engine.scroller.ScreenScroller;

/**
 * 
 * 类描述:飞走特效
 * 功能详细描述:
 * 
 * @author  songsiyu
 * @date  [2012-9-3]
 */
public class FlipEffector extends MSubScreenEffector {
	static final float CAMERAZ = 1200f; // 底层skia图形库定义的常量576f
	// 这里已经不用Camera去做深度变化，所以必须用canvas.getRefZ()去获取
	static final Matrix MATRIX = new Matrix();
	protected float mInnerRadius = 0; // 旋转中心到视图中心的距离（内切圆半径）
	protected float mOuterRadius; // 旋转中心到视图两边的距离（外接圆半径）
	protected float mCenterZ; // 旋转中心的z坐标
	protected float mRadRatio;
	protected float mAngleRatio;
	protected float mRatio;
	Transformation3D mTransformation = new Transformation3D();

	public FlipEffector() {
		mCombineBackground = false;
	}

	@Override
	public void onSizeChanged() {
		super.onSizeChanged();
		mRadRatio = (float) Math.PI / mScreenSize;
		mAngleRatio = HALF_ANGLE / mScreenSize;
		mOuterRadius = mScreenSize * HALF;
		mRatio = 1.0f / mWidth;
	}

	@Override
	protected boolean onDrawScreen(GLCanvas canvas, int screen, int offset1, boolean first) {
		float offset = mScroller.getCurrentScreenDrawingOffset(first);
		
		final float angleY = offset * mAngleRatio;
		final float angleAbs = Math.abs(angleY);
		if (angleAbs >= RIGHT_ANGLE) {
			return false;
		}
		canvas.save();
		if (mVerticalSlide) {
			float angleX = getAngleX(Math.min(mRatio * Math.abs(mScroller.getCurrentScreenOffset())
					* 2, 1));
			transformXandY(canvas, angleY, angleX);
		} else {
			transform(canvas, angleY);
		}

		return true;
	}

	float computeCurrentDepthZ(float rad) {
		// 在和旋转轴垂直的平面上，旋转中心到边（视图在zOx平面的投影）两端形成的角度为b时，
		// 则距离为mOuterRadius=mHalfScreenSize/sin(b/2)，
		// 当旋转角度为rad时，则该距离在Z轴上投影为d'=d*cos(rad-b/2)，
		// 则需将旋转中心移入深度d'，使得整个视图都在z+半空间中
		// 对于单面翻转模型，b=pi，1/sin(b/2)=1
//		return (float) Math.cos(rad - PI2) * mOuterRadius;
		return 0;
	}

	protected void transform(GLCanvas canvas, float angle) {
		canvas.translate(0, 0, -mCenterZ);
		if (mOrientation == ScreenScroller.HORIZONTAL) {
			canvas.translate(mScroll + mCenterX, mCenterY);
			canvas.rotateAxisAngle(angle, 0, 1, 0);
		} else {
			canvas.translate(mCenterX, mScroll + mCenterY);
			canvas.rotateAxisAngle(-angle, 1, 0, 0);
		}

		if (mInnerRadius != 0) {
			canvas.translate(0, 0, mInnerRadius);
		}
		canvas.translate(-mCenterX, -mCenterY);
	}

	void transformXandY(GLCanvas canvas, float angleY, float angleX) {
		canvas.translate(0, 0, -mCenterZ);
		if (mOrientation == ScreenScroller.HORIZONTAL) {
			canvas.translate(mScroll + mCenterX, mCenterY);
			canvas.rotateAxisAngle(angleX, 1, 0, 0);
			canvas.rotateAxisAngle(angleY, 0, 1, 0);
		} else {
			canvas.translate(mCenterX, mScroll + mCenterY);
			canvas.rotateAxisAngle(angleX, 1, 0, 0);
			canvas.rotateAxisAngle(-angleY, 1, 0, 0);
		}
		if (mInnerRadius != 0) {
			canvas.translate(0, 0, mInnerRadius);
		}
		canvas.translate(-mCenterX, -mCenterY);
	}

	@Override
	public void onScrollChanged(int scroll, int offset) {
		mCenterZ = computeCurrentDepthZ(Math.abs(offset * mRadRatio));
		super.onScrollChanged(scroll, offset);
	}

}
