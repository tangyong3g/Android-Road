package com.ty.example_unit_3.libgdx.loadmode;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;

public class SimpleLoadActivity extends AndroidApplication{
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initialize(new SimpleLoader(), false);
	}

}
