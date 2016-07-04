package com.graphics.engine.gl.scroller.effector.subscreeneffector;

import com.go.gl.graphics.GLCanvas;

/**
 * 放大缩小、淡入淡出效果
 * @author chenjingmian
 *
 */
public class FlyAwayScreenEffectorForAppDrawer extends MSubScreenEffector {

	private float mRatio;
	private float mAlphaRatio;
	private static final float SCALEMIN = 0.0f;
	private static final float SCALENORMAL = 1.0f;
	private static final float SCALEMAX = 2.0f;
	private static final float ALPHAMAX = 255f;
	private final static int RADIUS = 1;

	@Override
	public void onSizeChanged() {
		super.onSizeChanged();
		mScreenSize = mScroller.getScreenSize();
		mRatio = 1.0f / mScreenSize;
		mAlphaRatio = (float) Math.PI / (RADIUS * 2) / mScreenSize;
		mCenterX = mScroller.getScreenWidth() * HALF;
		mCenterY = mScroller.getScreenHeight() * HALF;
	}

	@Override
	protected boolean onDrawScreen(GLCanvas canvas, int screen, int offset1, boolean first) {
		float offset = mScroller.getCurrentScreenDrawingOffset(first);
		
		//先移动抵消掉scroller滑动的偏移值
		canvas.translate(mScroll, 0);

		if (mVerticalSlide) {
			float angleX = getAngleX(Math.min(mRatio * Math.abs(mScroller.getCurrentScreenOffset())
					* 2, 1));
			canvas.translate(0, mCenterY);
			canvas.rotateAxisAngle(angleX, 1, 0, 0);
			canvas.translate(0, -mCenterY);
		}

		float scale = 1;
		float t = Math.abs(offset) * mRatio;

		if (first) {
			scale = SCALENORMAL - (SCALENORMAL - SCALEMIN) * t;
			// 前3/13的时间里不进行透明度的变化
			mAlpha = (int) (ALPHAMAX * (1.3 - t * 1.3));
			mAlpha = (int) (mAlpha > ALPHAMAX ? ALPHAMAX : mAlpha);
		} else {
			scale = SCALENORMAL + (SCALEMAX - SCALENORMAL) * t;
			mAlpha = (int) (ALPHAMAX * (1 - t));
		}

		canvas.scale(scale, scale, mCenterX, mCenterY);

		return true;

	}

}
