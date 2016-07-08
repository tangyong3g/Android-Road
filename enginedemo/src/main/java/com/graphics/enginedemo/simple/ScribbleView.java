package com.graphics.enginedemo.simple;

import com.graphics.engine.graphics.BitmapGLDrawable;
import com.graphics.engine.graphics.GLCanvas;
import com.graphics.engine.scroller.ScreenScroller;
import com.graphics.engine.scroller.ScreenScrollerListener;
import com.graphics.engine.scroller.effector.subscreeneffector.SubScreenContainer;
import com.graphics.engine.scroller.effector.subscreeneffector.SubScreenEffector;
import com.graphics.engine.view.GLView;
import com.graphics.engine.view.GLViewGroup;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  dengweiming
 * @date  [2012-9-6]
 */
public class ScribbleView extends GLViewGroup implements ScreenScrollerListener, SubScreenContainer {
	private static final int TOUCH_STATE_RESET = 0;
	private static final int TOUCH_STATE_SCROLL = 1;
	private int mTouchState;
	private float mTouchX;
	private float mTouchY;

	private ScreenScroller mScreenScroller;
	private BitmapGLDrawable mDrawable;

	public ScribbleView(Context context) {
		super(context);
		mScreenScroller = new ScreenScroller(context, this);
		mScreenScroller.setEffector(new SubScreenEffector(mScreenScroller));
		mScreenScroller.setMaxOvershootPercent(10); //CHECKSTYLE IGNORE
		mScreenScroller.setDuration(450);	//CHECKSTYLE IGNORE
		ScreenScroller.setCycleMode(this, true);

//		WallpaperManager manager = WallpaperManager.getInstance(context);
//		Drawable d = manager.getDrawable();
//		if (d instanceof BitmapDrawable) {
//			mDrawable = new BitmapGLDrawable((BitmapDrawable) d);
//			mScreenScroller.setBackground(mDrawable);
//		}

	}

	public ScribbleView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ScribbleView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		mScreenScroller.setScreenSize(w, h);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		for (int i = 0; i < getChildCount(); ++i) {
			getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);
		}
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {

		final int action = ev.getAction() & MotionEvent.ACTION_MASK;

		final int slop = getTouchSlop();
		switch (action) {
			case MotionEvent.ACTION_DOWN :
				mTouchState = mScreenScroller.isFinished() ? TOUCH_STATE_RESET : TOUCH_STATE_SCROLL;
				mTouchX = ev.getX();
				mTouchY = ev.getY();
				break;
			case MotionEvent.ACTION_MOVE :
				if (mTouchState != TOUCH_STATE_SCROLL) {
					if (Math.abs(ev.getX() - mTouchX) > slop
							|| Math.abs(ev.getY() - mTouchY) > slop) {
						mTouchState = TOUCH_STATE_SCROLL;
						mScreenScroller.onTouchEvent(ev, MotionEvent.ACTION_DOWN);
					}
				}
				break;
			case MotionEvent.ACTION_UP :
			case MotionEvent.ACTION_CANCEL :
				mTouchState = TOUCH_STATE_RESET;
				break;
		}
		return mTouchState != TOUCH_STATE_RESET;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		final int action = event.getAction() & MotionEvent.ACTION_MASK;
		return mScreenScroller.onTouchEvent(event, action);
	}

	@Override
	public void computeScroll() {
		mScreenScroller.computeScrollOffset();
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		final int width = right - left;
		int l = 0, t = 0, r = width, b = bottom - top;
		for (int i = 0; i < getChildCount(); ++i) {
			getChildAt(i).layout(l, t, r, b);
			l += width;
			r += width;
		}
		mScreenScroller.setScreenCount(getChildCount());
	}

	@Override
	public ScreenScroller getScreenScroller() {
		return mScreenScroller;
	}

	@Override
	public void setScreenScroller(ScreenScroller scroller) {
		mScreenScroller = scroller;
	}

	@Override
	public void onFlingIntercepted() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onScrollStart() {

	}

	@Override
	public void onFlingStart() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onScrollChanged(int newScroll, int oldScroll) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onScreenChanged(int newScreen, int oldScreen) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onScrollFinish(int currentScreen) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void dispatchDraw(GLCanvas canvas) {
		mScreenScroller.onDraw(canvas);
	}

	@Override
	public void drawScreen(GLCanvas canvas, int screen) {
		GLView view = getChildAt(screen);
		if (view != null) {
			view.draw(canvas);
		}

	}

	@Override
	public void drawScreen(GLCanvas canvas, int screen, int alpha) {
		// TODO Auto-generated method stub

	}

}
