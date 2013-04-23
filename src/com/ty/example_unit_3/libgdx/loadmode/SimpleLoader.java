package com.ty.example_unit_3.libgdx.loadmode;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.loaders.ModelLoaderRegistry;
import com.badlogic.gdx.graphics.g3d.model.still.StillModel;
import com.ty.util.DemoWapper;


/**
 * 
 * 本demo 主要为了学习libGDX加载模型
 * 
 * demo  的内容是加载一个正方形
 * 
 * @author Z61
 *
 */
public class SimpleLoader extends DemoWapper{

	//透视投影相机
	PerspectiveCamera mCamera;
	//纹理 
	Texture mTexture;
	//模型 
	StillModel mesh;
	
	
	@Override
	public void create() {
		super.create();
		mesh =  ModelLoaderRegistry.loadStillModel(Gdx.files.internal("data/unit3/cube.obj"));
		mTexture = new Texture(Gdx.files.internal("data/cube_simple.png"));
		
		//设置相机参数
		mCamera = new PerspectiveCamera(127, 4, 4);
		mCamera.position.set(3, 3, 3000);
		mCamera.direction.set(-1, -1, -1);
	}
	
	@Override
	public void render() {
		super.render();
		
		GL10 gl = Gdx.graphics.getGL10();

		gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		gl.glEnable(GL10.GL_DEPTH_TEST);
//		gl.glEnable(GL10.GL_LIGHTING);
//		gl.glEnable(GL10.GL_COLOR_MATERIAL);
		gl.glEnable(GL10.GL_TEXTURE_2D);

		mCamera.update();
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glLoadIdentity();
		//投影矩阵 用来投影的如果不设置那么，就没有任何图像
		gl.glLoadMatrixf(mCamera.projection.val, 0);
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		//视图矩阵  对相机位置起作用
		gl.glLoadMatrixf(mCamera.view.val, 0);

		mTexture.bind();
		mesh.render();
	}
	
	
	
	
	
}
