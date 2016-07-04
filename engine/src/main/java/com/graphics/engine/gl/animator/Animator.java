/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.graphics.engine.gl.animator;

import java.util.ArrayList;

import android.view.animation.Interpolator;

/**
 * <br>类描述: 动画基类
 * <br>功能详细描述: 
 * <br>比视图动画更具一般性，不只是改变视图的变换和透明度。
 * <br>提供开始，结束，取消动画以及设置启动延时，插值器，添加监听器{@link AnimatorListener}等等接口。
 * <br>一般来说，对于监听器，不需要重载每个接口，可以使用{@link AnimatorListenerAdapter}这个默认实现。
 * <br>使用{@link #setName(String)}方法可以设置动画名称以便调试。
 * 
 * @author  dengweiming
 * @date  [2013-10-17]
 */
public abstract class Animator implements Cloneable {
	public static final int INFINITE = -1;

    /**
     * The set of listeners to be sent events through the life of an animation.
     */
    ArrayList<AnimatorListener> mListeners = null;
    
    String mName = null;

    /**
     * Starts this animation. If the animation has a nonzero startDelay, the animation will start
     * running after that delay elapses. A non-delayed animation will have its initial
     * value(s) set immediately, followed by calls to
     * {@link AnimatorListener#onAnimationStart(Animator)} for any listeners of this animator.
     *
     * <p>The animation started by calling this method will be run on the thread that called
     * this method. This thread should have a Looper on it (a runtime exception will be thrown if
     * this is not the case). Also, if the animation will animate
     * properties of objects in the view hierarchy, then the calling thread should be the UI
     * thread for that view hierarchy.</p>
     *
     */
    public void start() {
    }

    /**
     * Cancels the animation. Unlike {@link #end()}, <code>cancel()</code> causes the animation to
     * stop in its tracks, sending an
     * {@link android.animation.Animator.AnimatorListener#onAnimationCancel(Animator)} to
     * its listeners, followed by an
     * {@link android.animation.Animator.AnimatorListener#onAnimationEnd(Animator)} message.
     *
     * <p>This method must be called on the thread that is running the animation.</p>
     */
    public void cancel() {
    }

    /**
     * Ends the animation. This causes the animation to assign the end value of the property being
     * animated, then calling the
     * {@link android.animation.Animator.AnimatorListener#onAnimationEnd(Animator)} method on
     * its listeners.
     *
     * <p>This method must be called on the thread that is running the animation.</p>
     */
    public void end() {
    }

    /**
     * The amount of time, in milliseconds, to delay starting the animation after
     * {@link #start()} is called.
     *
     * @return the number of milliseconds to delay running the animation
     */
    public abstract long getStartDelay();

    /**
     * The amount of time, in milliseconds, to delay starting the animation after
     * {@link #start()} is called.

     * @param startDelay The amount of the delay, in milliseconds
     */
    public abstract void setStartDelay(long startDelay);


    /**
     * Sets the length of the animation.
     *
     * @param duration The length of the animation, in milliseconds.
     */
    public abstract Animator setDuration(long duration);

    /**
     * Gets the length of the animation.
     *
     * @return The length of the animation, in milliseconds.
     */
    public abstract long getDuration();
    
    /**
     * Gets the total play time of the animation, includes start delay, and repeat time.
     *
     * @return The length of the animation, in milliseconds.
     */
    public long getTotalPlayTime() {
    	return getStartDelay() + getDuration();
    }

    /**
     * The time interpolator used in calculating the elapsed fraction of this animation. The
     * interpolator determines whether the animation runs with linear or non-linear motion,
     * such as acceleration and deceleration. The default value is
     * {@link android.view.animation.LinearInterpolator}
     *
     * @param value the interpolator to be used by this animation
     */
    public abstract void setInterpolator(Interpolator value);

    /**
     * Returns whether this Animator is currently running (having been started and gone past any
     * initial startDelay period and not yet ended).
     *
     * @return Whether the Animator is running.
     */
    public abstract boolean isRunning();

    /**
     * Returns whether this Animator has been started and not yet ended. This state is a superset
     * of the state of {@link #isRunning()}, because an Animator with a nonzero
     * {@link #getStartDelay() startDelay} will return true for {@link #isStarted()} during the
     * delay phase, whereas {@link #isRunning()} will return true only after the delay phase
     * is complete.
     *
     * @return Whether the Animator has been started and not yet ended.
     */
    public boolean isStarted() {
        // Default method returns value for isRunning(). Subclasses should override to return a
        // real value.
        return isRunning();
    }

    /**
     * Adds a listener to the set of listeners that are sent events through the life of an
     * animation, such as start, repeat, and end.
     *
     * @param listener the listener to be added to the current set of listeners for this animation.
     */
    public void addListener(AnimatorListener listener) {
        if (mListeners == null) {
            mListeners = new ArrayList<AnimatorListener>();
        }
        mListeners.add(listener);
    }

    /**
     * Removes a listener from the set listening to this animation.
     *
     * @param listener the listener to be removed from the current set of listeners for this
     *                 animation.
     */
    public void removeListener(AnimatorListener listener) {
        if (mListeners == null) {
            return;
        }
        mListeners.remove(listener);
        if (mListeners.size() == 0) {
            mListeners = null;
        }
    }

    /**
     * Gets the set of {@link android.animation.Animator.AnimatorListener} objects that are currently
     * listening for events on this <code>Animator</code> object.
     *
     * @return ArrayList<AnimatorListener> The set of listeners.
     */
    public ArrayList<AnimatorListener> getListeners() {
        return mListeners;
    }

    /**
     * Removes all listeners from this object. This is equivalent to calling
     * <code>getListeners()</code> followed by calling <code>clear()</code> on the
     * returned list of listeners.
     */
    public void removeAllListeners() {
        if (mListeners != null) {
            mListeners.clear();
            mListeners = null;
        }
    }

    @Override
    public Animator clone() {
        try {
            final Animator anim = (Animator) super.clone();
            if (mListeners != null) {
                ArrayList<AnimatorListener> oldListeners = mListeners;
                anim.mListeners = new ArrayList<AnimatorListener>();
                int numListeners = oldListeners.size();
                for (int i = 0; i < numListeners; ++i) {
                    anim.mListeners.add(oldListeners.get(i));
                }
            }
            return anim;
        } catch (CloneNotSupportedException e) {
           throw new AssertionError();
        }
    }

    /**
     * This method tells the object to use appropriate information to extract
     * starting values for the animation. For example, a AnimatorSet object will pass
     * this call to its child objects to tell them to set up the values. A
     * ObjectAnimator object will use the information it has about its target object
     * and PropertyValuesHolder objects to get the start values for its properties.
     * A ValueAnimator object will ignore the request since it does not have enough
     * information (such as a target object) to gather these values.
     */
    public void setupStartValues() {
    }

    /**
     * This method tells the object to use appropriate information to extract
     * ending values for the animation. For example, a AnimatorSet object will pass
     * this call to its child objects to tell them to set up the values. A
     * ObjectAnimator object will use the information it has about its target object
     * and PropertyValuesHolder objects to get the start values for its properties.
     * A ValueAnimator object will ignore the request since it does not have enough
     * information (such as a target object) to gather these values.
     */
    public void setupEndValues() {
    }

    /**
     * Sets the target object whose property will be animated by this animation. Not all subclasses
     * operate on target objects (for example, {@link ValueAnimator}, but this method
     * is on the superclass for the convenience of dealing generically with those subclasses
     * that do handle targets.
     *
     * @param target The object being animated
     */
    public void setTarget(Object target) {
    }
    
    public void cleanup() {
    	removeAllListeners();
    }
    
    /**
     * <br>功能简述: 设置动画的名称
     * <br>功能详细描述: toString()时会返回这个名称，方便调试。
     * <br>注意:
     * @param name
     */
    public void setName(String name) {
    	mName = "Animator:" + name;
    }
    
    @Override
    public String toString() {
		if (mName != null) {
			return mName;
		}
    	return super.toString();
    }

    /**
     * <br>类描述: 动画事件监听者
     * <br>功能详细描述:
     * 
     * @author  dengweiming
     * @date  [2013-10-17]
     */
    public static interface AnimatorListener {
    	
        /**
         * 通知动画开始
         */
        void onAnimationStart(Animator animation);

        /**
         * 通知动画结束
         * <br>注意：如果动画设置了重复次数为INFINITE则不会通知
         */
        void onAnimationEnd(Animator animation);

        /**
         * 通知动画取消
         * <br>注意：如果动画设置了重复次数为INFINITE则不会通知
         */
        void onAnimationCancel(Animator animation);

        /**
         * 通知动画开始一次重复
         */
        void onAnimationRepeat(Animator animation);
    }
}
