package com.ty.example_unit_3.libgdx.loadmode;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.graphics.g3d.test.StillModelViewerGL20;

/**
 * 
 * @author tangyong
 *
 */
public class MaterialOpenGL2Activity extends AndroidApplication{
	
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initialize(new StillModelViewerGL20("data/models/basicscene.obj", "data/multipleuvs_1.png","data/multipleuvs_2.png"), true);
	}

}
