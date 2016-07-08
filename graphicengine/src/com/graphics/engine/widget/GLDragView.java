package com.graphics.engine.widget;

import java.util.ArrayList;
import java.util.HashSet;

import android.content.Context;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

import com.graphics.engine.animation.Animation;
import com.graphics.engine.animation.Animation.AnimationListener;
import com.graphics.engine.animation.Transformation3D;
import com.graphics.engine.graphics.GLCanvas;
import com.graphics.engine.math3d.Ray;
import com.graphics.engine.view.GLContentView;
import com.graphics.engine.view.GLView;
import com.graphics.engine.view.GLViewGroup;
import com.graphics.engine.view.GLViewParent;

/**
 * 
 * <br>类描述: 实现拖拽效果的视图
 * <br>功能详细描述:
 * 
 * @author  dengweiming
 * @date  [2013-7-1]
 * @see {@link GLDragListener}
 */
public class GLDragView extends GLViewGroup implements AnimationListener {
	/** 没有触摸命中 */
	public static final int MISS = 0;
	/** 触摸命中 */
	public static final int HIT = 1;
	/** 触摸待定，命中了但是允许更近的物体被触摸命中，一般用在3D环境 */
	public static final int PENDING = 2;

	/** 公用静态的临时变换对象 */
	public final static Transformation3D TMP_TRANSFORMATION = new Transformation3D();
	
	private static final long HOVER_TIMEOUT = ViewConfiguration.getLongPressTimeout();
	
	private static final int INDEX_TX = 12;
	private static final int INDEX_TY = 13;
	private static final int INDEX_TZ = 14;
	private static final float UNTOUCH = Float.MIN_VALUE;
	private static final float[] TEMP_VECTOR = new float[4];
	private static final int[] LOCATION = new int[2];

	private GLDragListener mSource;
	private GLDragListener mTarget;
	private GLView mView;
	private Transformation3D mTransformation = new Transformation3D();
	private Object mExtraData;
	
	private Ray mDownRay = new Ray();
	private Ray mLastRay = new Ray();
	private Ray mRay = new Ray();
	private float mTouchDownX;
	private float mTouchDownY;
	private float mLastTouchX;
	private float mLastTouchY;
	private float mTouchX; 
	private float mTouchY;
	private float mOffsetCenterX;
	private float mOffsetCenterY;
	private boolean mTouchReleased;
	private boolean mCheckForLongPressPosted;
	private CheckForLongPress mPendingCheckForLongPress = new CheckForLongPress();
	
	private static final int ARRAY_INITIAL_CAPACITY = 12;
    private ArrayList<GLDragListener> mListeners = new ArrayList<GLDragListener>(ARRAY_INITIAL_CAPACITY);
    private HashSet<GLDragListener> mListenersSet = new HashSet<GLDragListener>(ARRAY_INITIAL_CAPACITY);
    
    private Animation mAnimation;
    private AnimationListener mAttachedAnimationListener;
	private Transformation3D mTransformationOnAnimationStart = new Transformation3D();
	private Transformation3D mAnimationTransformation = new Transformation3D();
	private float mTouchXOnAnimationStart; 
	private float mTouchYOnAnimationStart;
	private float mMoveXDuringAnimation; 
	private float mMoveYDuringAnimation;
	
	private boolean mIsCheck = true; // 该标志位用于其他手动调用dispatchTouch方法的操作，用于暂时不做mOffsetCenterX，mOffsetCenterY的记录
	private float mTouchOffsetCenterX;
	private float mTouchOffsetCenterY;
	
	public GLDragView(Context context) {
		super(context);
	}

	public GLDragView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public GLDragView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	/**
	 * <br>功能简述:
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param listener
	 * @param view	被拖拽的视图
	 * @param t 被拖拽的视图的当前变换，可以为null。可以使用{@link #TMP_TRANSFORMATION} 避免new一个实例。
	 * @param extraData 额外数据，可以为null
	 */
	public void startDrag(GLDragListener listener, GLView view, Transformation3D t, Object extraData) {
		mSource = listener;
		mView = view;
		
		GLViewParent parent = getGLParent();
		if (parent != null) {
			parent.bringChildToFront(this);
		}
		
		if (t != null) {
			mTransformation.set(t);
		} else {
			view.getLoactionInGLViewRoot(LOCATION);
			mTransformation.clear().setTranslate(LOCATION[0], LOCATION[1]);
		}
		mExtraData = extraData;
		
		registerListener(listener);
		setVisibility(VISIBLE);
		
		final int count = mListeners.size();
		for (int i = 0; i < count; ++i) {
			GLDragListener l = mListeners.get(i);
			l.onDragStart(this);
		}
		
		mTouchY = UNTOUCH;
		mTouchReleased = false;
		if (!mCheckForLongPressPosted) {
			postCheckForLongClick(0);
		}
	}
	
	public void finishDrag() {
		if (mView != null) {
			mView.setVisibility(VISIBLE);
		}
		setVisibility(GONE);
		for (int i = 0, count = mListeners.size(); i < count; ++i) {
			GLDragListener l = mListeners.get(i);
			l.onDragEnd(this);
		}
		mTouchReleased = true;
		mSource = null;
		mTarget = null;
		mView = null;
		mExtraData = null;
		mAnimation = null;
		mAttachedAnimationListener = null;
		removeLongPressCallback();
	}
	
	public void finishDrag(Animation a, AnimationListener attachedListener) {
		mTouchReleased = true;
		mAttachedAnimationListener = attachedListener;
		a.setAnimationListener(this);
		startDragAnimation(a);
	}
	
	public void startDragAnimation(Animation animation) {
		mAnimation = animation;
		if (animation != null) {
			mTransformationOnAnimationStart.set(mTransformation);
			mAnimationTransformation.clear();
			mTouchYOnAnimationStart = UNTOUCH;
			mMoveXDuringAnimation = 0;
			mMoveYDuringAnimation = 0;
			
			animation.setStartTime(Animation.START_ON_FIRST_FRAME);
			animation.reset();
			invalidate();
		}
	}
	
	public void cancleDragAnimation() {
		mAnimation = null;
	}
	
	public boolean registerListener(GLDragListener listener) {
		if (listener != null && mListenersSet.add(listener)) {
			mListeners.add(listener);
			return true;
		}
		return false;
	}
	
	public boolean unregisterListener(GLDragListener listener) {
		if (listener != null && mListenersSet.remove(listener)) {
			mListeners.remove(listener);
			return true;
		}
		return false;
	}
	
	public void cleanup() {
		mListeners.clear();
		mListenersSet.clear();
	}
	
	public final boolean isTouchReleased() {
		return mTouchReleased;
	}
	
	public final boolean isInDrag() {
		return mView != null;
	}
	
	public final GLView getDraggedView() {
		return mView;
	}
	
	public final GLDragListener getDragSource() {
		return mSource;
	}
	
	public final GLDragListener getDropTarget() {
		return mTarget;
	}
	
	public final Object getExtraData() {
		return mExtraData;
	}
	
	public final float getTouchDownX() {
		return mTouchDownX;
	}
	
	public final float getTouchDownY() {
		return mTouchDownY;
	}
	
	public final Ray getTouchDownRay() {
		return mDownRay;
	}
	
	public final float getLastTouchX() {
		return mLastTouchX;
	}
	
	public final float getLastTouchY() {
		return mLastTouchY;
	}
	
	public final float getOffsetCenterX() {
		return mOffsetCenterX;
	}
	
	public final float getOffsetCenterY() {
		return mOffsetCenterY;
	}
	
	public void setOffsetCenterX(float centerX) {
		mOffsetCenterX = centerX;
	}
	
	public void setOffsetCenterY(float centerY) {
		mOffsetCenterY = centerY;
	}
	
	public final float getTouchOffsetCenterX() {
		return mTouchOffsetCenterX;
	}
	
	public final float getTouchOffsetCenterY() {
		return mTouchOffsetCenterY;
	}
	
	public void setTouchOffsetCenterX(float centerX) {
		mTouchOffsetCenterX = centerX;
	}
	
	public void setTouchOffsetCenterY(float centerY) {
		mTouchOffsetCenterY = centerY;
	}
	
	public final Ray getLastTouchRay() {
		return mLastRay;
	}

	/**
	 * <br>功能简述: 获取拖拽的视图中心的3D位置
	 * <br>功能详细描述:
	 * <br>注意: 返回的数组的y值是Y轴向上的
	 * @return
	 */
	public final float[] getDraggedViewCenterPosition() {
		float[] u = TEMP_VECTOR;
		u[0] = mView.getWidth() * 0.5f;		//CHECKSTYLE IGNORE
		u[1] = mView.getHeight() * -0.5f;	//CHECKSTYLE IGNORE
		u[2] = 0;
		mTransformation.mapVector(u, 0, u, 0, 1);
		return u;
	}
	
	public final Transformation3D getTransformation() {
		return mTransformation;
	}
	
	public void setTransformation(Transformation3D t) {
		mTransformation.set(t);
		invalidate();
	}
	
	public void conposetTransformation(Transformation3D t) {
		mTransformation.compose(t);
		invalidate();
	}
	
	/**
	 * <br>功能简述:强制查找新的拖拽响应者
	 * <br>功能详细描述:例如，视图层次结构改变了，但是没有新的触摸事件，此时需要查找新的拖拽响应者
	 * <br>注意:
	 */
	public void forceFindTarget() {
		GLDragListener oldTarget = mTarget;
		mTarget = null;
		mTarget = findTarget(null);
		
		if (oldTarget != mTarget) {
			if (oldTarget != null) {
				oldTarget.onDragExit(this);
			}

			if (mTarget != null) {
				mTarget.onDragEnter(this);
			}
		}
	}

	@Override
	protected void dispatchDraw(GLCanvas canvas) {
		if (mView == null) {
			return;
		}
		
		final Animation a = mAnimation;
		boolean more = false;
		if (a != null) {
			final long drawingTime = getDrawingTime();
			final boolean initialized = a.isInitialized();
			if (!initialized) {
				a.initialize(mView.getWidth(), mView.getHeight(), getWidth(), getHeight());
			}
			more = a.getTransformation(drawingTime, mAnimationTransformation);
			mTransformation.set(mTransformationOnAnimationStart);
			mTransformation.compose(mAnimationTransformation);
			float[] matrix = mTransformation.getMatrix();
			matrix[INDEX_TX] += mMoveXDuringAnimation;
			matrix[INDEX_TY] -= mMoveYDuringAnimation;
		}
		
		final int oldAlpha = canvas.getAlpha();
		final int saveCount = canvas.save();
		canvas.reset();
		
		final boolean drawn = mTarget != null && mTarget.onDrawDraggedView(this, canvas, mView, mTransformation);
		if (!drawn) {
			canvas.concat(mTransformation.getMatrix(), 0);
			final float alpha = mTransformation.getAlpha();
			if (alpha != 1) {
				canvas.setAlpha((int) (oldAlpha * alpha));
			}
			canvas.translate(-mView.getLeft(), -mView.getTop());
			drawChild(canvas, mView, getDrawingTime());
		}
		
		canvas.restoreToCount(saveCount);
		canvas.setAlpha(oldAlpha);
		
		if (more) {
			invalidate();
		} else {
			mAnimation = null;
		}
	}
	
	//设置是否需要检查mOffsetCenterX,mOffsetCenterY
	public void setIsCheckOffsetCenterInfo(boolean isCheck) {
		mIsCheck = isCheck;
	}
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if (mView == null) {
			return false;
		}
		if (mTouchReleased) {
			return true;
		}
		
		Ray tmpRay = mRay;
		mRay = mLastRay;
		mLastRay = tmpRay;
		mLastTouchX = mTouchX;
		mLastTouchY = mTouchY;
		
		final float x = ev.getX();
		final float y = ev.getY();
		if (!getTouchRay(mRay, true)) { //假定了视图左上角就在世界原点
			return false;
		}
		mRay.startCast();
		if (mTouchY == UNTOUCH) {
			mTouchDownX = x;
			mTouchDownY = y;
			mDownRay.set(mRay);
			
			mLastTouchX = x;
			mLastTouchY = y;
			mLastRay.set(mRay);
			
			if (mView != null && mIsCheck) {
				int[] loc = LOCATION;
				mView.getLocationInWindow(loc);
				mOffsetCenterX = mTouchDownX - (loc[0] + mView.getWidth() / 2);
				mOffsetCenterY = mTouchDownY - (loc[1] + mView.getHeight() / 2);
			}
			
			mPendingCheckForLongPress.rememberTouchPosition(x, y);
		}
		mTouchX = x;
		mTouchY = y;
		
		final int action = ev.getAction() & MotionEvent.ACTION_MASK;
		
		switch (action) {
			case MotionEvent.ACTION_DOWN :
				break;
				
			case MotionEvent.ACTION_MOVE :
				onTouchMove();
				break;
				
			case MotionEvent.ACTION_UP :
			case MotionEvent.ACTION_CANCEL :
				onTouchUp();
				
				break;

			default :
				break;
		}
		return true;
	}

	private void onTouchMove() {
		mPendingCheckForLongPress.onTouchMove(mTouchX, mTouchY);
		
		final GLDragListener oldTarget = mTarget;
		int res = oldTarget == null ? PENDING : oldTarget.onCheckTouch(this, mTouchX, mTouchY, mRay);
		if (res == MISS) {
			mTarget = findTarget(oldTarget);
		} else if (res == PENDING) {
			mTarget = findTarget(null);
		}
		if (oldTarget != mTarget) {
			if (oldTarget != null) {
				oldTarget.onDragExit(this);
			}
			if (mTarget != null) {
				mTarget.onDragEnter(this);
			}
		}
		
		if (mTarget == null || !mTarget.onDragMove(this, mTouchX, mTouchY, mRay)) {
			GLContentView rootView = getGLRootView();
			
			if (mAnimation == null) {
				float[] matrix = mTransformation.getMatrix();
				float tz = matrix[INDEX_TZ];
				float s = 1 / rootView.getProjectScale(tz);
				matrix[INDEX_TX] += (mTouchX - mLastTouchX) * s;
				matrix[INDEX_TY] -= (mTouchY - mLastTouchY) * s;
			} else {
				float[] matrix = mTransformationOnAnimationStart.getMatrix();
				float tz = matrix[INDEX_TZ];
				float s = 1 / rootView.getProjectScale(tz);
				if (mTouchYOnAnimationStart == UNTOUCH) {
					mTouchXOnAnimationStart = mTouchX;
					mTouchYOnAnimationStart = mTouchY;
				}
				mMoveXDuringAnimation = (mTouchX - mTouchXOnAnimationStart) * s;
				mMoveYDuringAnimation = (mTouchY - mTouchYOnAnimationStart) * s;
			}
			
			invalidate();
		}
	}
	
	private void onTouchUp() {
		mTouchReleased = true;
		removeLongPressCallback();

		if (mTarget != null) {
			mTarget.onDragExit(this);
		}
		boolean handled = mTarget != null && mTarget.onDropTo(this, mTouchX, mTouchY, mRay, mSource);
		handled = mSource != null && mSource.onDropFrom(this, mTouchX, mTouchY, mRay, mTarget, handled);
		if (!handled && mView != null) {
			finishDrag();
		}
	}

	private GLDragListener findTarget(GLDragListener exclude) {
		final int count = mListeners.size();
		GLDragListener pendingListener = null;
		for (int i = count - 1; i >= 0; i--) {
			GLDragListener listener = mListeners.get(i);
			if (listener != exclude && listener.getVisibility() == VISIBLE) {
				int res = listener.onCheckTouch(this, mTouchX, mTouchY, mRay);
				if (res == HIT) {
					return listener;
				} else if (res == PENDING) {
					pendingListener = listener;
				}
			}
		}
		return pendingListener;
	}

	@Override
	public void onAnimationStart(Animation animation) {
		if (mAttachedAnimationListener != null) {
			mAttachedAnimationListener.onAnimationStart(animation);
		}
	}

	@Override
	public void onAnimationEnd(Animation animation) {
		if (mAttachedAnimationListener != null) {
			mAttachedAnimationListener.onAnimationEnd(animation);
		}
		
		post(new Runnable() {
			@Override
			public void run() {
				finishDrag();
			}
		});
	}

	@Override
	public void onAnimationRepeat(Animation animation) {
		if (mAttachedAnimationListener != null) {
			mAttachedAnimationListener.onAnimationRepeat(animation);
		}
	}
	
	/**
	 * 检查触摸悬停的类
	 */
    class CheckForLongPress implements Runnable {
        private int mOriginalWindowAttachCount;
        private float mOriginalTouchX;
        private float mOriginalTouchY;
        private long mLastMoveTime;
        private boolean mMoved;

        public void run() {
        	long delayOffset = 0;
            if (!mTouchReleased && (getGLParent() != null)
                    && mOriginalWindowAttachCount == getWindowAttachCount()) {
				if (mMoved) {
					long delay = mLastMoveTime + HOVER_TIMEOUT - SystemClock.uptimeMillis();
					if (delay > 0) {
						postDelayed(this, delay + HOVER_TIMEOUT / 2);	//delay再加上触摸移动的惩罚时间
						return;
					}
				}
				mCheckForLongPressPosted = false;
				delayOffset = onLongClick();
            }
            postCheckForLongClick(delayOffset);
        }

        public void rememberWindowAttachCount() {
            mOriginalWindowAttachCount = getWindowAttachCount();
        }
        
        public void rememberTouchPosition(float x, float y) {
        	mOriginalTouchX = x;
        	mOriginalTouchY = y;
        	mLastMoveTime = SystemClock.uptimeMillis();
        	mMoved = false;
        }
        
        public void onTouchMove(float x, float y) {
			if (!checkStay(mOriginalTouchX, mOriginalTouchY)) {
				rememberTouchPosition(x, y);
				mMoved = true;
			}
        }
    }

    private void postCheckForLongClick(long delayOffset) {
        mCheckForLongPressPosted = true;
        
        mPendingCheckForLongPress.rememberWindowAttachCount();
        mPendingCheckForLongPress.rememberTouchPosition(mTouchX, mTouchY);
        postDelayed(mPendingCheckForLongPress, HOVER_TIMEOUT + delayOffset);
    }
    
    private void removeLongPressCallback() {
    	removeCallbacks(mPendingCheckForLongPress);
        mCheckForLongPressPosted = false;
    }
    
    private long onLongClick() {
		if (mTarget != null) {
			return mTarget.onDragHover(this, mTouchX, mTouchY, mRay);
		}
		return 0;
		
    }
    
    private boolean checkStay(float ox, float oy) {
		if (mTouchY == UNTOUCH) {
			return true;
		}
    	final float touchSlop = getTouchSlop();
		return Math.abs(mTouchX - ox) <= touchSlop && Math.abs(mTouchY - oy) <= touchSlop;
    }
    
}
