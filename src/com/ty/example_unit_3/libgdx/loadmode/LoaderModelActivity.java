package com.ty.example_unit_3.libgdx.loadmode;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;

public class LoaderModelActivity extends AndroidApplication{
	
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initialize(new LoadModelApp("data/unit3/model/basicscene.obj","data/unit3/pic/multipleuvs_1.png","data/unit3/pic/multipleuvs_2.png"), true);
	}
}
