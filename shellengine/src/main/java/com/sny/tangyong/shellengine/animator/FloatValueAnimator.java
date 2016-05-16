package com.sny.tangyong.shellengine.animator;


/**
 * <br>类描述: 对单个浮点数属性值的动画，支持多个分段取值
 * <br>功能详细描述:
 * <br>使用{@link #setValues(float...)}设置动画值。
 * <br>使用{@link #getAnimatedValue()}获取当前动画值。
 * 
 * @author  dengweiming
 * @date  [2013-7-22]
 */
public class FloatValueAnimator extends ValueAnimator {
	private float[] mValues;
	private float mCurrentValue;
	
    /**
     * 设置动画过程的值，支持多个值，每两个值之间的时间间隔都相同。
     * 例如：setValues(0, 1, 2)，会在动画进行到一半时值为 1。
     * @param values	参数可以是一个浮点数数组 float[]，或者是浮点数参数列表
     */
    public void setValues(float... values) {
        if (values == null || values.length == 0) {
            return;
        }
        
        mNumKeyframes = Math.max(values.length, 2);
		if (mValues == null 
				|| mValues.length < mNumKeyframes 
				|| mValues.length >= mNumKeyframes * 2) {
			mValues = new float[mNumKeyframes];
			mFractions = new float[mNumKeyframes];
		}
        mFirstKeyframe = 0;
        mLastKeyframe = mNumKeyframes - 1;
		if (values.length == 1) {
			mValues[0] = mValues[1] = values[0];
			mFractions[0] = 0;
		} else {
			for (int i = 0; i < mNumKeyframes; ++i) {
				mValues[i] = values[i];
				mFractions[i] = i / (float) (mNumKeyframes - 1);
			}
		}
		mFractions[mLastKeyframe] = 1;
		mCurrentValue = mValues[0];
    }
    
    /**
     * 获取最近计算得到的动画值
     */
    public float getAnimatedValue() {
    	return mCurrentValue;
    }

	@Override
	protected void evaluate(float fraction, int startFrame, int endFrame) {
		float startFloat = mValues[startFrame];
		mCurrentValue = startFloat + fraction * (mValues[endFrame] - startFloat);
	}


	

}
