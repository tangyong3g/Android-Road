package com.graphics.engine.view;

import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

/**
 * Helper class to handle situations where you want a view to have a larger touch area than its
 * actual view bounds. The view whose touch area is changed is called the delegate view. This
 * class should be used by an ancestor of the delegate. To use a TouchDelegate, first create an
 * instance that specifies the bounds that should be mapped to the delegate and the delegate
 * view itself.
 * <p>
 * The ancestor should then forward all of its touch events received in its
 * {@link android.view.View#onTouchEvent(MotionEvent)} to {@link #onTouchEvent(MotionEvent)}.
 * </p>
 */
public class TouchDelegate {
    
    /**
     * View that should receive forwarded touch events 
     */
    private GLView mDelegateView;
    
    /**
     * Bounds in local coordinates of the containing view that should be mapped to the delegate
     * view. This rect is used for initial hit testing.
     */
    private Rect mBounds;
    
    /**
     * mBounds inflated to include some slop. This rect is to track whether the motion events
     * should be considered to be be within the delegate view.
     */
    private Rect mSlopBounds;
    
    /**
     * True if the delegate had been targeted on a down event (intersected mBounds).
     */
    private boolean mDelegateTargeted;

    /**
     * The touchable region of the View extends above its actual extent.
     */
    public static final int ABOVE = 1;

    /**
     * The touchable region of the View extends below its actual extent.
     */
    public static final int BELOW = 2;

    /**
     * The touchable region of the View extends to the left of its
     * actual extent.
     */
    public static final int TO_LEFT = 4;

    /**
     * The touchable region of the View extends to the right of its
     * actual extent.
     */
    public static final int TO_RIGHT = 8;

    private int mSlop;
    
    private static Rect sTempRect = new Rect();
    
    /**
     * <br>功能简述:
     * <br>功能详细描述:
     * <br>注意:	<var>delegateView</var>应该添加到视图容器中，因为实际上TouchDelegate是设置给父容器的。
     * @param extendPadding		各边界的扩展大小
     * @param delegateView		要响应触摸事件的视图
     * @param delay				获取视图的点击区域的延时，因为需要等待排版后才能去获取。如果不大于0，则立即去获取，调用者应该保证视图已经排版过。
     */
	public static void setTouchDelegate(final Rect extendPadding, final GLView delegateView,
			int delay) {
		if (delay > 0) {
			delegateView.postDelayed(new Runnable() {
				// Post in the parent's message queue to make sure the parent
				// lays out its children before we call getHitRect()
				public void run() {
					setTouchDelegateInternal(extendPadding, delegateView);
				}
			}, delay);
		} else {
			setTouchDelegateInternal(extendPadding, delegateView);
		}
	}
	
	private static void setTouchDelegateInternal(Rect extendPadding, GLView delegateView) {
		final Rect r = sTempRect;
		delegateView.getHitRect(r);
		r.left -= extendPadding.left;
		r.top -= extendPadding.top;
		r.right += extendPadding.right;
		r.bottom += extendPadding.bottom;
		final GLView parent = (GLView) delegateView.getGLParent();
		if (parent != null) {
			parent.setTouchDelegate(new TouchDelegate(r, delegateView));
		}
	}

    /**
     * Constructor
     * 
     * @param bounds Bounds in local coordinates of the containing view that should be mapped to
     *        the delegate view
     * @param delegateView The view that should receive motion events
     */
    public TouchDelegate(Rect bounds, GLView delegateView) {
        mBounds = bounds;

        mSlop = ViewConfiguration.get(delegateView.getContext()).getScaledTouchSlop();
        mSlopBounds = new Rect(bounds);
        mSlopBounds.inset(-mSlop, -mSlop);
        mDelegateView = delegateView;
    }

    /**
     * Will forward touch events to the delegate view if the event is within the bounds
     * specified in the constructor.
     * 
     * @param event The touch event to forward
     * @return True if the event was forwarded to the delegate, false otherwise.
     */
    public boolean onTouchEvent(MotionEvent event) {
		int x = (int) event.getX();
		int y = (int) event.getY();
        boolean sendToDelegate = false;
        boolean hit = true;
        boolean handled = false;

        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
            Rect bounds = mBounds;

            if (bounds.contains(x, y)) {
                mDelegateTargeted = true;
                sendToDelegate = true;
            }
            break;
        case MotionEvent.ACTION_UP:
        case MotionEvent.ACTION_MOVE:
            sendToDelegate = mDelegateTargeted;
            if (sendToDelegate) {
                Rect slopBounds = mSlopBounds;
                if (!slopBounds.contains(x, y)) {
                    hit = false;
                }
            }
            break;
        case MotionEvent.ACTION_CANCEL:
            sendToDelegate = mDelegateTargeted;
            mDelegateTargeted = false;
            break;
        }
        if (sendToDelegate) {
            final GLView delegateView = mDelegateView;
            
            if (hit) {
                // Offset event coordinates to be inside the target view
                event.setLocation(delegateView.getWidth() / 2, delegateView.getHeight() / 2);
            } else {
                // Offset event coordinates to be outside the target view (in case it does
                // something like tracking pressed state)
                int slop = mSlop;
                event.setLocation(-(slop * 2), -(slop * 2));
            }
            handled = delegateView.dispatchTouchEvent(event);
        }
        return handled;
    }
}
