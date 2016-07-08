package com.graphics.enginedemo;

import com.graphics.engine.graphics.GLCanvas;
import com.graphics.engine.graphics.GLDrawable;
import com.graphics.engine.graphics.ext.ConvolutionShaderWrapper;
import com.graphics.engine.graphics.filters.GlowGLDrawable;
import com.graphics.engine.view.GLView;

import android.content.Context;


/**
 * 对比测试边缘检测效果和挖空发光效果
 * 
 * @author  dengweiming
 * @date  [2013-7-4]
 */
public class EdgeDetectTestView extends GLView {
	GLDrawable mDrawable1;	//原图
	GLDrawable mDrawable2;	//使用sobel算子进行边缘检测
	GLDrawable mDrawable3;	//使用挖空的发光效果，较慢，内部实现需要使用帧缓冲区
	
	float mDensity;
	int mColor = 0xff6dcaec;

	public EdgeDetectTestView(Context context) {
		super(context);
		setBackgroundColor(0xffffffff);
		mDrawable1 = GLDrawable.getDrawable(getResources(), R.drawable.weather);
		
		mDrawable2 = GLDrawable.getDrawable(getResources(), R.drawable.weather);
		mDrawable2.setShaderWrapper(ConvolutionShaderWrapper.getEdgeDetectionEffect(mColor, 0.5f));

		float density = getResources().getDisplayMetrics().density;
		mDensity = density;
		mDrawable3 = new GlowGLDrawable(getResources(), mDrawable1, 1 * density, false, true);
		((GlowGLDrawable) mDrawable3).setGlowColor(mColor);
		((GlowGLDrawable) mDrawable3).setGlowStrength((int) (50 * density));
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		int width = mDrawable1.getIntrinsicWidth();
		int height = mDrawable1.getIntrinsicHeight();
		float scale = h / 3 / (float) height / 1.1f;
		width = Math.round(width * scale);
		height = Math.round(height * scale);
		
		mDrawable1.setBounds(0, 0, width, height);
		mDrawable2.setBounds(0, 0, width, height);
		mDrawable3.setBounds(0, 0, width, height);
	}

	@Override
	protected void onDraw(GLCanvas canvas) {
		int height = mDrawable1.getBounds().height();
		float padding = 5 * mDensity;
		canvas.translate(padding, padding);
		mDrawable1.draw(canvas);
		canvas.translate(0, height + padding);
		mDrawable2.draw(canvas);
		canvas.translate(0, height + padding);
		mDrawable3.draw(canvas);

	}
}
