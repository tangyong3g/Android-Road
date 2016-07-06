package com.graphics.engine.gl.scroller;

import android.content.Context;
import android.hardware.SensorManager;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;

import com.graphics.engine.gl.animation.InterpolatorFactory;
import com.graphics.engine.gl.graphics.GLCanvas;

/**
 * 连续滑屏的滚动器
 * 
 * @author dengweiming
 * 
 */
public class Scroller extends MScroller {

	ScrollerListener mListener;
	protected FastVelocityTracker mVelocityTracker;

	protected int mLastTouchP;
	protected int mTouchDownP;

	protected int mScreenWidth;
	protected int mScreenHeight;
	protected int mTotalWidth;
	protected int mTotalHeight;

	protected float mPaddingFactor = 0.5f; // CHECKSTYLE IGNORE THIS LINE
	protected int mPaddingBegin;
	protected int mPaddingEnd;
	protected int mInnerSize;
	protected int mScreenSize;
	protected int mTotalSize;

	protected int mOldScroll;
	// [mMinScrollOnFling, mMaxScrollOnFling) 包含于 [mMinScroll, mMaxScroll)
	protected int mMinScrollOnFling;
	protected int mMaxScrollOnFling;
	protected int mMinScroll;
	protected int mMaxScroll;
	protected int mLastScroll;

	private final static int FLING_STATE = 0; // 甩动匀减速状态
	private final static int SCROLL_TO_EDGE_STATE = 1; // 滚向两端的状态
	private final static int SCROLL_BACK_STATE = 2; // 从两端反弹回来的状态
	int mFlingState;
	protected double mScrollDurationRatio;

	private float mDensity;
	private float mVelocity;
	private float mDeceleration;
	private Interpolator mInterpolator;

	ScrollerEffector mEffector;

	/**
	 * 构造滚动器并绑定到一个视图上，注意还要另外调用 {@link #setSize}方法。
	 * 
	 * @param context
	 * @param listener
	 */
	public Scroller(Context context, ScrollerListener listener) {
		this(context, listener, null);
	}

	/**
	 * 构造滚动器并绑定到一个视图上，注意还要另外调用 {@link #setSize}方法。
	 * 
	 * @param context
	 * @param listener
	 * @param tracker
	 *            外部传入的触摸速度检测器，如果为null则内部建立一个
	 */
	public Scroller(Context context, ScrollerListener listener, FastVelocityTracker tracker) {
		super(context);
		assert listener != null; // 如果为null也就没任何意义了  
		mListener = listener;

		mDensity = context.getResources().getDisplayMetrics().density;
		float ppi = mDensity * 160.0f;  // CHECKSTYLE IGNORE THIS LINE
		mDeceleration = SensorManager.GRAVITY_EARTH // g (m/s^2)
				* 39.37f // inch/meter  // CHECKSTYLE IGNORE THIS LINE
				* ppi // pixels per inch
				* ViewConfiguration.getScrollFriction();
		mInterpolator = VISCOUS_FLUID_INTERPOLATOR;
		mVelocityTracker = tracker != null ? tracker : new FastVelocityTracker();
	}

	/**
	 * 设置视图在屏幕上的显示区域（视口）大小和实际大小
	 * 
	 * @param width
	 * @param height
	 * @param totalWidth
	 * @param totalHeight
	 */
	public void setSize(int width, int height, int totalWidth, int totalHeight) {
		abortAnimation();
		if (mScreenWidth == width && mScreenHeight == height && mTotalWidth == width
				&& mTotalHeight == totalHeight) {
			return;
		}
		mScreenWidth = width;
		mScreenHeight = height;
		mTotalWidth = totalWidth;
		mTotalHeight = totalHeight;
		updateSize();
	}

	/**
	 * 设置视图在屏幕上的显示区域（视口）里面的两端空白范围
	 * 
	 * @param paddingBegin
	 * @param paddingEnd
	 */
	public void setPadding(int paddingBegin, int paddingEnd) {
		abortAnimation();
		if (mPaddingBegin == paddingBegin && mPaddingEnd == paddingEnd) {
			return;
		}
		mPaddingBegin = paddingBegin;
		mPaddingEnd = paddingEnd;
		updateSize();
	}

	/**
	 * 设置整个视图两端可拉伸的多余部分相对于在屏幕上的显示区域（视口）的比例，默认值为0.5
	 * 
	 * @param paddingFactor
	 */
	public void setPaddingFactor(float paddingFactor) {
		abortAnimation();
		if (mPaddingFactor == paddingFactor) {
			return;
		}
		mPaddingFactor = paddingFactor;
		updateSize();
	}

	/**
	 * 设置滚动方向，默认为{@link #HORIZONTAL}
	 * 
	 * @param orientation
	 *            取值为{@link #HORIZONTAL}, {@link #VERTICAL}
	 */
	@Override
	public void setOrientation(int orientation) {
		abortAnimation();
		if (orientation == mOrientation) {
			return;
		}
		// 取消原来方向的偏移量
		mScroll = 0;
		if (orientation == HORIZONTAL) {
			mListener.scrollBy(0, -mListener.getScrollY());
		} else {
			mListener.scrollBy(-mListener.getScrollX(), 0);
		}
		mOrientation = orientation;
		updateSize();
	}

	private void updateSize() {
		if (mOrientation == HORIZONTAL) {
			mScreenSize = mScreenWidth;
			mTotalSize = mTotalWidth;
		} else {
			mScreenSize = mScreenHeight;
			mTotalSize = mTotalHeight;
		}
		mInnerSize = mScreenSize - mPaddingBegin - mPaddingEnd;
		mLastScroll = mTotalSize - mInnerSize;
		mMinScroll = -(int) (mInnerSize * mPaddingFactor);
		mMaxScroll = mLastScroll - mMinScroll;
		mMinScrollOnFling = -(int) (mInnerSize * .1f);  // CHECKSTYLE IGNORE THIS LINE
		mMaxScrollOnFling = mLastScroll - mMinScrollOnFling;
		mScrollDurationRatio = 20 * Math.log(mInnerSize);  // CHECKSTYLE IGNORE THIS LINE
		if (mEffector != null) {
			mEffector.onSizeChanged(mScreenWidth, mScreenHeight, mOrientation);
		}
	}

	/**
	 * 响应触摸事件
	 * 
	 * @param event
	 * @param action
	 *            在某些特殊情况下可以强制指定为某一值，但是默认应该为event.getAction()
	 * @return
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event, int action) {
		int p = mOrientation == HORIZONTAL ? (int) event.getX() : (int) event.getY();
		final int delta = mLastTouchP - p;
		mLastTouchP = p;
		switch (action) {
			case MotionEvent.ACTION_DOWN :
				int eventAction = event.getAction() & MotionEvent.ACTION_MASK;
				mCurrentTouchSlop = eventAction == action ? mTouchSlop : 0;
				mVelocityTracker.clear();
				mVelocityTracker.addMovement(event);
				mTouchDownP = mLastTouchP;
				if (mState != MScroller.FINISHED) {
					mState = MScroller.TO_SCROLL;
				}
				break;
			case MotionEvent.ACTION_MOVE :
				mVelocityTracker.addMovement(event);
				if (mState != MScroller.ON_SCROLL) {
					if (Math.abs(mLastTouchP - mTouchDownP) >= mCurrentTouchSlop) {
						mTouchDownP = mLastTouchP;
						onScrollStart();
					}
				} else if (mState == MScroller.ON_SCROLL) {
					onScroll(delta);
				}
				break;
			case MotionEvent.ACTION_UP :
			case MotionEvent.ACTION_CANCEL :
				if (!scrollBack(mScroll)) {
					mVelocityTracker.addMovement(event); 
					mVelocityTracker.computeCurrentVelocity(1000, mMaxFlingVelocity);  // CHECKSTYLE IGNORE THIS LINE
					final int velocity = mOrientation == HORIZONTAL ? (int) mVelocityTracker
							.getXVelocity() : (int) mVelocityTracker.getYVelocity();
					fling(mScroll, -velocity);
					mState = ON_FLING;
					mFlingState = FLING_STATE;
				}
				invalidate();
				break;
			default :
				return false;
		}
		return true;
	}

	@Override
	protected void onScroll(int delta) {
		if (mScroll < 0 || mScroll >= mLastScroll) {
			delta = (int) (delta * mPaddingFactor);
		}
		final int newScroll = Math.max(mMinScroll, Math.min(mScroll + delta, mMaxScroll));
		super.onScroll(newScroll - mScroll);
	}

	/**
	 * 
	 * @param newScroll
	 *            不检查是否越界
	 */
	@Override
	protected void scrollScreenGroup(int newScroll) {
		mOldScroll = mScroll;
		mScroll = newScroll;
		if (mScroll != mOldScroll) {
			if (mOrientation == HORIZONTAL) {
				mListener.scrollBy(mScroll - mOldScroll, 0);
			} else {
				mListener.scrollBy(0, mScroll - mOldScroll);
			}
			mListener.onScrollChanged(mScroll, mOldScroll);
		}
		super.scrollScreenGroup(newScroll);
	}

	@Override
	protected void onComputeFlingOffset(float t) {
		int scroll;
		switch (mFlingState) {
			case FLING_STATE :
				long curTime = AnimationUtils.currentAnimationTimeMillis();
				int timePassed = timePassed(curTime);
				t = timePassed * 0.001f;  // CHECKSTYLE IGNORE THIS LINE
				float distance = (mVelocity * t) - (mDeceleration * t * t * 0.5f);  // CHECKSTYLE IGNORE THIS LINE

				scroll = mStartScroll + Math.round(distance);
				// if(scroll < 0)
				// {
				// setScroll(0);
				// }
				// else if(scroll >= mLastScroll)
				// {
				// setScroll(mLastScroll);
				// }
				if (scroll < 0 || scroll >= mLastScroll) {
					scrollToEdge(scroll, (int) getCurrVelocity());
				} else {
					scrollScreenGroup(scroll);
				}
				break;
			case SCROLL_TO_EDGE_STATE :
				t = mInterpolator.getInterpolation(t);
				scroll = isFlingFinished() ? mEndScroll : mStartScroll
						+ Math.round(t * mDeltaScroll);
				scrollScreenGroup(scroll);
				if (t > 0.99f) {  // CHECKSTYLE IGNORE THIS LINE
					scrollBack(mScroll);
				}
				break;
			case SCROLL_BACK_STATE :
				t = mInterpolator.getInterpolation(t);
				scroll = isFlingFinished() ? mEndScroll : mStartScroll
						+ Math.round(t * mDeltaScroll);
				scrollScreenGroup(scroll);
				break;
		}
	}

	/**
	 * 直接设置当前滚动量
	 * 
	 * @param scroll
	 */
	public void setScroll(int scroll) {
		scroll = Math.max(mMinScroll, Math.min(scroll, mMaxScroll));
		mState = FINISHED;
		scrollScreenGroup(scroll);
	}

	private float getCurrVelocity() {
		long time = AnimationUtils.currentAnimationTimeMillis();
		return mVelocity - mDeceleration * timePassed(time) * 0.001f;  // CHECKSTYLE IGNORE THIS LINE
	}

	private void fling(int scroll, int velocity) {
		mVelocity = velocity;
		if (mVelocity > 0 ^ mDeceleration > 0) {
			mDeceleration = -mDeceleration; // mDeceleration保持和mVelocity同号
		}
		int duration = (int) (1000 * velocity / mDeceleration);  // CHECKSTYLE IGNORE THIS LINE
		int change = (int) ((velocity * velocity) / (2 * mDeceleration));
		onFling(scroll, change, duration);
	}

	private boolean scrollToEdge(int scroll, int velocity) {
		final int tm = 6931; 
		/*
		 * 令f(x)为插值器函数，则实际运动方程为F(t)=b+f(t/d) * c， （b<=>begin, t<=>time,
		 * d<=>duration, c<=>change<=>end-begin）， 则速度即一阶导数F'(t)=c/d*f'(t/d)，
		 * 而加速度即二阶导数F''(t)=c/d^2*f''(t/d)。 令当前速度v=F'(0)，加速度a=F''(0)，可解得 d=v/a *
		 * f''(0) / f'(0)， 因为使用的插值器函数为f(x)=1-2^(-10x)，解得 f'(x)=10 * 2^(-10x) *
		 * ln2，f'(0)=10*ln2=6.931 另f''(x)=-10 * 10 * 2^(-10x) * ln2,
		 * f''(x)/f'(x)=10*ln2=6.931
		 */
		if (velocity < 0) {
			int duration = Math.abs((int) (velocity * tm / mDeceleration));
			duration = (int) (Math.min(duration, 450) * .5);  // CHECKSTYLE IGNORE THIS LINE
			int change = (int) (velocity * duration / tm);  
			change = Math.max(change, mMinScrollOnFling - scroll);
			onFling(scroll, change, duration);
		} else if (velocity > 0) {
			int duration = Math.abs((int) (velocity * tm / mDeceleration));
			duration = (int) (Math.min(duration, 450) * .5);  // CHECKSTYLE IGNORE THIS LINE
			int change = (int) (velocity * duration / tm);
			change = Math.min(change, mMaxScrollOnFling - scroll);
			onFling(scroll, change, duration);
		}
		mInterpolator = InterpolatorFactory.getInterpolator(InterpolatorFactory.EXPONENTIAL, 0);
		mFlingState = SCROLL_TO_EDGE_STATE;
		mState = ON_FLING;
		invalidate();
		return true;
	}

	private boolean scrollBack(int scroll) {
		if (scroll < 0) {
			final int duration = (int) (mScrollDurationRatio * Math.log(-scroll));
			onFling(scroll, -scroll, duration);
		} else {
			final int dy = mLastScroll - scroll;
			if (dy < 0) {
				final int duration = (int) (mScrollDurationRatio * Math.log(-dy));
				onFling(scroll, dy, duration);
			} else {
				return false;
			}
		}
		mInterpolator = VISCOUS_FLUID_INTERPOLATOR;
		mFlingState = SCROLL_BACK_STATE;
		mState = ON_FLING;
		invalidate();
		return true;
	}

	final public int getTotalWidth() {
		return mTotalWidth;
	}

	final public int getTotalHeight() {
		return mTotalSize;
	}

	/**
	 * 获取使用的屏幕密度
	 * 
	 * @return
	 */
	final public float getDensity() {
		return mDensity;
	}

	@Override
	protected void onFling(int begin, int change, int duration) {
		super.onFling(begin, change, duration);
		mDepthUpdateTime += duration;
	}

	/**
	 * 设置动画效果
	 * 
	 * @param effector
	 *            为null表示不使用效果
	 */
	public void setEffector(ScrollerEffector effector) {
		ScrollerEffector oldEffector = mEffector;
		mEffector = effector;
		if (oldEffector != mEffector && oldEffector != null) {
			oldEffector.onDetach();
		}
		if (mEffector != null) {
			mEffector.onAttach(this, mListener);
		}
	}

	public final ScrollerEffector getEffector() {
		return mEffector;
	}

	@Override
	public boolean onDraw(GLCanvas canvas) {
		invalidateScroll();
		return mEffector != null && mEffector.onDraw(canvas);
	}

	@Override
	protected void invalidate() {
		mListener.invalidate();
	}
}
