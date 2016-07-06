package com.sany.tangyong.engineoriginal;

import android.content.Context;
import android.view.MotionEvent;

import com.go.gl.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.go.gl.badlogic.gdx.graphics.g2d.ParticleEmitter;
import com.go.gl.badlogic.gdx.graphics.g2d.Sprite;
import com.go.gl.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.go.gl.graphics.GLCanvas;
import com.go.gl.graphics.GLDrawable;
import com.go.gl.math3d.Math3D;
import com.go.gl.view.GLView;

/**
 * 
 * 精灵类和粒子系统的简单测试样例
 * 
 * @author  dengweiming
 * @date  [2013-7-4]
 */
public class SpriteTestView extends GLView {
	SpriteBatch mSpriteBatch;
	Sprite[] mSprites = new Sprite[200];
	ParticleEffect mParticleEffect;

	float mTouchDownX;
	float mTouchDownY;
	float mPositionX;
	float mPositionY;

	public SpriteTestView(Context context) {
		super(context);
		GLDrawable drawable = GLDrawable.getDrawable(getResources(), R.drawable.quick_time);
		mSpriteBatch = new SpriteBatch(2500);

		for (int i = 0; i < mSprites.length; ++i) {
			mSprites[i] = new Sprite(drawable.getTexture());
			float x = Math3D.random() * 100;
			float y = Math3D.random() * -100 - 300;
			mSprites[i].setBounds(x, y, Math3D.random() * 300 + 100, Math3D.random() * 300 + 100);
			mSprites[i].setColor(Math3D.random(), Math3D.random(), Math3D.random(), 1);
		}

		mParticleEffect = new ParticleEffect();
		mParticleEffect.load(getContext(), "fire.p");
		mParticleEffect.setPosition(0, 0);
		mParticleEffect.start();
	}

	@Override
	protected void onDraw(GLCanvas canvas) {
		mSpriteBatch.setMVPMatrix(canvas);
		mSpriteBatch.begin(canvas);

		for (int i = 0; i < mSprites.length; ++i) {
			mSprites[i].draw(mSpriteBatch);
		}

		canvas.translate(getWidth() * 0.5f, getHeight() * 0.5f);
		mSpriteBatch.setMVPMatrix(canvas);
		mParticleEffect.draw(mSpriteBatch, canvas.getDeltaDrawingTime() * GLCanvas.MILLISECONDS_TO_SECOND);
		invalidate();

		mSpriteBatch.end();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		float x = event.getX();
		float y = event.getY();
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN :
				mTouchDownX = x;
				mTouchDownY = y;
				ParticleEmitter emitter = mParticleEffect.getEmitters().get(0);
				mPositionX = emitter.getX();
				mPositionY = emitter.getY();
				break;

			case MotionEvent.ACTION_MOVE :
				mParticleEffect.setPosition(mPositionX + x - mTouchDownX, mPositionY - (y - mTouchDownY));
				break;
		}
		invalidate();
		return true;
	}

}
