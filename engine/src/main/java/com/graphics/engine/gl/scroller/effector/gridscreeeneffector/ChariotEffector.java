package com.graphics.engine.gl.scroller.effector.gridscreeeneffector;

import com.graphics.engine.gl.graphics.GLCanvas;

import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

/**
 * 
 * <br>类描述:咕噜咕噜滚效果
 * <br>功能详细描述:
 * 
 * @author  songsiyu
 * @date  [2012-9-3]
 */
public class ChariotEffector extends MGridScreenEffector {
	public static final float FACTOR = 0.618f * 1.5f;
	public static final float RADIUS_RATIO = 0.48f;
	public static final float RATIO = 2.05f;
	public static final float T_RATIO = 1.5f;

	float mRadius;
	float mRatio;

	static Interpolator sAccInterpolator = new AccelerateInterpolator(FACTOR);
	static Interpolator sDecInterpolator = new DecelerateInterpolator(FACTOR);

	@Override
	public void onSizeChanged(int w, int h) {
		super.onSizeChanged(w, h);
		mRadius = (Math.min(w, h) - mContainer.getCellHeight()) * RADIUS_RATIO;
		mRatio = RATIO / w; // 改成比2.0大可以让两个圈同时存在的时间段增大		
	}
	
	@Override
	protected void onDrawScreen(GLCanvas canvas, int screen, float offset) {
		requestQuality(canvas, GridScreenEffector.DRAW_QUALITY_HIGH);

		final GridScreenContainer container = mContainer;
		final int row = container.getCellRow();
		final int col = container.getCellCol();
		final int screenWidth = container.getWidth();
		int index = row * col * screen;
		final int end = Math.min(container.getCellCount(), index + row * col);
		final int count = end - index;
		if (count <= 0) {
			return;
		}
		float t = offset * mRatio;
		final float dAngle = -FULL_ANGLE / count;
		final double dRad = Math.toRadians(dAngle);
		final float sin = (float) Math.sin(dRad);
		final float cos = (float) Math.cos(dRad);
		float iconDstX = mRadius, iconDstY = 0, iconDstAngle = -RIGHT_ANGLE;
		if (count == 1) {
			iconDstX = 0; // 只有一个的时候把它画在圆心处
			iconDstAngle = -FULL_ANGLE;
		}
		canvas.translate(mCenterX, mCenterY);

		//上下偏移
		if (mVerticalSlide) {
			float angleX = getAngleX(Math.min(mRatio * Math.abs(mScroller.getCurrentScreenOffset())
					* 2, 1));
			canvas.rotateAxisAngle(angleX, 1, 0, 0);
		}

		final boolean isScrollAtEnd = mScroller.isScrollAtEnd();
		if (isScrollAtEnd) {
			t *= T_RATIO; // 这样在两端的时候更容易组成圈
		}
		// 以下注释的代码是对cell做包围盒裁剪的，
		// 如果cell的内部结构比较简单，其实优化作用不明显
		float sinWheelAngle = 0, cosWheelAngle = 1;
		if (t > 1) {
			sinWheelAngle = (float) Math.sin((Math.PI * HALF) * (t - 1));
			cosWheelAngle = (float) Math.cos((Math.PI * HALF) * (t - 1));
			canvas.rotate(RIGHT_ANGLE * (t - 1));
			t = 1;
		} else if (t < -1) {
			sinWheelAngle = (float) Math.sin((Math.PI * HALF) * (t + 1));
			cosWheelAngle = (float) Math.cos((Math.PI * HALF) * (t + 1));
			canvas.rotate(RIGHT_ANGLE * (t + 1));
			t = 1;
		} else if (t < 0) {
			t = -t;
		}
		float t1 = t, t2 = t;
		if (isScrollAtEnd) {
			// 修改插值方式，使在两端恢复时动作更平滑
			t1 = sAccInterpolator.getInterpolation(t);
			t2 = sDecInterpolator.getInterpolation(t);
		}
		final int cellWidth = container.getCellWidth();
		final int cellHeight = container.getCellHeight();
		final int paddingLeft = container.getPaddingLeft();
		final int paddingTop = container.getPaddingTop();
		final int cellCenterX = cellWidth / 2;
		final int cellCenterY = cellHeight / 2;
		final int cellRadius = (int) (Math.hypot(cellWidth, cellHeight) * HALF) + 2;
		// 计算物理屏幕在当前坐标系的左右边界值
		final float screenLeft = -mCenterX - offset;
		final float screenRight = mCenterX - offset;
		canvas.translate(-screenWidth * screen, 0);
		for (int i = 0, cellY = paddingTop + cellCenterY; i < row && index < end; ++i) {
			for (int j = 0, cellX = paddingLeft + cellCenterX; j < col && index < end; ++j, ++index) {
				final float x = interpolate(cellX - mCenterX, iconDstX, t1);
				final float y = interpolate(cellY - mCenterY, iconDstY, t1);
				final float x2 = x * cosWheelAngle - y * sinWheelAngle;
				if (x2 - cellRadius < screenRight && x2 + cellRadius >= screenLeft) {
					final float a = interpolate(0, iconDstAngle, t2);
					canvas.save();
					canvas.translate(screenWidth * screen + x, y);
					canvas.rotateAxisAngle(-a, 0, 0, 1);
					canvas.translate(-(screenWidth * screen + cellX), -cellY);
					//					canvas.translate(-cellCenterX, -cellCenterY);
					container.drawScreenCell(canvas, screen, index);

					canvas.restore();
				}
				final float xBak = iconDstX;
				iconDstX = cos * xBak - sin * iconDstY;
				iconDstY = sin * xBak + cos * iconDstY;
				iconDstAngle += dAngle;
				cellX += cellWidth;
			}
			cellY += cellHeight;
		}
	}

	@Override
	public void onDrawScreen(GLCanvas canvas, int screen, int offset) {
		onDrawScreen(canvas, screen, (float) offset);
	}

	
	@Override
	public boolean isFloatAdapted() {
		return true;
	}
}
