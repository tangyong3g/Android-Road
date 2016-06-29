package com.ty.example_unit_2.opengl_1.cube;

import android.app.Activity;
import android.os.Bundle;

/**
 * 
 * 这个DEMO，演示了在openGL1.x中去绘制一个立方体。
 * 
 * 
 * @author tangyong
 * 
 */
public class CubeActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		CubeSurfaceView view = new CubeSurfaceView(this);
		this.setContentView(view);
	}

}
