package com.ty.example_unit_1;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;

public class ISOMapActivity extends AndroidApplication{
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initialize(new ISOMap(), false);
	}

}
