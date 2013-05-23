package com.ty.example_unit_3.libgdx.animation;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;

/**
 * 
 * @author tangyong ty_sany@163.com
 *
 */
public class MaterialAnimcationActivity extends AndroidApplication{
	
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initialize(new MaterialAnimation(), false);
	}
	

}
