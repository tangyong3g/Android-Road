package com.graphics.engine.gl.scroller.effector.gridscreeeneffector;


import com.graphics.engine.gl.graphics.GLCanvas;

/**
 * 
 * 类描述:放大缩小特效
 * 功能详细描述:
 * 
 * @author  songsiyu
 * @date  [2012-9-3]
 */
public class ZoomEffector extends MGridScreenEffector {

	private float mRatio;

	@Override
	public void onSizeChanged(int w, int h) {
		super.onSizeChanged(w, h);
		mRatio = 1.0f / mScroller.getScreenWidth();
	}
	
	@Override
	public void onDrawScreen(GLCanvas canvas, int screen, int offset) {
		onDrawScreen(canvas, screen, (float) offset);
	}

	@Override
	public void onDrawScreen(GLCanvas canvas, int screen, float offset) {
		final GridScreenContainer container = mContainer;
		final float scale1 = 0.2f;
		final float scale2 = 0.8f;
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

		//上下偏移
		if (mVerticalSlide) {
			float angleX = getAngleX(Math.min(mRatio * Math.abs(mScroller.getCurrentScreenOffset())
					* 2, 1));
			rorateX(canvas, angleX, mCenterY);
		}

		for (int j = 0; j < col && index < end; ++j, ++index) {
			float scale = 0;
			//乘以2.0是为了图标能做从0~1的放大缩小
			if (t > 0) {
				scale = Math.max(0, Math.min(t - j, 1)) * 2.0f;
			} else {
				scale = Math.max(-1, Math.min((col - 1 - j) + t, 0)) * 2.0f;
			}
			//			if((0f <= Math.abs(scale) && Math.abs(scale) < 1.0f)){
			//				for(int i = 0, index2 = index; i < row && index2 < end; ++i) {
			//					if(Math.abs(scale) > 0.8f && Math.abs(scale) < 1.0f){
			////						container.setFlipParam(0.2f);
			//						container.drawScreenCell(canvas,screen,index2);
			//					}else{
			////						container.setFlipParam(1.0f - Math.abs(scale));
			//						container.drawScreenCell(canvas,screen,index2);
			//					}
			//					index2 += col;
			//				}
			//			}else{
			//				for(int i = 0, index2 = index; i < row && index2 < end; ++i) {
			//					container.resetScreenCell(screen,index2);
			//					index2 += col;
			//				}
			//			}
			float viewScale = 1.0f;
			if (0f <= Math.abs(scale) && Math.abs(scale) < 1.0f) {
				if (Math.abs(scale) > scale2 && Math.abs(scale) < 1.0f) {
					viewScale = scale1;
				} else {
					viewScale = 1.0f - Math.abs(scale);
				}
				for (int i = 0, index2 = index; i < row && index2 < end; ++i) {
					canvas.save();
					canvas.translate(screenWidth * screen + j * cellWidth + centerX, -(i
							* cellHeight + centerY), 0);
					canvas.scale(viewScale, viewScale, viewScale);
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
