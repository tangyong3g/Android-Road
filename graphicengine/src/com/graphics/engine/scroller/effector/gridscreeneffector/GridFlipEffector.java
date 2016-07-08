package com.graphics.engine.scroller.effector.gridscreeneffector;


import com.graphics.engine.graphics.GLCanvas;

/**
 * 
 */
public class GridFlipEffector extends MGridScreenEffector {

	private final float mRatioAngle = 180;
	private int mPageCount;
	private float mPerPercent;
	
	@Override
	public void onDrawScreen(GLCanvas canvas, int screen, int offset) {
		onDrawScreen(canvas, screen, (float) offset);
	}

	@Override
	protected void onDrawScreen(GLCanvas canvas, int screen, float offset) {
		final GridScreenContainer container = (GridScreenContainer) mContainer;
		final int row = container.getCellRow();
		int col = container.getCellCol();
		int pageCount = row * col;
		mPageCount = pageCount;
		int firstIndex = pageCount * screen;
		final int end = Math.min(container.getCellCount(), firstIndex + pageCount);
		final int screenWidth = container.getWidth();
		final int cellWidth = container.getCellWidth();
		float percent = 1.0f * offset / screenWidth;
		float curScreenAngle = percent * mRatioAngle;
		canvas.translate(-offset, 0);
		float tempAngleY = curScreenAngle;
		boolean isEnd = false;
		for (int i = 0; i < row; i++) {
			for (int j = 0; j < col; j++) {
				int index = i * col + j;
				if (index > end) {
					isEnd = true;
					break;
				}
				if (percent >= 0) {
					float startAngle = getStartPercent(index) * 180;
					tempAngleY = (curScreenAngle - startAngle) / mPerPercent;
					tempAngleY = Math.max(0, Math.min(180, tempAngleY));

				} else {
					float startAngle = ((getStartPercent(index) - 1)) * 180;
					tempAngleY = (curScreenAngle - startAngle) / mPerPercent - 180;
					tempAngleY = Math.max(-180, Math.min(0, tempAngleY));
				}

				float refZ = canvas.getCameraZ();
				float temp = j * cellWidth + cellWidth / 2 - mScroller.getScreenWidth() / 2;
				double radianValue = Math.atan2(refZ, temp);
				double tempAngle = (HALF_ANGLE * radianValue / Math.PI) - HALF_ANGLE;
				if (!(tempAngle < tempAngleY && tempAngleY < (HALF_ANGLE - Math.abs(tempAngle)))) {
					continue;
				}
				index += firstIndex;
				canvas.save();
				canvas.translate(+(0.5f + j) * cellWidth, 0);
				canvas.rotateAxisAngle(tempAngleY, 0, 1, 0);
				canvas.translate(-(0.5f + j) * cellWidth, 0);
				container.drawScreenCell(canvas, screen, index);
				canvas.restore();
			}
			if (isEnd) {
				break;
			}
		}

	}

	private float getStartPercent(int index) {
		float startPercent = 0;
		/**
		 * 目前的效果是前一片旋转了30度之后后一片开始旋转，off代表两片之间同时旋转的角度的百分比。比如现在完整角度是180，则off = （180-30）／（180+150）
		 */
		float off = 0.45f;
		float pertime = (1 + (mPageCount - 1) * off) / mPageCount;
		mPerPercent = pertime;
		startPercent = index * pertime - index * off;
		return startPercent;
	}
	
	
	@Override
	public boolean isFloatAdapted() {
		return true;
	}
}
