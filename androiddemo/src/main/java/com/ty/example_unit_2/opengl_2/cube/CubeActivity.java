package com.ty.example_unit_2.opengl_2.cube;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;


/**
 * 
 * @author tangyong
 *
 */
public class CubeActivity extends Activity{
	
	GLSurfaceView view = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		view = new CubeView(this);
		setContentView(view);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		view.onResume();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		view.onPause();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
	}
}
