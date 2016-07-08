package com.graphics.engine.animation;

import android.content.Context;
import android.util.AttributeSet;

/**
 * 
 * <br>类描述: 3D版本的Translate Animation
 * <br>功能详细描述:
 * 
 * @author  luopeihuan
 * @date  [2012-9-5]
 */
public class Translate3DAnimation extends Animation {
    private int mFromXType = ABSOLUTE;
    private int mToXType = ABSOLUTE;

    private int mFromYType = ABSOLUTE;
    private int mToYType = ABSOLUTE;

    private int mFromZType = ABSOLUTE;
    private int mToZType = ABSOLUTE;

    private float mFromXValue = 0.0f;
    private float mToXValue = 0.0f;

    private float mFromYValue = 0.0f;
    private float mToYValue = 0.0f;
    
    private float mFromZValue = 0.0f;
    private float mToZValue = 0.0f;

    private float mFromXDelta;
    private float mToXDelta;
    private float mFromYDelta;
    private float mToYDelta;
    private float mFromZDelta;
    private float mToZDelta;

    /**
     * Constructor used when a Translate3DAnimation is loaded from a resource.
     * 
     * @param context Application context to use
     * @param attrs Attribute set from which to read values
     */
    public Translate3DAnimation(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Constructor to use when building a Translate3DAnimation from code
     * 
     * @param fromXDelta Change in X coordinate to apply at the start of the
     *        animation
     * @param toXDelta Change in X coordinate to apply at the end of the
     *        animation
     * @param fromYDelta Change in Y coordinate to apply at the start of the
     *        animation
     * @param toYDelta Change in Y coordinate to apply at the end of the
     *        animation
     */
    public Translate3DAnimation(float fromXDelta, 
    		float toXDelta, 
    		float fromYDelta, 
    		float toYDelta, 
    		float fromZDelta, 
    		float toZDelta) {
        mFromXValue = fromXDelta;
        mToXValue = toXDelta;
        mFromYValue = fromYDelta;
        mToYValue = toYDelta;
        mFromZValue = fromZDelta;
        mToZValue = toZDelta;

        mFromXType = ABSOLUTE;
        mToXType = ABSOLUTE;
        mFromYType = ABSOLUTE;
        mToYType = ABSOLUTE;
        mFromZType = ABSOLUTE;
        mToZType = ABSOLUTE;
    }

    /**
     * Constructor to use when building a Translate3DAnimation from code
     * 
     * @param fromXType Specifies how fromXValue should be interpreted. One of
     *        Animation.ABSOLUTE, Animation.RELATIVE_TO_SELF, or
     *        Animation.RELATIVE_TO_PARENT.
     * @param fromXValue Change in X coordinate to apply at the start of the
     *        animation. This value can either be an absolute number if fromXType
     *        is ABSOLUTE, or a percentage (where 1.0 is 100%) otherwise.
     * @param toXType Specifies how toXValue should be interpreted. One of
     *        Animation.ABSOLUTE, Animation.RELATIVE_TO_SELF, or
     *        Animation.RELATIVE_TO_PARENT.
     * @param toXValue Change in X coordinate to apply at the end of the
     *        animation. This value can either be an absolute number if toXType
     *        is ABSOLUTE, or a percentage (where 1.0 is 100%) otherwise.
     * @param fromYType Specifies how fromYValue should be interpreted. One of
     *        Animation.ABSOLUTE, Animation.RELATIVE_TO_SELF, or
     *        Animation.RELATIVE_TO_PARENT.
     * @param fromYValue Change in Y coordinate to apply at the start of the
     *        animation. This value can either be an absolute number if fromYType
     *        is ABSOLUTE, or a percentage (where 1.0 is 100%) otherwise.
     * @param toYType Specifies how toYValue should be interpreted. One of
     *        Animation.ABSOLUTE, Animation.RELATIVE_TO_SELF, or
     *        Animation.RELATIVE_TO_PARENT.
     * @param toYValue Change in Y coordinate to apply at the end of the
     *        animation. This value can either be an absolute number if toYType
     *        is ABSOLUTE, or a percentage (where 1.0 is 100%) otherwise.
     */
    public Translate3DAnimation(int fromXType, float fromXValue, int toXType, float toXValue,
            int fromYType, float fromYValue, int toYType, float toYValue,
            int fromZType, float fromZValue, int toZType, float toZValue) {

        mFromXValue = fromXValue;
        mToXValue = toXValue;
        mFromYValue = fromYValue;
        mToYValue = toYValue;
        mFromZValue = fromZValue;
        mToZValue = toZValue;

        mFromXType = fromXType;
        mToXType = toXType;
        mFromYType = fromYType;
        mToYType = toYType;
        mFromZType = fromZType;
        mToZType = toZType;
    }


    @Override
    protected void applyTransformation(float interpolatedTime, Transformation3D t) {
        float dx = mFromXDelta;
        float dy = mFromYDelta;
        float dz = mFromZDelta;
        if (mFromXDelta != mToXDelta) {
            dx = mFromXDelta + ((mToXDelta - mFromXDelta) * interpolatedTime);
        }
        if (mFromYDelta != mToYDelta) {
            dy = mFromYDelta + ((mToYDelta - mFromYDelta) * interpolatedTime);
        }
        if (mFromZDelta != mToZDelta) {
        	dz = mFromZDelta + ((mToZDelta - mFromZDelta) * interpolatedTime);
        }
        
//        Log.i("Translate3d", "dz:"+dz+" time:"+interpolatedTime);
        t.setTranslate(dx, dy, dz);
    }

    @Override
    public void initialize(int width, int height, int parentWidth, int parentHeight) {
        super.initialize(width, height, parentWidth, parentHeight);
        mFromXDelta = resolveSize(mFromXType, mFromXValue, width, parentWidth);
        mToXDelta = resolveSize(mToXType, mToXValue, width, parentWidth);
        mFromYDelta = resolveSize(mFromYType, mFromYValue, height, parentHeight);
        mToYDelta = resolveSize(mToYType, mToYValue, height, parentHeight);
        mFromZDelta = resolveSize(mFromZType, mFromZValue, height, parentHeight);
        mToZDelta = resolveSize(mToZType, mToZValue, height, parentHeight);
    }
}