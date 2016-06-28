package com.graphics.enginedemo;

import com.go.gl.GLActivity;

import android.os.Bundle;

/**
 * 
 * @author tang
 * 
 */
public class Cire3DActivity extends GLActivity {

	CylinderDragTestView mCylinderView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mCylinderView = new CylinderDragTestView(this);
		setContentGlView(mCylinderView);
	}

}
