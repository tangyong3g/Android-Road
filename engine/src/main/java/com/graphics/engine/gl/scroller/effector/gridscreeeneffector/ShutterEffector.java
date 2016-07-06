package com.graphics.engine.gl.scroller.effector.gridscreeeneffector;


import com.graphics.engine.gl.graphics.GLCanvas;

/**
 * 
 * 类描述:百叶窗效果
 * 功能详细描述:
 * 
 * @author  chenjiayu
 * @date  [2012-7-23]
 */
public class ShutterEffector extends MGridScreenEffector {
	float mRatio;
	public boolean mRotateByCell = false; // 绕单元格本身的中心旋转

	@Override
	public void onSizeChanged(int w, int h) {
		super.onSizeChanged(w, h);
		mRatio = 1.0f / w;
	}
	
	@Override
	public void onDrawScreen(GLCanvas canvas, int screen, int offset) {
		onDrawScreen(canvas, screen, (float) offset);
	}

	@Override
	public void onDrawScreen(GLCanvas canvas, int screen, float offset) {
		final float angle = offset * mRatio * 180;
		if (Math.abs(angle) > RIGHT_ANGLE) {
			return;
		}
		final GridScreenContainer container = mContainer;
		final int row = container.getCellRow();
		final int col = container.getCellCol();
		int index = row * col * screen;
		final int end = Math.min(container.getCellCount(), index + row * col);
		final int cellWidth = container.getCellWidth();
		final int cellHeight = container.getCellHeight();
		final int paddingLeft = container.getPaddingLeft();
		final int paddingTop = container.getPaddingTop();
		final int screenWidth = container.getWidth();
		final float centerX = cellWidth * 0.5f;
		final float centerY = (mRotateByCell ? cellHeight : container.getHeight()) * 0.5f;
		//		final float depthZ = centerX * (float)Math.sin(Math.toRadians(Math.abs(angle))); 
		canvas.translate(-offset, 0);
		canvas.translate(-screenWidth * screen, 0);
		requestQuality(canvas, GridScreenEffector.DRAW_QUALITY_HIGH);
		for (int i = 0, cellY = paddingTop; i < row && index < end; ++i) {
			for (int j = 0, cellX = paddingLeft; j < col && index < end; ++j, ++index) {
				canvas.save();
				final float cy = mRotateByCell ? centerY : centerY - cellY;
				canvas.translate(screenWidth * screen + cellX + centerX, cellY + cy);
				canvas.rotateAxisAngle(angle, 0, 1, 0);
				canvas.translate(-(screenWidth * screen + cellX + centerX), -(cellY + cy));
				container.drawScreenCell(canvas, screen, index);
				canvas.restore();
				cellX += cellWidth;
			}
			cellY += cellHeight;
		}
	}
	
	
	@Override
	public boolean isFloatAdapted() {
		return true;
	}
}
