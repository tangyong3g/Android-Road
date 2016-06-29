package com.ty.example_unit_1.cube;

import android.app.Activity;
import android.os.Bundle;

/**
 * 
 * @author tangyong
 * 
 */
public class CubeActivity  extends Activity{
	
	private ExerciseSurfaceView mSurfaceView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mSurfaceView = new ExerciseSurfaceView(this);
		setContentView(mSurfaceView);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mSurfaceView.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mSurfaceView.onPause();
	}

}
