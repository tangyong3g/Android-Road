package com.example.androiddemo.unit_7;

import android.content.Context;
import android.util.AttributeSet;

import com.go.gl.graphics.GLCanvas;
import com.go.gl.graphics.geometry.ColorGLObjectRender;
import com.go.gl.graphics.geometry.GLCircle;
import com.go.gl.view.GLFrameLayout;

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
