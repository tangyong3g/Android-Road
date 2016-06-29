package com.ty.example_unit_6.colorselector;

import android.app.Activity;
import android.os.Bundle;

/**
 * 
 * @author tangyong
 *
 */
public class ColorSelectorActivity extends Activity{
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(new CircleColorPicker(this));
	}

}
