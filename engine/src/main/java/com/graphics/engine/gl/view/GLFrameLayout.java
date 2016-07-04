package com.graphics.engine.gl.view;

import android.view.ViewDebug;

import com.graphics.engine.gl.graphics.GLCanvas;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewDebug;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;

/**
 * FrameLayout is designed to block out an area on the screen to display
 * a single item. You can add multiple children to a FrameLayout, but all
 * children are pegged to the top left of the screen.
 * Children are drawn in a stack, with the most recently added child on top.
 * The size of the frame layout is the size of its largest child (plus padding), visible
 * or not (if the FrameLayout's parent permits). Views that are GONE are used for sizing
 * only if {@link #setMeasureAllChildren(boolean) setConsiderGoneChildrenWhenMeasuring()}
 * is set to true.
 *
 * @attr ref android.R.styleable#FrameLayout_foreground
 * @attr ref android.R.styleable#FrameLayout_foregroundGravity
 * @attr ref android.R.styleable#FrameLayout_measureAllChildren
 */
//@RemoteView
public class GLFrameLayout extends GLViewGroup {

    @ViewDebug.ExportedProperty
    boolean mMeasureAllChildren = false;

    @ViewDebug.ExportedProperty
    private Drawable mForeground;
    @ViewDebug.ExportedProperty
    private int mForegroundPaddingLeft = 0;
    @ViewDebug.ExportedProperty
    private int mForegroundPaddingTop = 0;
    @ViewDebug.ExportedProperty
    private int mForegroundPaddingRight = 0;
    @ViewDebug.ExportedProperty
    private int mForegroundPaddingBottom = 0;

    private final Rect mSelfBounds = new Rect();
    private final Rect mOverlayBounds = new Rect();
    @ViewDebug.ExportedProperty
    private int mForegroundGravity = Gravity.FILL;

    /** {@hide} */
    @ViewDebug.ExportedProperty
    protected boolean mForegroundInPadding = true;

    boolean mForegroundBoundsChanged = false;
	
	public GLFrameLayout(Context context) {
		super(context);
	}

    public GLFrameLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GLFrameLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray a = context.obtainStyledAttributes(attrs, com_android_internal_R_styleable.FrameLayout,
                    defStyle, 0);

        mForegroundGravity = a.getInt(com_android_internal_R_styleable.FrameLayout_foregroundGravity, mForegroundGravity);

        final Drawable d = a.getDrawable(com_android_internal_R_styleable.FrameLayout_foreground);
        if (d != null) {
            setForeground(d);
        }
        
        if (a.getBoolean(com_android_internal_R_styleable.FrameLayout_measureAllChildren, false)) {
            setMeasureAllChildren(true);
        }

        mForegroundInPadding = a.getBoolean(
        		com_android_internal_R_styleable.FrameLayout_foregroundInsidePadding, true);

        a.recycle();
    }

    /**
     * Describes how the foreground is positioned. Defaults to FILL.
     *
     * @param foregroundGravity See {@link android.view.Gravity}
     *
     * @attr ref android.R.styleable#FrameLayout_foregroundGravity
     */
//    @android.view.RemotableViewMethod
    public void setForegroundGravity(int foregroundGravity) {
        if (mForegroundGravity != foregroundGravity) {
            if ((foregroundGravity & Gravity.HORIZONTAL_GRAVITY_MASK) == 0) {
                foregroundGravity |= Gravity.LEFT;
            }

            if ((foregroundGravity & Gravity.VERTICAL_GRAVITY_MASK) == 0) {
                foregroundGravity |= Gravity.TOP;
            }

            mForegroundGravity = foregroundGravity;


            if (mForegroundGravity == Gravity.FILL && mForeground != null) {
                Rect padding = new Rect();
                if (mForeground.getPadding(padding)) {
                    mForegroundPaddingLeft = padding.left;
                    mForegroundPaddingTop = padding.top;
                    mForegroundPaddingRight = padding.right;
                    mForegroundPaddingBottom = padding.bottom;
                }
            } else {
                mForegroundPaddingLeft = 0;
                mForegroundPaddingTop = 0;
                mForegroundPaddingRight = 0;
                mForegroundPaddingBottom = 0;
            }

            requestLayout();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean verifyDrawable(Drawable who) {
        return super.verifyDrawable(who) || (who == mForeground);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        if (mForeground != null && mForeground.isStateful()) {
            mForeground.setState(getDrawableState());
        }
    }


    /**
     * Returns a set of layout parameters with a width of
     * {@link android.view.ViewGroup.LayoutParams#MATCH_PARENT},
     * and a height of {@link android.view.ViewGroup.LayoutParams#MATCH_PARENT}.
     */
    @Override
    protected FrameLayout.LayoutParams generateDefaultLayoutParams() {
        return new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }

    /**
     * Supply a Drawable that is to be rendered on top of all of the child
     * views in the frame layout.  Any padding in the Drawable will be taken
     * into account by ensuring that the children are inset to be placed
     * inside of the padding area.
     *
     * @param drawable The Drawable to be drawn on top of the children.
     *
     * @attr ref android.R.styleable#FrameLayout_foreground
     */
    public void setForeground(Drawable drawable) {
        if (mForeground != drawable) {
            if (mForeground != null) {
                mForeground.setCallback(null);
                unscheduleDrawable(mForeground);
            }

            mForeground = drawable;
            mForegroundPaddingLeft = 0;
            mForegroundPaddingTop = 0;
            mForegroundPaddingRight = 0;
            mForegroundPaddingBottom = 0;

            if (drawable != null) {
                setWillNotDraw(false);
                drawable.setCallback(this);
                if (drawable.isStateful()) {
                    drawable.setState(getDrawableState());
                }
                if (mForegroundGravity == Gravity.FILL) {
                    Rect padding = new Rect();
                    if (drawable.getPadding(padding)) {
                        mForegroundPaddingLeft = padding.left;
                        mForegroundPaddingTop = padding.top;
                        mForegroundPaddingRight = padding.right;
                        mForegroundPaddingBottom = padding.bottom;
                    }
                }
            }  else {
                setWillNotDraw(true);
            }
            requestLayout();
            invalidate();
        }
    }

    /**
     * Returns the drawable used as the foreground of this FrameLayout. The
     * foreground drawable, if non-null, is always drawn on top of the children.
     *
     * @return A Drawable or null if no foreground was set.
     */
    public Drawable getForeground() {
        return mForeground;
    }

    public int getForegroundPaddingLeft() {
    	return mForegroundPaddingLeft;
    }

    public int getForegroundPaddingRight() {
    	return mForegroundPaddingRight;
    }

    public int getForegroundPaddingTop() {
    	return mForegroundPaddingTop;
    }

    public int getForegroundPaddingBottom() {
    	return mForegroundPaddingBottom;
    }

    public void setForegroundBoundsChanged(boolean changed) {
    	mForegroundBoundsChanged = changed;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int count = getChildCount();

        int maxHeight = 0;
        int maxWidth = 0;

        // Find rightmost and bottommost child
        for (int i = 0; i < count; i++) {
            final GLView child = getChildAt(i);
            if (mMeasureAllChildren || child.getVisibility() != GONE) {
                measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
                maxWidth = Math.max(maxWidth, child.getMeasuredWidth());
                maxHeight = Math.max(maxHeight, child.getMeasuredHeight());
            }
        }

        // Account for padding too
        maxWidth += mPaddingLeft + mPaddingRight + mForegroundPaddingLeft + mForegroundPaddingRight;
        maxHeight += mPaddingTop + mPaddingBottom + mForegroundPaddingTop + mForegroundPaddingBottom;

        // Check against our minimum height and width
        maxHeight = Math.max(maxHeight, getSuggestedMinimumHeight());
        maxWidth = Math.max(maxWidth, getSuggestedMinimumWidth());

        // Check against our foreground's minimum height and width
        final Drawable drawable = getForeground();
        if (drawable != null) {
            maxHeight = Math.max(maxHeight, drawable.getMinimumHeight());
            maxWidth = Math.max(maxWidth, drawable.getMinimumWidth());
        }

        setMeasuredDimension(resolveSize(maxWidth, widthMeasureSpec),
                resolveSize(maxHeight, heightMeasureSpec));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        final int count = getChildCount();

        final int parentLeft = mPaddingLeft + mForegroundPaddingLeft;
        final int parentRight = right - left - mPaddingRight - mForegroundPaddingRight;

        final int parentTop = mPaddingTop + mForegroundPaddingTop;
        final int parentBottom = bottom - top - mPaddingBottom - mForegroundPaddingBottom;

        mForegroundBoundsChanged = true;

        for (int i = 0; i < count; i++) {
            final GLView child = getChildAt(i);
            if (child != null && child.getVisibility() != GONE) {
                final LayoutParams lp = (LayoutParams) child.getLayoutParams();

                final int width = child.getMeasuredWidth();
                final int height = child.getMeasuredHeight();

                int childLeft = parentLeft;
                int childTop = parentTop;

                final int gravity = lp.gravity;

                if (gravity != -1) {
                    final int horizontalGravity = gravity & Gravity.HORIZONTAL_GRAVITY_MASK;
                    final int verticalGravity = gravity & Gravity.VERTICAL_GRAVITY_MASK;

                    switch (horizontalGravity) {
                        case Gravity.LEFT:
                            childLeft = parentLeft + lp.leftMargin;
                            break;
                        case Gravity.CENTER_HORIZONTAL:
                            childLeft = parentLeft + (parentRight - parentLeft - width) / 2 +
                                    lp.leftMargin - lp.rightMargin;
                            break;
                        case Gravity.RIGHT:
                            childLeft = parentRight - width - lp.rightMargin;
                            break;
                        default:
                            childLeft = parentLeft + lp.leftMargin;
                    }

                    switch (verticalGravity) {
                        case Gravity.TOP:
                            childTop = parentTop + lp.topMargin;
                            break;
                        case Gravity.CENTER_VERTICAL:
                            childTop = parentTop + (parentBottom - parentTop - height) / 2 +
                                    lp.topMargin - lp.bottomMargin;
                            break;
                        case Gravity.BOTTOM:
                            childTop = parentBottom - height - lp.bottomMargin;
                            break;
                        default:
                            childTop = parentTop + lp.topMargin;
                    }
                }

                child.layout(childLeft, childTop, childLeft + width, childTop + height);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mForegroundBoundsChanged = true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void dispatchDraw(GLCanvas canvas) {
    	super.dispatchDraw(canvas);

        if (mForeground != null) {
            final Drawable foreground = mForeground;

            if (mForegroundBoundsChanged) {
                mForegroundBoundsChanged = false;
                final Rect selfBounds = mSelfBounds;
                final Rect overlayBounds = mOverlayBounds;

				final int w = mRight - mLeft;
				final int h = mBottom - mTop;

                if (mForegroundInPadding) {
                    selfBounds.set(0, 0, w, h);
                } else {
                    selfBounds.set(mPaddingLeft, mPaddingTop, w - mPaddingRight, h - mPaddingBottom);
                }

                Gravity.apply(mForegroundGravity, foreground.getIntrinsicWidth(),
                        foreground.getIntrinsicHeight(), selfBounds, overlayBounds);
                foreground.setBounds(overlayBounds);
            }

//            foreground.draw(canvas);
            canvas.drawDrawable(foreground);
        }
    }

    //XXX
//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    public boolean gatherTransparentRegion(Region region) {
//        boolean opaque = super.gatherTransparentRegion(region);
//        if (region != null && mForeground != null) {
//            applyDrawableToTransparentRegion(mForeground, region);
//        }
//        return opaque;
//    }

    /**
     * Determines whether to measure all children or just those in
     * the VISIBLE or INVISIBLE state when measuring. Defaults to false.
     * @param measureAll true to consider children marked GONE, false otherwise.
     * Default value is false.
     *
     * @attr ref android.R.styleable#FrameLayout_measureAllChildren
     */
//    @android.view.RemotableViewMethod
    public void setMeasureAllChildren(boolean measureAll) {
        mMeasureAllChildren = measureAll;
    }

    /**
     * Determines whether to measure all children or just those in
     * the VISIBLE or INVISIBLE state when measuring.
     */
    public boolean getConsiderGoneChildrenWhenMeasuring() {
        return mMeasureAllChildren;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new FrameLayout.LayoutParams(getContext(), attrs);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof FrameLayout.LayoutParams;
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new FrameLayout.LayoutParams(p);
    }
}
