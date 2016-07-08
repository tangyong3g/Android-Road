package com.graphics.engine.animator;


/**
 * <br>类描述: 支持多组浮点数属性的动画，每组属性各两个值（起始值和终结值）
 * <br>功能详细描述:
 * <br>可以用来对坐标(x, y)这样的属性组进行动画，各个属性按照相同的进度来动画，保持线性关系。
 * <br>使用{@link #setValues(float...)}设置动画值。
 * <br>使用{@link #getAnimatedValue()}获取当前动画值数组。
 * 
 * @author  dengweiming
 * @date  [2013-7-22]
 */
public class FloatValuePairsAnimator extends ValueAnimator {
	private float[] mStartValues;
	private float[] mEndValues;
	private float[] mCurrentValues;
	private int mNumValuePairs;
	
	public FloatValuePairsAnimator() {
		mNumKeyframes = 2;
		mFirstKeyframe = 0;
		mLastKeyframe = mNumKeyframes - 1;
		mFractions = new float[] { 0, 1 };
	}
	
    /**
     * 设置动画过程的值，支持多组属性，每组属性各两个值（起始值和终结值），依次排列。
     * @param values	参数可以是一个浮点数数组 float[]，或者是浮点数参数列表
     */
	public void setValues(float... values) {
		if (values == null || values.length < 2) {
			return;
		}

		int numValuePairs = values.length / 2;
		if (mNumValuePairs < numValuePairs || mNumValuePairs >= numValuePairs * 2) {
			mNumValuePairs = numValuePairs;
			mStartValues = new float[mNumValuePairs];
			mEndValues = new float[mNumValuePairs];
			mCurrentValues = new float[mNumValuePairs];
		}
		for (int i = 0; i < values.length; i += 2) {
			int j = i / 2;
			mStartValues[j] = values[i];
			mEndValues[j] = values[i + 1];
		}
	}
	
	/**
	 * 获取有多少对动画值
	 */
	public int getValuePairsCount() {
		return mNumValuePairs;
	}
    
    /**
     * 获取最近计算得到的动画值
     * <br>注意：返回的数组有效的元素个数为{@link #getValuePairsCount()}，并且不要修改其内容。
     */
    public float[] getAnimatedValue() {
    	return mCurrentValues;
    }

	@Override
	protected void evaluate(float fraction, int startFrame, int endFrame) {
		for (int i = 0; i < mNumValuePairs; ++i) {
			mCurrentValues[i] = (mEndValues[i] - mStartValues[i]) * fraction + mStartValues[i];
		}
	}


	

}
