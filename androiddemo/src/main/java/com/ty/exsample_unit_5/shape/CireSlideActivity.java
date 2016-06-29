package com.ty.exsample_unit_5.shape;

import android.app.Activity;
import android.opengl.GLSurfaceView;
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


}
