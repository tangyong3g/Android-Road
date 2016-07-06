package com.graphics.engine.gl.scroller.effector.gridscreeeneffector;

import com.graphics.engine.gl.graphics.GLCanvas;

/**
 * 
 * <br>类描述:功能表弦效果
 * <br>功能详细描述:
 * 
 * @author  songsiyu
 * @date  [2012-9-3]
 */
public class ChordEffector extends MGridScreenEffector {

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
	protected void onDrawScreen(GLCanvas canvas, int screen, float offset) {
		final GridScreenContainer container = (GridScreenContainer) mContainer;
		final int row = container.getCellRow();
		int col = container.getCellCol();
		int index = row * col * screen;
		final int end = Math.min(container.getCellCount(), index + row * col);
		final int cellWidth = container.getCellWidth();
		final int cellHeight = container.getCellHeight();
		final float centerX = cellWidth * 0.5f;
		final float centerY = cellHeight * 0.5f;
		final int screenWidth = container.getWidth();
		float t = offset * mRatio;
		if (mScroller.isScrollAtEnd()) {
			col = Math.min(col, end - index);
		} else {
			t *= col;
		}
		//先移动抵消掉scroller滑动的偏移值
		canvas.translate(-offset, 0);
		canvas.translate(-screenWidth * screen, 0);
		for (int j = 0; j < col && index < end; ++j, ++index) {
			float angle = 0;
			if (t > 0) {
				angle = Math.max(0, Math.min(t - j, 1)) * HALF_ANGLE;
			} else {
				angle = Math.max(-1, Math.min((col - 1 - j) + t, 0)) * HALF_ANGLE;
			}
			float refZ = canvas.getCameraZ();
			int indexCol = index % col;
			float temp = indexCol * cellWidth + centerX - mScroller.getScreenWidth() / 2;
			double radianValue = Math.atan2(refZ, temp);
			double tempAngle = (HALF_ANGLE * radianValue / Math.PI) - HALF_ANGLE;
			/*
			 * 翻页时候可以显示出来的view的angle，tempAngle由于view转动的方向，所以为负数，假如tempAngle = -80°，
			 * 那么两屏view可见的角度就为-80°~110°
			 */
			//			if(tempAngle < angle && angle < (180 - Math.abs(tempAngle))){
			//				for(int i = 0, index2 = index; i < row && index2 < end; ++i) {
			//					container.drawScreenCell(canvas,screen,index2);
			//					index2 += col;
			//				}
			//			}else{
			//				for(int i = 0, index2 = index; i < row && index2 < end; ++i) {
			//					container.resetScreenCell(screen,index2);
			//					index2 += col;
			//				}
			//			}

			if (tempAngle < angle && angle < (HALF_ANGLE - Math.abs(tempAngle))) {
				for (int i = 0, index2 = index; i < row && index2 < end; ++i) {
					canvas.save();
					canvas.translate(screenWidth * screen + j * cellWidth + centerX, -(i
							* cellHeight + centerY), 0);
					canvas.rotateAxisAngle(angle, 0, 1, 0);
					canvas.translate(-(screenWidth * screen + j * cellWidth + centerX), i
							* cellHeight + centerY, 0);
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
