package com.ty.example_unit_3.libgdx.loadmode;

import android.os.Bundle;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.loaders.ModelLoaderRegistry;
import com.badlogic.gdx.graphics.g3d.model.Model;
import com.badlogic.gdx.graphics.g3d.model.still.StillModel;
import com.ty.util.DemoWapper;

public class LoadModeActivity extends AndroidApplication {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initialize(new EdgeDetectionTest(), true);
	}
	
	class ModeRenderer extends DemoWapper {

		// 定义相机
		PerspectiveCamera mCamera;
		// 定义模型　现在还不知道此模型有什么　不一样
		Model mStillModel;
		//定义纹理　
		Texture mTexture;
		float mAngle = 0.0f;
		
		//相机控制器
		PersperctiveCameraControler mControle ;
		
		@Override
		public void create() {
			super.create();
			//加载模型　
			mStillModel = 	ModelLoaderRegistry.loadStillModel(Gdx.files.internal("data/unit3/model/car.obj"));
			//设置相机
			mCamera = new PerspectiveCamera(200, Gdx.graphics.getWidth(),Gdx.graphics.getHeight());
//			mCamera.position.set(0, 300, 0);
//			mControle = mControle == null ? new PersperctiveCameraControler(mCamera) : mControle;
//			Gdx.input.setInputProcessor(mControle);
		}

		@Override
		public void render() {
			super.render();
			
			GL10 gl = Gdx.graphics.getGL10();

//			gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
			//清除缓存
			gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
			//打开浓度测试，光照，材质，纹理　
			gl.glEnable(GL10.GL_DEPTH_TEST);
			gl.glEnable(GL10.GL_LIGHTING);
			gl.glEnable(GL10.GL_COLOR_MATERIAL);
			gl.glEnable(GL10.GL_TEXTURE_2D);

			//指定哪一个矩阵堆栈是下一个矩阵操作的目标, 模型，纹理　，投影　
			gl.glMatrixMode(GL10.GL_PROJECTION);
			//回到中心原点
			gl.glLoadIdentity();
			// 	Gdx.graphics.getGLU().gluPerspective(Gdx.gl10, 45, 1, 1, 100);
			
			gl.glLoadMatrixf(mCamera.projection.val, 0);
			gl.glMatrixMode(GL10.GL_MODELVIEW);
			gl.glLoadMatrixf(mCamera.view.val, 0);
			gl.glEnable(GL10.GL_LIGHT0);
			gl.glLoadIdentity();
			
//			mCamera.position.add(0.1f,0.1f,0.1f);
			mCamera.update();
			
			gl.glPushMatrix();
			gl.glRotatef(mAngle, 0f, 1.0f, 0.0f);
			mStillModel.render();
			gl.glPopMatrix();
			mAngle += 0.8f;
		}

		@Override
		public void resize(int arg0, int arg1) {
			super.resize(arg0, arg1);
		}

	}

}
