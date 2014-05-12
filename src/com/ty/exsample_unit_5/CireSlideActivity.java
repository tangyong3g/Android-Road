package com.ty.exsample_unit_5;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.app.Activity;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.os.Bundle;

/**
 * 
 * @author tang
 *
 */
public class CireSlideActivity extends Activity {

	GLSurfaceView mSurfaceView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mSurfaceView = new CireSlideSurfaceView(this);
		setContentView(mSurfaceView);
	}

	/**
	 * 
	 * @author tang
	 *CireSlideRender
	 */
	class CireSlideSurfaceView extends GLSurfaceView {

		public CireSlideSurfaceView(Context context) {
			super(context);
		}
	}

	/**
	 * @author tang
	 */
	class CireSlideRender implements Renderer {

		@Override
		public void onSurfaceCreated(GL10 gl, EGLConfig config) {

		}

		@Override
		public void onSurfaceChanged(GL10 gl, int width, int height) {

		}

		@Override
		public void onDrawFrame(GL10 gl) {

		}

	}
}
