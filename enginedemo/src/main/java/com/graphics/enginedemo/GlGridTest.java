package com.graphics.enginedemo;

import android.content.Context;
import android.util.AttributeSet;

import com.graphics.engine.graphics.GLCanvas;
import com.graphics.engine.graphics.geometry.ColorGLObjectRender;
import com.graphics.engine.graphics.geometry.GLCircle;
import com.graphics.engine.view.GLFrameLayout;


/**
 * 
 * @author tang
 *
 */
public class GlGridTest extends GLFrameLayout {

	//要绘制的圆
	GLCircle mCirecle;
	// render
	ColorGLObjectRender mTxRender;

	public GlGridTest(Context context, AttributeSet attrs) {
		super(context, attrs);

		init();
	}

	@Override
	protected void dispatchDraw(GLCanvas canvas) {
		super.dispatchDraw(canvas);

	}

	private void init() {

		mCirecle = new GLCircle(64, true);
		mCirecle.setRadius(20);

		mTxRender = new ColorGLObjectRender();

	}

}
