package com.ty.example_unit_2.opengl_1.cuberotate;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

/**
 * 
 * @author tangyong
 * 
 */
public class CubeRotateActivity extends Activity {
	
	
	GLSurfaceView view = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		view = new CubeRotateSurfaceView(this);
		setContentView(view);
	}
	
	@Override
	protected void onRestart() {
		super.onRestart();
	}
	
	protected void onResume() {
		super.onResume();
		view.onResume();
	}


	@Override
	protected void onPause() {
		super.onPause();
		view.onPause();
	}
	
}
