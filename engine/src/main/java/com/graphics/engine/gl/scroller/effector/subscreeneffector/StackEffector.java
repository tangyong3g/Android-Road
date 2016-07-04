package com.graphics.engine.gl.scroller.effector.subscreeneffector;

import com.go.gl.graphics.GLCanvas;
import com.go.gl.scroller.ScreenScroller;


/**
 * 
 * @author jiangxuwen
 * 
 */
public class StackEffector extends MSubScreenEffector {

	final static int Radius = 1; //CHECKSTYLE IGNORE

	// float mScaleRatio;
	float mAlphaRatio;
	float mScaleMin = 0.0f;
	float mScaleMax = 1.0f;

	public StackEffector() {
		mCombineBackground = false;
		mReverse = true;
	}

	@Override
	protected boolean onDrawScreen(GLCanvas canvas, int screen, int offset1, boolean first) {
		float offset = mScroller.getCurrentScreenDrawingOffset(first);
		
		mNeedQuality = false;
		float t;
		float s;
		float leftTop;

		if (first) {
			s = 0;
			leftTop = offset;
			mAlpha = 255;
		} else {
			leftTop = offset;
			t = 1 - offset / mScreenSize;
			s = (mScaleMax - mScaleMin) * t + mScaleMin;
			// 前3/13的时间里不进行透明度的变化
			mAlpha = (int) (255 * t * 1.3f);
			mAlpha = mAlpha > 255 ? 255 : mAlpha;
		}

		if (mOrientation == ScreenScroller.HORIZONTAL) {
			if (first) {
				canvas.translate(mScroll + leftTop, 0);
			} else {
				canvas.translate(mScroll + (1 - s) * 0.5f * mWidth, (1 - s) * 0.5f * mHeight);
				canvas.scale(s, s);
			}
		} else {
			if (first) {
				canvas.translate(0, mScroll + leftTop);
			} else {
				canvas.translate((1 - s) * 0.5f * mWidth, mScroll + (1 - s) * 0.5f * mHeight);
				canvas.scale(s, s);
			}
		}
		return true;
	}

	@Override
	public void onSizeChanged() {
		super.onSizeChanged();
		// mScaleRatio = (float)Math.PI / (Radius * 2 + 1) / mScreenSize;
		mAlphaRatio = (float) Math.PI / (Radius * 2) / mScreenSize;
	}

}
