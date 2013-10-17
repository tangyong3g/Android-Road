package com.ty.example_unit_3.libgdx;

import java.util.ArrayList;

import android.util.Log;
import android.view.animation.AnimationUtils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector2;
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

	private static final float ANIMATION_SPEED = 0.065f;
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

		rightWalk = new Animation(ANIMATION_SPEED, rightWalkReg);
		upWalk = new Animation(ANIMATION_SPEED, upWalkReg);
		
		
		Texture t_1 = new Texture(Gdx.files.internal("data/one.png"));
		Texture t_2 = new Texture(Gdx.files.internal("data/two.png"));
		Texture t_3 = new Texture(Gdx.files.internal("data/three.png"));
		Texture t_4 = new Texture(Gdx.files.internal("data/four.png"));
		
		TextureRegion t_1_  = new TextureRegion(t_1);
		TextureRegion t_2_  = new TextureRegion(t_2);
		TextureRegion t_3_  = new TextureRegion(t_3);
		TextureRegion t_4_  = new TextureRegion(t_4);
		
		leftWalkReg = new TextureRegion[]{t_1_,t_2_,t_3_,t_4_};
		
		leftWalk = new Animation(ANIMATION_SPEED, leftWalkReg);

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
		
//		currentWalk = mAnimationList.get(++mIndex);
		if(mIndex>=3){
			mIndex = 0;
		}
		
		return true;
	}
	
	
	private boolean mOnAnimation =  false;
	private long mDurtionTime = 2000;
	private long mStartTime = 0;
	private float mDownPositionX;
	private float mUpPositionX;
	private float mUpPositionY;
	
	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		mUpPositionX = screenX;
		mUpPositionY = screenY;
		mOnAnimation = true;
		mStartTime = AnimationUtils.currentAnimationTimeMillis();
		
		return super.touchUp(screenX, screenY, pointer, button);
	}
	
	
	private void onAnimation(){
		
		float start = mUpPositionX;
		float end = Gdx.graphics.getWidth();
		// CHECKSTYLE:ON

		// 从开始到现在的时间片
		long stepTime = AnimationUtils.currentAnimationTimeMillis() - mStartTime;
		// 占总时间的比重
		float t = stepTime * 1.0f / mDurtionTime;
		// 运动这完成
		t = Math.max(0, Math.min(t, 1));
		if (t == 1) {
			mOnAnimation = false;
		}
		// 现在的角度是开始角度加上现在动运的角度
		float xOffSet = start + (end - start) * t;
		float y = -((xOffSet- mUpPositionX) * (xOffSet - mUpPositionX) /( 2 * 50)- mUpPositionY);

		position.x = xOffSet;
		position.y = Gdx.graphics.getHeight() - y;
		
		Log.i("cycle","位置:\tx"+xOffSet+"\ty:"+position.y);
		
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

		Gdx.gl20.glClearColor(1, 1, 1, 1);
		currentFrameTime += Gdx.graphics.getDeltaTime();

		spriteBatch.begin();
		TextureRegion frame = currentWalk.getKeyFrame(currentFrameTime, true);
		spriteBatch.draw(frame, position.x, position.y);
		spriteBatch.end();

		if(mOnAnimation){
			onAnimation();
		}
	}

}

