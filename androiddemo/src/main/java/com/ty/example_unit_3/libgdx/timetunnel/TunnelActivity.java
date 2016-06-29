package com.ty.example_unit_3.libgdx.timetunnel;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;

/**
 * 
 * 
 * @author tangyong
 *
 */
public class TunnelActivity extends AndroidApplication{
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initialize(new Tunnel(), true);
	}

}
