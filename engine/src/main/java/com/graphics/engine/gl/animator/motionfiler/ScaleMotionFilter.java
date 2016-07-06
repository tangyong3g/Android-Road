package com.graphics.engine.gl.animator.motionfiler;


import com.graphics.engine.gl.animation.Transformation3D;
import com.graphics.engine.gl.animator.FloatValuePairsAnimator;

/**
 * 
 * <br>类描述: 缩放运动过滤器
 * <br>功能详细描述:
 * 
 * @author  dengweiming
 * @date  [2013-10-9]
 */
public class ScaleMotionFilter extends FloatValuePairsAnimator implements MotionFilter {

    private int mPivotXType = ABSOLUTE;
    private int mPivotYType = ABSOLUTE;
    private float mPivotXValue = 0.0f;
    private float mPivotYValue = 0.0f;

    private float mPivotX;
    private float mPivotY;
    private float mPivotZ;
    
	public ScaleMotionFilter(float fromX, float toX, float fromY, float toY) {
		setScale(fromX, toX, fromY, toY);
	}
	
	public ScaleMotionFilter(float fromX, float toX, float fromY, float toY, 
			float pivotX, float pivotY) {
        mPivotXType = ABSOLUTE;
        mPivotYType = ABSOLUTE;
        mPivotXValue = pivotX;
        mPivotYValue = pivotY;
		setScale(fromX, toX, fromY, toY);
	}
	
	public ScaleMotionFilter(float fromX, float toX, float fromY, float toY,
            int pivotXType, float pivotXValue, int pivotYType, float pivotYValue) {
        mPivotXValue = pivotXValue;
        mPivotXType = pivotXType;
        mPivotYValue = pivotYValue;
        mPivotYType = pivotYType;
		setScale(fromX, toX, fromY, toY);
	}
	
	//--------这几个构造方法便于构造3D缩放版本--------v
	//因为会有参数列表相同的冲突，所以先构造指定缩放中心，再使用setScale方法指定缩放比例
	
	public ScaleMotionFilter() {
		
	}
	
	public ScaleMotionFilter(float pivotX, float pivotY, float pivotZ) {
        mPivotXValue = pivotX;
        mPivotYValue = pivotY;
        mPivotZ = pivotZ;
	}
	
	public ScaleMotionFilter(int pivotXType, float pivotXValue, int pivotYType, float pivotYValue, float pivotZ) {
		mPivotXValue = pivotXValue;
		mPivotXType = pivotXType;
		mPivotYValue = pivotYValue;
		mPivotYType = pivotYType;
		mPivotZ = pivotZ;
	}
	
	//--------这几个构造方法便于构造3D缩放版本--------^
	
	public void setScale(float fromX, float toX, float fromY, float toY) {
		setValues(fromX, toX, fromY, toY);
	}
	
	public void setScale(float fromX, float toX, float fromY, float toY, float fromZ, float toZ) {
		setValues(fromX, toX, fromY, toY, fromZ, toZ);
	}
	
	@Override
	public void getTransformation(Transformation3D t) {
		float[] values = getAnimatedValue();
		int numPairs = getValuePairsCount();
		float z = numPairs > 2 ? values[2] : 1;
		if (mPivotX == 0 && mPivotY == 0) {
			t.setScale(values[0], values[1], z);
		} else {
			t.setScale(values[0], values[1], z, mPivotX, mPivotY, mPivotZ);
		}
	}

    @Override
    public void initializeIfNeeded(int width, int height, int parentWidth, int parentHeight) {
		if (mNeedInitializeMotionFilter) {
			mNeedInitializeMotionFilter = false;
			mPivotX = resolveSize(mPivotXType, mPivotXValue, width, parentWidth);
			mPivotY = -resolveSize(mPivotYType, mPivotYValue, height, parentHeight);
		}
    }
}
