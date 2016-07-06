package com.graphics.engine.gl.scroller.effector.subscreeneffector;


import com.graphics.engine.gl.graphics.GLCanvas;
import com.graphics.engine.gl.scroller.ScreenScroller;

/**
 * 
 */
public class BulldozeEffector extends MSubScreenEffector {
	public BulldozeEffector() {
		super();
		mCombineBackground = false;
	}

	@Override
	protected boolean onDrawScreen(GLCanvas canvas, int screen, int offset1, boolean first) {
		float offset = mScroller.getCurrentScreenDrawingOffset(first);
		
		mNeedQuality = false;
		float min, max;

		if (first) {
			min = 0;
			max = offset + mScreenSize;
		} else {
			min = offset;
			max = mScreenSize;
		}

		if (mOrientation == ScreenScroller.HORIZONTAL) {
//			canvas.translate(mScroll + min, 0);
//			canvas.scale((float) (max - min) / mWidth, 1);
			double a = Math.PI * (max - min) / mScreenSize;
			int halfW = mScreenSize / 2;
			float x = (float) (halfW - Math.cos(a) * halfW);
			float z = (float) (Math.sin(a) * halfW);
			float w = (float) (Math.sqrt(x * x + z * z));
			float angle = (float) (Math.asin(z / w) / Math.PI * 180);
			canvas.translate(mScroll + (first ? 0 : mScreenSize), 0);
			canvas.rotateAxisAngle(first ? -angle : angle, 0, 1, 0);

			float scale = (float) w / mScreenSize;
			canvas.scale(scale, 1);

			canvas.translate(first ? 0 : -mScreenSize, 0);
		} else {
			canvas.translate(0, mScroll + min);
			canvas.scale(1, (float) (max - min) / mHeight);
		}
		return true;
	}
}
