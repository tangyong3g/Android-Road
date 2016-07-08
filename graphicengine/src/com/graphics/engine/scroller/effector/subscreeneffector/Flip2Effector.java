package com.graphics.engine.scroller.effector.subscreeneffector;

import com.graphics.engine.graphics.GLCanvas;

/**
 * 翻屏幕特效
 * @author chenjiayu
 *
 */
public class Flip2Effector extends MSubScreenEffector {

	private float mRatioAngle;
	
	@Override
	public void onSizeChanged() {
		super.onSizeChanged();
		mRatioAngle = HALF_ANGLE / mWidth;
	}
	
	@Override
	protected boolean onDrawScreen(GLCanvas canvas, int screen, int offset1,
			boolean first) {
		float offset = mScroller.getCurrentScreenDrawingOffset(first);
		
		canvas.translate(mScroll, 0);
		final float angleY = offset * mRatioAngle;
		final float angleAbs = Math.abs(angleY);
		if (angleAbs >= RIGHT_ANGLE) {
			return false;
		}
		canvas.translate(mWidth / 2, 0);
		canvas.rotateAxisAngle(angleY, 0, 1, 0);
		canvas.translate(-mWidth / 2, 0);
		return true;
	}

}
