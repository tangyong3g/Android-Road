package com.graphics.engine.gl.animation;


import com.graphics.engine.gl.animator.ValueAnimator;

/**
 * 单个值的动画。
 * 通过{@link #start(float, float, long)}开始动画，
 * 或者{@link #start(float, long)}从当前值向另一个目标值开始动画。
 * 在绘制周期中调用{@link #animate()}，返回值表示动画是否还在继续，
 * 如果是，那么可能需要要求重绘。通过{@link #getValue()} 来获取当前动画的值。
 * 
 * @author dengweiming
 *
 * @deprecated 使用{@link InterpolatorValueAnimation}或者{@link com.go.gl.animator.Animator}代替。
 */
public class ValueAnimation {
	private float mValue;
	private float mDstValue;
	private long mStartTime;
	private long mDstTime;
	private long mDuration;
	private boolean mFinished = true;
	
	public ValueAnimation(float value) {
		mValue = value;
		mDstValue = value;
	}
	
	/**
	 * 开始动画
	 * @param startValue
	 * @param dstValue	如果当前正在动画中，并且目标值和这个指定的目标值相等，则不会重新开始动画（忽略duration的改变）
	 * @param duration
	 */
	public void start(float startValue, float dstValue, long duration) {
		if (mDstValue == dstValue && !isFinished()) {
			return;
		}
		mValue = startValue;
		mDstValue = dstValue;
		mDuration = duration;
		mStartTime = -1;
		mFinished = false;
	}
	
	/**
	 * 从当前值开始新的动画
	 * @param dstValue	如果当前正在动画中，并且目标值和这个指定的目标值相等，则不会重新开始动画（忽略duration的改变）`
	 * @param duration
	 */
	public void start(float dstValue, long duration) {
		start(mValue, dstValue, duration);
	}

	public boolean animate() {
		if (mFinished) {
			return false;
		}
		long time = ValueAnimator.sCurrentTime;
		if (mStartTime == -1) {
			mStartTime = time;
			mDstTime = mStartTime + mDuration;
		}
		float t = (time - mStartTime) / (float) (mDstTime - mStartTime);
		if (t > 1) {
			t = 1;
			mFinished = true;
		}
		//XXX: 这里有bug，变化曲线是指数级别的
		mValue += (mDstValue - mValue) * t;
		return true;
	}
	
	public void setValue(float value) {
		mValue = value;
	}
	
	public float getValue() {
		return mValue;
	}
	
	public float getDstValue() {
		return mDstValue;
	}
	
	public void setDstValue(float dstValue) {
		mDstValue = dstValue;
	}
	
	public boolean isFinished() {
		return mFinished;
	}

}
