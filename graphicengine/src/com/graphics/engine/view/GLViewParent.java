package com.graphics.engine.view;

import android.graphics.Rect;
import android.view.ContextMenu;
import android.view.ViewGroup;
import android.view.ViewManager;

/**
 * 
 * <br>类描述: GLView的父容器接口
 * <br>功能详细描述:
 * 
 */
public interface GLViewParent {
    /**
     * Called when something has changed which has invalidated the layout of a
     * child of this view parent. This will schedule a layout pass of the view
     * tree.
     */
    public void requestLayout();

    /**
     * Indicates whether layout was requested on this view parent.
     *
     * @return true if layout was requested, false otherwise
     */
    public boolean isLayoutRequested();
    
    /**
     * All or part of a child is dirty and needs to be redrawn.
     * 
     * @param child The child which is dirty
     * @param r The area within the child that is invalid
     */
    public void invalidateChild(GLView child, Rect r);

    /**
     * All or part of a child is dirty and needs to be redrawn.
     *
     * The location array is an array of two int values which respectively
     * define the left and the top position of the dirty child.
     *
     * This method must return the parent of this ViewParent if the specified
     * rectangle must be invalidated in the parent. If the specified rectangle
     * does not require invalidation in the parent or if the parent does not
     * exist, this method must return null.
     *
     * When this method returns a non-null value, the location array must
     * have been updated with the left and top coordinates of this ViewParent.
     *
     * @param location An array of 2 ints containing the left and top
     *        coordinates of the child to invalidate
     * @param r The area within the child that is invalid
     *
     * @return the parent of this ViewParent or null
     */
    public GLViewParent invalidateChildInParent(int[] location, Rect r);
    
    /**
     * Returns the parent if it exists, or null.
     *
     * @return a ViewParent or null if this ViewParent does not have a parent
     */
    public GLViewParent getGLParent();

    /**
     * Called when a child of this parent wants focus
     * 
     * @param child The child of this ViewParent that wants focus. This view
     *        will contain the focused view. It is not necessarily the view that
     *        actually has focus.
     * @param focused The view that is a descendant of child that actually has
     *        focus
     */
    public void requestChildFocus(GLView child, GLView focused);
    
    /**
     * Called when a child of this parent is giving up focus
     * 
     * @param child The view that is giving up focus
     */
    public void clearChildFocus(GLView child);
    
    public boolean getChildVisibleRect(GLView child, Rect r, android.graphics.Point offset);
    
    /**
     * Find the nearest view in the specified direction that wants to take focus
     * 
     * @param v The view that currently has focus
     * @param direction One of FOCUS_UP, FOCUS_DOWN, FOCUS_LEFT, and FOCUS_RIGHT
     */
    public GLView focusSearch(GLView v, int direction);

    /**
     * Change the z order of the child so it's on top of all other children
     * 
     * @param child
     */
    public void bringChildToFront(GLView child);
    
    /**
     * Tells the parent that a new focusable view has become available. This is
     * to handle transitions from the case where there are no focusable views to
     * the case where the first focusable view appears.
     * 
     * @param v The view that has become newly focusable
     */
    public void focusableViewAvailable(GLView v);
    
    /**
     * Bring up a context menu for the specified view or its ancestors.
     * <p>
     * In most cases, a subclass does not need to override this.  However, if
     * the subclass is added directly to the window manager (for example,
     * {@link ViewManager#addView(View, android.view.ViewGroup.LayoutParams)})
     * then it should override this and show the context menu.
     * 
     * @param originalView The source view where the context menu was first invoked
     * @return true if a context menu was displayed
     */
    public boolean showContextMenuForChild(GLView originalView);

    /**
     * Have the parent populate the specified context menu if it has anything to
     * add (and then recurse on its parent).
     * 
     * @param menu The menu to populate
     */
    public void createContextMenu(ContextMenu menu);

    /**
     * This method is called on the parent when a child's drawable state
     * has changed.
     *
     * @param child The child whose drawable state has changed.
     */
    public void childDrawableStateChanged(GLView child);
    
    /**
     * Called when a child does not want this parent and its ancestors to
     * intercept touch events with
     * {@link ViewGroup#onInterceptTouchEvent(MotionEvent)}.
     * <p>
     * This parent should pass this call onto its parents. This parent must obey
     * this request for the duration of the touch (that is, only clear the flag
     * after this parent has received an up or a cancel.
     * 
     * @param disallowIntercept True if the child does not want the parent to
     *            intercept touch events.
     */
    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept);
    
    /**
     * Called when a child of this group wants a particular rectangle to be
     * positioned onto the screen.  {@link ViewGroup}s overriding this can trust
     * that:
     * <ul>
     *   <li>child will be a direct child of this group</li>
     *   <li>rectangle will be in the child's coordinates</li>
     * </ul>
     *
     * <p>{@link ViewGroup}s overriding this should uphold the contract:</p>
     * <ul>
     *   <li>nothing will change if the rectangle is already visible</li>
     *   <li>the view port will be scrolled only just enough to make the
     *       rectangle visible</li>
     * <ul>
     *
     * @param child The direct child making the request.
     * @param rectangle The rectangle in the child's coordinates the child
     *        wishes to be on the screen.
     * @param immediate True to forbid animated or delayed scrolling,
     *        false otherwise
     * @return Whether the group scrolled to handle the operation
     */
    public boolean requestChildRectangleOnScreen(GLView child, Rect rectangle,
            boolean immediate);
}
