package com.sany.tangyong.engineoriginal;

import android.content.Context;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;

import com.go.gl.animation.InterpolatorFactory;
import com.go.gl.graphics.GLCanvas;
import com.go.gl.graphics.geometry.GLGrid;
import com.go.gl.graphics.geometry.TextureGLObjectRender;
import com.go.gl.view.GLView;

/**
 * 
 */
public class TwistGridTestView extends GLView {

	GLGrid mMesh;
	TwistMesh mTwistMesh;
	TextureGLObjectRender mRender = new TextureGLObjectRender();
	float mRatio = 1;

	public TwistGridTestView(Context context) {
		super(context);
		setBackgroundColor(0xff000000);

		mTwistMesh = new TwistMesh(32, 32, true);
		mMesh = mTwistMesh;
		mRatio = 0.75f;

		mRender.setTexture(getResources(), R.drawable.sunflower);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		int x = Math.min(w, h) * 3 / 4;
		int y = (int) (x / mRatio);
		mMesh.setBounds((w - x) / 2, (h - y) / 2, (w + x) / 2, (h + y) / 2);
		mMesh.setTexcoords(0, 0, 1, 1);
	}

	@Override
	protected void onDraw(GLCanvas canvas) {
		if (mTwistMesh != null) {
			mTwistMesh.update();
			invalidate();
		}

		canvas.setCullFaceEnabled(false);
		canvas.setDepthEnable(true);
		mRender.draw(canvas, mMesh);
	}

	float mLastX;
	float mLastY;

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		float x = event.getX();
		float y = event.getY();

		if (event.getAction() == MotionEvent.ACTION_UP) {
			if (mTwistMesh != null) {
				mTwistMesh.startAnimation();
				invalidate();
			}
		}

		mLastX = x;
		mLastY = y;
		return true;
	}

}

/**
 * 
 */
class TwistMesh extends GLGrid {
	float[] mPositionArray2;
	Interpolator mInterpolator;

	public TwistMesh(int xDiv, int yDiv, boolean fill) {
		super(xDiv, yDiv, fill);
		mPositionArray2 = new float[mPositionArray.length];
		mInterpolator = new OvershootInterpolator(3);
	}

	@Override
	protected void onBoundsChange(float left, float top, float right, float bottom) {
		super.onBoundsChange(left, top, right, bottom);
		System.arraycopy(mPositionArray, 0, mPositionArray2, 0, mPositionArray.length);
	}

	long mStartTime = -2;
	long mDuration = 1000;
	int mHalfCycle;

	public void update() {
		long curTime = System.currentTimeMillis();
		if (mStartTime == -1) {
			mStartTime = curTime;
		} else if (mStartTime == -2) {
			return;
		}
		final int divY = getDivY();
		float t = Math.min(1, (curTime - mStartTime) / (float) mDuration);

		float rad = (mHalfCycle + mInterpolator.getInterpolation(t)) * (float) Math.PI;
		final float minRad = mHalfCycle * (float) Math.PI;
		float t2 = InterpolatorFactory.quadraticEaseInOut(1, 0, t);
		final float deltaRad = (float) Math.toRadians(60) / divY * t2;
		for (int i = 0; i <= divY; ++i) {
			setRotate(i, rad);
			rad = Math.max(minRad, rad - deltaRad);
		}

		if (t == 1) {
			mHalfCycle = 1 - mHalfCycle;
			mStartTime = -2;
		}

	}

	public void startAnimation() {
		if (mStartTime == -2) {
			mStartTime = -1;
		}
	}

	void setRotate(int i, float rad) {
		RectF rect = getBounds();
		final float centerX = (rect.left + rect.right) * 0.5f;
		final int divX = getDivX();
		final float[] pos1 = mPositionArray;
		final float[] pos2 = mPositionArray2;
		final float sin = (float) Math.sin(rad);
		final float cos = (float) Math.cos(rad);

		int index = getPositionArrayStride() * i;
		for (int j = 0; j <= divX; ++j) {
			float x = pos2[index] - centerX;
			float z = pos2[index + 2];
			pos1[index] = cos * x + sin * z + centerX;
			pos1[index + 2] = -sin * x + cos * z;
			index += 3;
		}
	}

}
