package com.graphics.engine.animation;


/**
 * 
 * <br>类描述: 3D旋转动画
 * <br>功能详细描述:
 * 
 * @date  [2012-9-5]
 */
public class Rotate3DAnimation extends Animation {
	//	Transformation3D mTempTransformation3d = new Transformation3D();
	float mFromDegrees;
	float mToDegrees;
	
	float mAxisXValue;
	float mAxisYValue;
	float mAxisZValue;
	
	float mPivotXValue = 0.0f;
	float mPivotYValue = 0.0f;
	float mPivotZValue = 0.0f;

	@Override
	protected void applyTransformation(float interpolatedTime, Transformation3D t) {
		final float angle = mFromDegrees + (mToDegrees - mFromDegrees) * interpolatedTime;
		
		if (mPivotXValue == 0.0f && mPivotYValue == 0.0f && mPivotZValue == 0.0f) {			
			t.setRotateAxisAngle(angle, mAxisXValue, mAxisYValue, mAxisZValue);
		} else {
			t.setRotateAxisAngle(angle, mAxisXValue, mAxisYValue, mAxisZValue, 
				mPivotXValue, mPivotYValue, mPivotZValue);
		}
	}
	
	   /**
     * Constructor to use when building a RotateAnimation from code
     * 
     * @param fromDegrees Rotation offset to apply at the start of the
     *        animation.
     * 
     * @param toDegrees Rotation offset to apply at the end of the animation.
     * 
     * @param axisX/axisY/axisZ The X/Y/Z coordinate of the rotate axis
     *        
     */
    public Rotate3DAnimation(float fromDegrees, float toDegrees, float axisX, float axisY, float axisZ) {
        mFromDegrees = fromDegrees;
        mToDegrees = toDegrees;

        mAxisXValue = axisX;
        mAxisYValue = axisY;
        mAxisZValue = axisZ;
        
    	mPivotXValue = 0.0f;
    	mPivotYValue = 0.0f;
    	mPivotZValue = 0.0f;
    }	
    
    /**
     * Constructor to use when building a RotateAnimation from code
     * 
     * @param fromDegrees Rotation offset to apply at the start of the
     *        animation.
     * 
     * @param toDegrees Rotation offset to apply at the end of the animation.
     * 
     * @param pivotX/pivotY/pivotZ The X/Y/Z coordinate of the point about which the object is
     *        being rotated, specified as an absolute number where 0 is the left
     *        edge.
     * @param axisX/axisY/axisZ The X/Y/Z coordinate of the rotate axis
     */
    public Rotate3DAnimation(float fromDegrees, float toDegrees, float pivotX, float pivotY, float pivotZ,
    		float axisX, float axisY, float axisZ) {
        mFromDegrees = fromDegrees;
        mToDegrees = toDegrees;

        mPivotXValue = pivotX;
        mPivotYValue = pivotY;
        mPivotZValue = pivotZ;
        
        mAxisXValue = axisX;
        mAxisYValue = axisY;
        mAxisZValue = axisZ;
    }    
}