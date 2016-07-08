package com.graphics.enginedemo;


import android.content.Context;

import com.graphics.engine.animator.FloatValueAnimator;
import com.graphics.engine.animator.ValueAnimator;
import com.graphics.engine.graphics.GLCanvas;
import com.graphics.engine.view.GLView;
import com.graphics.engine.view.GLViewGroup;

/**
 * ValueAnimator 动画类的测试样例
 * 
 * @author  dengweiming
 * @date  [2013-7-4]
 */
public class ValueAnimatorTestView extends GLViewGroup {
	FloatValueAnimator mValueAnimator = new FloatValueAnimator();
	int mFrameCount;
	int mDuration = 2000;
	int mMaxValue = 1;
	float[] mTimes;
	float[] mValues;
	int mFrame;
	float mRectHalfSize;

	public ValueAnimatorTestView(Context context) {
		super(context);
		mFrameCount = Math.min(mDuration / 16 + 2, 500);
		mTimes = new float[mFrameCount];
		mValues = new float[mFrameCount];
		mValueAnimator.setDuration(mDuration);
//		mValueAnimator.setRepeatCount(1);
//		mValueAnimator.setRepeatMode(ValueAnimator.REVERSE);
//		mValueAnimator.setInterpolator(new BounceInterpolator());
//		mValueAnimator.setInterpolator(InterpolatorFactory.getInterpolator(InterpolatorFactory.ELASTIC));
		mValueAnimator.setValues(0, 1);
		mMaxValue = 1;
//		mValueAnimator.start();
		mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				if (mFrame >= mFrameCount) {
					return;
				}
				FloatValueAnimator a = (FloatValueAnimator) animation;
				mTimes[mFrame] = a.getRawAnimatedFraction();
				mValues[mFrame] = a.getAnimatedValue();
				++mFrame;
				invalidate();	//必须通知更新绘制
			}
		});
		mRectHalfSize = getResources().getDisplayMetrics().density * 20;
		
		setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(GLView v) {
				mValueAnimator.relativeReverse();
				mFrame = 0;
				invalidate();

			}
		});
	}
	
	@Override
	protected void dispatchDraw(GLCanvas canvas) {
		float[] vertex = canvas.getTempFloatArray();
		float w = getWidth();
		float h = getHeight();
		float s = Math.min(w, h) * 0.8f;
		canvas.translate(w * 0.5f - s * 0.5f, h * 0.5f - s * 0.5f);
		int offset = 0;
		for (int i = 0; i < mFrame; ++i) {
			vertex[offset++] = mTimes[i] * s;
			vertex[offset++] = s - s * mValues[i] / mMaxValue;
		}
		canvas.setDrawColor(0x7fffffff);
		canvas.drawRect(0, 0, s, s);
		canvas.setDrawColor(0xffffffff);
		canvas.drawVertex(GLCanvas.LINE_STRIP, vertex, 0, mFrame, false);
		canvas.setDrawColor(0xffff0000);
		canvas.drawVertex(GLCanvas.POINTS, vertex, 0, mFrame, false);
		canvas.setDrawColor(0x7fffffff);
		canvas.translate(0, s - s * mValueAnimator.getAnimatedValue() / mMaxValue);
		canvas.fillRect(-mRectHalfSize, -mRectHalfSize, mRectHalfSize, mRectHalfSize);
	}
	
	@Override
	public void cleanup() {
		mValueAnimator.cleanup();
		super.cleanup();
	}

}