package com.ty.example_unit_2.opengl_2.loadmodel;

import android.app.Activity;
import android.os.Bundle;


/**
 * 
 * @author tangyong
 * 
 */
public class LoadModelActivity extends Activity {
	
	ModelView mView = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mView = new ModelView(this);
		setContentView(mView);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		mView.onPause();
	}
	
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onRestart();
		mView.onResume();
	}

}
