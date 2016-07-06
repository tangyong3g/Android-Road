package com.graphics.engine.gl.scroller.effector.subscreeneffector;


import com.graphics.engine.gl.graphics.GLCanvas;
import com.graphics.engine.gl.scroller.ScreenScroller;

/**
 * 
 * 类描述:风车特效
 * 功能详细描述:
 * 
 * @author  songsiyu
 * @date  [2012-9-3]
 */
public class WindmillEffector extends MSubScreenEffector {
	float mAngleRatio; // Angle / ScreenSize;
	float mOffsetAngle;
	float mRatio;

	@Override
	public void onSizeChanged() {
		super.onSizeChanged();
		mRatio = 1.0f / mWidth;
		float angle;
		if (mOrientation == ScreenScroller.HORIZONTAL) {
			angle = (float) Math.toDegrees(Math.asin(mCenterX / mHeight)) * 2;
			if (mCenterX <= mHeight && angle <= RIGHT_ANGLE) {
				mCenterY = -(float) Math.sqrt(mHeight * mHeight - mCenterX * mCenterX);
			} else {
				angle = RIGHT_ANGLE;
				mCenterY = -mCenterX;
			}
		} else {
			angle = (float) Math.toDegrees(Math.asin(mCenterY / mWidth)) * 2;
			if (mCenterY <= mWidth && angle <= RIGHT_ANGLE) {
				mCenterX = mWidth + (float) Math.sqrt(mWidth * mWidth - mCenterY * mCenterY);
			} else {
				angle = RIGHT_ANGLE;
				mCenterX = mWidth + mCenterY;
			}
		}
		mAngleRatio = -angle / mScreenSize;
	}

	@Override
	protected boolean onDrawScreen(GLCanvas canvas, int screen, int offset1, boolean first) {
		float offset = mScroller.getCurrentScreenDrawingOffset(first);
		mOffsetAngle = offset * mAngleRatio;
		
		if (mOrientation == ScreenScroller.HORIZONTAL) {
			canvas.translate(mScroll + mCenterX, mCenterY);
		} else {
			canvas.translate(mCenterX, mScroll + mCenterY);
		}

		if (mVerticalSlide) {

			//受左右滑动量制约
			float angleX = getAngleX(Math.min(mRatio * Math.abs(mScroller.getCurrentScreenOffsetFloat())
					* 2, 1));

			//不受左右滑动量制约
			//float angleX = getAngleX(1);
			if (mOrientation == ScreenScroller.HORIZONTAL) {
				//将x轴平移到屏幕中间
				canvas.translate(0, -(mCenterY + mCenterY / 2));
				canvas.rotateAxisAngle(angleX, 1, 0, 0);
				canvas.translate(0, mCenterY + mCenterY / 2);
			} else {

			}
		}
		canvas.rotate(mOffsetAngle);
		canvas.translate(-mCenterX, -mCenterY);
		return true;
	}
}
