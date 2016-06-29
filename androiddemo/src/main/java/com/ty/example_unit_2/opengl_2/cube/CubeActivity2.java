package com.ty.example_unit_2.opengl_2.cube;

import android.app.Activity;
import android.os.Bundle;

/**
 * 
 * @author tangyong
 * 
 */
public class CubeActivity2 extends Activity {
	
	CubeView2 view2 = null;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		view2 = new CubeView2(this);
		view2.setEGLContextClientVersion(2);
	
		Render render = new Render(this);
		view2.setRenderer(render);
		
		setContentView(view2);
	}

}
