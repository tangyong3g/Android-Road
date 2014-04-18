package com.example.android_begin_gl_3d.unit_7;

import android.content.Context;
import android.view.MotionEvent;

import com.example.android_begin_gl_3d.R;
import com.go.gl.graphics.GLCanvas;
import com.go.gl.graphics.GLDrawable;
import com.go.gl.view.GLView;

/**
 * 
 */
public class RotateTestView extends GLView {
	// 用来绘制的图片
	GLDrawable mDrawable;
	float mEuler[] = new float[3];

	// 初始化
	public RotateTestView(Context context) {
		super(context);
		mDrawable = GLDrawable.getDrawable(getResources(), R.drawable.bg_one);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		mDrawable.setBounds(0, 0, (int) (w * 0.75f), (int) (h * 0.75f));
	}

	float t;

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		t = (event.getX() - 100) / (getWidth() - 200);
		t = Math.max(0, Math.min(t, 1));
		invalidate();
		return true;
	}

	@Override
	protected void onDraw(GLCanvas canvas) {
		canvas.translate(getWidth() / 2, getHeight() / 2);

		int w = mDrawable.getBounds().width();
		int h = mDrawable.getBounds().height();

		float delta = (float) Math.toRadians(30);
		//		float delta = (float) Math.atan(w / (float) h);
		float a = delta * (1 - t);
		//		canvas.rotateAxisAngle(180 * t, (float) Math.sin(a), (float) Math.cos(a), 0);
		GLCanvas.convertAxisAngleToEulerAngle(180 * t, (float) Math.sin(a), (float) Math.cos(a), 0,
				mEuler);
		canvas.rotateEuler(mEuler[0], mEuler[1], mEuler[2]);

		canvas.translate(-w / 2, -h / 2);
		mDrawable.draw(canvas);

		canvas.translate(w / 2, 0);
		canvas.rotateAxisAngle(180, 0, 1, 0);
		canvas.translate(-w / 2, 0);
		mDrawable.draw(canvas);
	}

}
