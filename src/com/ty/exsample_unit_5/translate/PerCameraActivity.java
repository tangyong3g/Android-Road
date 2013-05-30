package com.ty.exsample_unit_5.translate;

import android.app.Activity;
import android.os.Bundle;


/**
 * 透视投影
 * 
 * @author tangyong ty_sany@163.com
 *
 */
public class PerCameraActivity extends Activity{
	
	PerCameraView surfaceView;
	
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		surfaceView = new PerCameraView(this);
		setContentView(surfaceView);
	}

}
