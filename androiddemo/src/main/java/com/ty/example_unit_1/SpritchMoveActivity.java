package com.ty.example_unit_1;

import android.os.Bundle;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.ty.util.DemoWapper;


/**
 * 
 * 本Demo要学会的内容有:
 * 
 * 1:SpritchBatch绘制纹理　。
 * 2:touchScreen的利用。
 * 3:屏幕坐标系到世界坐标系的转换。
 * 
 * @author tangyong
 * 
 */
public class SpritchMoveActivity extends AndroidApplication {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initialize(new SpriteMoveRenderer(), true);
	}

	/**
	 * 
	 * @author tangyong
	 * 
	 */
	class SpriteMoveRenderer extends DemoWapper {

		Texture mTexture;
		Vector3 mSpriteVerctor = new Vector3();
		SpriteBatch mSpriteBatch = null;
		OrthographicCamera mCamera;

		@Override
		public void create() {
			super.create();

			// 创建一幅纹理　
			mTexture = new Texture(Gdx.files.internal("data/unit1/ic_launcher.png"));

			// 创建　SpriteBatch
			mSpriteBatch = new SpriteBatch();
			// 相机
			mCamera = new OrthographicCamera();
			
			//因为 	mSpriteBatch.draw(mTexture, mSpriteVerctor.x, mSpriteVerctor.y); 指定的 x, y origin原点是在左下角所以Y轴是向上的 so false
			mCamera.setToOrtho(false);
		}

		
		@Override
		public void render() {
			super.render();

			Gdx.gl20.glClearColor(0, 0, 0, 1);
			Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);

			mSpriteBatch.begin();
			mSpriteBatch.draw(mTexture, mSpriteVerctor.x, mSpriteVerctor.y);
			mSpriteBatch.end();

			if (Gdx.input.isTouched()) {
				// 屏幕坐标系向世界坐标系的转换
				mCamera.unproject(mSpriteVerctor.set(Gdx.input.getX(),
						Gdx.input.getY(), 0));
			}
		}

		@Override
		public void resize(int arg0, int arg1) {
			super.resize(arg0, arg1);
		}
	}

	class SpritchMoveRenderer extends DemoWapper {

		Texture texture;
		SpriteBatch batch;
		OrthographicCamera camera;
		Vector3 spritePosition = new Vector3();

		public void create() {
			// create a SpriteBatch with which to render the sprite
			batch = new SpriteBatch();

			// load the sprite's texture. note: usually you have more than
			// one sprite in a texture, see {@see TextureAtlas} and {@see
			// TextureRegion}.
			texture =  new Texture(
					Gdx.files.internal("data/unit1/ic_launcher.png"));


			// create an {@link OrthographicCamera} which is used to transform
			// touch coordinates to world coordinates.
			camera = new OrthographicCamera();

			// we want the camera to setup a viewport with pixels as units, with
			// the
			// y-axis pointing upwards. The origin will be in the lower left
			// corner
			// of the screen.
			camera.setToOrtho(false);
		}

		public void render() {
			// set the clear color and clear the screen.
			Gdx.gl.glClearColor(1, 1, 1, 1);
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

			// draw the sprite
			batch.begin();
			batch.draw(texture, spritePosition.x, spritePosition.y);
			batch.end();

			// if a finger is down, set the sprite's x/y coordinate.
			if (Gdx.input.isTouched()) {
				// the unproject method takes a Vector3 in window coordinates
				// (origin in
				// upper left corner, y-axis pointing down) and transforms it to
				// world
				// coordinates.
				camera.unproject(spritePosition.set(Gdx.input.getX(),
						Gdx.input.getY(), 0));
			}
		}
	}

}
