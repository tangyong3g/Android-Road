/*
 * Copyright (C) 2006 The Android Open Source Project
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

package com.graphics.engine.gl.widget;

import android.content.Context;
import android.database.DataSetObserver;
import android.os.Handler;
import android.os.Parcelable;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.ContextMenu;
import android.view.SoundEffectConstants;
import android.view.ViewDebug;
import android.view.ViewGroup.LayoutParams;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Adapter;
import android.widget.Gallery;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.Spinner;

import com.graphics.engine.gl.view.GLView;
import com.graphics.engine.gl.view.GLViewGroup;


/**
 * An GLAdapterView is a view whose children are determined by an {@link Adapter}.
 *
 * <p>
 * See {@link ListView}, {@link GridView}, {@link Spinner} and
 *      {@link Gallery} for commonly used subclasses of GLAdapterView.
 * @param <T>
 */
public abstract class GLAdapterView<T extends GLAdapter> extends GLViewGroup {

    /**
     * The item view type returned by {@link Adapter#getItemViewType(int)} when
     * the adapter does not want the item's view recycled.
     */
    public static final int ITEM_VIEW_TYPE_IGNORE = -1;

    /**
     * The item view type returned by {@link Adapter#getItemViewType(int)} when
     * the item is a header or footer.
     */
    public static final int ITEM_VIEW_TYPE_HEADER_OR_FOOTER = -2;

    /**
     * The position of the first child displayed
     */
    @ViewDebug.ExportedProperty
    int mFirstPosition = 0;

    /**
     * The offset in pixels from the top of the GLAdapterView to the top
     * of the view to select during the next layout.
     */
    int mSpecificTop;

    /**
     * Position from which to start looking for mSyncRowId
     */
    int mSyncPosition;

    /**
     * Row id to look for when data has changed
     */
    long mSyncRowId = INVALID_ROW_ID;

    /**
     * Height of the view when mSyncPosition and mSyncRowId where set
     */
    long mSyncHeight;

    /**
     * True if we need to sync to mSyncRowId
     */
    boolean mNeedSync = false;

    /**
     * Indicates whether to sync based on the selection or position. Possible
     * values are {@link #SYNC_SELECTED_POSITION} or
     * {@link #SYNC_FIRST_POSITION}.
     */
    int mSyncMode;

    /**
     * Our height after the last layout
     */
    private int mLayoutHeight;

    /**
     * Sync based on the selected child
     */
    static final int SYNC_SELECTED_POSITION = 0;

    /**
     * Sync based on the first child displayed
     */
    static final int SYNC_FIRST_POSITION = 1;

    /**
     * Maximum amount of time to spend in {@link #findSyncPosition()}
     */
    static final int SYNC_MAX_DURATION_MILLIS = 100;

    /**
     * Indicates that this view is currently being laid out.
     */
    boolean mInLayout = false;

    /**
     * The listener that receives notifications when an item is selected.
     */
    OnItemSelectedListener mOnItemSelectedListener;

    /**
     * The listener that receives notifications when an item is clicked.
     */
    OnItemClickListener mOnItemClickListener;

    /**
     * The listener that receives notifications when an item is long clicked.
     */
    OnItemLongClickListener mOnItemLongClickListener;

    /**
     * True if the data has changed since the last layout
     */
    protected boolean mDataChanged;

    /**
     * The position within the adapter's data set of the item to select
     * during the next layout.
     */
    @ViewDebug.ExportedProperty
    int mNextSelectedPosition = INVALID_POSITION;

    /**
     * The item id of the item to select during the next layout.
     */
    long mNextSelectedRowId = INVALID_ROW_ID;

    /**
     * The position within the adapter's data set of the currently selected item.
     */
    @ViewDebug.ExportedProperty
    protected int mSelectedPosition = INVALID_POSITION;

    /**
     * The item id of the currently selected item.
     */
    long mSelectedRowId = INVALID_ROW_ID;

    /**
     * View to show if there are no items to show.
     */
    private GLView mEmptyView;

    /**
     * The number of items in the current adapter.
     */
    @ViewDebug.ExportedProperty
    int mItemCount;

    /**
     * The number of items in the adapter before a data changed event occured.
     */
    protected int mOldItemCount;

    /**
     * Represents an invalid position. All valid positions are in the range 0 to 1 less than the
     * number of items in the current adapter.
     */
    public static final int INVALID_POSITION = -1;

    /**
     * Represents an empty or invalid row id
     */
    public static final long INVALID_ROW_ID = Long.MIN_VALUE;

    /**
     * The last selected position we used when notifying
     */
    int mOldSelectedPosition = INVALID_POSITION;
    
    /**
     * The id of the last selected position we used when notifying
     */
    long mOldSelectedRowId = INVALID_ROW_ID;

    /**
     * Indicates what focusable state is requested when calling setFocusable().
     * In addition to this, this view has other criteria for actually
     * determining the focusable state (such as whether its empty or the text
     * filter is shown).
     *
     * @see #setFocusable(boolean)
     * @see #checkFocus()
     */
    private boolean mDesiredFocusableState;
    private boolean mDesiredFocusableInTouchModeState;

    private SelectionNotifier mSelectionNotifier;
    /**
     * When set to true, calls to requestLayout() will not propagate up the parent hierarchy.
     * This is used to layout the children during a layout pass.
     */
    boolean mBlockLayoutRequests = false;

    public GLAdapterView(Context context) {
        super(context);
    }

    public GLAdapterView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GLAdapterView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    /**
     * Interface definition for a callback to be invoked when an item in this
     * GLAdapterView has been clicked.
     */
    public interface OnItemClickListener {

        /**
         * Callback method to be invoked when an item in this GLAdapterView has
         * been clicked.
         * <p>
         * Implementers can call getItemAtPosition(position) if they need
         * to access the data associated with the selected item.
         *
         * @param parent The GLAdapterView where the click happened.
         * @param view The view within the GLAdapterView that was clicked (this
         *            will be a view provided by the adapter)
         * @param position The position of the view in the adapter.
         * @param id The row id of the item that was clicked.
         */
        void onItemClick(GLAdapterView<?> parent, GLView view, int position, long id);
    }

    /**
     * Register a callback to be invoked when an item in this GLAdapterView has
     * been clicked.
     *
     * @param listener The callback that will be invoked.
     */
    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    /**
     * @return The callback to be invoked with an item in this GLAdapterView has
     *         been clicked, or null id no callback has been set.
     */
    public final OnItemClickListener getOnItemClickListener() {
        return mOnItemClickListener;
    }

    /**
     * Call the OnItemClickListener, if it is defined.
     *
     * @param view The view within the GLAdapterView that was clicked.
     * @param position The position of the view in the adapter.
     * @param id The row id of the item that was clicked.
     * @return True if there was an assigned OnItemClickListener that was
     *         called, false otherwise is returned.
     */
    public boolean performItemClick(GLView view, int position, long id) {
        if (mOnItemClickListener != null) {
            playSoundEffect(SoundEffectConstants.CLICK);
            mOnItemClickListener.onItemClick(this, view, position, id);
            return true;
        }

        return false;
    }

    /**
     * Interface definition for a callback to be invoked when an item in this
     * view has been clicked and held.
     */
    public interface OnItemLongClickListener {
        /**
         * Callback method to be invoked when an item in this view has been
         * clicked and held.
         *
         * Implementers can call getItemAtPosition(position) if they need to access
         * the data associated with the selected item.
         *
         * @param parent The AbsListView where the click happened
         * @param view The view within the AbsListView that was clicked
         * @param position The position of the view in the list
         * @param id The row id of the item that was clicked
         *
         * @return true if the callback consumed the long click, false otherwise
         */
        boolean onItemLongClick(GLAdapterView<?> parent, GLView view, int position, long id);
    }


    /**
     * Register a callback to be invoked when an item in this GLAdapterView has
     * been clicked and held
     *
     * @param listener The callback that will run
     */
    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        if (!isLongClickable()) {
            setLongClickable(true);
        }
        mOnItemLongClickListener = listener;
    }

    /**
     * @return The callback to be invoked with an item in this GLAdapterView has
     *         been clicked and held, or null id no callback as been set.
     */
    public final OnItemLongClickListener getOnItemLongClickListener() {
        return mOnItemLongClickListener;
    }

    /**
     * Interface definition for a callback to be invoked when
     * an item in this view has been selected.
     */
    public interface OnItemSelectedListener {
        /**
         * Callback method to be invoked when an item in this view has been
         * selected.
         *
         * Impelmenters can call getItemAtPosition(position) if they need to access the
         * data associated with the selected item.
         *
         * @param parent The GLAdapterView where the selection happened
         * @param view The view within the GLAdapterView that was clicked
         * @param position The position of the view in the adapter
         * @param id The row id of the item that is selected
         */
        void onItemSelected(GLAdapterView<?> parent, GLView view, int position, long id);

        /**
         * Callback method to be invoked when the selection disappears from this
         * view. The selection can disappear for instance when touch is activated
         * or when the adapter becomes empty.
         *
         * @param parent The GLAdapterView that now contains no selected item.
         */
        void onNothingSelected(GLAdapterView<?> parent);
    }


    /**
     * Register a callback to be invoked when an item in this GLAdapterView has
     * been selected.
     *
     * @param listener The callback that will run
     */
    public void setOnItemSelectedListener(OnItemSelectedListener listener) {
        mOnItemSelectedListener = listener;
    }

    public final OnItemSelectedListener getOnItemSelectedListener() {
        return mOnItemSelectedListener;
    }

    /**
     * Extra menu information provided to the
     * {@link android.view.View.OnCreateContextMenuListener#onCreateContextMenu(ContextMenu, View, ContextMenuInfo) }
     * callback when a context menu is brought up for this GLAdapterView.
     *
     */
    public static class AdapterContextMenuInfo implements ContextMenu.ContextMenuInfo {

        public AdapterContextMenuInfo(GLView targetView, int position, long id) {
            this.targetView = targetView;
            this.position = position;
            this.id = id;
        }

        /**
         * The child view for which the context menu is being displayed. This
         * will be one of the children of this GLAdapterView.
         */
        public GLView targetView;

        /**
         * The position in the adapter for which the context menu is being
         * displayed.
         */
        public int position;

        /**
         * The row id of the item for which the context menu is being displayed.
         */
        public long id;
    }

    /**
     * Returns the adapter currently associated with this widget.
     *
     * @return The adapter used to provide this GLView's content.
     */
    public abstract T getAdapter();

    /**
     * Sets the adapter that provides the data and the views to represent the data
     * in this widget.
     *
     * @param adapter The adapter to use to create this GLView's content.
     */
    public abstract void setAdapter(T adapter);

    /**
     * This method is not supported and throws an UnsupportedOperationException when called.
     *
     * @param child Ignored.
     *
     * @throws UnsupportedOperationException Every time this method is invoked.
     */
    @Override
    public void addView(GLView child) {
        throw new UnsupportedOperationException("addView(GLView) is not supported in GLAdapterView");
    }

    /**
     * This method is not supported and throws an UnsupportedOperationException when called.
     *
     * @param child Ignored.
     * @param index Ignored.
     *
     * @throws UnsupportedOperationException Every time this method is invoked.
     */
    @Override
    public void addView(GLView child, int index) {
        throw new UnsupportedOperationException("addView(GLView, int) is not supported in GLAdapterView");
    }

    /**
     * This method is not supported and throws an UnsupportedOperationException when called.
     *
     * @param child Ignored.
     * @param params Ignored.
     *
     * @throws UnsupportedOperationException Every time this method is invoked.
     */
    @Override
    public void addView(GLView child, LayoutParams params) {
        throw new UnsupportedOperationException("addView(GLView, LayoutParams) "
                + "is not supported in GLAdapterView");
    }

    /**
     * This method is not supported and throws an UnsupportedOperationException when called.
     *
     * @param child Ignored.
     * @param index Ignored.
     * @param params Ignored.
     *
     * @throws UnsupportedOperationException Every time this method is invoked.
     */
    @Override
    public void addView(GLView child, int index, LayoutParams params) {
        throw new UnsupportedOperationException("addView(GLView, int, LayoutParams) "
                + "is not supported in GLAdapterView");
    }

    /**
     * This method is not supported and throws an UnsupportedOperationException when called.
     *
     * @param child Ignored.
     *
     * @throws UnsupportedOperationException Every time this method is invoked.
     */
    @Override
    public void removeView(GLView child) {
        throw new UnsupportedOperationException("removeView(GLView) is not supported in GLAdapterView");
    }

    /**
     * This method is not supported and throws an UnsupportedOperationException when called.
     *
     * @param index Ignored.
     *
     * @throws UnsupportedOperationException Every time this method is invoked.
     */
    @Override
    public void removeViewAt(int index) {
        throw new UnsupportedOperationException("removeViewAt(int) is not supported in GLAdapterView");
    }

    /**
     * This method is not supported and throws an UnsupportedOperationException when called.
     *
     * @throws UnsupportedOperationException Every time this method is invoked.
     */
    @Override
    public void removeAllViews() {
        throw new UnsupportedOperationException("removeAllViews() is not supported in GLAdapterView");
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        mLayoutHeight = getHeight();
    }

    /**
     * Return the position of the currently selected item within the adapter's data set
     *
     * @return int Position (starting at 0), or {@link #INVALID_POSITION} if there is nothing selected.
     */
    @ViewDebug.CapturedViewProperty
    public int getSelectedItemPosition() {
        return mNextSelectedPosition;
    }

    /**
     * @return The id corresponding to the currently selected item, or {@link #INVALID_ROW_ID}
     * if nothing is selected.
     */
    @ViewDebug.CapturedViewProperty
    public long getSelectedItemId() {
        return mNextSelectedRowId;
    }

    /**
     * @return The GLView corresponding to the currently selected item, or null
     * if nothing is selected
     */
    public abstract GLView getSelectedView();

    /**
     * @return The data corresponding to the currently selected item, or
     * null if there is nothing selected.
     */
    public Object getSelectedItem() {
        T adapter = getAdapter();
        int selection = getSelectedItemPosition();
        if (adapter != null && adapter.getCount() > 0 && selection >= 0) {
            return adapter.getItem(selection);
        } else {
            return null;
        }
    }

    /**
     * @return The number of items owned by the Adapter associated with this
     *         GLAdapterView. (This is the number of data items, which may be
     *         larger than the number of visible GLView.)
     */
    @ViewDebug.CapturedViewProperty
    public int getCount() {
        return mItemCount;
    }

    /**
     * Get the position within the adapter's data set for the GLView, where GLView is a an adapter item
     * or a descendant of an adapter item.
     *
     * @param GLView an adapter item, or a descendant of an adapter item. This must be visible in this
     *        GLAdapterView at the time of the call.
     * @return the position within the adapter's data set of the GLView, or {@link #INVALID_POSITION}
     *         if the GLView does not correspond to a list item (or it is not currently visible).
     */
    public int getPositionForView(GLView view) {
        GLView listItem = view;
        try {
            GLView v;
            while (!(v = (GLView) listItem.getGLParent()).equals(this)) {
                listItem = v;
            }
        } catch (ClassCastException e) {
            // We made it up to the window without find this list GLView
            return INVALID_POSITION;
        }

        // Search the children for the list item
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            if (getChildAt(i).equals(listItem)) {
                return mFirstPosition + i;
            }
        }

        // Child not found!
        return INVALID_POSITION;
    }

    /**
     * Returns the position within the adapter's data set for the first item
     * displayed on screen.
     *
     * @return The position within the adapter's data set
     */
    public int getFirstVisiblePosition() {
        return mFirstPosition;
    }

    /**
     * Returns the position within the adapter's data set for the last item
     * displayed on screen.
     *
     * @return The position within the adapter's data set
     */
    public int getLastVisiblePosition() {
        return mFirstPosition + getChildCount() - 1;
    }

    /**
     * Sets the currently selected item. To support accessibility subclasses that
     * override this method must invoke the overriden super method first.
     *
     * @param position Index (starting at 0) of the data item to be selected.
     */
    public abstract void setSelection(int position);

    /**
     * Sets the GLView to show if the adapter is empty
     */
    public void setEmptyView(GLView emptyView) {
        mEmptyView = emptyView;

        final T adapter = getAdapter();
		final boolean empty = (adapter == null) || adapter.isEmpty();
        updateEmptyStatus(empty);
    }

    /**
     * When the current adapter is empty, the GLAdapterView can display a special GLView
     * call the empty GLView. The empty GLView is used to provide feedback to the user
     * that no data is available in this GLAdapterView.
     *
     * @return The GLView to show if the adapter is empty.
     */
    public GLView getEmptyView() {
        return mEmptyView;
    }

    /**
     * Indicates whether this GLView is in filter mode. Filter mode can for instance
     * be enabled by a user when typing on the keyboard.
     *
     * @return True if the GLView is in filter mode, false otherwise.
     */
    boolean isInFilterMode() {
        return false;
    }

    @Override
    public void setFocusable(boolean focusable) {
        final T adapter = getAdapter();
        final boolean empty = adapter == null || adapter.getCount() == 0;

        mDesiredFocusableState = focusable;
        if (!focusable) {
            mDesiredFocusableInTouchModeState = false;
        }

        super.setFocusable(focusable && (!empty || isInFilterMode()));
    }

    @Override
    public void setFocusableInTouchMode(boolean focusable) {
        final T adapter = getAdapter();
        final boolean empty = adapter == null || adapter.getCount() == 0;

        mDesiredFocusableInTouchModeState = focusable;
        if (focusable) {
            mDesiredFocusableState = true;
        }

        super.setFocusableInTouchMode(focusable && (!empty || isInFilterMode()));
    }

    void checkFocus() {
        final T adapter = getAdapter();
        final boolean empty = adapter == null || adapter.getCount() == 0;
        final boolean focusable = !empty || isInFilterMode();
        // The order in which we set focusable in touch mode/focusable may matter
        // for the client, see GLView.setFocusableInTouchMode() comments for more
        // details
        super.setFocusableInTouchMode(focusable && mDesiredFocusableInTouchModeState);
        super.setFocusable(focusable && mDesiredFocusableState);
        if (mEmptyView != null) {
            updateEmptyStatus((adapter == null) || adapter.isEmpty());
        }
    }

    /**
     * Update the status of the list based on the empty parameter.  If empty is true and
     * we have an empty GLView, display it.  In all the other cases, make sure that the listview
     * is VISIBLE and that the empty GLView is GONE (if it's not null).
     */
    private void updateEmptyStatus(boolean empty) {
        if (isInFilterMode()) {
            empty = false;
        }

        if (empty) {
            if (mEmptyView != null) {
                mEmptyView.setVisibility(GLView.VISIBLE);
                setVisibility(GLView.GONE);
            } else {
                // If the caller just removed our empty GLView, make sure the list GLView is visible
                setVisibility(GLView.VISIBLE);
            }

            // We are now GONE, so pending layouts will not be dispatched.
            // Force one here to make sure that the state of the list matches
            // the state of the adapter.
            if (mDataChanged) {           
                this.onLayout(false, mLeft, mTop, mRight, mBottom); 
            }
        } else {
			if (mEmptyView != null) {
				mEmptyView.setVisibility(GLView.GONE);
			}
            setVisibility(GLView.VISIBLE);
        }
    }

    /**
     * Gets the data associated with the specified position in the list.
     *
     * @param position Which data to get
     * @return The data associated with the specified position in the list
     */
    public Object getItemAtPosition(int position) {
        T adapter = getAdapter();
        return (adapter == null || position < 0) ? null : adapter.getItem(position);
    }

    public long getItemIdAtPosition(int position) {
        T adapter = getAdapter();
        return (adapter == null || position < 0) ? INVALID_ROW_ID : adapter.getItemId(position);
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        throw new RuntimeException("Don't call setOnClickListener for an GLAdapterView. "
                + "You probably want setOnItemClickListener instead");
    }

	class AdapterDataSetObserver extends DataSetObserver { // CHECKSTYLE IGNORE

        private Parcelable mInstanceState = null;

        @Override
        public void onChanged() {
            mDataChanged = true;
            mOldItemCount = mItemCount;
            mItemCount = getAdapter().getCount();

            // Detect the case where a cursor that was previously invalidated has
            // been repopulated with new data.
            if (GLAdapterView.this.getAdapter().hasStableIds() && mInstanceState != null
                    && mOldItemCount == 0 && mItemCount > 0) {
                GLAdapterView.this.onRestoreInstanceState(mInstanceState);
                mInstanceState = null;
            } else {
                rememberSyncState();
            }
            checkFocus();
            requestLayout();
        }

        @Override
        public void onInvalidated() {
            mDataChanged = true;

            if (GLAdapterView.this.getAdapter().hasStableIds()) {
                // Remember the current state for the case where our hosting activity is being
                // stopped and later restarted
                mInstanceState = GLAdapterView.this.onSaveInstanceState();
            }

            // Data is invalid so we should reset our state
            mOldItemCount = mItemCount;
            mItemCount = 0;
            mSelectedPosition = INVALID_POSITION;
            mSelectedRowId = INVALID_ROW_ID;
            mNextSelectedPosition = INVALID_POSITION;
            mNextSelectedRowId = INVALID_ROW_ID;
            mNeedSync = false;
            checkSelectionChanged();

            checkFocus();
            requestLayout();
        }

        public void clearSavedState() {
            mInstanceState = null;
        }
    }

    private class SelectionNotifier extends Handler implements Runnable { // CHECKSTYLE IGNORE
        public void run() {
            if (mDataChanged) {
                // Data has changed between when this SelectionNotifier
                // was posted and now. We need to wait until the GLAdapterView
                // has been synched to the new data.
                post(this);
            } else {
                fireOnSelected();
            }
        }
    }

    void selectionChanged() {
        if (mOnItemSelectedListener != null) {
            if (mInLayout || mBlockLayoutRequests) {
                // If we are in a layout traversal, defer notification
                // by posting. This ensures that the GLView tree is
                // in a consistent state and is able to accomodate
                // new layout or invalidate requests.
                if (mSelectionNotifier == null) {
                    mSelectionNotifier = new SelectionNotifier();
                }
                mSelectionNotifier.post(mSelectionNotifier);
            } else {
                fireOnSelected();
            }
        }

        // we fire selection events here not in GLView
        //TODO:change to GLListView
        if (mSelectedPosition != ListView.INVALID_POSITION && isShown() && !isInTouchMode()) {
            sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_SELECTED);
        }
    }

    private void fireOnSelected() {
        if (mOnItemSelectedListener == null) {
            return;
        }

        int selection = this.getSelectedItemPosition();
        if (selection >= 0) {
            GLView v = getSelectedView();
            mOnItemSelectedListener.onItemSelected(this, v, selection,
                    getAdapter().getItemId(selection));
        } else {
            mOnItemSelectedListener.onNothingSelected(this);
        }
    }

    @Override
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        boolean populated = false;
        // This is an exceptional case which occurs when a window gets the
        // focus and sends a focus event via its focused child to announce
        // current focus/selection. GLAdapterView fires selection but not focus
        // events so we change the event type here.
        if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_FOCUSED) {
            event.setEventType(AccessibilityEvent.TYPE_VIEW_SELECTED);
        }

        // we send selection events only from GLAdapterView to avoid
        // generation of such event for each child
        GLView selectedView = getSelectedView();
        if (selectedView != null) {
            populated = selectedView.dispatchPopulateAccessibilityEvent(event);
        }

        if (!populated) {
            if (selectedView != null) {
                event.setEnabled(selectedView.isEnabled());
            }
            event.setItemCount(getCount());
            event.setCurrentItemIndex(getSelectedItemPosition());
        }

        return populated;
    }
    
    @Override
    protected boolean canAnimate() {
        return super.canAnimate() && mItemCount > 0;
    }

    void handleDataChanged() {
        final int count = mItemCount;
        boolean found = false;

        if (count > 0) {

            int newPos;

            // Find the row we are supposed to sync to
            if (mNeedSync) {
                // Update this first, since setNextSelectedPositionInt inspects
                // it
                mNeedSync = false;

                // See if we can find a position in the new data with the same
                // id as the old selection
                newPos = findSyncPosition();
                if (newPos >= 0) {
                    // Verify that new selection is selectable
                    int selectablePos = lookForSelectablePosition(newPos, true);
                    if (selectablePos == newPos) {
                        // Same row id is selected
                        setNextSelectedPositionInt(newPos);
                        found = true;
                    }
                }
            }
            if (!found) {
                // Try to use the same position if we can't find matching data
                newPos = getSelectedItemPosition();

                // Pin position to the available range
                if (newPos >= count) {
                    newPos = count - 1;
                }
                if (newPos < 0) {
                    newPos = 0;
                }

                // Make sure we select something selectable -- first look down
                int selectablePos = lookForSelectablePosition(newPos, true);
                if (selectablePos < 0) {
                    // Looking down didn't work -- try looking up
                    selectablePos = lookForSelectablePosition(newPos, false);
                }
                if (selectablePos >= 0) {
                    setNextSelectedPositionInt(selectablePos);
                    checkSelectionChanged();
                    found = true;
                }
            }
        }
        if (!found) {
            // Nothing is selected
            mSelectedPosition = INVALID_POSITION;
            mSelectedRowId = INVALID_ROW_ID;
            mNextSelectedPosition = INVALID_POSITION;
            mNextSelectedRowId = INVALID_ROW_ID;
            mNeedSync = false;
            checkSelectionChanged();
        }
    }

    void checkSelectionChanged() {
        if ((mSelectedPosition != mOldSelectedPosition) || (mSelectedRowId != mOldSelectedRowId)) {
            selectionChanged();
            mOldSelectedPosition = mSelectedPosition;
            mOldSelectedRowId = mSelectedRowId;
        }
    }

    /**
     * Searches the adapter for a position matching mSyncRowId. The search starts at mSyncPosition
     * and then alternates between moving up and moving down until 1) we find the right position, or
     * 2) we run out of time, or 3) we have looked at every position
     *
     * @return Position of the row that matches mSyncRowId, or {@link #INVALID_POSITION} if it can't
     *         be found
     */
    int findSyncPosition() {
        int count = mItemCount;

        if (count == 0) {
            return INVALID_POSITION;
        }

        long idToMatch = mSyncRowId;
        int seed = mSyncPosition;

        // If there isn't a selection don't hunt for it
        if (idToMatch == INVALID_ROW_ID) {
            return INVALID_POSITION;
        }

        // Pin seed to reasonable values
        seed = Math.max(0, seed);
        seed = Math.min(count - 1, seed);

        long endTime = SystemClock.uptimeMillis() + SYNC_MAX_DURATION_MILLIS;

        long rowId;

        // first position scanned so far
        int first = seed;

        // last position scanned so far
        int last = seed;

        // True if we should move down on the next iteration
        boolean next = false;

        // True when we have looked at the first item in the data
        boolean hitFirst;

        // True when we have looked at the last item in the data
        boolean hitLast;

        // Get the item ID locally (instead of getItemIdAtPosition), so
        // we need the adapter
        T adapter = getAdapter();
        if (adapter == null) {
            return INVALID_POSITION;
        }

        while (SystemClock.uptimeMillis() <= endTime) {
            rowId = adapter.getItemId(seed);
            if (rowId == idToMatch) {
                // Found it!
                return seed;
            }

            hitLast = last == count - 1;
            hitFirst = first == 0;

            if (hitLast && hitFirst) {
                // Looked at everything
                break;
            }

            if (hitFirst || (next && !hitLast)) {
                // Either we hit the top, or we are trying to move down
                last++;
                seed = last;
                // Try going up next time
                next = false;
            } else if (hitLast || (!next && !hitFirst)) {
                // Either we hit the bottom, or we are trying to move up
                first--;
                seed = first;
                // Try going down next time
                next = true;
            }

        }

        return INVALID_POSITION;
    }

    /**
     * Find a position that can be selected (i.e., is not a separator).
     *
     * @param position The starting position to look at.
     * @param lookDown Whether to look down for other positions.
     * @return The next selectable position starting at position and then searching either up or
     *         down. Returns {@link #INVALID_POSITION} if nothing can be found.
     */
    int lookForSelectablePosition(int position, boolean lookDown) {
        return position;
    }

    /**
     * Utility to keep mSelectedPosition and mSelectedRowId in sync
     * @param position Our current position
     */
    void setSelectedPositionInt(int position) {
        mSelectedPosition = position;
        mSelectedRowId = getItemIdAtPosition(position);
    }

    /**
     * Utility to keep mNextSelectedPosition and mNextSelectedRowId in sync
     * @param position Intended value for mSelectedPosition the next time we go
     * through layout
     */
    void setNextSelectedPositionInt(int position) {
        mNextSelectedPosition = position;
        mNextSelectedRowId = getItemIdAtPosition(position);
        // If we are trying to sync to the selection, update that too
        if (mNeedSync && mSyncMode == SYNC_SELECTED_POSITION && position >= 0) {
            mSyncPosition = position;
            mSyncRowId = mNextSelectedRowId;
        }
    }

    /**
     * Remember enough information to restore the screen state when the data has
     * changed.
     *
     */
    void rememberSyncState() {
        if (getChildCount() > 0) {
            mNeedSync = true;
            mSyncHeight = mLayoutHeight;
            if (mSelectedPosition >= 0) {
                // Sync the selection state
                GLView v = getChildAt(mSelectedPosition - mFirstPosition);
                mSyncRowId = mNextSelectedRowId;
                mSyncPosition = mNextSelectedPosition;
                if (v != null) {
                    mSpecificTop = v.getTop();
                }
                mSyncMode = SYNC_SELECTED_POSITION;
            } else {
                // Sync the based on the offset of the first GLView
                GLView v = getChildAt(0);
                T adapter = getAdapter();
                if (mFirstPosition >= 0 && mFirstPosition < adapter.getCount()) {
                    mSyncRowId = adapter.getItemId(mFirstPosition);
                } else {
                    mSyncRowId = NO_ID;
                }
                mSyncPosition = mFirstPosition;
                if (v != null) {
                    mSpecificTop = v.getTop();
                }
                mSyncMode = SYNC_FIRST_POSITION;
            }
        }
    }
}
