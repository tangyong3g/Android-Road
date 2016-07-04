package com.graphics.engine.gl.scroller.effector.gridscreeeneffector;

import com.go.gl.graphics.GLCanvas;

import android.graphics.DrawFilter;

/**
 * 
 * 类描述:球特效
 * 功能详细描述:
 * 
 * @author  songsiyu
 * @date  [2012-9-3]
 */
public class SphereEffector extends CylinderEffector {

	static final float HALF_ANGLE = 180f;
	static final float FULL_ANGLE = 360f;
	float mRotateX;
	

	@Override
	public void onDrawScreen(GLCanvas canvas, int screen, int offset) {
		onDrawScreen(canvas, screen , (float) offset);
	}
	
	@Override
	public void onDrawScreen(GLCanvas canvas, int screen, float offset) {
		float t = offset * mRatio;
		final float yOffsetRatio = 2.1f;
		final float interpolateEnd = 64;

		final GridScreenContainer container = mContainer;
		final int row = container.getCellRow();
		final int col = container.getCellCol();
		int index = row * col * screen;
		final int end = Math.min(container.getCellCount(), index + row * col);
		final int cellWidth = container.getCellWidth();
		final int cellHeight = container.getCellHeight();
		final int paddingLeft = container.getPaddingLeft();
		final int paddingTop = container.getPaddingTop();
		final int paddingBottom = container.getPaddingBottom();
		final float realCenterY = mCenterY - (paddingTop + paddingBottom) / 2 + paddingTop;
		final int screenWidth = container.getWidth();
		float globalAngle = HALF_ANGLE * t;
		canvas.translate(mCenterX - offset, realCenterY);
		final DrawFilter filter = canvas.getDrawFilter();
		requestQuality(canvas, GridScreenEffector.DRAW_QUALITY_HIGH);
		final float ratio = HALF_ANGLE / col;
		final float absT = Math.abs(t);
		float t2 = Math.min(Math.max(absT * col, mScroller.getCurrentDepth()), 1);
		canvas.translate(0, 0, -mRadius);
		
		// 上下偏移
		if (mVerticalSlide) {
			float angleX = getAngleX(Math.min(
					mRatio * Math.abs(mScroller.getCurrentScreenOffset()) * 2,
					1));
			canvas.rotateEuler(angleX, globalAngle, 0);
		} else {
			canvas.rotateAxisAngle(globalAngle, 0, 1, 0);
		}
		
		int j = 0, jEnd = col, dj = 1, dx = cellWidth;
		if (t > 0) { // 为了实现深度排序
			j = col - 1;
			jEnd = -1;
			dj = -dj;
			dx = -dx;
			index += j;
		}
		for (int cellX = paddingLeft + cellWidth * j; j != jEnd; j += dj, index += dj, cellX += dx) {
			if (index >= end) {
				continue;
			}
			float localAngle = (j + HALF - col * HALF) * ratio;
			localAngle = interpolate(0, localAngle, t2); // 在平面和球面间插值角度
			float angle = globalAngle + localAngle;
			if (angle < -HALF_ANGLE) {
				angle += FULL_ANGLE;
			}
			if (angle >= HALF_ANGLE) {
				angle -= FULL_ANGLE;
			}
			if (angle < 0) {
				angle = -angle;
			}
			int alpha = ALPHA;
			int fadeAngle = FADE_ANGLE;
			if (angle > fadeAngle) {
				alpha = (int) interpolate(ALPHA, interpolateEnd, (angle - fadeAngle)
						/ (HALF_ANGLE - fadeAngle));
			}

			if (Math.abs(t) * col > col - 1) {
				alpha *= (1 - Math.abs(t)) * col; // 在[1-1/col, 1]及对称范围内插值alpha，避免刚触摸时左右移动的不连续
			}
			canvas.save();
			canvas.rotateAxisAngle(localAngle, 0, 1, 0);
			canvas.translate(-screenWidth * screen, 0);
			
			int i = 0, iEnd = row, di = 1, dy = cellHeight, index2 = index;
			if (mRotateX * t2 < 0) {
				i = row - 1;
				iEnd = -1;
				di = -di;
				dy = -dy;
				index2 += i * col;
			}
			for (int cellY = paddingTop + cellHeight * i; i != iEnd; i += di, index2 += di * col, cellY += dy) {
				if (index2 >= end) {
					continue;
				}

				canvas.save();
				final float angle2 = row > 1
						? (i + HALF - row * HALF) * 90 / (row - 1)
						: 0;
				canvas.rotateAxisAngle(interpolate(0, angle2, t2), 1, 0, 0);
				canvas.translate(0, 0, mRadius);
				canvas.translate(interpolate(cellX - mCenterX, -cellWidth * 0.5f, t2) - cellX,
						interpolate(cellY - realCenterY, -cellHeight * 0.5f, t2) - cellY);
				if (alpha == ALPHA) {
					container.drawScreenCell(canvas, screen, index2);
				} else if (alpha > 0) {
					container.drawScreenCell(canvas, screen, index2, alpha);
				}
				
				canvas.restore();
			}
			canvas.restore();
		}
		canvas.setDrawFilter(filter); // restore filter
	}
	
	@Override
	public void onSizeChanged(int w, int h) {
		super.onSizeChanged(w, h);
		mRadius = Math.min(w, h) / (float) Math.toRadians(HALF_ANGLE) * 1.5f; //CHECKSTYLE IGNORE
	}
	
	
	@Override
	public boolean isFloatAdapted() {
		return true;
	}
}
