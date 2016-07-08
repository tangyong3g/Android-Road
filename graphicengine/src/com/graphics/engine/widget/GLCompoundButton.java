/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.graphics.engine.widget;

import android.R;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewDebug;

import com.graphics.engine.graphics.GLCanvas;

/**
 * <p>
 * A button with two states, checked and unchecked. When the button is pressed
 * or clicked, the state changes automatically.
 * </p>
 *
 * <p><strong>XML attributes</strong></p>
 * <p>
 * See {@link android.R.styleable#CompoundButton
 * CompoundButton Attributes}, {@link android.R.styleable#Button Button
 * Attributes}, {@link android.R.styleable#TextView TextView Attributes}, {@link
 * android.R.styleable#View View Attributes}
 * </p>
 */
//CHECKSTYLE:OFF
public abstract class GLCompoundButton extends GLButton implements GLCheckable {
    private boolean mChecked;
    private int mButtonResource;
    private boolean mBroadcasting;
    private Drawable mButtonDrawable;
    private OnCheckedChangeListener mOnCheckedChangeListener;
    private OnCheckedChangeListener mOnCheckedChangeWidgetListener;

    private static final int[] CHECKED_STATE_SET = {
        R.attr.state_checked
    };

    public GLCompoundButton(Context context) {
        this(context, null);
    }

    public GLCompoundButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GLCompoundButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

//        TypedArray a =
//                context.obtainStyledAttributes(
//                        attrs, com.android.internal.R.styleable.CompoundButton, defStyle, 0);
//
//        Drawable d = a.getDrawable(com.android.internal.R.styleable.CompoundButton_button);
//        if (d != null) {
//            setButtonDrawable(d);
//        }
//
//        boolean checked = a
//                .getBoolean(com.android.internal.R.styleable.CompoundButton_checked, false);
//        setChecked(checked);
//
//        a.recycle();
    }

    public void toggle() {
        setChecked(!mChecked);
    }

    @Override
    public boolean performClick() {
        /*
         * XXX: These are tiny, need some surrounding 'expanded touch area',
         * which will need to be implemented in Button if we only override
         * performClick()
         */

        /* When clicked, toggle the state */
        toggle();
        return super.performClick();
    }

    @ViewDebug.ExportedProperty
    public boolean isChecked() {
        return mChecked;
    }

    /**
     * <p>Changes the checked state of this button.</p>
     *
     * @param checked true to check the button, false to uncheck it
     */
    public void setChecked(boolean checked) {
        if (mChecked != checked) {
            mChecked = checked;
            refreshDrawableState();

            // Avoid infinite recursions if setChecked() is called from a listener
            if (mBroadcasting) {
                return;
            }

            mBroadcasting = true;
            if (mOnCheckedChangeListener != null) {
                mOnCheckedChangeListener.onCheckedChanged(this, mChecked);
            }
            if (mOnCheckedChangeWidgetListener != null) {
                mOnCheckedChangeWidgetListener.onCheckedChanged(this, mChecked);
            }

            mBroadcasting = false;            
        }
    }

    /**
     * Register a callback to be invoked when the checked state of this button
     * changes.
     *
     * @param listener the callback to call on checked state change
     */
    public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
        mOnCheckedChangeListener = listener;
    }

    /**
     * Register a callback to be invoked when the checked state of this button
     * changes. This callback is used for internal purpose only.
     *
     * @param listener the callback to call on checked state change
     * @hide
     */
    void setOnCheckedChangeWidgetListener(OnCheckedChangeListener listener) {
        mOnCheckedChangeWidgetListener = listener;
    }

    /**
     * Interface definition for a callback to be invoked when the checked state
     * of a compound button changed.
     */
    public static interface OnCheckedChangeListener {
        /**
         * Called when the checked state of a compound button has changed.
         *
         * @param buttonView The compound button view whose state has changed.
         * @param isChecked  The new checked state of buttonView.
         */
        void onCheckedChanged(GLCompoundButton buttonView, boolean isChecked);
    }

    /**
     * Set the background to a given Drawable, identified by its resource id.
     *
     * @param resid the resource id of the drawable to use as the background 
     */
    public void setButtonDrawable(int resid) {
        if (resid != 0 && resid == mButtonResource) {
            return;
        }

        mButtonResource = resid;

        Drawable d = null;
        if (mButtonResource != 0) {
            d = getResources().getDrawable(mButtonResource);
        }
        setButtonDrawable(d);
    }

    /**
     * Set the background to a given Drawable
     *
     * @param d The Drawable to use as the background
     */
    public void setButtonDrawable(Drawable d) {
        if (d != null) {
            if (mButtonDrawable != null) {
                mButtonDrawable.setCallback(null);
                unscheduleDrawable(mButtonDrawable);
            }
            d.setCallback(this);
            d.setState(getDrawableState());
            d.setVisible(getVisibility() == VISIBLE, false);
            mButtonDrawable = d;
            mButtonDrawable.setState(null);
//            setMinHeight(mButtonDrawable.getIntrinsicHeight());
            setMinimumWidth(mButtonDrawable.getIntrinsicWidth());
            setMinimumHeight(mButtonDrawable.getIntrinsicHeight());
        }

        refreshDrawableState();
    }

//    @Override
//    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
//        super.onInitializeAccessibilityEvent(event);
//        event.setChecked(mChecked);
//    }
//
//    @Override
//    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
//        super.onInitializeAccessibilityNodeInfo(info);
//        info.setCheckable(true);
//        info.setChecked(mChecked);
//    }

    @Override
    protected void onDraw(GLCanvas canvas) {
        super.onDraw(canvas);

        final Drawable buttonDrawable = mButtonDrawable;
        if (buttonDrawable != null) {
            final int verticalGravity = Gravity.CENTER_VERTICAL; // getGravity() & Gravity.VERTICAL_GRAVITY_MASK;
            final int height = buttonDrawable.getIntrinsicHeight();

            int y = 0;

            switch (verticalGravity) {
                case Gravity.BOTTOM:
                    y = getHeight() - height;
                    break;
                case Gravity.CENTER_VERTICAL:
                    y = (getHeight() - height) / 2;
                    break;
            }

            buttonDrawable.setBounds(0, y, buttonDrawable.getIntrinsicWidth(), y + height);
//            buttonDrawable.draw(canvas);
            canvas.drawDrawable(buttonDrawable);
        }
    }

    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if (isChecked()) {
            mergeDrawableStates(drawableState, CHECKED_STATE_SET);
        }
        return drawableState;
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        
        if (mButtonDrawable != null) {
            int[] myDrawableState = getDrawableState();
            
            // Set the state of the Drawable
            mButtonDrawable.setState(myDrawableState);
            
            invalidate();
        }
    }

    @Override
    protected boolean verifyDrawable(Drawable who) {
        return super.verifyDrawable(who) || who == mButtonDrawable;
    }

    @Override
    public Parcelable onSaveInstanceState() {
        super.onSaveInstanceState();
        return null;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        super.onRestoreInstanceState(null);
    }
}
