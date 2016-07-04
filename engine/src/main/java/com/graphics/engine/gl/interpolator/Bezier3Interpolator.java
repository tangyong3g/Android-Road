package com.graphics.engine.gl.interpolator;

import android.view.animation.Interpolator;

/**
 * <br>类描述: 三次贝塞尔曲线插值器
 * <br>功能详细描述:
 * 支持设置初始速度和结束速度
 * 
 * @author  dengweiming
 * @date  [2013-9-10]
 */
public class Bezier3Interpolator implements Interpolator {
	float p1x3, p2x3;	//CHECKSTYLE IGNORE
	
	/**
	 * @param v0	初始速度，直线 (0, 0) -> (1, v0) 的斜率
	 * @param v1	结束速度，直线 (0, 1 - v1) -> (1, 1) 的斜率
	 * <br>注意：一些参数的组合可能会导致视觉上不自然的结果，例如 (1, 3)。
	 * 实际动画值的速度 = 插值器曲线的速度 x (动画值变化范围/动画持续时间)。
	 * 
	 * <pre>
	 * 一些参数值 (v0, v1) 的结果：
	 * (0, 0) -> u^2(3-2u)	smoothStep
	 * (0, 3) -> u^3		cubic ease-in
	 * (3, 0) -> 1-(1-u)^3	cubic ease-out
	 * (0, 2) -> u^2		quadratic ease-in	适合自然进入
	 * (2, 0) -> 1-(1-u)^2	quadratic ease-out
	 * (1, 1) -> u		linear
	 * (tension+3, 0) 	overshoot		张力tension=3效果较好
	 * (0.5, 2.5) -> u^3-0.5u^2+0.5u			适合快速地退出
	 * </pre>
	 */
	public Bezier3Interpolator(float v0, float v1) {
		p1x3 = v0;
		p2x3 = 3 - v1;
	}

	@Override
	public float getInterpolation(float u) {
		if (u <= 0) {
			return 0;
		} else if (u >= 1) {
			return 1;
		}
		float v = 1 - u;
		float uu = u * u;
		float vv = v * v;
		// p0 = 0, p1 = 1
		return vv * u * p1x3 + v * uu * p2x3 + uu * u;
	}
	
}
