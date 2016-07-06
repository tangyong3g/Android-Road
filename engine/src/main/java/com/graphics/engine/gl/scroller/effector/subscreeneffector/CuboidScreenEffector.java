package com.graphics.engine.gl.scroller.effector.subscreeneffector;


import com.graphics.engine.gl.animation.Animation;
import com.graphics.engine.gl.animation.AnimationListenerAdapter;
import com.graphics.engine.gl.animation.InterpolatorValueAnimation;
import com.graphics.engine.gl.graphics.GLCanvas;
import com.graphics.engine.gl.scroller.ScreenScroller;
import com.graphics.engine.gl.view.GLView;

/**
 * 
 * 类描述:长方体特效
 * 功能详细描述:
 * 
 * @author  songsiyu
 * @date  [2012-9-3]
 */
public class CuboidScreenEffector extends CuboidOutsideEffector {

	float mRatio;

	@Override
	public void onSizeChanged() {
		super.onSizeChanged();
		mRatio = 1.0f / mWidth;
	}

	@Override
	protected boolean onDrawScreen(GLCanvas canvas, int screen, int offset1, boolean first) {
		float offset = mScroller.getCurrentScreenDrawingOffset(first);
		mScroller.setMaxOvershootPercent(0);
		final float angle = offset * mAngleRatio;
		final float angleAbs = Math.abs(angle);
		float tempAngle = (Math.abs(mScroll) % mWidth) * mAngleRatio;
		float distance = 0;
		if (tempAngle <= RIGHT_ANGLE / 2) {
			distance = (float) (Math.sin(tempAngle * (Math.PI / HALF_ANGLE)) * (mWidth / 2));
		} else if (tempAngle > RIGHT_ANGLE / 2 && tempAngle <= RIGHT_ANGLE) {
			distance = (float) (Math.cos(tempAngle * (Math.PI / HALF_ANGLE)) * (mWidth / 2));
		}
		//mCullFailAngle原来是这个
//		if (angleAbs > RIGHT_ANGLE) {
//			return false;
//		}
		//计算当前透明度的值，0.8是为了调节快慢而设的参数
		float percentAlpha = (float) ((Math.abs(offset) / (mWidth * 0.8)) * Math.sin(Math   // CHECKSTYLE IGNORE THIS LINE
				.abs(offset * 0.8) * mAngleRatio * (Math.PI / HALF_ANGLE)));                // CHECKSTYLE IGNORE THIS LINE
		if (percentAlpha > 1) {
			percentAlpha = 1;
		}
		
		if (mDepthEntryAnimation.animate()) {
			((GLView) mContainer).invalidate();
		}
		
		int bgAlpha = (int) (mDepthEntryAnimation.getValue() * 255);

		//保存canvas的原有alpha值
		int oldAlpha = canvas.getAlpha();

		canvas.save();
		canvas.translate(0, 0, -distance);
		//垂直改变角度
		transformView(canvas, angle);
		canvas.setCullFaceEnabled(false);
		canvas.save();
		canvas.setAlpha(bgAlpha);
		if (first) {
			canvas.rotateAxisAngle(-90, 0, 1, 0);
			canvas.translate(-mScroller.getScreenWidth(), 0);
			((AppdrawerSubScreenContainer) mContainer).drawScreenBackground(canvas, screen);
		} else {
			canvas.translate(mScroller.getScreenWidth(), 0);
			canvas.rotateAxisAngle(-90, 0, 1, 0);
			canvas.translate(-mScroller.getScreenWidth(), 0);
			((AppdrawerSubScreenContainer) mContainer).drawScreenBackground(canvas, screen);
		}
		canvas.restore();
		//画背景
		((AppdrawerSubScreenContainer) mContainer).drawScreenBackground(canvas, screen);
		canvas.setAlpha(oldAlpha);
		canvas.setCullFaceEnabled(true);
		canvas.restore();
		drawAll(canvas, percentAlpha, oldAlpha, screen, offset, angle);
		return false;
	}

	private void drawScreenContent(GLCanvas canvas, float percent, int oldAlpha, int screen,
								   float offset) {
		canvas.translate(offset, 0);
		canvas.multiplyAlpha((int) (ALPHA * (1 - percent)));
		mContainer.drawScreen(canvas, screen);
		canvas.setAlpha(oldAlpha);
	}

	private void transformView(GLCanvas canvas, float angleY) {
		if (mVerticalSlide) {
			float angleX = getAngleX(Math.min(mRatio * Math.abs(mScroller.getCurrentScreenOffsetFloat())
					* 2, 1));
			transformXandY(canvas, angleY, angleX);
		} else {
			transform(canvas, angleY);
		}
	}

	private void drawAll(GLCanvas canvas, float percent, int oldAlpha, int screen, float offset,
			float angle) {
		//垂直改变角度
		transformView(canvas, angle);
		drawScreenContent(canvas, percent, oldAlpha, screen, offset);
	}
	
	@Override
	protected boolean toReverse() {
		return mScroller.getCurrentScreen() == mScroller.getDrawingScreenA();
	}
	
	@Override
	protected void drawView(GLCanvas canvas, int screen, int offset,
			boolean first) {
		if (screen == ScreenScroller.INVALID_SCREEN) {
			if (screen == mScroller.getDrawingScreenB()) {
				screen = mScroller.getDrawingScreenA() + 1;
			} else if (screen == mScroller.getDrawingScreenA()) {
				screen = mScroller.getDrawingScreenB() + 1;
			}
			canvas.save();
			onDrawScreen(canvas, screen, offset, first);
			canvas.restore();
			return;
		}
		int saveCount = canvas.save();
		if (onDrawScreen(canvas, screen, offset, first)) {
			if (mCombineBackground) {
				mScroller.drawBackgroundOnScreen(canvas, screen);
			}
			if (mAlpha == ALPHA) {
				mContainer.drawScreen(canvas, screen);
			} else if (mAlpha > 0) {

				mContainer.drawScreen(canvas, screen, mAlpha);

			}
		}
		canvas.restoreToCount(saveCount);
	}
	
	@Override
	public void onAttach(SubScreenContainer container, ScreenScroller scroller) {
		super.onAttach(container, scroller);
		mScroller.setDepthEnabled(true);
	}

	@Override
	public void onDetach() {
		mScroller.setDepthEnabled(false);
		super.onDetach();
	}
	
	private InterpolatorValueAnimation mDepthEntryAnimation = new InterpolatorValueAnimation(0);

	private static final int DEPTH_DURATION = 300; // 卷曲动画的时间
	private static final int DEPTH_DURATION_OFFSET = 350; // 卷曲动画的时间
	
	@Override
	public void onScrollStart() {
		super.onScrollStart();
		startEntryAnim(true, false);
	}
	
	@Override
	public void onFlipInterupted() {
		super.onFlipInterupted();
		startEntryAnim(true, false);
	}
	
	@Override
	public void onFlipStart() {
		super.onFlipStart();
		if (mDepthEntryAnimation.animate()) {
			mDepthEntryAnimation.setAnimationListener(new AnimationListenerAdapter() {
				@Override
				public void onAnimationEnd(Animation animation) {
					startEntryAnim(false, true);
				}
			});
		} else {
			startEntryAnim(false, true);
		}
	}
	
	@Override
	public boolean isAnimationing() {
		return !mDepthEntryAnimation.isFinished();
	}
	
	
	private void startEntryAnim(boolean enter, boolean offset) {
		mDepthEntryAnimation.setAnimationListener(null);
		float depth = mDepthEntryAnimation.getValue();
		mDepthEntryAnimation.start(depth, enter ? 1 : 0, DEPTH_DURATION);
		int startOffset = 0; 
		if (offset) {
			int restTime = mScroller.getFlingRestTime();
			startOffset = Math.max(50, restTime - DEPTH_DURATION_OFFSET);
		}
		mDepthEntryAnimation.setStartOffset(startOffset);
	}
}