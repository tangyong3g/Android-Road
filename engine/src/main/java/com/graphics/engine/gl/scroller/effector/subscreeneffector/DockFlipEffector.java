package com.graphics.engine.gl.scroller.effector.subscreeneffector;

import com.go.gl.graphics.GLCanvas;

import android.util.FloatMath;

/**
 * 
 * <br>
 * 类描述:dock栏滑动特效 <br>
 * 功能详细描述:
 * 
 * @author songsiyu
 * @date [2013-1-5]
 */
public class DockFlipEffector extends MSubScreenEffector {

	private float mRatio;
	private float mRadiu;

	@Override
	public void onSizeChanged() {
		super.onSizeChanged();
		mRatio = 1.0f / mWidth;
		mRadiu = mWidth / 3 * 2;
	}

	@Override
	protected boolean onDrawScreen(GLCanvas canvas, int screen, int offset1,
			boolean first) {
		
		float offset = mScroller.getCurrentScreenDrawingOffset(first);
		canvas.translate(mScroll, 0);
		int oldAlpha = canvas.getAlpha();
		float percent = Math.abs(offset) * mRatio;
		int alpha = (int) (255 * (1 - percent));
		canvas.multiplyAlpha(alpha);

		// 计算translateX和translateZ时用的坐标系，是以旋转中心为原点，以原Z轴负方向为Y方向的坐标系统。
		// 在此坐标系中，不滑动时，屏幕的中心点坐标为（0，-mRadiu）
		float angle = (float) (offset * mRatio * Math.PI - Math.PI / 2);
		float translateX = FloatMath.cos(angle) * mRadiu;
		float translateZ = FloatMath.sin(angle) * mRadiu - (-mRadiu);

		canvas.translate(translateX, 0, -translateZ);
		mContainer.drawScreen(canvas, screen);
		canvas.setAlpha(oldAlpha);
		return false;
	}

}
