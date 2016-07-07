package com.graphics.engine.gl.scroller.effector.gridscreeeneffector;


import com.graphics.engine.gl.graphics.GLCanvas;

/**
 * 
 * 类描述:功能表整屏飞入飞出特效
 * 功能详细描述:
 * 
 * @author  chenjiayu
 * @date  [2012-7-23]
 */

public class FlyAwayEffector extends MGridScreenEffector {

	private float mRatio;

	@Override
	public void onSizeChanged(int w, int h) {
		mRatio = 1.0f / mScroller.getScreenWidth();
	}
	
	@Override
	public void onDrawScreen(GLCanvas canvas, int screen, int offset) {
		onDrawScreen(canvas, screen, (float) offset);
	}

	@Override
	public void onDrawScreen(GLCanvas canvas, int screen, float offset) {
		final GridScreenContainer container = mContainer;
		final int row = container.getCellRow();
		int col = container.getCellCol();
		int index = row * col * screen;
		final int end = Math.min(container.getCellCount(), index + row * col);
		final int screenWidth = container.getWidth();
		float t = offset * mRatio;
		if (mScroller.isScrollAtEnd()) {
			col = Math.min(col, end - index);
		}
		//先移动抵消掉scroller滑动的偏移值
		canvas.translate(-offset, 0);
		canvas.translate(-screenWidth * screen, 0);
		if (t < 0) {
			float distanceZ = canvas.getCameraZ() / 2 + 1;
			float translateZ = t * distanceZ;
			for (int j = 0; j < col && index < end; ++j, ++index) {
				for (int i = 0, index2 = index; i < row && index2 < end; ++i) {
					canvas.save();
					canvas.translate(0, 0, -translateZ);
					container.drawScreenCell(canvas, screen, index2);
					canvas.restore();
					index2 += col;
				}
			}
		}
	}
	
	
	@Override
	public boolean isFloatAdapted() {
		return true;
	}

}
