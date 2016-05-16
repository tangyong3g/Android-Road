package com.sny.tangyong.shellengine.animation;

import android.util.SparseArray;
import android.view.animation.Interpolator;

/**
 * 
 * 插值器动画的集合！
 * by 潘国维
 * */
public class InterpolatorValueAnimationSet {

	private boolean mShareInterpolator = false; // 是否共享插值器

	private boolean mIsFinished = false; // 是否已经执行完毕了

	private Interpolator mInterpolator; // 插值器

	private SparseArray<InterpolatorValueAnimation> mAnimations = new SparseArray<InterpolatorValueAnimation>(); // animation 列表
	

	/**
	 * 创建
	 * @param shareInterpolator 是否共享插值器
	 */
	public InterpolatorValueAnimationSet(boolean shareInterpolator) {
		mShareInterpolator = shareInterpolator;
	}

	/**
	 * 设置插值器
	 * */
	public void setInterpolator(Interpolator interpolator) {
		if (interpolator == null) {
			mInterpolator = InterpolatorFactory
					.getInterpolator(InterpolatorFactory.LINEAR);
		}
		if (mShareInterpolator) {
			for (int i = 0; i < mAnimations.size(); i++) {
				mAnimations.valueAt(i).setInterpolation(mInterpolator);
			}
		}
	}

	/**
	 * 加入一个animation
	 * */
	public void addAnimation(int key, InterpolatorValueAnimation animation) {
		if (animation != null) {
			if (mShareInterpolator && mInterpolator != null) {
				animation.setInterpolation(mInterpolator);
			}
			mAnimations.put(key, animation);
		}
	}

	/**
	 * 执行animate
	 * */
	public boolean animate() {
		if (mIsFinished) {
			return false;
		}
		boolean animated = false;
		if (mAnimations.size() > 0) {
			for (int i = 0; i < mAnimations.size(); i++) {
				InterpolatorValueAnimation animation = mAnimations.valueAt(i);
				boolean animate = animation.animate();
				if (!animated && animate) {
					animated = true;
				}
			}
		}
		mIsFinished = !animated;
		return true;
	}

	/**
	 * 获取值
	 * key是 add的时候传入的key
	 * */
	public float getValue(int key) {
		InterpolatorValueAnimation animation = mAnimations.get(key);
		if (animation != null) {
			return animation.getValue();
		} else {
			return 0;
		}
	}

	/**
	 * 清空重置
	 * */
	public void reset() {
		mAnimations.clear();
		mIsFinished = false;
	}
}
