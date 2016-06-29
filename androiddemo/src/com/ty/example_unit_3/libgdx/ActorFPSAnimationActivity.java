package com.ty.example_unit_3.libgdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.ty.example_unit_3.libgdx.ex.Base3D;

public class ActorFPSAnimationActivity extends AndroidApplication {

	protected void onCreate(android.os.Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//		ActorLis lis = new ActorLis();

		ActorLis lis = new ActorLis();
		initialize(lis, true);

	};

	class ActorLis extends GdxTest {

		MoveActor mActor;
		Texture mTx;
		OrthographicCamera camera;
		Stage mStage;

		@Override
		public void create() {
			super.create();

//			initCamera();
			initActorTexture();
			initStage();

			Gdx.input.setInputProcessor(mStage);
		}

		private void initStage() {

			mStage = new Stage(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
//			mStage.setCamera(camera);

			mStage.addActor(mActor);
		}

		private void initCamera() {

			camera = new OrthographicCamera();
			camera.setToOrtho(false);

		}

		private void initActorTexture() {

			//初始化要绘制的纹理
			mTx = new Texture(Gdx.files.internal("data/animation.png"));
			TextureRegion[][] regions = TextureRegion.split(mTx, 32, 48);
			TextureRegion[] downWalkReg = regions[0];
			mActor = new MoveActor(downWalkReg);

		}

		protected void render(ModelBatch batch, Array<ModelInstance> instances) {

//			mCamera.update();
			
			mStage.act(Gdx.graphics.getDeltaTime());
			mStage.draw();

		}
		
		@Override
		public void render() {
			// TODO Auto-generated method stub
			super.render();
			mStage.act(Gdx.graphics.getDeltaTime());
			mStage.draw();
		}


		@Override
		public void resize(int width, int height) {
			// TODO Auto-generated method stub
			super.resize(width, height);
//			camera.setToOrtho(true, width, height);
		}

		@Override
		public void dispose() {
			super.dispose();
		}

	}

	class MoveActor extends Actor {

		TextureRegion[] regionArray;
		//要绘制图片的动画
		Animation mAnimation;
		// 要绘制图形的纹理
		Texture mTexture;

		float ANIMATION_SPEED = 0.1f;

		float lastX;
		float lastY;

		float currentFrameTime = 0;

		public MoveActor(TextureRegion[] regionArray) {

			this.regionArray = regionArray;

			mAnimation = new Animation(ANIMATION_SPEED, regionArray);
			TextureRegion region = regionArray[0];

			setWidth(region.getRegionWidth());
			setHeight(region.getRegionHeight());

			addListener(new InputListener() {
				public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
					// we only care for the first finger to make things easier
					if (pointer != 0)
						return false;

					// record the coordinates the finger went down on. they
					// are given relative to the actor's upper left corner (0, 0)
					lastX = x;
					lastY = y;
					
					Gdx.app.log("cycle", "点中我了!");
					
					return true;
				}
				
				@Override
				public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
					
					if(pointer != 0){
						return;
					}
					
					Gdx.app.log("cycle", "起来了");
				}

				public void touchDragged(InputEvent event, float x, float y, int pointer) {
					// we only care for the first finger to make things easier
					if (pointer != 0)
						return;

					// adjust the actor's position by (current mouse position - last mouse position)
					// in the actor's coordinate system.
					translate(x - lastX, y - lastY);

					// save the current mouse position as the basis for the next drag event.
					// we adjust by the same delta so next time drag is called, lastX/lastY
					// are in the actor's local coordinate system automatically.
					lastX = x - (x - lastX);
					lastY = y - (y - lastY);
				}
				
			});
		}

		@Override
		public void draw(SpriteBatch batch, float parentAlpha) {

			currentFrameTime += Gdx.graphics.getDeltaTime();

			TextureRegion frame = mAnimation.getKeyFrame(currentFrameTime, true);
			batch.draw(frame, getX(), getY());

		}

	}

}
