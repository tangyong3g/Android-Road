package com.graphics.engine.scroller.effector.subscreeneffector;

import com.graphics.engine.graphics.GLCanvas;
import com.graphics.engine.scroller.ScreenScroller;

/**
 * 
 * 类描述:长方体特效(外部)
 * 功能详细描述:
 * 
 * @author  songsiyu
 * @date  [2012-9-3]
 */
public class CuboidInsideEffector extends FlipEffector {
	final static float PI4 = (float) Math.PI / 4; // CHECKSTYLE IGNORE THIS LINE
	final static float SQRT2 = (float) Math.sqrt(2);

	protected int mTranZ = 0; // （当盒子边长大于CameraZ时），为了使其他视图不位于 Camera 后面（-Z方向），
					// 需要将视图向前移动 mTranZ。只是裁剪视图位于 Camera 后面的部分是不可行的，
					// 貌似是因为底层实现是使用裁剪前的顶点坐标来投影并计算屏幕上的增量。
	protected float mScale = 1; // 向前移动之后视图在屏幕上的投影会变小，那么必须放大一个倍数来修正

	public CuboidInsideEffector() {
		mCombineBackground = false;
	}

	@Override
	public void onSizeChanged() {
		super.onSizeChanged();
		mRadRatio = -PI2 / mScreenSize;
		mAngleRatio = -RIGHT_ANGLE / mScreenSize;
		mInnerRadius = -mScreenSize * HALF;
		mOuterRadius = mInnerRadius * SQRT2;
		mScale = 1 + mScreenSize / (float) CAMERAZ;
		mTranZ = mScreenSize;

	}

	@Override
	float computeCurrentDepthZ(float rad) {
		// 对于盒子来说，b=pi/2, 1/sin(b/2)=sqrt(2)
		return mInnerRadius + (mInnerRadius - (float) (Math.cos(rad - PI4)) * mOuterRadius) // 动态深度控制 
				+ mTranZ;
	}

	@Override
	protected void transform(GLCanvas canvas, float angle) {
		mScale = 1 + mScreenSize / canvas.getCameraZ();
		canvas.translate(0, 0, -mCenterZ);
		mCenterY = (mScroller.getScreenHeight() - mScroller.getScreenOffsetY()) * HALF;
		if (mOrientation == ScreenScroller.HORIZONTAL) {
			canvas.translate(mScroll + mCenterX, mCenterY);
			canvas.scale(mScale, mScale);
			canvas.rotateAxisAngle(angle, 0, 1, 0);
		} else {
			canvas.translate(mCenterX, mScroll + mCenterY);
			canvas.scale(mScale, mScale);
			canvas.rotateAxisAngle(-angle, 1, 0, 0);
		}
		if (mInnerRadius != 0) {
			canvas.translate(0, 0, mInnerRadius);
		}
		canvas.translate(-mCenterX, -mCenterY);
	}
}
