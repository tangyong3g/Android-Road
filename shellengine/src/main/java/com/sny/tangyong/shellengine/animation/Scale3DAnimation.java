package com.sny.tangyong.shellengine.animation;

/**
 * 
 * <br>类描述: 3D缩放动画
 * <br>功能详细描述:
 * 
 * @author  luopeihuan
 * @date  [2012-9-5]
 */
public class Scale3DAnimation extends Animation {
	private float mFromX;
    private float mToX;
    private float mFromY;
    private float mToY;
    private float mFromZ;
    private float mToZ;


    /**
     * Constructor to use when building a ScaleAnimation from code
     * 
     * @param fromX Horizontal scaling factor to apply at the start of the
     *        animation
     * @param toX Horizontal scaling factor to apply at the end of the animation
     * @param fromY Vertical scaling factor to apply at the start of the
     *        animation
     * @param toY Vertical scaling factor to apply at the end of the animation
     */
	public Scale3DAnimation(float fromX, float toX, float fromY, float toY, float fromZ, float toZ) {
        mFromX = fromX;
        mToX = toX;
        mFromY = fromY;
        mToY = toY;
        mFromZ = fromZ;
        mToZ = toZ;
    }



    @Override
    protected void applyTransformation(float interpolatedTime, Transformation3D t) {
        float sx = 1.0f;
        float sy = 1.0f;
        float sz = 1.0f;

        if (mFromX != 1.0f || mToX != 1.0f) {
            sx = mFromX + ((mToX - mFromX) * interpolatedTime);
        }
        if (mFromY != 1.0f || mToY != 1.0f) {
            sy = mFromY + ((mToY - mFromY) * interpolatedTime);
        }
        if (mFromZ != 1.0f || mToZ != 1.0f) {
        	sz = mFromZ + ((mToZ - mFromZ) * interpolatedTime);
        }
		t.setScale(sx, sy, sz);
    }

}
