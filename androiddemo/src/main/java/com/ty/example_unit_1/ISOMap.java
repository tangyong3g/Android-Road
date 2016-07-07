package com.ty.example_unit_1;

import javax.microedition.khronos.opengles.GL;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.ty.libgdxusers.GuOrthoCam;
import com.ty.libgdxusers.GuPerspCam;
import com.ty.libgdxusers.Log;

/**
 * 
 * 
 * 绘制方式有点问题用的是Loop
 * 
 * 
 * 
 * @author tangyong
 * 
 */
public class ISOMap implements ApplicationListener, InputProcessor {
	
	private Vector2 mScreen = null;
	private Vector2 GRIDSIZE = new Vector2(16, 16);
	private GuOrthoCam mGuCam;
	private GuPerspCam mPersCam;
	
	private FloorGrid floor;
	
	@Override
	public void create() {
		//设置响应事件的实例
		Gdx.input.setInputProcessor(this);
		
		//打开深度测试
		Gdx.gl.glEnable(GL10.GL_DEPTH_TEST);
		Gdx.gl10.glDepthFunc(GL10.GL_LESS);
		
		//宽高
		mScreen = new Vector2(Gdx.graphics.getWidth(),Gdx.graphics.getHeight());
		
		//创建相机
		mPersCam = new GuPerspCam(mScreen.x, mScreen.y, 67.0f);
		mPersCam.position.x = 0f;
		mPersCam.position.y = 0f;
		mPersCam.position.z = 8f;
		
		mPersCam.lookAt(0, 0, 0);
		
		//创建16*16的 Floor 
		floor = new FloorGrid(GRIDSIZE);
		Gdx.input.setInputProcessor(this);
	}
	
	private float angle = 0.8f;

	@Override
	public void render() {
		Log.out("render");
		Gdx.gl10.glClear(GL10.GL_COLOR_BUFFER_BIT);
		
		mPersCam.update();
		mPersCam.apply(Gdx.graphics.getGL10());
		
		GL10 gl  = Gdx.graphics.getGL10();
		gl.glPushMatrix();
		angle+=0.8;
		gl.glRotatef(angle, 1, 0, 0);
		floor.renderWireframe(gl);
		gl.glPopMatrix();
	}

	@Override
	public boolean keyDown(int arg0) {
		return false;
	}

	@Override
	public boolean keyTyped(char arg0) {
		return false;
	}

	@Override
	public boolean keyUp(int arg0) {
		return false;
	}

	@Override
	public boolean mouseMoved(int arg0, int arg1) {
		return false;
	}

	@Override
	public boolean scrolled(int arg0) {
		return false;
	}

	@Override
	public boolean touchDown(int arg0, int arg1, int arg2, int arg3) {
		return false;
	}

	@Override
	public boolean touchDragged(int arg0, int arg1, int arg2) {
		return false;
	}

	@Override
	public boolean touchUp(int arg0, int arg1, int arg2, int arg3) {
		return false;
	}



	@Override
	public void dispose() {

	}

	@Override
	public void pause() {

	}


	@Override
	public void resize(int arg0, int arg1) {

	}

	@Override
	public void resume() {

	}

}

