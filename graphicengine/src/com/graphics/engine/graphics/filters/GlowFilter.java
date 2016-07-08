package com.graphics.engine.graphics.filters;

import android.content.res.Resources;

import com.graphics.engine.graphics.GLCanvas;
import com.graphics.engine.graphics.GLDrawable;

/**
 * <br>类描述: 发光效果过滤器
 * <br>功能详细描述:
 * 
 * @author  dengweiming
 * @date  [2013-5-14]
 */
public class GlowFilter implements GraphicsFilter {
	int mColor;
	float mGlowRadius;
	boolean mInner;
	boolean mKnockOut;
	int mStrength; 
	
	GlowGLDrawable mGlowGLDrawable;
	
	/**
	 * <默认构造函数>
	 * @param color			发光的颜色
	 * @param glowRadius 	发光的半径（单位是px，不同dpi下要预先将dp转到px），值越大则传播发光范围越大越模糊
	 * @param strength		发光的强度 [-255, 255]
	 * @param inner 		是否为内发光
	 * @param knockOut		是否镂空
	 * 例如创建一个勾勒轮廓的效果，可以使用(具体数值可以再细调)glowRadius=1*density, inner=false, knockOut=true。
	 * 如果是普通发光效果，可以使用glowRadius=6*density, inner=false, knockOut=false, 并且setGlowStrength(12*density)。
	 * 如果是内发光效果，可以使用glowRadius=8*density, inner=true, knockOut=true
	 */
	public GlowFilter(int color, float glowRadius, int strength, boolean inner, boolean knockOut) {
		if (glowRadius <= 0) {
			throw new IllegalArgumentException("glow Radius <= 0");
		}
		
		mColor = color;
		mGlowRadius = glowRadius;
		mStrength = strength;
		mInner = inner;
		mKnockOut = knockOut;
	}

	@Override
	public GLDrawable apply(GLCanvas canvas, Resources res, GLDrawable drawable, boolean yieldWhenDone) {
		if (mGlowGLDrawable == null) {
			mGlowGLDrawable = new GlowGLDrawable(res, drawable, mGlowRadius, mInner, mKnockOut);
			mGlowGLDrawable.setGlowColor(mColor);
			mGlowGLDrawable.setGlowStrength(mStrength);
			if (yieldWhenDone) {
				mGlowGLDrawable.setSourceGLDrawableYieldOnFilterDone();
			}
		}
		return mGlowGLDrawable.apply(canvas);
	}

	@Override
	public void reset() {
		if (mGlowGLDrawable != null) {
			mGlowGLDrawable.clear();
			mGlowGLDrawable = null;
		}
	}

	@Override
	public void invalidate() {
		if (mGlowGLDrawable != null) {
			mGlowGLDrawable.invalidate();
		}
	}

}
