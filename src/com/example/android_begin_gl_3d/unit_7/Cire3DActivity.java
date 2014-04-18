package com.example.android_begin_gl_3d.unit_7;

import android.os.Bundle;

import com.go.gl.GLActivity;

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
