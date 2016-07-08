package com.graphics.enginedemo;

import com.graphics.engine.graphics.BitmapGLDrawable;
import com.graphics.engine.graphics.GLCanvas;
import com.graphics.engine.graphics.GLDrawable;
import com.graphics.engine.graphics.filters.GlowGLDrawable;
import com.graphics.engine.view.GLView;

import android.content.Context;
import android.view.MotionEvent;

/**
 * 
 */
class GlowTestView extends GLView {

	GLDrawable mDrawable;
	GLDrawable mDrawable2;

	GlowGLDrawable mGlowDrawable;
	boolean mDrawOrigin;

	public GlowTestView(Context context) {
		super(context);

		mDrawable = BitmapGLDrawable.getDrawable(getResources(), R.drawable.quick_time);
		//		setBackgroundColor(0xFF000000);//0xFFFFFFFF);

		float density = context.getResources().getDisplayMetrics().density;
		mDrawable2 = new BitmapGLDrawable(getResources(), mDrawable.getBitmap());

		mGlowDrawable = new GlowGLDrawable(getResources(), mDrawable, 2 * 6 * density, false, false);
		mGlowDrawable.setGlowColor(0xff00ff00);
		mGlowDrawable.setGlowStrength((int) (8 * density));
		mGlowDrawable.setBounds(-100, -100, 356, 356);
		mDrawable2.setBounds(-100, -100, 356, 356);
		mDrawable.setBounds(-100, -100, 356, 356);
		//		mDrawable2.setColorFilter(0x5f00ff00, PorterDuff.Mode.SRC_OUT);

		//内发光： inner=true ,knockOut=true, radius=7
		//轮廓：inner=false, knockOut=true, radius=1, strength=50, step=3

	}

	@Override
	protected void onDraw(GLCanvas canvas) {
		canvas.translate(200, 200);
		canvas.scale(0.7f, 0.7f);

		mGlowDrawable.draw(canvas);
		if (!mGlowDrawable.isBlurDone()) {
			invalidate();
		}
		if (mDrawOrigin) {
			mDrawable2.draw(canvas);
		}

	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN :
				mDrawOrigin = true;
				invalidate();
				break;
			case MotionEvent.ACTION_UP :
				mDrawOrigin = false;
				invalidate();
				break;
		}
		return true;
	}

}
