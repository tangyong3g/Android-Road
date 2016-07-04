package com.graphics.engine.gl.interpolator;

import android.view.animation.Interpolator;

/**
 * <br>类描述: 加速减速的插值器
 * <br>功能详细描述:
 * 和SDK的加速减速插值器比较接近，比其他加速减速插值器（quadratic, cubic等）还要平缓
 * 
 * @author  dengweiming
 * @date  [2013-9-10]
 */
public class SmoothStepInterpolator implements Interpolator {
	
	@Override
	public float getInterpolation(float input) {
		 return (3 - input * 2) * input * input;	//CHECKSTYLE IGNORE
	}
	
}

