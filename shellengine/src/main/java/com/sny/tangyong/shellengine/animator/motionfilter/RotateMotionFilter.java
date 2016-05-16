package com.sny.tangyong.shellengine.animator.motionfilter;

import com.sny.tangyong.shellengine.animation.Transformation3D;
import com.sny.tangyong.shellengine.animator.FloatValuePairsAnimator;

/**
 * 
 * <br>类描述: 旋转运动过滤器
 * <br>功能详细描述:
 * 
 * @author  dengweiming
 * @date  [2013-10-9]
 */
public class RotateMotionFilter extends FloatValuePairsAnimator implements MotionFilter {
	float mAxisX;
	float mAxisY;
	float mAxisZ;
	
    int mPivotXType = ABSOLUTE;
    int mPivotYType = ABSOLUTE;
    float mPivotXValue = 0.0f;
    float mPivotYValue = 0.0f;
    float mPivotX;
    float mPivotY;
    float mPivotZ;
	
	public RotateMotionFilter(float fromDegrees, float toDegrees) {
		setRotation(fromDegrees, toDegrees);
	}
	
	public RotateMotionFilter(float fromDegrees, float toDegrees, float pivotX, float pivotY) {
		setRotation(fromDegrees, toDegrees);
		
        mPivotXType = ABSOLUTE;
        mPivotYType = ABSOLUTE;
        mPivotXValue = pivotX;
        mPivotYValue = pivotY;
	}
	
	public RotateMotionFilter(float fromDegrees, float toDegrees, int pivotXType, float pivotXValue,
            int pivotYType, float pivotYValue) {
		setRotation(fromDegrees, toDegrees);
		
        mPivotXValue = pivotXValue;
        mPivotXType = pivotXType;
        mPivotYValue = pivotYValue;
        mPivotYType = pivotYType;
	}
	
	public RotateMotionFilter(float fromDegrees, float toDegrees, float axisX, float axisY, float axisZ) {
		setRotation(fromDegrees, toDegrees, axisX, axisY, axisZ);
	}
	
	public RotateMotionFilter(float fromDegrees, float toDegrees, float axisX, float axisY, float axisZ, 
			float pivotX, float pivotY, float pivotZ) {
		setRotation(fromDegrees, toDegrees, axisX, axisY, axisZ);
		
        mPivotXType = ABSOLUTE;
        mPivotYType = ABSOLUTE;
        mPivotXValue = pivotX;
        mPivotYValue = pivotY;
        
        mPivotZ = pivotZ;
	}
	
	public RotateMotionFilter(float fromDegrees, float toDegrees, float axisX, float axisY, float axisZ, 
			int pivotXType, float pivotXValue, int pivotYType, float pivotYValue, float pivotZ) {
		setRotation(fromDegrees, toDegrees, axisX, axisY, axisZ);
		
        mPivotXValue = pivotXValue;
        mPivotXType = pivotXType;
        mPivotYValue = pivotYValue;
        mPivotYType = pivotYType;
		
		mPivotZ = pivotZ;
	}
	
	public void setRotation(float fromDegrees, float toDegrees) {
		setRotation(-fromDegrees, -toDegrees, 0, 0, 1);
	}
	
	public void setRotation(float fromDegrees, float toDegrees, float axisX, float axisY, float axisZ) {
		setValues(fromDegrees, toDegrees);
		mAxisX = axisX;
		mAxisY = axisY;
		mAxisZ = axisZ;
	}

	@Override
	public void getTransformation(Transformation3D t) {
		float[] values = getAnimatedValue();
		if (mPivotX == 0.0f && mPivotY == 0.0f && mPivotZ == 0.0f) {
			t.setRotateAxisAngle(values[0], mAxisX, mAxisY, mAxisZ);
		} else {
			t.setRotateAxisAngle(values[0], mAxisX, mAxisY, mAxisZ, mPivotX, mPivotY, mPivotZ);
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
