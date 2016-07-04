package com.graphics.engine.gl.scroller.effector.subscreeneffector;

import com.go.gl.graphics.GLCanvas;

/**
 * 放大缩小、淡入淡出效果
 * @author chenjingmian
 *
 */
public class FlyAwayScreenEffector extends MSubScreenEffector {

	private float mRatio;
	private float mAlphaRatio;
	private static final float SCALEMIN = 0.1f;
	private static final float SCALENORMAL = 1.0f;
	private static final float SCALEMAX = 1.5f;
	private static final float ALPHAMAX = 255f;
	private final static int RADIUS = 1;

	@Override
	public void onSizeChanged() {
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

		float scale = 1;
		float t = Math.abs(offset) * mRatio;

		if (first) {
			scale = SCALENORMAL - (SCALENORMAL - SCALEMIN) * t;
		} else {
			scale = SCALENORMAL + (SCALEMAX - SCALENORMAL) * t;
		}

		//为了效果最佳，不用sin和cos变化函数，直接用匀速变化
		//		t = (float)Math.cos(Math.abs(offset) * mAlphaRatio);
		mAlpha = (int) (ALPHAMAX * (1 - t));

		canvas.scale(scale, scale, mCenterX, mCenterY);

		return true;
	}

}
