package com.graphics.engine.gl.scroller.effector.gridscreeeneffector;


import android.graphics.DrawFilter;

import com.graphics.engine.gl.graphics.GLCanvas;
import com.graphics.engine.gl.scroller.ScreenScroller;

/**
 * 绕圆柱体旋转特效
 * @author chenjiayu
 *
 */
public class Cylinder2Effector extends MGridScreenEffector {
	
	float mRatio;
	float mRadius;
	float mSurfaceAngle;
	float mTransZ;

	@Override
	public void onSizeChanged(int w, int h) {
		super.onSizeChanged(w, h);
		mRatio = 1.0f / w;
		mRadius = ((GridScreen3DContainer) mContainer).getRadius();
		mSurfaceAngle = ((GridScreen3DContainer) mContainer).getShowSurfaceAngle();
		mTransZ = ((GridScreen3DContainer) mContainer).getContainerTransZ();
	}

	@Override
	public void onDrawScreen(GLCanvas canvas, int screen, int offset) {
		onDrawScreen(canvas, screen, (float) offset);
	}
	
	@Override
	public void onDrawScreen(GLCanvas canvas, int screen, float offset) {
		
		final DrawFilter filter = canvas.getDrawFilter();
		requestQuality(canvas, GridScreenEffector.DRAW_QUALITY_HIGH);
		float t = offset * mRatio;
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
		float globalAngle = HALF_ANGLE * t;
		final float ratio = mSurfaceAngle / col;
		canvas.translate(mCenterX - offset, mCenterY);
		canvas.save();
		canvas.translate(0, 0, -mTransZ);
		
		
		//上下偏移
		if (mVerticalSlide) {
			float angleX = getAngleX(Math.min(
					mRatio * Math.abs(mScroller.getCurrentScreenOffset()) * 2, 1));
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
				alpha = (int) interpolate(ALPHA, INTERPOLATE_END, (angle - fadeAngle)
						/ (HALF_ANGLE - fadeAngle));
			}

			if (Math.abs(t) * col > col - 1) {
				alpha *= (1 - Math.abs(t)) * col; // 在[1-1/col, 1]及对称范围内插值alpha，避免刚触摸时左右移动的不连续
			}
			
			canvas.save();
			canvas.rotateAxisAngle(localAngle, 0, 1, 0);
			canvas.translate(0, 0, mRadius);
			// 在平面和圆柱面间插值位置
			canvas.translate(-screenWidth * screen, 0);
			canvas.translate(-cellWidth * HALF - cellX, -mCenterY);


			for (int i = 0, cellY = paddingTop, index2 = index; i < row && index2 < end; ++i) {
				if (alpha == ALPHA) {
					container.drawScreenCell(canvas, screen, index2);
				} else if (alpha > 0) {
					container.drawScreenCell(canvas, screen, index2, alpha);
				}
				cellY += cellHeight;
				index2 += col;
			}
			canvas.restore();
		}
		canvas.restore();
		canvas.setDrawFilter(filter); // restore filter
	}

	@Override
	public void onAttach(GridScreenContainer container, ScreenScroller scroller) {
		super.onAttach(container, scroller);
		scroller.setDepthEnabled(true);
	}

	@Override
	public void onDetach() {
		mScroller.setDepthEnabled(false);
		super.onDetach();
	}

	@Override
	protected boolean isCurrentScreenOnTop() {
		return true; // 为了实现深度排序
	}
	
	
	@Override
	public boolean isFloatAdapted() {
		return true;
	}
}
