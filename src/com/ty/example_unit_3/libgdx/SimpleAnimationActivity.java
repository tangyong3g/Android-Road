package com.ty.example_unit_3.libgdx;

import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLSurfaceView.Renderer;
import android.os.Bundle;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.ty.example_unit_3.libgdx.ex.Base3D;

/**
 * 
 * @author tangyong
 *
 */
public class SimpleAnimationActivity extends AndroidApplication{
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initialize(new SimpleAnimationRender(), true);
	}
	
	class SimpleAnimationRender extends Base3D{


		private Animation currentWalk;
		private float currentFrameTime;
		private Vector2 position;

		private Texture texture;

		private Animation downWalk;
		private Animation leftWalk;
		private Animation rightWalk;
		private Animation upWalk;
		private SpriteBatch spriteBatch;
		private ArrayList<Animation> mAnimationList;

		private static final float ANIMATION_SPEED = 0.2f;
		
		private float mDownPositionX;
		private float mUpPositionX;
		private int mIndex = 0;

		@Override
		public void create () {
			Gdx.input.setInputProcessor(this);
			texture = new Texture(Gdx.files.internal("data/animation.png"));
			TextureRegion[][] regions = TextureRegion.split(texture, 32, 48);
			TextureRegion[] downWalkReg = regions[0];
			TextureRegion[] leftWalkReg = regions[1];
			TextureRegion[] rightWalkReg = regions[2];
			TextureRegion[] upWalkReg = regions[3];
			downWalk = new Animation(ANIMATION_SPEED, downWalkReg);
			leftWalk = new Animation(ANIMATION_SPEED, leftWalkReg);
			rightWalk = new Animation(ANIMATION_SPEED, rightWalkReg);
			upWalk = new Animation(ANIMATION_SPEED, upWalkReg);

			currentWalk = leftWalk;
			currentFrameTime = 0.0f;

			spriteBatch = new SpriteBatch();
			position = new Vector2();
			mAnimationList = new ArrayList<Animation>();
			
			mAnimationList.add(downWalk);
			mAnimationList.add(leftWalk);
			mAnimationList.add(rightWalk);
			mAnimationList.add(upWalk);
		}

		@Override
		public void render () {
			Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
			Gdx.gl.glClearColor(1, 1, 1, 1);
			currentFrameTime += Gdx.graphics.getDeltaTime();

			spriteBatch.begin();
			TextureRegion frame = currentWalk.getKeyFrame(currentFrameTime, true);
			spriteBatch.draw(frame, position.x, position.y);
			spriteBatch.end();
		}

		@Override
		public boolean touchDown (int x, int y, int pointer, int button) {
		
			position.x = x;
			position.y = Gdx.graphics.getHeight() - y;
			mDownPositionX = x;
			return true;
		}
		
		@Override
		public boolean touchUp(int screenX, int screenY, int pointer, int button) {
			mUpPositionX = screenX;
			if(mUpPositionX - mDownPositionX > 80){
				currentWalk = mAnimationList.get(++mIndex);
				if(mIndex>=3){
					mIndex = 0;
				}
			}
			return super.touchUp(screenX, screenY, pointer, button);
		}

		@Override
		public void dispose () {
			spriteBatch.dispose();
			texture.dispose();
		}

		@Override
		protected void render(ModelBatch batch, Array<ModelInstance> instances) {
			
		}

		@Override
		protected void update(float delaTime) {
			
		}
		
		@Override
		public boolean touchDragged(int screenX, int screenY, int pointer) {
			return super.touchDragged(screenX, screenY, pointer);
		}

		
	}

}
