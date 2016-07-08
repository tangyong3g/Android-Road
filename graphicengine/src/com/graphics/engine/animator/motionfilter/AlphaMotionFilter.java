package com.graphics.engine.animator.motionfilter;

import com.graphics.engine.animation.Transformation3D;
import com.graphics.engine.animator.FloatValuePairsAnimator;
import com.graphics.engine.view.GLView;

/**
 * 
 * <br>类描述: 透明度的运动过滤器
 * <br>功能详细描述:
 * 注意对相应的 GLView 调用 {@link GLView#setHasPixelOverlayed(boolean)}
 * 
 * @author  dengweiming
 * @date  [2013-10-9]
 */
public class AlphaMotionFilter extends FloatValuePairsAnimator implements MotionFilter {

	public AlphaMotionFilter(float fromAlpha, float toAlpha) {
		setAlpha(fromAlpha, toAlpha);
	}

	public void setAlpha(float fromAlpha, float toAlpha) {
		setValues(fromAlpha, toAlpha);
	}

	@Override
	public void getTransformation(Transformation3D t) {
		float[] values = getAnimatedValue();
		float alpha = Math.max(0, Math.min(values[0], 1));
		t.setAlpha(alpha);
	}

	@Override
	public boolean willChangeTransformationMatrix() {
		return false;
	}

	@Override
	public boolean willChangeBounds() {
		return false;
	}

	@Override
	public boolean hasAlpha() {
		return true;
	}

}
