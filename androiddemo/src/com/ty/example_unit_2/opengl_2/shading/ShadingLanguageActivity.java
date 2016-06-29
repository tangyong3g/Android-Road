package com.ty.example_unit_2.opengl_2.shading;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

/**
 * 此DEMO主要演示，着色语言的使用。
 * 
 * 主要是顶点着色器与片元着色器。
 * 
 * 另外在不同的深度可以见到的区域是可以计算的。
 * 
 * 本DEMO中就把左右变成了单位 1 
 * 
 * @author tangyong
 * 
 */
public class ShadingLanguageActivity extends Activity {

	
	GLSurfaceView mView = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mView = new ShandingView(this);
		this.setContentView(mView);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mView.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mView.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		
	}

}
