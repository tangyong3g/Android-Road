package com.graphics.engine.gl.animator.motionfiler;

import java.util.ArrayList;

import com.graphics.engine.gl.animation.Transformation3D;

import android.view.animation.AnimationSet;

/**
 * 
 * <br>类描述: 运动过滤器{@link MotionFilter}的集合
 * <br>功能详细描述: 
 * <br>调度多个MotionFilter，完成复合的动画效果。
 * <br>使用{@link #addMotionFilter(ValueAnimator)} 添加子运动过滤器，注意添加的顺序。
 * <br>和{@link AnimatorSet}差不多，但是可以设置给视图。
 * 
 * @author  dengweiming
 * @date  [2013-10-9]
 */
public class MotionFilterSet implements MotionFilter {
	AnimatorSet mAnimatorSet;	//因为是final限定的，不能使用继承的方式，只好作为成员
	ArrayList<MotionFilter> mMotionFilters;
	Transformation3D mChildTransformation;
	
	Transformation3D mTransformation;
	Transformation3D mInverseTransformation;
	boolean mIsInverseTransformationDirty;
	protected boolean mNeedInitialize;
	
	boolean mWillChangeTransformationMatrix;
	boolean mWillChangeBounds;
	boolean mHasAlpha;
	
	/**
	 * 创建一个实例
	 */
	public MotionFilterSet() {
		mAnimatorSet = new AnimatorSet();
		mMotionFilters = new ArrayList<MotionFilter>();
		mChildTransformation = new Transformation3D();
	}
	
	/**
	 * <br>功能简述: 添加一个ValueAnimator作为子MotionFilter
	 * <br>功能详细描述:
	 * <br>注意: 变换组合的顺序是：后添加的变换矩阵在左边，即变换是相对于物体的，
	 * 而不是相对于坐标系，这和sdk的{@link AnimationSet} 一致，并且和动画的
	 * 播放顺序无关。一般来说，先添加缩放的运动，再添加旋转，最后是平移。
	 * @param filter
	 */
	public void addMotionFilter(ValueAnimator filter) {
		mWillChangeTransformationMatrix |= filter.willChangeTransformationMatrix();
		mWillChangeBounds |= filter.willChangeBounds();
		mHasAlpha |= filter.hasAlpha();
		mAnimatorSet.play((ValueAnimator) filter);
		mMotionFilters.add(filter);
	}

	@Override
	public void getTransformation(Transformation3D t) {
		for (int i = mMotionFilters.size() - 1; i >= 0; --i) {
			MotionFilter filter = mMotionFilters.get(i);
			mChildTransformation.clear();
			filter.getTransformation(mChildTransformation);
			t.compose(mChildTransformation);
		}
	}

	@Override
	public Transformation3D getTransformation() {
		if (mTransformation == null) {
			mTransformation = new Transformation3D();
		}
		return mTransformation;
	}
	
	@Override
	public void setInverseTransformationDirty() {
		mIsInverseTransformationDirty |= mWillChangeTransformationMatrix;
	}
	
	@Override
	public Transformation3D getInverseTransformation() {
		if (mInverseTransformation == null) {
			mInverseTransformation = new Transformation3D();
			mIsInverseTransformationDirty = true;
		}
		if (mIsInverseTransformationDirty) {
			mIsInverseTransformationDirty = false;
			Transformation3D t = getTransformation();
			t.invert(mInverseTransformation);
		}
		return mInverseTransformation;
	}
	
	@Override
	public boolean isRunning() {
		return mAnimatorSet.isRunning();
	}

	@Override
	public void start() {
		mNeedInitialize = true;
		mAnimatorSet.start();
	}
	
	@Override
	public void cancel() {
		mAnimatorSet.cancel();
	}
	
	@Override
	public void reverse() {
		mNeedInitialize = true;
		mAnimatorSet.reverse();
	}

	@Override
	public void relativeReverse() {
		mNeedInitialize = true;
		mAnimatorSet.relativeReverse();
	}

	@Override
	public boolean willChangeTransformationMatrix() {
		return mWillChangeTransformationMatrix;
	}
	
	@Override
	public boolean willChangeBounds() {
		return mWillChangeBounds;
	}
	
	@Override
	public boolean hasAlpha() {
		return mHasAlpha;
	}

	@Override
	public void cleanup() {
		if (mAnimatorSet != null) {
			mAnimatorSet.cleanup();
			mAnimatorSet = null;
		}
		if (mMotionFilters != null) {
			mMotionFilters.clear();
			mMotionFilters = null;
		}
	}
	
	@Override
	public void initializeIfNeeded(int width, int height, int parentWidth, int parentHeight) {
		if (mNeedInitialize) {
			mNeedInitialize = false;
			for (int i = mMotionFilters.size() - 1; i >= 0; --i) {
				MotionFilter filter = mMotionFilters.get(i);
				filter.initializeIfNeeded(width, height, parentWidth, parentHeight);
			}
		}
		
	}

}
