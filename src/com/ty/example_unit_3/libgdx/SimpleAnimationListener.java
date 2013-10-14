package com.ty.example_unit_3.libgdx;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.materials.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.materials.IntAttribute;
import com.badlogic.gdx.graphics.g3d.materials.Material;
import com.badlogic.gdx.graphics.g3d.materials.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.ty.example_unit_3.libgdx.ex.Base3D;

/**
 * 
 * 
 * 说明:
 * 
 * SpriteBatch 在用来绘制2D平面上面的东西．有自己的着色器．
 * 
 * 
 * 
 * @author 师爷GBK[ty_sany@163.com] 
 * 
 */
public class SimpleAnimationListener extends Base3D {
	
	
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
		super.create();
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
	public void resize(int width, int height) {
		
		Gdx.input.setInputProcessor(inputController = new CameraInputController(mCamera));
		inputController = new CameraInputController(mCamera);
		Gdx.input.setInputProcessor(new InputMultiplexer(inputController, this, new GestureDetector(this)));
		
		super.resize(width, height);
	}

	@Override
	public boolean touchDown (int x, int y, int pointer, int button) {
		
		currentWalk = mAnimationList.get(++mIndex);
		if(mIndex>=3){
			mIndex = 0;
		}
		
		position.x = x;
		position.y = Gdx.graphics.getHeight() - y;
		mDownPositionX = x;
		return true;
	}
	
	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		mUpPositionX = screenX;
		if(mUpPositionX - mDownPositionX > 80){
		
		}
		return super.touchUp(screenX, screenY, pointer, button);
	}

	@Override
	public void dispose () {
		spriteBatch.dispose();
		texture.dispose();
	}


	@Override
	protected void update(float delaTime) {
		
	}
	
	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		return super.touchDragged(screenX, screenY, pointer);
	}


	@Override
	protected void render(ModelBatch batch, Array<ModelInstance> instances) {

		currentFrameTime += Gdx.graphics.getDeltaTime();

		spriteBatch.begin();
		TextureRegion frame = currentWalk.getKeyFrame(currentFrameTime, true);
		spriteBatch.draw(frame, position.x, position.y);
		spriteBatch.end();


	}

}
