package com.ty.example_unit_1;

import android.os.Bundle;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;


/**
 * 
 * @author tangyong
 * 
 */
public class SpritchActivity extends AndroidApplication {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initialize(new ViewPortRenderer(), true);
	}

	/**
	 * listener
	 * 
	 * @author tangyong
	 * 
	 */
	class ViewPortRenderer implements ApplicationListener {
		
		Sprite mSprite;
		SpriteBatch mSpriteBatch;

		@Override
		public void create() {
			Texture texture = new Texture(Gdx.files.internal("data/unit1/ic_launcher.png"));
			mSprite = new Sprite(new TextureRegion(texture));
			mSpriteBatch = new SpriteBatch();
		}

		@Override
		public void dispose() {

		}

		@Override
		public void pause() {

		}

		@Override
		public void render() {
			Gdx.gl20.glClear(Gdx.gl20.GL_COLOR_BUFFER_BIT);
			
			mSpriteBatch.begin();
			mSprite.draw(mSpriteBatch);
			mSpriteBatch.end();
		}

		@Override
		public void resize(int width, int height) {

		}

		@Override
		public void resume() {

		}

	}

}
