package com.graphics.engine.gl.scroller.effector.gridscreeeneffector;


import android.view.animation.AccelerateInterpolator;
import android.view.animation.Interpolator;
import com.graphics.engine.gl.graphics.GLCanvas;

/**
 * 
 * <br>类描述:双子星效果
 * <br>功能详细描述:
 * 
 * @author  songsiyu
 * @date  [2012-9-3]
 */
public class BinaryStarEffector extends MGridScreenEffector {
	float mRatio;

	static Interpolator sInterpolator = new AccelerateInterpolator();

	@Override
	public void onSizeChanged(int w, int h) {
		super.onSizeChanged(w, h);
		mRatio = 2.0f / w;
	}
	
	@Override
	public void onDrawScreen(GLCanvas canvas, int screen, int offset) {
		onDrawScreen(canvas, screen, (float) offset);
	}

	@Override
	public void onDrawScreen(GLCanvas canvas, int screen, float offset) {
		final GridScreenContainer container = mContainer;
		final int row = container.getCellRow();
		final int col = container.getCellCol();
		int index = row * col * screen;
		final int end = Math.min(container.getCellCount(), index + row * col);
		float t = offset * mRatio;
		if (t < 0) {
			t = -t;
		}

		if (t > 1) {
			t = 1;
		}

		if (mScroller.isScrollAtEnd()) {
			t = sInterpolator.getInterpolation(t);
		}
		final int cellWidth = container.getCellWidth();
		final int cellHeight = container.getCellHeight();
		final int paddingLeft = container.getPaddingLeft();
		final int paddingTop = container.getPaddingTop();
		final float x1 = mCenterX - cellWidth * HALF;
		final float y1 = mCenterY - cellHeight * HALF;
		final int screenWidth = container.getWidth();
		float lastX = 0, lastY = 0;
		//先移动抵消掉scroller滑动的偏移值
		//		canvas.translate(-offset, 0);
		canvas.translate(-screenWidth * screen, 0);
		for (int i = 0, cellY = 0; i < row && index < end; ++i) {
			for (int j = 0, cellX = 0; j < col && index < end; ++j, ++index) {
				final float x = interpolate(cellX, x1, t);
				final float y = interpolate(cellY, y1, t);
				canvas.save();
				canvas.translate(x - cellX, y - cellY);
				container.drawScreenCell(canvas, screen, index);
				lastX = x;
				lastY = y;
				cellX += cellWidth;
				canvas.restore();
			}
			cellY += cellHeight;
		}
	}

	
	@Override
	public boolean isFloatAdapted() {
		return true;
	}
}
