package com.graphics.engine.gl.animation;


import android.view.animation.Interpolator;

import com.graphics.engine.gl.animator.ValueAnimator;


/**
 * 
 * <br>类描述:大概功能与 {@link ValueAnimation}}相同,不过可以设置插值器
 * <br>注意: 在需要边做动画的过程中边修改动画时间时，请不要使用这个类
 * 
 * @author  panguowei
 * @date  [2013-3-1]
 */
public class InterpolatorValueAnimation {
	
	private static final int STATE_NOT_START = 0;
	private static final int STATE_OFFSETTING = 1;
	private static final int STATE_ANIMATING = 2;
	private static final int STATE_FINISHED = 3;
	
	private float mValue;
	private float mOriginValue;	
	private float mDstValue;
	private long mStartTime;
	private long mDuration;
	private long mStartOffset = 0;
	private int mAnimState = STATE_FINISHED;
	private boolean mFillAfter = true;
	private boolean mIsReverse = false;
	
	private Animation.AnimationListener mAnimationListener;
	private Interpolator mInterpolator = InterpolatorFactory
			.getInterpolator(InterpolatorFactory.LINEAR);

	public InterpolatorValueAnimation(float value) {
		mValue = value;
		mDstValue = value;
		mOriginValue = value;
	}

	/**
	 * 开始动画
	 * @param startValue
	 * @param dstValue
	 * @param duration
	 */
	public void start(float startValue, float dstValue, long duration) {
		mValue = startValue;
		mOriginValue = mValue;
		mDstValue = dstValue;
		mDuration = duration;
		mStartTime = -1;
		mAnimState = STATE_NOT_START;
		mIsReverse = false;
	}

	/**
	 * 从当前值开始新的动画
	 * @param dstValue	如果当前正在动画中，并且目标值和这个指定的目标值相等，则不会重新开始动画（忽略duration的改变）`
	 * @param duration
	 */
	public void start(float dstValue, long duration) {
		start(mValue, dstValue, duration);
	}

	/**
	 * 执行动画，在draw的时候调用来更新动画的值
	 * */
	public boolean animate() {
		if (mAnimState == STATE_FINISHED) {
			return false;
		}
		long time = ValueAnimator.sCurrentTime;
		if (mStartTime == -1) {
			mStartTime = time;
		}
		if (time - mStartTime < mStartOffset) {
			if (mAnimState != STATE_OFFSETTING) {
				mAnimState = STATE_OFFSETTING;
			}
			return true;
		} else if (mAnimState != STATE_ANIMATING) {
			mAnimState = STATE_ANIMATING;
			if (mAnimationListener != null) {
				mAnimationListener.onAnimationStart(null);
			}
		}
		
		float t = (float) (time - mStartTime - mStartOffset) / mDuration;
		if (t > 1) {
			t = 1;
			mAnimState = STATE_FINISHED;
		}
		mValue = (mDstValue - mOriginValue) * mInterpolator.getInterpolation(t) + mOriginValue;
		
		if (mAnimState == STATE_FINISHED && mAnimationListener != null) {
			mAnimationListener.onAnimationEnd(null);
		}
		return true;
	}

	public void setInterpolation(Interpolator interpolator) {
		mInterpolator = interpolator;
	}

	public float getValue() {
		if (mAnimState == STATE_FINISHED) {
			return mFillAfter ? mValue : mOriginValue;
		} else {
			return mValue;
		}
	}

	public boolean isFinished() {
		return mAnimState == STATE_FINISHED;
	}
	
	public void setFillAfter(boolean fillAfter) {
		mFillAfter = fillAfter;
	}
	
	public void setStartOffset(long offset) {
		mStartOffset = offset;
	}
	
	public void setAnimationListener(Animation.AnimationListener listener) {
		mAnimationListener = listener;
	}
	
	public void reverse() {
		animate();
		boolean tempReverse = !mIsReverse;
		if (mAnimState == STATE_FINISHED) {
			// 已经finished掉了，直接跑这个
			start(mDstValue, mOriginValue, mDuration);
		} else {
			// 没有 finished掉，回播，
			long time = ValueAnimator.sCurrentTime - mStartTime - mStartOffset;
			time = Math.max(0, time);
			start(mValue, mOriginValue, time);
		}
		mIsReverse = tempReverse;
	}
	
	public boolean isReverse() {
		return mIsReverse;
	}
	
	public long getAnimationRestTime() {
		if (isFinished()) {
			return 0;
		} else {
			return mDuration - (ValueAnimator.sCurrentTime - mStartTime) + mStartOffset;
		}
	}
}
