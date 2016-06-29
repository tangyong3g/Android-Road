package com.ty.exsample_unit_5.translate;

import android.app.Activity;
import android.os.Bundle;

public class OrthCameraActivity extends Activity {
	
	OrthGLSurfaceView surfaceView;
	
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		surfaceView = new OrthGLSurfaceView(this);
		setContentView(surfaceView);
	}

}
