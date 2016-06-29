package com.ty.example_unit_1.coordiatesystem;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;

/**
 * 
 * 本DEMO要讨论的问题是　相机位置是原点吗？当相机位置发生变化的时候是否物体的位置也会变呢？
 * 
 * @author tangyong
 * 
 */
public class CoordiateSystemActivity extends AndroidApplication {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initialize(new CameraMove(), true);
	}

}
