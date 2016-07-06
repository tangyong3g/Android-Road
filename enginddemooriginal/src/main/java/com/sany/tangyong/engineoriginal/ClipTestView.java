package com.sany.tangyong.engineoriginal;

import android.content.Context;

import com.go.gl.graphics.ColorGLDrawable;
import com.go.gl.graphics.GLCanvas;
import com.go.gl.graphics.GLDrawable;
import com.go.gl.view.GLView;

/**
 * 
 */
public class ClipTestView extends GLView {
	GLDrawable mDrawable1;
	GLDrawable mDrawable2;
	GLDrawable mDrawable3;
	long mStartTime;

	public ClipTestView(Context context) {
		super(context);
		mDrawable1 = new ColorGLDrawable(0x7fff0000);
		mDrawable2 = new ColorGLDrawable(0x7f00ff00);
		mDrawable3 = new ColorGLDrawable(0x7f0000ff);
		mDrawable1.setBounds(-200, -200, 200, 200);
		mDrawable2.setBounds(-200, -200, 200, 200);
		mDrawable3.setBounds(-200, -200, 200, 200);
		setBackgroundColor(0xff000000);

		mStartTime = System.currentTimeMillis();
	}

	@Override
	protected void onDraw(GLCanvas canvas) {
		float t = (System.currentTimeMillis() - mStartTime) / 50000.0f;

		//canvas.translate(0, 0, -500);
		canvas.translate(getWidth() / 2, getHeight() / 2);
		canvas.clipRect(-220, -220, 220, 220);

		/*
		canvas.startClipRegion();
		canvas.fillTriangle(-200, -200, -200, 200, 200, -200);
		//		canvas.finishClipRegion();
		//		canvas.startClipRegion();
		canvas.fillTriangle(-200, -200, -200, 200, 200, 200);
		canvas.finishClipRegion();
		*/

		canvas.translate(200, 0);
		canvas.save();
		canvas.translate(-200, 0);
		canvas.rotate(360 * t);
		mDrawable1.draw(canvas);
		canvas.clipRect(-200, -200, 200, 200);
		canvas.rotate(300 * t);
		canvas.clipRect(-100, -50, 180, 160);
		int saveCount = canvas.save();
		canvas.clipRect(-100, -50, 180, 160);
		mDrawable2.draw(canvas);
		canvas.restoreToCount(saveCount);
		canvas.rotate(400 * t);
		canvas.clipRect(-100, -100, 200, 200);
		mDrawable3.draw(canvas);
		canvas.restore();

		canvas.translate(-200, 0);
		canvas.rotate(360 * t);
		mDrawable3.draw(canvas);

		invalidate();
	}

}
