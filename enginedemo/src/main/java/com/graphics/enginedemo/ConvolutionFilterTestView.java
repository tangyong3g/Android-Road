package com.graphics.enginedemo;

import com.graphics.engine.graphics.GLCanvas;
import com.graphics.engine.graphics.GLDrawable;
import com.graphics.engine.graphics.ext.ConvolutionShaderWrapper;
import com.graphics.engine.view.GLView;

import android.content.Context;
import android.view.MotionEvent;

/**
 * 测试图片卷积滤波效果
 * 
 * @author  dengweiming
 * @date  [2013-7-4]
 */
public class ConvolutionFilterTestView extends GLView {
	GLDrawable mDrawable;
	
	boolean mIsInTouch;
	

	public ConvolutionFilterTestView(Context context) {
		super(context);
		mDrawable = GLDrawable.getDrawable(getResources(), R.mipmap.ic_launcher);
		
		float[] filter1 = { //模糊
			1, 1, 1, 
			1, 1, 1,
			1, 1, 1,
		};
		float[] filter2 = { //边缘检测
				-2, -1, 0, 
				-1, 0, 1,
				0, 1, 2,

		};
		float[] filter3 = { //锐化
				0, -2, 0, 
				-2, 11, -2,
				0, -2, 0,
		};
		float[] filter4 = {	//浮雕
				-2, -1, 0, 
				-1, 1, 1,
				0, 1, 2,
		};
		float[] filter5 = { //横向边缘检测 Soble-X
				1, 0, -1, 
				2, 0, -2,
				1, 0, -1,
		};
		float[] filter6 = { //纵向边缘检测 Soble-Y
				-1, -2, -1,
				0, 0, 0,
				1, 2, 1, 
		};
		
		mDrawable.setShaderWrapper(new ConvolutionShaderWrapper(filter4));
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
