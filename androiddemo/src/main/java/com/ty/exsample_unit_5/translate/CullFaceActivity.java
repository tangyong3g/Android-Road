package com.ty.exsample_unit_5.translate;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;


/**
 * 
 * 本Demo用来说明Cull_face 和卷绕方式的关系
 * 
 * @author tangyong
 *
 */
public class CullFaceActivity extends AndroidApplication{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initialize(new CullFace(),true);
	}
	
}
