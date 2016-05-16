package com.sny.tangyong.shellengine.animation;

import android.view.MotionEvent;

/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 */
public class Smoother {
	/**
	 * 
	 * <br>类描述:
	 * <br>功能详细描述:
	 * 
	 * @author  dengweiming
	 * @date  [2012-9-7]
	 */
	public interface SmoothListener {
		public void invalidate();
		public void onMotion(int x, int y);
	}
	
    private static final float SMOOTHING_SPEED = 0.75f;
	private static final float SMOOTHING_CONSTANT = (float) (0.016 / Math.log(SMOOTHING_SPEED));	// CHECKSTYLE IGNORE
    private static final float ONE_OVER_SMOOTHING_CONSTANT = 1 / SMOOTHING_CONSTANT;
    protected static final float NANOTIME_DIV = 1000000000.0f;
    protected static final float ONE_OVER_NANOTIME_DIV = 1 / NANOTIME_DIV;
    
	private float mSmoothingTime;
	private boolean mFirstSmooth;

	private int mTouchX;
	private int mTouchY;
	
	private int mX;
	private int mY;
	private int mDstX;
	private int mDstY;
	
	SmoothListener mListener;
	
	public Smoother(SmoothListener listener) {
		assert listener != null;
		listener = mListener;
	}
	
	public void onMotion(int x, int y, int action) {
		switch (action) {
			case MotionEvent.ACTION_DOWN :
				mTouchX = x;
				mTouchY = y;
				break;
			case MotionEvent.ACTION_MOVE :
				mDstX += x - mTouchX;
				mDstY += y - mTouchY;
				mTouchX = x;
				mTouchY = y;
				mFirstSmooth = true;
				mSmoothingTime = System.nanoTime() * ONE_OVER_NANOTIME_DIV;
				invalidate();
				break;
			case MotionEvent.ACTION_UP :
			case MotionEvent.ACTION_CANCEL :
				break;
		}
	}
	
	public boolean smooth() {
		final int dx = mDstX - mX, dy = mDstY - mY;
		if (dx > 1 || dx < -1 || dy > 1 | dy < -1) {
			final float now = System.nanoTime() * ONE_OVER_NANOTIME_DIV;
			float e = (float) Math.exp((now - mSmoothingTime) * ONE_OVER_SMOOTHING_CONSTANT);
			if (mFirstSmooth) {
				mFirstSmooth = false;
				e *= 0.5f;	// CHECKSTYLE IGNORE
			}
			mX = Math.round(mX + dx * e);
			mY = Math.round(mY + dy * e);
			mSmoothingTime = now;
			mListener.onMotion(mX, mY);
			return true;
		}
		return false;
	}
	
	public int getCurX() {
		return mX;
	}
	
	public int getCurY() {
		return mY;
	}
	
	public int getDstX() {
		return mDstX;
	}
	
	public int getDstY() {
		return mDstY;
	}
	
	public void invalidate() {
		mListener.invalidate();
	}
	
	public void reset() {
		mX = mY = mDstX = mDstY = 0;
		mListener.onMotion(mX, mY);
	}
	
}
