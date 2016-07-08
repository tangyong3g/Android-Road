package com.graphics.enginedemo;

import com.graphics.engine.graphics.GLCanvas;
import com.graphics.engine.graphics.GLDrawable;
import com.graphics.engine.graphics.ext.ColorMatrixShaderWrapper;
import com.graphics.engine.view.GLView;

import android.content.Context;
import android.view.MotionEvent;


/**
 * 测试图片灰度化效果
 * 
 * @author  dengweiming
 * @date  [2013-7-4]
 */
public class ColorMatrixTestView extends GLView {
	GLDrawable mDrawable;
	
	boolean mIsInTouch;
	

	public ColorMatrixTestView(Context context) {
		super(context);
		mDrawable = GLDrawable.getDrawable(getResources(), R.mipmap.ic_launcher);
		
		/*
		ColorMatrix matrix = new ColorMatrix(new float[] {
				0.299f, 0.587f, 0.114f, 0, 0,  
				0.299f, 0.587f, 0.114f, 0, 0,
				0.299f, 0.587f, 0.114f, 0, 0,
				0, 0, 0, 1, 0,
			});
		ColorMatrixShaderWrapper wrapper = new ColorMatrixShaderWrapper(matrix);
		*/
//		ColorMatrixShaderWrapper wrapper = ColorMatrixShaderWrapper.getSepiaEffect();
		ColorMatrixShaderWrapper wrapper = ColorMatrixShaderWrapper.getGrayEffect();
		
		mDrawable.setShaderWrapper(wrapper);
	}
	
	@Override
	protected void onDraw(GLCanvas canvas) {
		if (mIsInTouch) {
			mDrawable.draw(canvas);
		} else {
			mDrawable.drawWithoutEffect(canvas);
		}
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		mIsInTouch = event.getAction() == MotionEvent.ACTION_MOVE;
		invalidate();
		return true;
	}
}
