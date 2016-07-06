package com.graphics.engine.gl.scroller;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;

import com.graphics.engine.gl.graphics.BitmapGLDrawable;
import com.graphics.engine.gl.graphics.GLCanvas;
import com.graphics.engine.gl.graphics.GLDrawable;

/**
 * 分屏视图的滚动器。 
 * <br>默认滚动到两端会阻塞住，调用{@link #setCycleMode(ScreenScrollerListener, boolean)}
 * 可以循环，或者使用{@link CycloidScreenScroller}。
 * <br>视图要使用滚动器，需要实现{@link ScreenScrollerListener}接口。
 * <br>要使用整屏切屏特效，视图需要实现{@link SubScreenContainer}接口；
 * 要使用分格切屏特效，视图需要实现{@link GridScreenContainer}接口。
 * 之后使用{@link #setEffector(ScreenScrollerEffector)}设置特效，具体的特效实现
 * 在这两种接口所在的包内，其中有两个工厂类：{@link SubScreenEffector}以及{@link GridScreenEffector}。
 * 
 * <br><br>使用样例：（基本上每个方法都是override的，为了避免文档化格式错误就删掉了，还有几个必须实现的方法因为可以为空就省略了）
 * <pre><code>
 * public class ScribbleView extends GLViewGroup implements ScreenScrollerListener, SubScreenContainer {
 * 	private static final int TOUCH_STATE_RESET = 0;
 * 	private static final int TOUCH_STATE_SCROLL = 1;
 * 	private int mTouchState;
 * 	private float mTouchX;
 * 	private float mTouchY;
 * 
 * 	private ScreenScroller mScreenScroller;
 * 	private BitmapGLDrawable mDrawable;
 * 
 * 	public ScribbleView(Context context) {
 * 		super(context);
 * 		mScreenScroller = new ScreenScroller(context, this);
 * 		mScreenScroller.setEffector(new SubScreenEffector(mScreenScroller));
 * 		//mScreenScroller.setPadding(0.5f);
 * 		//mScreenScroller.setMaxOvershootPercent(0);
 * 		//mScreenScroller.setInterpolator(null);
 * 		mScreenScroller.setDuration(450);
 * 		ScreenScroller.setCycleMode(this, true);
 * 	}
 * 
 * 	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
 * 		mScreenScroller.setScreenSize(w, h);
 * 	}
 * 
 * 	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
 * 		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
 * 		for (int i = 0; i < getChildCount(); ++i) {
 * 			getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);
 * 		}
 * 	}
 * 
 * 	public boolean onInterceptTouchEvent(MotionEvent ev) {
 * 		final int action = ev.getAction() & MotionEvent.ACTION_MASK;
 * 		final int slop = getTouchSlop();
 * 		switch (action) {
 * 			case MotionEvent.ACTION_DOWN :
 * 				mTouchState = mScreenScroller.isFinished() ? TOUCH_STATE_RESET : TOUCH_STATE_SCROLL;
 * 				mTouchX = ev.getX();
 * 				mTouchY = ev.getY();
 * 				break;
 * 			case MotionEvent.ACTION_MOVE :
 * 				if (mTouchState != TOUCH_STATE_SCROLL) {
 * 					if (Math.abs(ev.getX() - mTouchX) > slop
 * 						|| Math.abs(ev.getY() - mTouchY) > slop) {
 * 						mTouchState = TOUCH_STATE_SCROLL;
 * 						mScreenScroller.onTouchEvent(ev, MotionEvent.ACTION_DOWN);
 * 					}
 * 				}
 * 				break;
 * 			case MotionEvent.ACTION_UP :
 * 			case MotionEvent.ACTION_CANCEL :
 * 				mTouchState = TOUCH_STATE_RESET;
 * 				break;
 * 		}
 * 		return mTouchState != TOUCH_STATE_RESET;
 * 	}
 * 	
 * 	public boolean onTouchEvent(MotionEvent event) {
 * 		final int action = event.getAction() & MotionEvent.ACTION_MASK;
 * 		return mScreenScroller.onTouchEvent(event, action);
 * 	}
 * 
 * 	public void computeScroll() {
 * 		mScreenScroller.computeScrollOffset();
 * 	}
 * 
 * 	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
 * 		final int width = right - left;
 * 		int l = 0, t = 0, r = width, b = bottom - top;
 * 		for (int i = 0; i < getChildCount(); ++i) {
 * 			getChildAt(i).layout(l, t, r, b);
 * 			l += width;
 * 			r += width;
 * 		}
 * 		mScreenScroller.setScreenCount(getChildCount());
 * 	}
 * 
 * 	public ScreenScroller getScreenScroller() {
 * 		return mScreenScroller;
 * 	}
 * 
 * 	public void setScreenScroller(ScreenScroller scroller) {
 * 		mScreenScroller = scroller;
 * 	}
 * 
 * 	protected void dispatchDraw(GLCanvas canvas) {
 * 		mScreenScroller.onDraw(canvas);	//如果不调用这句，就不支持循环滚动以及切屏特效，
 * 										//并且一定要调用mScreenScroller.invalidateScroll();
 * 	}
 * 
 * 	public void drawScreen(GLCanvas canvas, int screen) {
 * 		GLView view = getChildAt(screen);
 * 		if (view != null) {
 * 			view.draw(canvas);
 * 		}
 * 	}
 *}
 * </code></pre>
 * 
 * @author dengweiming
 */
public class ScreenScroller extends MScroller {

	/** 无效索引值 */
	public static final int INVALID_SCREEN = -1;

	protected FastVelocityTracker mVelocityTracker;
	protected int mFlingVelocity;
	protected int mFlingVelocityX;
	protected int mFlingVelocityY;

	protected ScreenScrollerListener mListener;
	protected Interpolator mInterpolator;
	protected Interpolator mInterpolatorBak;

	protected ScreenScrollerEffector mEffector;

	protected int mMinScroll;
	protected int mMaxScroll;
	protected int mLastScreenPos; // 最后一屏的位置
	protected int mOldScroll;
	protected int mScrollRange;
	protected int mTotalSize;
	protected float mScrollRatio;
	protected float mTotalSizeInv;
	protected float mScreenCountInv;
	protected float mPaddingFactor = 0.5f; // CHECKSTYLE IGNORE THIS LINE

	protected int mScreenCount = 1; // 为了防止除0，初始化为1
	protected int mScreenWidth;
	protected int mScreenHeight;
	protected int mScreenSize = 1; // 为了防止除0，初始化为1
	protected int mCurrentScreen;
	protected int mDstScreen;
	protected boolean mIsOvershooting;
	protected float mFloatIndex;

	protected int mTouchDownScreen;
	protected int mTouchDownP;
	protected int mTouchDownX;
	protected int mTouchDownY;
	protected int mTouchDownScrollP;
	protected int mLastTouchP;
	protected int mLastTouchX;
	protected int mLastTouchY;

	protected int mScrollingDuration = 1000; // CHECKSTYLE IGNORE THIS LINE // 切屏最大时间限制  
	protected int mDecelerateDuration = 500; // CHECKSTYLE IGNORE THIS LINE

	protected PorterDuffColorFilter mColorFilter;
	protected int mBackgroundWidth;
	protected int mBackgroundHeight;
	protected int mScreenOffsetY;
	protected int mScreenPaddingBottom;
	protected int mBackgroundOffsetY;
	boolean mBackgroundScrollEnabled = true;
	boolean mUseEffectorMaxOvershootPercent = true;
	protected boolean mCycloid;
	protected boolean mBgAlwaysDrawn; // 是否屏蔽绘制背景
	protected Drawable mBackgroundDrawable;
	protected Bitmap mBitmap;
	protected Paint mPaint;

	protected int mMaxOverShootPercent = 49; // CHECKSTYLE IGNORE THIS LINE
	protected int mOverShootPercent;

	private float mLayoutScale = 1.0f;

	protected boolean mGoShortPath = true; // 因为是循环滚动的，因此往两个方向都可以到达目标位置，启用这个选项可以选择较近的方向

	protected boolean mIsEffectorEnded = false;
	
	private boolean mEnableInteruptFlip = true;
	/**
	 * 构造滚动器并绑定到一个分屏视图上，注意还要另外调用 {@link #setScreenSize}, {@link #setScreenCount}
	 * 方法。
	 * 
	 * @param context
	 *            可以为null，此时使用默认的touch slop
	 * @param screenGroup
	 */
	public ScreenScroller(Context context, ScreenScrollerListener screenGroup) {
		this(context, screenGroup, null);
	}

	/**
	 * 构造滚动器并绑定到一个分屏视图上，注意还要另外调用 {@link #setScreenSize}, {@link #setScreenCount}
	 * 方法。
	 * 
	 * @param context
	 *            可以为null，此时使用默认的touch slop
	 * @param screenGroup
	 * @param tracker
	 *            外部传入的触摸速度检测器，如果为null则内部建立一个
	 */
	public ScreenScroller(Context context, ScreenScrollerListener screenGroup,
						  FastVelocityTracker tracker) {
		super(context);
		assert screenGroup != null; // 如果为null也就没任何意义了
		mInterpolatorBak = MScroller.DEFAULT_INTERPOLATOR;
		mInterpolator = mInterpolatorBak;
		mListener = screenGroup;
		mVelocityTracker = tracker != null ? tracker : new FastVelocityTracker();
	}

	/**
	 * 设置屏幕大小
	 * 
	 * @param width
	 * @param height
	 */
	public void setScreenSize(int width, int height) {
		abortAnimation();
		if (mScreenWidth == width && mScreenHeight == height) {
			return;
		}
		if (width <= 0 || height <= 0) {
			return;
		}
		mScreenWidth = width;
		mScreenHeight = height;
		setBackgroundOffsetY();
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

	public void updateSize() {
		// 取消当前方向的偏移量
		mScroll = 0;
		if (mOrientation == HORIZONTAL) {
			mScreenSize = mScreenWidth;
			mListener.scrollBy(-mListener.getScrollX(), 0);
		} else {
			mScreenSize = mScreenHeight;
			mListener.scrollBy(0, -mListener.getScrollY());
		}
		if (mEffector != null) {
			mEffector.onSizeChanged(mScreenWidth, mScreenHeight, mOrientation);
		}
		final int oldCount = mScreenCount;
		mScreenCount = -1; // 为了使updateScreenGroupChildCount中重新计算
		setScreenCount(oldCount);
	}

	/**
	 * 设置屏幕数量，删除或者增加新的子视图后调用
	 * 
	 * @param count
	 */
	public void setScreenCount(int count) {
		// 在abortAnimation前面先作这个判断避免无谓的onLayout引起动画中断
		if (mScreenCount == count) {
			return;
		}
		if (count <= 0) {
			return;
		}
		mScreenCount = count;
		abortAnimation();
		mScreenCountInv = mScreenCount > 0 ? 1.0f / mScreenCount : 0;
		mLastScreenPos = mScreenSize * (mScreenCount - 1);
		mTotalSize = mScreenSize * mScreenCount;
		mTotalSizeInv = mTotalSize > 0 ? 1.0f / mTotalSize : 0;
		// mDstScreen = Math.max(0, Math.min(mDstScreen, mScreenCount - 1));
		float oldPaddingFactor = mPaddingFactor;
		mPaddingFactor = -1;
		setPadding(oldPaddingFactor);
	}

	/**
	 * 设置视图容器两端拖动最大超出距离相对于屏幕的比例，初始化时是0.5
	 * 
	 * @param paddingFactor
	 *            限制范围在[0, 0.5]
	 */
	public void setPadding(float paddingFactor) {
		abortAnimation();
		if (mPaddingFactor == paddingFactor) {
			return;
		}
		mPaddingFactor = Math.max(0, Math.min(paddingFactor, 0.5f)); // CHECKSTYLE IGNORE THIS LINE
		// 限制最小值和最大值，防止computeScreenIndex()的结果越界
		mMinScroll = Math.max(-(int) (mScreenSize * paddingFactor), -mScreenSize / 2);
		mMaxScroll = Math.min(mLastScreenPos + (int) (mScreenSize * paddingFactor), mLastScreenPos
				+ mScreenSize / 2 - 1);
		mMaxScroll = Math.max(mMinScroll, mMaxScroll);

		mScrollRatio = mMaxScroll > mMinScroll ? 1.0f / (mMaxScroll - mMinScroll) : 0;

		// 重设当前滚动量
		scrollScreenGroup(getDstScreen() * mScreenSize);
		mScrollFloat = mScroll;;
	}

	/**
	 * 卷动视图容器
	 * 
	 * @param newScroll
	 *            不检查是否越界
	 */
	@Override
	protected void scrollScreenGroup(int newScroll) {
		mFloatIndex = newScroll / (float) mScreenSize;
		mOldScroll = mScroll;
		mScroll = newScroll;
		if (mScroll != mOldScroll) {
			if (mOrientation == HORIZONTAL) {
				mListener.scrollBy(mScroll - mOldScroll, 0);
			} else {
				mListener.scrollBy(0, mScroll - mOldScroll);
			}
			mListener.onScrollChanged(mScroll, mOldScroll);
			final int oldScreen = mCurrentScreen;
			mCurrentScreen = computeScreenIndex(mScroll);
			if (mCurrentScreen != oldScreen) {
				mListener.onScreenChanged(mCurrentScreen, oldScreen);
			}
		} else {
			super.scrollScreenGroup(newScroll);
		}
	}


	/**
	 * 设置插值器
	 * 
	 * @param interpolator
	 */
	public void setInterpolator(Interpolator interpolator) {
		if (interpolator == null) {
			interpolator = MScroller.DEFAULT_INTERPOLATOR;
		}
		mInterpolator = interpolator;
		mInterpolatorBak = mInterpolator;
	}

	/**
	 * 设置切换一屏需要的最长时间
	 * 
	 * @param duration
	 *            单位为毫秒，默认为1000
	 */
	public void setDuration(int duration) {
		duration = Math.max(1, duration);
		mScrollingDuration = duration;
	}

	/**
	 * 设置动画效果
	 * 
	 * @param effector
	 *            为null表示不使用效果
	 */
	public void setEffector(ScreenScrollerEffector effector) {
		ScreenScrollerEffector oldEffector = mEffector;
		mEffector = effector;
		if (oldEffector != mEffector && oldEffector != null) {
			oldEffector.onDetach();
		}
		if (mEffector != null) {
			mEffector.onAttach(mListener);
		}
	}

	/**
	 * 设置当前屏幕位置，不会产生动画。 调用者必须先检查dstScreen是否越界，或者先调用{@link #setScreenCount}使其不越界。
	 * 
	 * @param dstScreen
	 */
	public void setCurrentScreen(int dstScreen) {
		// if(isFinished()){
		// return;
		// }
		abortAnimation();
		mDstScreen = dstScreen; // 不作任何限制，因为监听者可能在调用本方法时还没有添加所有子屏
		if (mDstScreen == 0 && mScroll == 0) {
			final int oldScreen = mCurrentScreen;
			mCurrentScreen = 0;
			if (mCurrentScreen != oldScreen) {
				mListener.onScreenChanged(mCurrentScreen, oldScreen);
			}
		} else {
			scrollScreenGroup(mDstScreen * mScreenSize);
			mScrollFloat = mDstScreen * mScreenSize;
		}
	}

	/**
	 * 直接设置偏移量，同样也可能会引起监听者的onScrollChanged和onScreenChanged的回调 注意在连续调用完本方法后，要调用
	 * {@link #setCurrentScreen(int)} 或者{@link #gotoScreen(int, int, boolean)}
	 * 方法来修正位置（以及还原到非滚动状态）
	 * 
	 * @param percent
	 *            0对应第一屏，100对应最后一屏
	 */
	public void setScrollPercent(float percent) {
		if (mState != ON_SCROLL) {
			onScrollStart();
		}
		onScroll((int) (percent * mLastScreenPos * 0.01f) - mEndScroll);  // CHECKSTYLE IGNORE THIS LINE
	}
	
	public void setScroll(int scroll) {
		if (mState != ON_SCROLL) {
			onScrollStart();
		}
		onScroll(scroll - mEndScroll);
	}
	
	public void setScroll(float scroll) {
		if (mState != ON_SCROLL) {
			onScrollStart();
		}
		onScroll(scroll - mEndScroll);
	}
	
	/**
	 * 直接设置偏移量，同样也可能会引起监听者的onScrollChanged和onScreenChanged的回调 注意在连续调用完本方法后，要调用
	 * {@link #setCurrentScreen(int)} 或者{@link #gotoScreen(int, int, boolean)}
	 * 方法来修正位置（以及还原到非滚动状态）
	 * 
	 * @param index
	 *            屏幕的索引，支持浮点数值
	 */
	public void setScrollIndex(float index) {
		if (mState != ON_SCROLL) {
			onScrollStart();
		}
		index = Math.max(-mPaddingFactor, Math.min(index, mScreenCount - 1 + mPaddingFactor));
		onScroll((int) (index * mScreenSize) - mEndScroll);
	}

	/**
	 * 从当前位置滚动到指定的屏幕位置，会产生动画
	 * 
	 * @param dstScreen
	 * @param duration
	 */
	protected boolean flingToScreen(int dstScreen, int duration) {
		// 在两端采用默认插值器；在中间采用指定的插值器
		Interpolator interpolator = mInterpolatorBak;
		if (mScroll < 0 && dstScreen <= 0) {
			dstScreen = 0;
			duration = mDecelerateDuration;
			interpolator = MScroller.VISCOUS_FLUID_INTERPOLATOR;
		} else if (mScroll >= mLastScreenPos && dstScreen >= mScreenCount - 1) {
			dstScreen = mScreenCount - 1;
			duration = mDecelerateDuration;
			interpolator = MScroller.VISCOUS_FLUID_INTERPOLATOR;
		}
		return gotoScreen(dstScreen, duration, interpolator);
	}

	/**
	 * 检查索引范围
	 * 
	 * @param screen
	 * @return
	 */
	protected int checkScreen(int screen) {
		return Math.max(0, Math.min(screen, mScreenCount - 1));
	}

	/**
	 * 从当前位置使用指定的插值器滚动到指定的屏幕位置，会产生动画
	 * 
	 * @param dstScreen
	 * @param duration
	 *            切换的时间
	 * @param interpolator
	 *            如果为null，使用默认的插值器
	 */
	protected boolean gotoScreen(int dstScreen, int duration, Interpolator interpolator) {
		mInterpolator = interpolator != null ? interpolator : MScroller.DEFAULT_INTERPOLATOR;
		mDstScreen = checkScreen(dstScreen);
		final int delta = mDstScreen * mScreenSize - mScroll;
		if (delta == 0 && getCurrentDepth() == 0) {
			final int oldState = mState;
			if (mState != MScroller.FINISHED) {
				mState = MScroller.FINISHED;

				if (mEffector != null) {
					mEffector.onScrollEnd();
				}
				mListener.onScrollFinish(getDstScreen());
				clearTouchState();
			}
			// 如果没有开始滚动就收到 ACTION_UP 事件，那么返回 false 表示不处理该事件
			return oldState != MScroller.FINISHED;
		}


		if (mEffector != null) {
			mEffector.onScrollStart();
		}
		mListener.onScrollStart();
		if (mFlingVelocity != 0 && mInterpolator != VISCOUS_FLUID_INTERPOLATOR) {
			// 计算一个合理的时间，但是限制最大值，不能太慢
			duration = Math.min(duration, computeFlingDuration(delta, mFlingVelocity));
		}
		onFling(mScroll, delta, duration);
		if (mEffector != null) {
			mEffector.onFlipStart();
		}
		mListener.onFlingStart();
		mFlingVelocity = 0;
		return true;
	}

	/**
	 * 从当前屏幕使用当前的插值器滚动到指定的屏幕位置，会产生动画。 由外部直接调用（如响应Home键时）
	 * 
	 * @param duration
	 *            小于0则自动计算时间
	 * 
	 */
	public boolean gotoScreen(int dstScreen, int duration, boolean noElastic) {
		// TODO: 使用一个合理的初速度来求时间
		return gotoScreen(dstScreen, duration < 0 ? mScrollingDuration : duration, noElastic
				? MScroller.DEFAULT_INTERPOLATOR
				: mInterpolatorBak);
	}

	public final Interpolator getInterpolator() {
		return mInterpolatorBak;
	}

	public final ScreenScrollerEffector getEffector() {
		return mEffector;
	}

	public final int getScreenWidth() {
		return mScreenWidth;
	}

	public final int getScreenHeight() {
		return mScreenHeight;
	}

	public final int getScreenSize() {
		return mScreenSize;
	}

	public final int getScreenCount() {
		return mScreenCount;
	}
	
	public final int getScreenOffsetY() {
		return mScreenOffsetY;
	}

	public final float getScrollRatio() {
		return mScrollRatio;
	}
	
	public final int getMinScroll() {
		return mMinScroll;
	}
	
	public final int getMaxScroll() {
		return mMaxScroll;
	}
	
	/**
	 * 获取当前屏（显示面积较大的那屏）的索引
	 * 
	 * @return
	 */
	public final int getCurrentScreen() {
		return mCurrentScreen;
	}

	/**
	 * 获取最终停留的屏幕索引
	 * 
	 * @return
	 */
	public int getDstScreen() {
		return mDstScreen;
	}

	/**
	 * 获取当前屏（显示面积较大的那屏）的偏移量
	 * 
	 * @return
	 */
	public final int getCurrentScreenOffset() {
		return mCurrentScreen * mScreenSize - mScroll;
	}
	
	public final float getCurrentScreenOffsetFloat() {
		return mCurrentScreen * mScreenSize - mScrollFloat;
	}

	/**
	 * 获取浮点的索引值， 在循环的情况下从最后一屏切换到最前一屏，变化为从 mScreenCount - 1 到 mScreenCount，
	 * 反之其变化为从 0 到 -1
	 * 
	 * @return
	 */
	public final float getFloatIndex() {
		return mFloatIndex;
	}

	/**
	 * 获取甩动时的速度
	 * 
	 * @return
	 */
	public final float getFlingVelocity() {
		return mFlingVelocity;
	}

	/**
	 * 计算当前屏的索引
	 * 
	 * @param scroll
	 * @return
	 */
	protected int computeScreenIndex(int scroll) {
		int currentScreen = ((scroll + mScreenSize / 2) / mScreenSize + mScreenCount)
				% mScreenCount;
		while (currentScreen < 0) {
			currentScreen = (currentScreen + mScreenCount) % mScreenCount;
		}
		return currentScreen;
	}

	/**
	 * 是否循环滚屏
	 * 
	 * @return
	 */
	public boolean isCircular() {
		return mCycloid;
	}

	/**
	 * 响应触摸事件
	 * 
	 * @param event
	 * @param action
	 *            在某些特殊情况下可以强制指定为某一值，但是默认应该为event.getAction()
	 * @return 如果还没发生实质的滚动就 action_up/action_cancle 了，则返回 false，有必要就处理这种情况
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event, int action) {
		mLastTouchX = (int) event.getX();
		mLastTouchY = (int) event.getY();
		final int p = mOrientation == HORIZONTAL ? mLastTouchX : mLastTouchY;
		int delta = mLastTouchP - p;
		mLastTouchP = p;

		switch (action) {
			case MotionEvent.ACTION_DOWN : {
				int eventAction = event.getAction() & MotionEvent.ACTION_MASK;
				mCurrentTouchSlop = eventAction == action ? mTouchSlop : 0;
				mVelocityTracker.addMovement(event);
				mTouchDownP = mLastTouchP;
				mTouchDownX = mLastTouchX;
				mTouchDownY = mLastTouchY;
				mTouchDownScrollP = mScroll;
				mTouchDownScreen = mCurrentScreen;
				if (mEnableInteruptFlip && mState == MScroller.ON_FLING) {
					mState = MScroller.TO_SCROLL;
					if (mEffector != null) {
						mEffector.onFlipInterupted();
					}
					mListener.onFlingIntercepted();
				}
			}
				break;
			case MotionEvent.ACTION_MOVE : {
				mVelocityTracker.addMovement(event);
				// mVelocityTracker.computeCurrentVelocity(1000);
				// mFlingVelocity = (int)(mOrientation == HORIZONTAL
				// ? mVelocityTracker.getXVelocity()
				// : mVelocityTracker.getYVelocity());
				// if(Math.abs(mFlingVelocity) < FLING_VELOCITY){
				// mListener.onFlingIntercepted();
				// }
				if (!mEnableInteruptFlip && mState == MScroller.ON_FLING) {
					if (Math.abs(mLastTouchX - mTouchDownX) > mTouchSlop) {
						mState = MScroller.TO_SCROLL;
						if (mEffector != null) {
							mEffector.onFlipInterupted();
						}
						mListener.onFlingIntercepted();
					}
				} else {
					if (mState != MScroller.ON_SCROLL) {
						if (Math.abs(mLastTouchP - mTouchDownP) >= mCurrentTouchSlop) {
							// 开始拖动
							mTouchDownP = mLastTouchP;
							mTouchDownX = mLastTouchX;
							mTouchDownY = mLastTouchY;
							delta = 0;
							onScrollStart();
							if (mEffector != null) {
								mEffector.onScrollStart();
							}
							mListener.onScrollStart();
						}
					}
					if (mState == MScroller.ON_SCROLL) {
						onScroll(delta);
					}
				}
			}
				break;
			case MotionEvent.ACTION_UP :
			case MotionEvent.ACTION_CANCEL : {
				mVelocityTracker.addMovement(event);
				mVelocityTracker.computeCurrentVelocity(1000, mMaxFlingVelocity); // CHECKSTYLE IGNORE THIS LINE
				mFlingVelocityX = (int) mVelocityTracker.getXVelocity();
				mFlingVelocityY = (int) mVelocityTracker.getYVelocity();
				mFlingVelocity = mOrientation == HORIZONTAL ? mFlingVelocityX : mFlingVelocityY;
				mVelocityTracker.clear();
				mIsEffectorEnded = false;
				if (mState == MScroller.TO_SCROLL) {
					// 在很短促的甩动情况下会没有ACTION_MOVE事件导致没有启动拖动（从而不去创建绘图缓冲或者启动硬件加速），这里保证启动拖动
					onScrollStart();

					if (mEffector != null) {
						mEffector.onScrollStart();
					}
					mListener.onScrollStart();
				}
				if (mState != MScroller.ON_FLING) {
					if (mFlingVelocity > mMinFlingVelocity && mTouchDownP <= p) {
						flingToScreen(mTouchDownScreen - 1, mScrollingDuration);
					} else if (mFlingVelocity < -mMinFlingVelocity && mTouchDownP >= p) {
						flingToScreen(mTouchDownScreen + 1, mScrollingDuration);
					} else {
						mFlingVelocity = mMinFlingVelocity;
						return flingToScreen(computeScreenIndex(mScroll), mScrollingDuration);
					}
				}
			}
				break;
			default :
				return false;
		}
		return true;
	}

	public int getFlingVelocityX() {
		return mFlingVelocityX;
	}

	public int getFlingVelocityY() {
		return mFlingVelocityY;
	}

	@Override
	public boolean onDraw(GLCanvas canvas) {
		invalidateScroll();
		if (mScreenCount < 1) {
			return true;
		}
		if (mEffector != null) {
			mForceContinue = mEffector.isAnimationing();
		}
		boolean result = mEffector != null && mEffector.onDraw(canvas);
		return result;
	}

	public boolean isBackgroundAlwaysDrawn() {
		return mBgAlwaysDrawn;
	}

	protected int onScrollAtEnd(int delta) {
		delta /= 2;
		// 限制 mMinScroll <= mScroll + delta <= mMaxScroll
		delta = Math.max(mMinScroll - mScroll, Math.min(delta, mMaxScroll - mScroll));
		return delta;
	}

	@Override
	public void onScroll(int delta) {
		final int newScroll = mScroll + delta;
		if (newScroll < 0 || newScroll >= mLastScreenPos) {
			delta = onScrollAtEnd(delta);
		}

		if (delta == 0) {
			invalidate();
			return;
		}
		super.onScroll(delta);
	}
	
	@Override
	protected void onScroll(float delta) {
		final float newScroll = mScroll + delta;
		if (newScroll < 0 || newScroll >= mLastScreenPos) {
			delta /= 2;
			// 限制 mMinScroll <= mScroll + delta <= mMaxScroll
			delta = Math.max(mMinScroll - mScroll, Math.min(delta, mMaxScroll - mScroll));
		}

		if (delta == 0) {
			invalidate();
			return;
		}
		super.onScroll(delta);
	}

	@Override
	protected void invalidate() {
		mListener.invalidate();
	}

	@Override
	protected void onComputeFlingOffset(float t) {
		t = mInterpolator.getInterpolation(t);
		int scroll;
		scroll = isFlingFinished() ? mEndScroll : mStartScroll + Math.round(t * mDeltaScroll);
		mScrollFloat = isFlingFinished() ? mEndScroll : mStartScroll + t * mDeltaScroll;

		
		mIsOvershooting = !isFlingFinished() && t > 1;
		scrollScreenGroup(scroll);
		
		
		if (!isFlingFinished() || getCurrentDepth() != 0) {
			mIsEffectorEnded = false;
		} else if (mEffector != null && !mIsEffectorEnded)  {
			mEffector.onScrollEnd();
			mIsEffectorEnded = true;
			if (!mEffector.isAnimationing()) {
				mState = FINISHED;
			}
		}
		if (mState == FINISHED) {
			if (!isOldScrollAtEnd() && mEffector != null) {
				mEffector.updateRandomEffect(); // 切换随机特效
			}
			mListener.onScrollFinish(getDstScreen());
			clearTouchState();
		}
	}

	@Override
	public void abortAnimation() {
		if (mState == ON_FLING) {
			super.abortAnimation();
			onComputeFlingOffset(1);
		}
	}
	
	/**
	 * 是否滚屏到达两端
	 * 
	 * @return
	 */
	public boolean isScrollAtEnd() {
		return mScroll < 0 || mScroll >= mLastScreenPos;
	}

	/**
	 * 上一次是否滚屏到达两端
	 * 
	 * @return
	 */
	public boolean isOldScrollAtEnd() {
		return mOldScroll < 0 || mOldScroll >= mLastScreenPos;
	}

	public void setEffectorMaxOvershootEnabled(boolean enabled) {
		mUseEffectorMaxOvershootPercent = enabled;
		setOvershootPercent(mMaxOverShootPercent);
	}

	/**
	 * 设置背景内容
	 * 
	 * @param drawable
	 * @param isClearCurrent 是否清楚当前的
	 */
	public void setBackground(Drawable drawable, boolean isClearCurrent) {
		
		// 判断是否提前设置了bound
		Rect rect = new Rect();
		if (drawable != null) {
			rect = drawable.getBounds();
		}
		
		// TODO:由Workspace去释放
		if (isClearCurrent && mBackgroundDrawable != null && mBackgroundDrawable != drawable) {
			if (mBackgroundDrawable instanceof GLDrawable) {
				((GLDrawable) mBackgroundDrawable).clear();
			}
		}
		if (drawable instanceof BitmapDrawable) {
			drawable = new BitmapGLDrawable((BitmapDrawable) drawable);
		}
		mBackgroundDrawable = drawable;
		mBitmap = null;
		if (mBackgroundDrawable != null) {
			if (rect != null && rect.height() != 0 && rect.width() != 0) {
				mBackgroundWidth = rect.width();
				mBackgroundHeight = rect.height();
				mBackgroundDrawable.setBounds(rect);
			} else {
				mBackgroundWidth = mBackgroundDrawable.getIntrinsicWidth();
				mBackgroundHeight = mBackgroundDrawable.getIntrinsicHeight();
				// 如果是从WallpaperManager.getDrawable得到的Drawable对象，则未有设置边界，绘制不出来的
				mBackgroundDrawable.setBounds(0, 0, mBackgroundWidth, mBackgroundHeight);
			}
			
			setBackgroundOffsetY();
		}
	}
	
	public void setBackground(Drawable drawable) {
		setBackground(drawable, true);
	}

	/**
	 * 设置背景的掩码颜色（没什么用）
	 * 
	 * @param color
	 */
	public void setBackgroundColorFilter(int color) {
		if ((color >>> 24) == 0) {  // CHECKSTYLE IGNORE THIS LINE
			color = 0;
		}
		if (color == 0) {
			mColorFilter = null;
			if (mPaint != null) {
				mPaint.setColorFilter(null);
			}
		} else {
			mColorFilter = new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_OVER);
			if (mPaint == null) {
				mPaint = new Paint();
			}
			mPaint.setColorFilter(mColorFilter);
		}
	}

	/**
	 * 设置视图相对于背景的垂直方向偏移量（桌面上有通知栏的情况）
	 * 
	 * @param y
	 */
	public void setScreenOffsetY(int y) {
		mScreenOffsetY = y;
		setBackgroundOffsetY();
	}

	/**
	 * 设置背景是否跟随滑动
	 * 
	 * @param enabled
	 */
	public void setBackgroundScrollEnabled(Boolean enabled) {
		mBackgroundScrollEnabled = enabled;
	}

	protected void setBackgroundOffsetY() {
		// 假设屏幕只有状态栏和视图两个区域，使壁纸在屏幕上垂直居中
		// 计算视图左上角相对于壁纸左上角的Y坐标
		// 此表达式受三个值的影响，因此在setScreenOffsetY, setBackground,
		// setViewGroup方法中都要调用它来更新
		mBackgroundOffsetY = (int) ((mScreenOffsetY + mBackgroundHeight - mScreenPaddingBottom - mScreenHeight
				/ mLayoutScale) / 2);
	}
	
	public void setScreenPaddingBottom(int paddingBottom) {
		mScreenPaddingBottom = paddingBottom;
		setBackgroundOffsetY();
	}

	public int getBackgroundOffsetX(int scroll) {
		if (mBackgroundScrollEnabled) {
			float res = (mBackgroundWidth - mScreenWidth) * (scroll - mMinScroll) * mScrollRatio;
			return (int) (res + 0.5f);  // CHECKSTYLE IGNORE THIS LINE
		} else {
			return (int) ((mBackgroundWidth - mScreenWidth) / 2);
		}
	}

	public Drawable getBackground() {
		return mBackgroundDrawable;
	}

	public int getBackgroundOffsetX() {
		return getBackgroundOffsetX(mScroll);
	}

	public int getBackgroundOffsetY() {
		return mBackgroundOffsetY;
	}

	public boolean drawBackground(GLCanvas canvas, int scroll) {
		if (mBgAlwaysDrawn || mBackgroundDrawable == null) {
			return false;
		}
		int x = -getBackgroundOffsetX(scroll);
		int y = -mBackgroundOffsetY;
		if (mOrientation == HORIZONTAL) {
			x += mScroll;
		} else {
			y += mScroll;
		}
		if (mBitmap != null && mBitmap.isRecycled()) {
			// 如果背景壁纸被其他应用更改了，图片会失效
			mBitmap = null;
			mBackgroundDrawable = null;
			return false;
		}
		if (mBitmap != null) {
			canvas.drawBitmap(mBitmap, x, y, mPaint);
		} else {
			canvas.translate(x, y);
			if (mColorFilter != null) {
				mBackgroundDrawable.setColorFilter(mColorFilter);
			}
			// mBackgroundDrawable.draw(canvas);
			canvas.drawDrawable(mBackgroundDrawable);
			if (mColorFilter != null) {
				mBackgroundDrawable.setColorFilter(null);
			}
			canvas.translate(-x, -y);
		}
		return true;
	}

	/**
	 * 绘制背景到第screen屏上
	 * 
	 * @param canvas
	 * @param screen
	 */
	public boolean drawBackgroundOnScreen(GLCanvas canvas, int screen) {
		if (mBgAlwaysDrawn || mBackgroundDrawable == null) {
			return false;
		}
		screen = checkScreen(screen);
		final int x = -getBackgroundOffsetX(screen * mScreenSize);
		final int y = -mBackgroundOffsetY;
		if (mBitmap != null && mBitmap.isRecycled()) {
			// 如果背景壁纸被其他应用更改了，图片会失效
			mBitmap = null;
			mBackgroundDrawable = null;
			return false;
		}
		if (mBitmap != null) {
			canvas.drawBitmap(mBitmap, x, y, mPaint);
		} else {
			canvas.translate(x, y);
			if (mColorFilter != null) {
				mBackgroundDrawable.setColorFilter(mColorFilter);
			}
			// mBackgroundDrawable.draw(canvas);
			canvas.drawDrawable(mBackgroundDrawable);
			if (mColorFilter != null) {
				mBackgroundDrawable.setColorFilter(null);
			}
			canvas.translate(-x, -y);
		}
		return true;
	}

	/**
	 * 设置当前使用的插值器过冲的百分比，受{@link #setMaxOvershootPercent}的影响
	 * 
	 * @param percent
	 *            建议值[0, 50]
	 */
	public void setOvershootPercent(int percent) {
		if (!mUseEffectorMaxOvershootPercent && percent != mMaxOverShootPercent) {
			return;
		}
		if (mUseEffectorMaxOvershootPercent && percent == mMaxOverShootPercent && mEffector != null) {
			percent = Math.min(percent, mEffector.getMaxOvershootPercent());
		}
		percent = Math.min(percent, mMaxOverShootPercent);
		if (mOverShootPercent == percent) {
			return;
		}
		mOverShootPercent = percent;
		if (percent <= 0) {
			setInterpolator(DEFAULT_INTERPOLATOR);
		} else {
			final float tension = solveOvershootInterpolatorTension(percent);
			setInterpolator(new OvershootInterpolator(tension));
		}
	}

	/**
	 * 设置插值器过冲的最大百分比
	 * 
	 * @param percent
	 *            建议值[0, 50)
	 */
	public void setMaxOvershootPercent(int percent) {
		mMaxOverShootPercent = Math.max(0, Math.min(percent, 49));  // CHECKSTYLE IGNORE THIS LINE
		setOvershootPercent(mMaxOverShootPercent);
	}

	public float getProgress() {
		return mScroll * mTotalSizeInv;
	}

	/**
	 * 获取指示器滑块的偏移量。 假设在第一屏和最后一屏滑块和边缘对齐，滑块宽度为屏幕宽度/屏幕数量
	 * 
	 * @return
	 */
	public int getIndicatorOffset() {
		int scroll = Math.max(0, Math.min(mScroll, mLastScreenPos));
		return (int) (scroll * mScreenCountInv + 0.5f);  // CHECKSTYLE IGNORE THIS LINE
	}
	
	public int getIndicatorCycleOffset() {
		int offset = (int) (mScroll * mScreenCountInv + 0.5f); // CHECKSTYLE IGNORE THIS LINE
		return offset;
	}

	public void setBackgroundAlwaysDrawn(boolean bgDrawn) {
		mBgAlwaysDrawn = bgDrawn;
	}

	void recycle() {
	}

	/**
	 * 获取当前屏的前一屏
	 * 
	 * @return
	 */
	public int getPreviousScreen() {
		return mCurrentScreen - 1;
	}

	/**
	 * 获取当前屏的下一屏
	 * 
	 * @return
	 */
	public int getNextScreen() {
		return mCurrentScreen + 1;
	}

	/**
	 * 返回当前绘制的左边子屏索引
	 * 
	 * @return {@link #INVALID_SCREEN}表示无效索引
	 */
	public int getDrawingScreenA() {
		int drawingScreenA = mCurrentScreen;
		if (getCurrentScreenOffset() > 0) {
			--drawingScreenA;
		}
		if (drawingScreenA < 0 || drawingScreenA >= mScreenCount) {
			return INVALID_SCREEN;
		}
		return drawingScreenA;
	}

	/**
	 * 返回当前绘制的右边子屏索引
	 * 
	 * @return {@link #INVALID_SCREEN}表示无效索引（在只绘制一屏的时候也是返回该值）
	 */
	public int getDrawingScreenB() {
		int drawingScreenB = mCurrentScreen;
		final int offset = getCurrentScreenOffset();
		if (offset == 0) {
			return INVALID_SCREEN;
		}
		if (offset < 0) {
			++drawingScreenB;
		}
		if (drawingScreenB < 0 || drawingScreenB >= mScreenCount) {
			return INVALID_SCREEN;
		}
		return drawingScreenB;
	}

	public int getTouchDeltaX() {
		return mLastTouchX - mTouchDownX;
	}

	public int getTouchDeltaY() {
		return mLastTouchY - mTouchDownY;
	}

	/**
	 * 根据当前插值器的设置计算甩动一段距离需要的时间
	 * 
	 * @param change
	 *            甩动的距离，以像素为单位
	 * @param velocity
	 *            甩动的初速度，以像素/秒为单位，必须不能为0
	 * @return 需要的时间，以毫秒为单位
	 */
	protected int computeFlingDuration(int change, int velocity) {
		/*
		 * 令f(x)为插值器函数，则实际运动方程为F(t)=b+f(t/d) * c， （b<=>begin, t<=>time,
		 * d<=>duration, c<=>change<=>end-begin），
		 * 则速度即一阶导数F'(t)=c/d*f'(t/d)，给定v，则d=c*f'(0)/v。
		 * 
		 * 对于n次方的减速曲线插值，f(x)=1-(1-x)^n， 有f'(x)=n(1-x)^(n-1), f'(0)=n，则d=nc/v。
		 * 
		 * 对于过冲插值，f(x)=(k+1)(x-1)^3+k(x-1)^2+1，其中k为张力参数，
		 * 则f'(x)=3(k+1)(x-1)^2+2k(x-1)，f'(0)=k+3。
		 * 
		 * 但是区分插值器类型太麻烦了，采用差分近似方法来求f'(0)，并且支持任意插值器。
		 */
		float diff = mInterpolator.getInterpolation(EPSILON) * ONE_OVER_EPSILON;
		return (int) Math.abs(change * diff * 1000 / velocity);  // CHECKSTYLE IGNORE THIS LINE
	}

	/**
	 * 在循环滚动的情况下，往两个方向都可以到达目标位置，启用这个选项可以选择较近的方向，默认是启用的。 如果不启用，那么在循环滚动时，
	 * {@link #gotoScreen(int, int, boolean)} 可以不限制目标位置 在[-1, mScreenCount]
	 * 
	 * @param enabled
	 */
	public void setGoShortPathEnabled(boolean enabled) {
	}

	/**
	 * 设置循环模式
	 * 
	 * @param scroller
	 * @param cycle
	 * @return
	 */
	public static void setCycleMode(ScreenScrollerListener listener, boolean cycle) {
		if (listener == null) {
			return;
		}
		ScreenScroller scroller = listener.getScreenScroller();
		if (scroller != null && scroller.isCircular() == cycle) {
			return;
		}
		ScreenScroller newScroller = cycle
				? (new CycloidScreenScroller(null, listener))
				: (new ScreenScroller(null, listener));
		listener.setScreenScroller(newScroller);
		if (scroller != null) {
			copyScrollerAttributes(scroller, newScroller);
			scroller.recycle();
		}
	}

	/**
	 * 复制滚动器的状态
	 * 
	 * @param scroller
	 * @param newScroller
	 */
	private static void copyScrollerAttributes(ScreenScroller scroller, ScreenScroller newScroller) {
		newScroller.mDstScreen = scroller.getDstScreen();
		newScroller.mScreenOffsetY = scroller.mScreenOffsetY;
		newScroller.mScreenPaddingBottom = scroller.mScreenPaddingBottom;
		newScroller.mPaddingFactor = scroller.mPaddingFactor;
		newScroller.mScreenCount = scroller.mScreenCount;
		newScroller.mOrientation = scroller.mOrientation;
		newScroller.mTouchSlop = scroller.mTouchSlop;
		newScroller.mMinFlingVelocity = scroller.mMinFlingVelocity;
		newScroller.mMaxFlingVelocity = scroller.mMaxFlingVelocity;
		newScroller.mVelocityTracker = scroller.mVelocityTracker;
		newScroller.mGoShortPath = scroller.mGoShortPath;
		newScroller.mBgAlwaysDrawn = scroller.mBgAlwaysDrawn;
		newScroller.mContextHashCode = scroller.mContextHashCode;
		newScroller.setScreenSize(scroller.mScreenWidth, scroller.mScreenHeight); // 这里根据以上的设置重新计算
		newScroller.setInterpolator(scroller.getInterpolator());
		newScroller.setDuration(scroller.mScrollingDuration);
		newScroller.setEffector(scroller.mEffector);
		newScroller.setDepthEnabled(scroller.mDepthEnabled);
		newScroller.setBackground(scroller.mBackgroundDrawable);
		newScroller.setBackgroundAlwaysDrawn(scroller.mBgAlwaysDrawn);
		newScroller.setMaxOvershootPercent(scroller.mMaxOverShootPercent);
		newScroller.setBackgroundScrollEnabled(scroller.mBackgroundScrollEnabled);
		newScroller.setInteruptFlipEnable(scroller.isInteruptFlipEnabled());
	}

	/**
	 * 计算OvershootInterpolator的张力tension
	 * 
	 * @param percent
	 *            超出部分的百分比
	 * @return
	 */
	private static float solveOvershootInterpolatorTension(int percent) {
		/*
		 * OvershootInterpolator的计算公式：k为张力>=0，t为时间[0, 1]
		 * f(t)=(t-1)^2*((k+1)*(t-1)+k)+1=(k+1)(t-1)^3+k(t-1)^2+1
		 * 导数f'(t)=3(k+1)(t-1)^2+2k(t-1)^2 令f'(t)==0，解得t=1-2k/(3(k+1))，或t=1（舍去）
		 * 代入f(t)，得max(f(t))=4k^3/(27(k+1)^2)+1
		 * 即最大超出部分为g(k)=max(f(t))-1=4k^3/(27(k+1)^2) 使用Mathematica命令
		 * Solve[4k^3/(27(k+1)^2)==0.1, k]
		 * http://www.wolframalpha.com/input/?i=Solve
		 * [4k^3%2F%2827%28k%2B1%29^2%29%3D%3D0.1%2C+k] 解g(k)=0.1，得k=1.70154
		 * 解g(k)=0.5，得k=4.89486 如果我们指定g，那么通过解g(k)的方程就得到张力k了——
		 * Solve[4k^3/(27(k+1)^2)==g, k]
		 * http://www.wolframalpha.com/input/?i=Solve
		 * [4k^3%2F%2827%28k%2B1%29^2%29%3D%3Dg%2C+k] 部分结果如下： percent = 0
		 * tension=NaN percent = 10 tension=1.7015402 percent = 20
		 * tension=2.5923889 percent = 30 tension=3.3940518 percent = 40
		 * tension=4.155745 percent = 50 tension=4.8948593
		 */

		// if(percent <= 0) return 0; // 注意percent为0的时候最后除0会得到NaN
		// // 直接设张力为0，退化成DecelerateInterpolator(1.5f)
		// float g = percent / 100.0f;
		// float g2 = g * g;
		// float g3 = g * g2;
		// double d = 27 * g3 + 36 * g2 + 8 * Math.sqrt(g3 + g2) + 8 * g;
		// d = Math.pow(d, 1.0f / 3);
		// return (float)(0.75f * d + (729 * g2 + 648 * g) / (108 * d) + 2.25f *
		// g);

		// 用查找表记录percent=0,5,10,...,50的结果，其他percent使用线性插值计算，对<5的时候还是有10%以上误差
		final float[] tension = { 0.0f, 1.1652954f, 1.7015402f, 2.1642938f, 2.5923889f, 3.0f,
				3.3940518f, 3.7784798f, 4.155745f, 4.5274878f, 4.8948593f, };
		percent = Math.max(0, Math.min(percent, 49));  // CHECKSTYLE IGNORE THIS LINE
		int i = percent / 5;  // CHECKSTYLE IGNORE THIS LINE
		return tension[i] + (tension[i + 1] - tension[i]) * (percent / 5.0f - i);  // CHECKSTYLE IGNORE THIS LINE
	}

	public void setLayoutScale(float scale) {
		mLayoutScale = scale;
	}

	public float getLayoutScale() {
		return mLayoutScale;
	}
	
	public boolean isOnFilng() {
		return mState == MScroller.ON_FLING;
	}

	@Override
	public boolean isFinished() {
		return super.isFinished();
	}
	
	/**
	 * 清除touch状态
	 */
	public void clearTouchState() {
		mLastTouchX = 0;
		mLastTouchY = 0;
		mTouchDownX = 0;
		mTouchDownY = 0;
	}

	/**
	 * <br>功能简述:滑屏过程中再次触屏是否中断滑屏
	 * <br>功能详细描述:
	 * <br>注意:
	 * @return
	 */
	public boolean isInteruptFlipEnabled() {
		return mEnableInteruptFlip;
	}

	/**
	 * <br>功能简述:设置滑屏过程中再次触屏是否中断滑屏
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param enable
	 */
	public void setInteruptFlipEnable(boolean enable) {
		this.mEnableInteruptFlip = enable;
	}
	
	final public float getCurrentScreenDrawingOffset(boolean first) {
		float curOffset = getCurrentScreenOffsetFloat();
		float offset = curOffset;
		if (getCurrentScreenOffset() > 0) {
			offset -= mScreenSize;
		}
		return first ? offset : offset + mScreenSize;
	}
}
