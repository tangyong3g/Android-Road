package com.graphics.engine.animator.motionfilter;

import com.graphics.engine.animation.Transformation3D;
import com.graphics.engine.animator.FloatValuePairsAnimator;

/**
 * 
 * <br>类描述: 平移运动过滤器
 * <br>功能详细描述:
 * 
 * @author  dengweiming
 * @date  [2013-10-9]
 */
public class TranslateMotionFilter extends FloatValuePairsAnimator implements MotionFilter {
    private int mFromXType = ABSOLUTE;
    private int mToXType = ABSOLUTE;

    private int mFromYType = ABSOLUTE;
    private int mToYType = ABSOLUTE;

    private float mFromXValue = 0.0f;
    private float mToXValue = 0.0f;

    private float mFromYValue = 0.0f;
    private float mToYValue = 0.0f;

    private float mFromXDelta;
    private float mToXDelta;
    private float mFromYDelta;
    private float mToYDelta;
    
    private float mFromZDelta;
    private float mToZDelta;
    private boolean mIs3D;
	
	public TranslateMotionFilter(float fromXDelta, float toXDelta, float fromYDelta, float toYDelta) {
		mFromXValue = fromXDelta;
		mToXValue = toXDelta;
		mFromYValue = fromYDelta;
		mToYValue = toYDelta;
	}
	
	public TranslateMotionFilter(int fromXType, float fromXValue, int toXType, float toXValue, 
			int fromYType, float fromYValue, int toYType, float toYValue) {
		mFromXValue = fromXValue;
		mToXValue = toXValue;
		mFromYValue = fromYValue;
		mToYValue = toYValue;

		mFromXType = fromXType;
		mToXType = toXType;
		mFromYType = fromYType;
		mToYType = toYType;
		
	}
	
	public TranslateMotionFilter(float fromXDelta, float toXDelta, float fromYDelta, float toYDelta, 
			float fromZDelta, float toZDelta) {
		mFromXValue = fromXDelta;
		mToXValue = toXDelta;
		mFromYValue = fromYDelta;
		mToYValue = toYDelta;
		
        mFromZDelta = fromZDelta;
        mToZDelta = toZDelta;
        mIs3D = true;
	}
	
	public TranslateMotionFilter(int fromXType, float fromXValue, int toXType, float toXValue, 
			int fromYType, float fromYValue, int toYType, float toYValue, 
			float fromZDelta, float toZDelta) {
		mFromXValue = fromXValue;
		mToXValue = toXValue;
		mFromYValue = -fromYValue;
		mToYValue = -toYValue;

		mFromXType = fromXType;
		mToXType = toXType;
		mFromYType = fromYType;
		mToYType = toYType;

		mFromZDelta = fromZDelta;
		mToZDelta = toZDelta;
		mIs3D = true;
	}
	
	public void setTranslation(float fromXDelta, float toXDelta, float fromYDelta, float toYDelta) {
        mFromXValue = fromXDelta;
        mToXValue = toXDelta;
        mFromYValue = fromYDelta;
        mToYValue = toYDelta;
        
		mFromXType = ABSOLUTE;
		mToXType = ABSOLUTE;
		mFromYType = ABSOLUTE;
		mToYType = ABSOLUTE;
		
        mFromZDelta = 0;
        mToZDelta = 0;
        mIs3D = false;

		setValues(mFromXValue, mToXValue, -mFromYValue, -mToYValue);
	}
	
	public void setTranslation(float fromXDelta, float toXDelta, float fromYDelta, float toYDelta, 
			float fromZDelta, float toZDelta) {
        mFromXValue = fromXDelta;
        mToXValue = toXDelta;
        mFromYValue = fromYDelta;
        mToYValue = toYDelta;

		mFromXType = ABSOLUTE;
		mToXType = ABSOLUTE;
		mFromYType = ABSOLUTE;
		mToYType = ABSOLUTE;
        
        mFromZDelta = fromZDelta;
        mToZDelta = toZDelta;
        mIs3D = true;
        
		setValues(mFromXValue, mToXValue, mFromYValue, mToYValue, mFromZDelta, mToZDelta);
	}

	@Override
	public void getTransformation(Transformation3D t) {
		float[] values = getAnimatedValue();
		int numPairs = getValuePairsCount();
		float z = numPairs > 2 ? values[2] : 0;
		t.setTranslate(values[0], values[1], z);
	}

    @Override
    public void initializeIfNeeded(int width, int height, int parentWidth, int parentHeight) {
		if (mNeedInitializeMotionFilter) {
			mNeedInitializeMotionFilter = false;
			mFromXDelta = resolveSize(mFromXType, mFromXValue, width, parentWidth);
			mToXDelta = resolveSize(mToXType, mToXValue, width, parentWidth);
			mFromYDelta = resolveSize(mFromYType, mFromYValue, height, parentHeight);
			mToYDelta = resolveSize(mToYType, mToYValue, height, parentHeight);

			if (mIs3D) {
				setValues(mFromXDelta, mToXDelta, mFromYDelta, mToYDelta, mFromZDelta, mToZDelta);
			} else {
				setValues(mFromXDelta, mToXDelta, -mFromYDelta, -mToYDelta);
			}
		}
    }

}
