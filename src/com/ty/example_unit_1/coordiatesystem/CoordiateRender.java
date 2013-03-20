package com.ty.example_unit_1.coordiatesystem;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.ty.util.DemoWapper;

/**
 * 
 * 画一个物体作为参照物，然后缓慢的移动相机，如果相对位置发生变化那么，如果相对位置没有发生变化那么
 * 
 * @author tangyong
 * 
 */
public class CoordiateRender extends DemoWapper {

	OrthographicCamera mCamera = null;
	SpriteBatch spriteBatch = null;
	Sprite sprite = null;
	Texture mTexture;
	Texture mTextureMap;
	
	Sprite spriteMap = null;
	
	@Override
	public void create() {
		super.create();
		mCamera = new OrthographicCamera();
		mCamera.position.set(0, 0 , 50);
		
		mTexture = new Texture(Gdx.files.internal("data/unit1/ic_launcher.png"));
		mTextureMap = new Texture(Gdx.files.internal("data/unit1/src_map.jpg"));
		
		sprite = new Sprite(mTexture);
		spriteBatch = new SpriteBatch();
		
		spriteMap = new Sprite(mTextureMap);
	}
	

	@Override
	public void render() {
		super.render();
		Gdx.gl20.glClear(Gdx.gl20.GL_COLOR_BUFFER_BIT);
		
		mCamera.position.add(0.1f, 0, 0);
		mCamera.update();
		
		spriteBatch.setProjectionMatrix(mCamera.combined);
		
		spriteBatch.begin();
		spriteMap.draw(spriteBatch);
		sprite.draw(spriteBatch);
		spriteBatch.end();
		
	}

	@Override
	public void resize(int arg0, int arg1) {
		super.resize(arg0, arg1);
	}

}
