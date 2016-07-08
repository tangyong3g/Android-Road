package com.graphics.engine.interpolator;

import android.view.animation.Interpolator;

/**
 * <br>类描述: 二次贝塞尔曲线插值器
 * <br>功能详细描述:
 * 支持设置初始速度
 * 
 * @author  dengweiming
 * @date  [2013-9-10]
 */
public class Bezier2Interpolator implements Interpolator {
	float p1x2;	//CHECKSTYLE IGNORE
	
	/**
	 * @param v0	初始速度，直线 (0, 0) -> (1, v0) 的斜率
	 * <br>注意：
	 * 初始速度和结束速度之和为2。
	 * 实际动画值的速度 = 插值器曲线的速度 x (动画值变化范围/动画持续时间)。
	 * 
	 * <pre>
	 * 0 -> u^2	quadratic ease-in
	 * 1 -> u	linear
	 * 2 -> 1-(1-u)^2	quadratic ease-out
	 * >2		抛体运动，有过冲
	 * </pre>
	 */
	public Bezier2Interpolator(float v0) {
		p1x2 = v0;
	}

	@Override
	public float getInterpolation(float u) {
		if (u <= 0) {
			return 0;
		} else if (u >= 1) {
			return 1;
		}
		return (1 - u) * u * p1x2 + u * u;
	}
	
}
