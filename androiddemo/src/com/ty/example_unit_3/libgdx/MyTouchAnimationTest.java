package com.ty.example_unit_3.libgdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.ty.example_unit_3.libgdx.ex.Base3D;

/**
 * 
 * 
 * @author tangyong*/

public class MyTouchAnimationTest extends Base3D {

	SpriteBatch mBatch;
	BitmapFont font;
	TextureRegion region;
	Sprite sprite;
	TextureAtlas atlas;
	Stage stage;
	MyActor image;
	OrthographicCamera camera;

	private static final float ANIMATION_SPEED = 0.2f;

	SpriteBatch mSpriteBatch = null;

	private float currentFrameTime;

	private Vector2 position;

	private Texture mTx;

	private Animation mAni;

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		position.x = screenX;
		position.y = Gdx.graphics.getHeight() - screenY;
		return true;
	}

	@Override
	public void create() {

		mSpriteBatch = new SpriteBatch();

		//初始化要绘制的纹理
		mTx = new Texture(Gdx.files.internal("data/animation.png"));
		TextureRegion[][] regions = TextureRegion.split(mTx, 32, 48);

		TextureRegion[] downWalkReg = regions[0];

		mAni = new Animation(ANIMATION_SPEED, downWalkReg);

		position = new Vector2();

		// a bitmap font to draw some text, note that we
		// pass true to the constructor, which flips glyphs on y
		font = new BitmapFont(Gdx.files.internal("data/arial-15.fnt"), true);

		// a texture region, note the flipping on y again
		region = new TextureRegion(new Texture("data/badlogic.jpg"));
		region.flip(false, true);

		// a texture atlas, note the boolean
		atlas = new TextureAtlas(Gdx.files.internal("data/pack"), true);

		// a sprite, created from a region in the atlas
		sprite = atlas.createSprite("badlogicsmall");
		sprite.setPosition(0, 0);

		// a sprite batch with which we want to render
		mBatch = new SpriteBatch();

		// a camera, note the setToOrtho call, which will set the y-axis
		// to point downwards
		camera = new OrthographicCamera();
		camera.setToOrtho(true);

		// a stage which uses our y-down camera and a simple actor (see MyActor below),
		// which uses the flipped region. The key here is to
		// set our y-down camera on the stage, the rest is just for demo purposes.
		stage = new Stage(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);
		stage.setCamera(camera);
		image = new MyActor(region);
		image.setPosition(100, 100);
		stage.addActor(image);
		// finally we write up the stage as the input process and call it a day.
		Gdx.input.setInputProcessor(stage);
	}

	@Override
	public void resize(int width, int height) {
		// handling resizing is simple, just set the camera to ortho again
		camera.setToOrtho(true, width, height);
	}

	@Override
	public void render() {
		// clear the screen, update the camera and make the sprite batch
		// use its matrices.
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		camera.update();
		mBatch.setProjectionMatrix(camera.combined);

		// render all the things, we render in a y-down
		// cartesian coordinate system
		mBatch.begin();
		// drawing a region, x and y will be the top left corner of the region, would be bottom left
		// with y-up.
		mBatch.draw(region, 20, 100);
		// drawing text, x and y will be the top left corner for text, same as with y-up
		font.draw(mBatch, "This is a test", 270, 100);
		// drawing regions from an atlas, x and y will be the top left corner.
		// you shouldn't call findRegion every frame, cache the result.
		mBatch.draw(atlas.findRegion("badlogicsmall"), 360, 100);
		// drawing a sprite created from an atlas, FIXME wut?! AtlasSprite#setPosition seems to be wrong
		sprite.setColor(Color.RED);
		sprite.draw(mBatch);
		// finally we draw our current touch/mouse coordinates
		font.draw(mBatch, Gdx.input.getX() + ", " + Gdx.input.getY(), 0, 0);
		mBatch.end();

		currentFrameTime += Gdx.graphics.getDeltaTime();

		//		mSpriteBatch.begin();
		//		TextureRegion frame = mAni.getKeyFrame(currentFrameTime, true);
		//		mSpriteBatch.draw(frame, Gdx.input.getX(),Gdx.input.getY());
		//		mSpriteBatch.end();

		// tell the stage to act and draw itself
		stage.act(Gdx.graphics.getDeltaTime());
		stage.draw();

	}

	/** A very simple actor implementation that does not obey rotation/scale/origin set on the actor. Allows dragging of the actor.
	 * 
	 * 
	 * @author tangyong
	 * 
	 *  */
	public class MyActor extends Actor {

		TextureRegion region;

		//要绘制图片的动画
		Animation mAnimation;
		// 要绘制图形的纹理
		Texture mTexture;

		float lastX;
		float lastY;

		public MyActor(TextureRegion region) {

			//初始化要绘制的纹理
			mTexture = new Texture(Gdx.files.internal("data/animation.png"));
			TextureRegion[][] regions = TextureRegion.split(mTexture, 32, 48);

			TextureRegion[] downWalkReg = regions[0];

			mAnimation = new Animation(ANIMATION_SPEED, downWalkReg);

			this.region = region;
			//			setWidth(region.getRegionWidth());
			//			setHeight(region.getRegionHeight());

			setWidth(downWalkReg[0].getRegionWidth());
			setHeight(downWalkReg[0].getRegionHeight());

			addListener(new InputListener() {
				public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
					// we only care for the first finger to make things easier
					if (pointer != 0)
						return false;

					// record the coordinates the finger went down on. they
					// are given relative to the actor's upper left corner (0, 0)
					lastX = x;
					lastY = y;
					return true;
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

			//			batch.begin();

			TextureRegion frame = mAnimation.getKeyFrame(currentFrameTime, true);
			batch.draw(frame, getX(), getY());

			//			batch.end();

			//			batch.draw(region, getX(), getY());

		}
	}

	@Override
	public void dispose() {
		mBatch.dispose();
		font.dispose();
		atlas.dispose();
		region.getTexture().dispose();
		stage.dispose();
	}

	@Override
	protected void render(ModelBatch batch, Array<ModelInstance> instances) {
		// clear the screen, update the camera and make the sprite batch
		// use its matrices.
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		camera.update();
		mBatch.setProjectionMatrix(camera.combined);

		// render all the things, we render in a y-down
		// cartesian coordinate system
		mBatch.begin();
		// drawing a region, x and y will be the top left corner of the region, would be bottom left
		// with y-up.
		mBatch.draw(region, 20, 100);
		// drawing text, x and y will be the top left corner for text, same as with y-up
		font.draw(mBatch, "This is a test", 270, 100);
		// drawing regions from an atlas, x and y will be the top left corner.
		// you shouldn't call findRegion every frame, cache the result.
		mBatch.draw(atlas.findRegion("badlogicsmall"), 360, 100);
		// drawing a sprite created from an atlas, FIXME wut?! AtlasSprite#setPosition seems to be wrong
		sprite.setColor(Color.RED);
		sprite.draw(mBatch);
		// finally we draw our current touch/mouse coordinates
		font.draw(mBatch, Gdx.input.getX() + ", " + Gdx.input.getY(), 0, 0);
		mBatch.end();

		currentFrameTime += Gdx.graphics.getDeltaTime();

		//				mSpriteBatch.begin();
		//				TextureRegion frame = mAni.getKeyFrame(currentFrameTime, true);
		//				mSpriteBatch.draw(frame, Gdx.input.getX(),Gdx.input.getY());
		//				mSpriteBatch.end();

		// tell the stage to act and draw itself
		stage.act(Gdx.graphics.getDeltaTime());
		stage.draw();

	}

	@Override
	protected void update(float delaTime) {
		// TODO Auto-generated method stub

	}
}
