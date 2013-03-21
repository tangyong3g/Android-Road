package com.ty.example_unit_2.opengl_2.meshcube;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.graphics.Mesh;
import com.ty.util.DemoWapper;

import android.app.Activity;
import android.os.Bundle;

/**
 * 
 * @author tangyong
 * 
 */
public class ＭeshCubeActivity extends AndroidApplication {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	class MeshRender extends DemoWapper {

		Mesh mesh = null;
		
		
		//初始化网张的顶点坐标
		private void initVertices(){
			
			
		}

		@Override
		public void create() {
			super.create();
		}
		
		
		@Override
		public void resume() {
			super.resume();
		}

		@Override
		public void resize(int arg0, int arg1) {
			super.resize(arg0, arg1);
		}

	}

}
