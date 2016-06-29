package com.ty.example_unit_3.libgdx.loadmode;

import android.os.Bundle;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.android.AndroidApplication;

/**
 * 
 * @author tangyong
 *
 */
public class MaterialOpenGL2Activity extends AndroidApplication{
	
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		initialize(new StillModelViewerGL20("data/models/basicscene.obj", "data/multipleuvs_1.png","data/multipleuvs_2.png"), true);
		
		initialize(new ApplicationListener() {
			
			@Override
			public void resume() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void resize(int arg0, int arg1) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void render() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void pause() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void dispose() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void create() {
				// TODO Auto-generated method stub
				
			}
		}, true);
	}

}
