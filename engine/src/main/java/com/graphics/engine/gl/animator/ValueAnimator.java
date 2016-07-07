package com.graphics.engine.gl.animator;

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


import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import com.graphics.engine.gl.animation.Transformation3D;
import com.graphics.engine.gl.animator.motionfiler.MotionFilter;

import java.util.ArrayList;


/**
 * 
 * <br>类描述: 值动画，对指定的开始值和结束值，产生一系列的中间值。
 * <br>功能详细描述:
 * <p>
 * 支持逆序播放，见{@link #reverse()}以及{@link #relativeReverse()}。
 * <br>支持重复播放，见{@link #setRepeatCount(int)}以及{@link #setRepeatMode(int)}
 * <br>支持设置开始的延时，见{@link #setStartDelay(long)}
 * <br>支持设置动画时间，见{@link #setDuration(long)}
 * </p>
 * <p>
 * 如果有必要，使用{@link AnimatorUpdateListener} 来响应每帧的更新，相关方法：
 * <br>{@link #addUpdateListener(AnimatorUpdateListener)} 
 * <br>{@link #removeUpdateListener(AnimatorUpdateListener)}
 * <br>{@link #removeAllUpdateListeners()}。
 * </p>
 * 
 * @author  dengweiming
 * @date  [2013-7-22]
 */
public abstract class ValueAnimator extends Animator implements MotionFilter {
	/** @hide */
	public static long sCurrentTime = -1;

    /**
     * Internal constants
     */
    private static float sDurationScale = 1.0f;

    /**
     * Values used with internal variable mPlayingState to indicate the current state of an
     * animation.
     */
    static final int STOPPED    = 0; // Not yet playing
    static final int RUNNING    = 1; // Playing normally
    static final int SEEKED     = 2; // Seeked to some time value

    /**
     * Internal variables
     * NOTE: This object implements the clone() method, making a deep copy of any referenced
     * objects. As other non-trivial fields are added to this class, make sure to add logic
     * to clone() to make deep copies of them.
     */

    // The first time that the animation's animateFrame() method is called. This time is used to
    // determine elapsed time (and therefore the elapsed fraction) in subsequent calls
    // to animateFrame()
    long mStartTime;

    /**
     * Set when setCurrentPlayTime() is called. If negative, animation is not currently seeked
     * to a value.
     */
    long mSeekTime = -1;

    // The static sAnimationHandler processes the internal timing loop on which all animations
    // are based
//    private static ThreadLocal<AnimationHandler> sAnimationHandler =
//            new ThreadLocal<AnimationHandler>();
    /** @hide */
    public static AnimationHandler sAnimationHandler = new AnimationHandler();

    // The time interpolator to be used if none is set on the animation	//CHECKSTYLE IGNORE 1 LINES
    private static final Interpolator sDefaultInterpolator =
            new AccelerateDecelerateInterpolator();

    /**
     * Used to indicate whether the animation is currently playing in reverse. This causes the
     * elapsed fraction to be inverted to calculate the appropriate values.
     */
    boolean mPlayingBackwards = false;

    /**
     * This variable tracks the current iteration that is playing. When mCurrentIteration exceeds the
     * repeatCount (if repeatCount!=INFINITE), the animation ends
     */
    int mCurrentIteration = 0;

    /**
     * Tracks current elapsed/eased fraction, for querying in getAnimatedFraction().
     */
    private float mCurrentFraction = 0f;
    private float mCurrentRawFraction = 0f;

    /**
     * Tracks whether a startDelay'd animation has begun playing through the startDelay.
     */
    private boolean mStartedDelay = false;

    /**
     * Tracks the time at which the animation began playing through its startDelay. This is
     * different from the mStartTime variable, which is used to track when the animation became
     * active (which is when the startDelay expired and the animation was added to the active
     * animations list).
     */
    private long mDelayStartTime;

    /**
     * Flag that represents the current state of the animation. Used to figure out when to start
     * an animation (if state == STOPPED). Also used to end an animation that
     * has been cancel()'d or end()'d since the last animation frame. Possible values are
     * STOPPED, RUNNING, SEEKED.
     */
    int mPlayingState = STOPPED;

    /**
     * Additional playing state to indicate whether an animator has been start()'d. There is
     * some lag between a call to start() and the first animation frame. We should still note
     * that the animation has been started, even if it's first animation frame has not yet
     * happened, and reflect that state in isRunning().
     * Note that delayed animations are different: they are not started until their first
     * animation frame, which occurs after their delay elapses.
     */
    private boolean mRunning = false;

    /**
     * Additional playing state to indicate whether an animator has been start()'d, whether or
     * not there is a nonzero startDelay.
     */
    boolean mStarted = false;

    /**
     * Tracks whether we've notified listeners of the onAnimationSTart() event. This can be
     * complex to keep track of since we notify listeners at different times depending on
     * startDelay and whether start() was called before end().
     */
    private boolean mStartListenersCalled = false;

    /**
     * Flag that denotes whether the animation is set up and ready to go. Used to
     * set up animation that has not yet been started.
     */
    protected boolean mInitialized = false;

    //
    // Backing variables
    //

    // How long the animation should last in ms
    long mDuration = (long) (300 * sDurationScale);
    private long mUnscaledDuration = 300;

    // The amount of time in ms to delay starting the animation after start() is called
    long mStartDelay = 0;
    private long mUnscaledStartDelay = 0;

    // The number of times the animation will repeat. The default is 0, which means the animation
    // will play only once
    int mRepeatCount = 0;

    /**
     * The type of repetition that will occur when repeatMode is nonzero. RESTART means the
     * animation will start from the beginning on every new cycle. REVERSE means the animation
     * will reverse directions on each iteration.
     */
    int mRepeatMode = RESTART;

    /**
     * The time interpolator to be used. The elapsed fraction of the animation will be passed
     * through this interpolator to calculate the interpolated fraction, which is then used to
     * calculate the animated values.
     */
    private Interpolator mInterpolator = sDefaultInterpolator;

    /**
     * The set of listeners to be sent events through the life of an animation.
     */
    private ArrayList<AnimatorUpdateListener> mUpdateListeners = null;

    /**
     * Public constants
     */

    /**
     * When the animation reaches the end and <code>repeatCount</code> is INFINITE
     * or a positive value, the animation restarts from the beginning.
     */
    public static final int RESTART = 1;
    /**
     * When the animation reaches the end and <code>repeatCount</code> is INFINITE
     * or a positive value, the animation reverses direction on every iteration.
     */
    public static final int REVERSE = 2;
    /**
     * This value used used with the {@link #setRepeatCount(int)} property to repeat
     * the animation indefinitely.
     */
//    public static final int INFINITE = -1;

    
    private ArrayList<AnimatorListener> mTmpListeners = null;

    /**
     * @hide
     */
    public static void setDurationScale(float durationScale) {
        sDurationScale = durationScale;
    }

    /**
     * @hide
     */
    public static float getDurationScale() {
        return sDurationScale;
    }

    /**
     * Creates a new ValueAnimator object. This default constructor is primarily for
     * use internally; the factory methods which take parameters are more generally
     * useful.
     */
    public ValueAnimator() {
    }
    
    /**
     * Constructs and returns a ValueAnimator that animates between float values. A single
     * value implies that that value is the one being animated to. However, this is not typically
     * useful in a ValueAnimator object because there is no way for the object to determine the
     * starting value for the animation (unlike ObjectAnimator, which can derive that value
     * from the target object and property being animated). Therefore, there should typically
     * be two or more values.
     *
     * @param values A set of values that the animation will animate between over time.
     * @return A ValueAnimator object that is set up to animate between the given values.
     */
    public static FloatValueAnimator ofFloat(float... values) {
    	FloatValueAnimator anim = new FloatValueAnimator();
        anim.setValues(values);
        return anim;
    }

    /**
     * This function is called immediately before processing the first animation
     * frame of an animation. If there is a nonzero <code>startDelay</code>, the
     * function is called after that delay ends.
     * It takes care of the final initialization steps for the
     * animation.
     *
     *  <p>Overrides of this method should call the superclass method to ensure
     *  that internal mechanisms for the animation are set up correctly.</p>
     */
    void initAnimation() {
        if (!mInitialized) {
        	//XXX
//            int numValues = mValues.length;
//            for (int i = 0; i < numValues; ++i) {
//                mValues[i].init();
//            }
            mInitialized = true;
            mNeedInitializeMotionFilter = true;
        }
    }
    


    /**
     * Sets the length of the animation. The default duration is 300 milliseconds.
     *
     * @param duration The length of the animation, in milliseconds. This value cannot
     * be negative.
     * @return ValueAnimator The object called with setDuration(). This return
     * value makes it easier to compose statements together that construct and then set the
     * duration, as in <code>ValueAnimator.ofInt(0, 10).setDuration(500).start()</code>.
     */
    public ValueAnimator setDuration(long duration) {
        if (duration < 0) {
            throw new IllegalArgumentException("Animators cannot have negative duration: " +
                    duration);
        }
        mUnscaledDuration = duration;
        mDuration = (long) (duration * sDurationScale);
        return this;
    }

    /**
     * Gets the length of the animation. The default duration is 300 milliseconds.
     *
     * @return The length of the animation, in milliseconds.
     */
    public long getDuration() {
        return mUnscaledDuration;
    }

    /**
     * Sets the position of the animation to the specified point in time. This time should
     * be between 0 and the total duration of the animation, including any repetition. If
     * the animation has not yet been started, then it will not advance forward after it is
     * set to this time; it will simply set the time to this value and perform any appropriate
     * actions based on that time. If the animation is already running, then setCurrentPlayTime()
     * will set the current playing time to this value and continue playing from that point.
     *
     * @param playTime The time, in milliseconds, to which the animation is advanced or rewound.
     */
    public void setCurrentPlayTime(long playTime) {
        initAnimation();
		if (sCurrentTime == -1) {
			sCurrentTime = AnimationUtils.currentAnimationTimeMillis();
		}
        long currentTime = sCurrentTime;
        if (mPlayingState != RUNNING) {
            mSeekTime = playTime;
            mPlayingState = SEEKED;
        }
        mStartTime = currentTime - playTime;
        doAnimationFrame(currentTime);
    }

    /**
     * Gets the current position of the animation in time, which is equal to the current
     * time minus the time that the animation started. An animation that is not yet started will
     * return a value of zero.
     *
     * @return The current position in time of the animation.
     */
    public long getCurrentPlayTime() {
        if (!mInitialized || mPlayingState == STOPPED) {
            return 0;
        }
		if (sCurrentTime == -1) {
			sCurrentTime = AnimationUtils.currentAnimationTimeMillis();
		}
        return sCurrentTime - mStartTime;
    }

    /**
     * This custom, static handler handles the timing pulse that is shared by
     * all active animations. This approach ensures that the setting of animation
     * values will happen on the UI thread and that all animations will share
     * the same times for calculating their values, which makes synchronizing
     * animations possible.
     *
     * The handler uses the Choreographer for executing periodic callbacks.
     * @hide
     */
    public static class AnimationHandler implements Runnable {
        // The per-thread list of all active animations
        private final ArrayList<ValueAnimator> mAnimations = new ArrayList<ValueAnimator>();

        // Used in doAnimationFrame() to avoid concurrent modifications of mAnimations
        private final ArrayList<ValueAnimator> mTmpAnimations = new ArrayList<ValueAnimator>();

        // The per-thread set of animations to be started on the next animation frame
        private final ArrayList<ValueAnimator> mPendingAnimations = new ArrayList<ValueAnimator>();
        private final ArrayList<ValueAnimator> mTmpPendingAnimations = new ArrayList<ValueAnimator>();

        /**
         * Internal per-thread collections used to avoid set collisions as animations start and end
         * while being processed.
         */
        private final MyArrayList<ValueAnimator> mDelayedAnims = new MyArrayList<ValueAnimator>();
        private final ArrayList<ValueAnimator> mEndingAnims = new ArrayList<ValueAnimator>();
//        private final ArrayList<ValueAnimator> mReadyAnims = new ArrayList<ValueAnimator>();

        private boolean mAnimationScheduled;

        private AnimationHandler() {
        }

        /**
         * Start animating on the next frame.
         */
        public void start() {
            scheduleAnimation();
        }

        private void doAnimationFrame(long frameTime) {
            // mPendingAnimations holds any animations that have requested to be started
            // We're going to clear mPendingAnimations, but starting animation may
            // cause more to be added to the pending list (for example, if one animation
            // starting triggers another starting). So we loop until mPendingAnimations
            // is empty.
            while (mPendingAnimations.size() > 0) {
                ArrayList<ValueAnimator> pendingCopy = mTmpPendingAnimations;
                int count = mPendingAnimations.size();
				for (int i = 0; i < count; ++i) {
					pendingCopy.add(mPendingAnimations.get(i));
				}
				mPendingAnimations.clear();
                for (int i = 0; i < count; ++i) {
                    ValueAnimator anim = pendingCopy.get(i);
                    // If the animation has a startDelay, place it on the delayed list
                    if (anim.mStartDelay == 0) {
                        anim.startAnimation(this);
                    } else {
                        mDelayedAnims.add(anim);
                    }
                }
                pendingCopy.clear();
            }
            // Next, process animations currently sitting on the delayed queue, adding
            // them to the active animations if they are ready
            int numDelayedAnims = mDelayedAnims.size();
			if (numDelayedAnims > 0) {
				numDelayedAnims = mDelayedAnims.size();
			}
            for (int i = 0; i < numDelayedAnims; ++i) {
                ValueAnimator anim = mDelayedAnims.get(i);
                if (anim.delayedAnimationFrame(frameTime)) {
//                    mReadyAnims.add(anim);
					ValueAnimator lastAnim = mDelayedAnims.get(--numDelayedAnims);
					mDelayedAnims.set(i--, lastAnim);
                	
                	anim.startAnimation(this);
                    anim.mRunning = true;
                }
            }
            mDelayedAnims.removeRange(numDelayedAnims, mDelayedAnims.size());
//            int numReadyAnims = mReadyAnims.size();
//            if (numReadyAnims > 0) {
//                for (int i = 0; i < numReadyAnims; ++i) {
//                    ValueAnimator anim = mReadyAnims.get(i);
//                    anim.startAnimation(this);
//                    anim.mRunning = true;
//                    mDelayedAnims.remove(anim);
//                }
//                mReadyAnims.clear();
//            }

            // Now process all active animations. The return value from animationFrame()
            // tells the handler whether it should now be ended
            int numAnims = mAnimations.size();
            for (int i = 0; i < numAnims; ++i) {
                mTmpAnimations.add(mAnimations.get(i));
            }
            for (int i = 0; i < numAnims; ++i) {
                ValueAnimator anim = mTmpAnimations.get(i);
                if (mAnimations.contains(anim) && anim.doAnimationFrame(frameTime)) {
                    mEndingAnims.add(anim);
                }
            }
            mTmpAnimations.clear();
            
            
            if (mEndingAnims.size() > 0) {
                for (int i = 0; i < mEndingAnims.size(); ++i) {
                    mEndingAnims.get(i).endAnimation(this);
                }
                mEndingAnims.clear();
            }

            // If there are still active or delayed animations, schedule a future call to
            // onAnimate to process the next frame of the animations.
            if (!mAnimations.isEmpty() || !mDelayedAnims.isEmpty()) {
                scheduleAnimation();
            }
        }

        @Override
        public void run() {
			if (mAnimationScheduled) {
				mAnimationScheduled = false;
				doAnimationFrame(sCurrentTime);
			}
        }

        private void scheduleAnimation() {
            if (!mAnimationScheduled) {
                mAnimationScheduled = true;
            }
        }
        
        //为减少代码改动，增加get()方法代替ThreadLocal.get()
        AnimationHandler get() {
        	return this;
        }
        
        public boolean isScheduled() {
			return mAnimationScheduled;
		}
        
        //CHECKSTYLE IGNORE 1 LINES
        private static class MyArrayList<E> extends ArrayList<E> {
        	@Override
        	public void removeRange(int fromIndex, int toIndex) {
        		if (fromIndex < size() && toIndex < size()) {        		// TODO 加个保护，否则2.2系统会报运行时错误	
        			super.removeRange(fromIndex, toIndex);
        		}
        	}
        }
        
        /** @hide */
        public void cleanup() {
        	mPendingAnimations.clear();
        	mDelayedAnims.clear();
        }
    }

    /**
     * The amount of time, in milliseconds, to delay starting the animation after
     * {@link #start()} is called.
     *
     * @return the number of milliseconds to delay running the animation
     */
    public long getStartDelay() {
        return mUnscaledStartDelay;
    }

    /**
     * The amount of time, in milliseconds, to delay starting the animation after
     * {@link #start()} is called.

     * @param startDelay The amount of the delay, in milliseconds
     */
    public void setStartDelay(long startDelay) {
        this.mStartDelay = (long) (startDelay * sDurationScale);
        mUnscaledStartDelay = startDelay;
    }

    /**
     * Sets how many times the animation should be repeated. If the repeat
     * count is 0, the animation is never repeated. If the repeat count is
     * greater than 0 or {@link #INFINITE}, the repeat mode will be taken
     * into account. The repeat count is 0 by default.
     *
     * @param value the number of times the animation should be repeated
     */
    public void setRepeatCount(int value) {
        mRepeatCount = value;
    }
    /**
     * Defines how many times the animation should repeat. The default value
     * is 0.
     *
     * @return the number of times the animation should repeat, or {@link #INFINITE}
     */
    public int getRepeatCount() {
        return mRepeatCount;
    }

    /**
     * Defines what this animation should do when it reaches the end. This
     * setting is applied only when the repeat count is either greater than
     * 0 or {@link #INFINITE}. Defaults to {@link #RESTART}.
     *
     * @param value {@link #RESTART} or {@link #REVERSE}
     */
    public void setRepeatMode(int value) {
        mRepeatMode = value;
    }

    /**
     * Defines what this animation should do when it reaches the end.
     *
     * @return either one of {@link #REVERSE} or {@link #RESTART}
     */
    public int getRepeatMode() {
        return mRepeatMode;
    }

    /**
     * Adds a listener to the set of listeners that are sent update events through the life of
     * an animation. This method is called on all listeners for every frame of the animation,
     * after the values for the animation have been calculated.
     *
     * @param listener the listener to be added to the current set of listeners for this animation.
     */
    public void addUpdateListener(AnimatorUpdateListener listener) {
        if (mUpdateListeners == null) {
            mUpdateListeners = new ArrayList<AnimatorUpdateListener>();
        }
        mUpdateListeners.add(listener);
    }

    /**
     * Removes all listeners from the set listening to frame updates for this animation.
     */
    public void removeAllUpdateListeners() {
        if (mUpdateListeners == null) {
            return;
        }
        mUpdateListeners.clear();
        mUpdateListeners = null;
    }

    /**
     * Removes a listener from the set listening to frame updates for this animation.
     *
     * @param listener the listener to be removed from the current set of update listeners
     * for this animation.
     */
    public void removeUpdateListener(AnimatorUpdateListener listener) {
        if (mUpdateListeners == null) {
            return;
        }
        mUpdateListeners.remove(listener);
        if (mUpdateListeners.size() == 0) {
            mUpdateListeners = null;
        }
    }


    /**
     * The time interpolator used in calculating the elapsed fraction of this animation. The
     * interpolator determines whether the animation runs with linear or non-linear motion,
     * such as acceleration and deceleration. The default value is
     * {@link android.view.animation.LinearInterpolator}
     *
     * @param value the interpolator to be used by this animation. A value of <code>null</code>
     * will result in linear interpolation.
     */
    @Override
    public void setInterpolator(Interpolator value) {
        if (value != null) {
            mInterpolator = value;
        } else {
            mInterpolator = new LinearInterpolator();
        }
    }

    /**
     * Returns the timing interpolator that this ValueAnimator uses.
     *
     * @return The timing interpolator for this ValueAnimator.
     */
    public Interpolator getInterpolator() {
        return mInterpolator;
    }
    
    private ArrayList<AnimatorListener> cloneTmpListeners() {
    	if (mTmpListeners == null) {
			mTmpListeners = new ArrayList<AnimatorListener>();
		}
		for (int i = 0, count = mListeners.size(); i < count; ++i) {
			mTmpListeners.add(mListeners.get(i));
		}
		return mTmpListeners;
    }

    private void notifyStartListeners() {
        if (mListeners != null && !mStartListenersCalled) {
//            ArrayList<AnimatorListener> tmpListeners =
//                    (ArrayList<AnimatorListener>) mListeners.clone();
			ArrayList<AnimatorListener> tmpListeners = cloneTmpListeners();
			int numListeners = tmpListeners.size();
            for (int i = 0; i < numListeners; ++i) {
                tmpListeners.get(i).onAnimationStart(this);
            }
            tmpListeners.clear();
        }
        mStartListenersCalled = true;
    }

    /**
     * Start the animation playing. This version of start() takes a boolean flag that indicates
     * whether the animation should play in reverse. The flag is usually false, but may be set
     * to true if called from the reverse() method.
     *
     * <p>The animation started by calling this method will be run on the thread that called
     * this method. This thread should have a Looper on it (a runtime exception will be thrown if
     * this is not the case). Also, if the animation will animate
     * properties of objects in the view hierarchy, then the calling thread should be the UI
     * thread for that view hierarchy.</p>
     *
     * @param playBackwards Whether the ValueAnimator should start playing in reverse.
     */
    void start(boolean playBackwards) {
        mPlayingBackwards = playBackwards;
        mCurrentIteration = 0;
        mPlayingState = STOPPED;
        mStarted = true;
        mStartedDelay = false;
        AnimationHandler animationHandler = getOrCreateAnimationHandler();
        animationHandler.mPendingAnimations.add(this);
        if (mStartDelay == 0) {
            // This sets the initial value of the animation, prior to actually starting it running
            setCurrentPlayTime(0);
            mPlayingState = STOPPED;
            mRunning = true;
            notifyStartListeners();
        }
        animationHandler.start();
    }

    @Override
    public void start() {
        start(false);
    }

    @Override
    public void cancel() {
        // Only cancel if the animation is actually running or has been started and is about
        // to run
        AnimationHandler handler = getOrCreateAnimationHandler();
        if (mPlayingState != STOPPED
                || handler.mPendingAnimations.contains(this)
                || handler.mDelayedAnims.contains(this)) {
            // Only notify listeners if the animator has actually started
            if ((mStarted || mRunning) && mListeners != null) {
                if (!mRunning) {
                    // If it's not yet running, then start listeners weren't called. Call them now.
                    notifyStartListeners();
                }
//                ArrayList<AnimatorListener> tmpListeners =
//                		(ArrayList<AnimatorListener>) mListeners.clone();
    			ArrayList<AnimatorListener> tmpListeners = cloneTmpListeners();
    			int numListeners = tmpListeners.size();
                for (int i = 0; i < numListeners; ++i) {
                	tmpListeners.get(i).onAnimationCancel(this);
                }
                tmpListeners.clear();
            }
            endAnimation(handler);
        }
    }

    @Override
    public void end() {
        AnimationHandler handler = getOrCreateAnimationHandler();
        if (!handler.mAnimations.contains(this) && !handler.mPendingAnimations.contains(this)) {
            // Special case if the animation has not yet started; get it ready for ending
            mStartedDelay = false;
            startAnimation(handler);
            mStarted = true;
        } else if (!mInitialized) {
            initAnimation();
        }
        animateValue(mPlayingBackwards ? 0f : 1f);
        endAnimation(handler);
    }

    @Override
    public boolean isRunning() {
        return (mPlayingState == RUNNING) || mRunning;
    }

    @Override
    public boolean isStarted() {
        return mStarted;
    }

    /**
     * Plays the ValueAnimator in reverse. If the animation is already running,
     * it will stop itself and play backwards from the point reached when reverse was called.
     * If the animation is not currently running, then it will start from the end and
     * play backwards. This behavior is only set for the current animation; future playing
     * of the animation will use the default behavior of playing forward.
     * 
     * 动画进行中则以当前的相反方向播放动画，否则将从动画结束端向开始端播放
     */
    public void reverse() {
    	mPlayingBackwards = !mPlayingBackwards;
    	if (mPlayingState == RUNNING) {
    		if (sCurrentTime == -1) {
    			sCurrentTime = AnimationUtils.currentAnimationTimeMillis();
    		}
    		long currentTime = sCurrentTime;
    		long currentPlayTime = currentTime - mStartTime;
    		long timeLeft = mDuration - currentPlayTime;
    		mStartTime = currentTime - timeLeft;
    	} else {
    		start(true);
    	}
    }
    
    /**
     * <br>功能简述: 以当前的相反方向播放动画
     * <br>功能详细描述: 
     * <br>注意: 如果当前播放了几次，那么回播时也同样播放这么多次，
     * <br>而{@link #reverse()}会播放{@link #getRepeatCount()}次。
     * <br>另外，无限循环的动画不能逆序播放。
     */
	public void relativeReverse() {
		if (mPlayingState == RUNNING || mCurrentRawFraction == 1) {
			if (mRepeatCount == INFINITE) {
				throw new RuntimeException("Infinity repeating ValueAnimator can not reverse.");
			}
			int iteration = mCurrentIteration;
			reverse();
			mCurrentIteration = mRepeatCount - iteration;
		} else {
			start();
		}
	}

    /**
     * Called internally to end an animation by removing it from the animations list. Must be
     * called on the UI thread.
     */
    private void endAnimation(AnimationHandler handler) {
        handler.mAnimations.remove(this);
        handler.mPendingAnimations.remove(this);
        handler.mDelayedAnims.remove(this);
        mPlayingState = STOPPED;
        if ((mStarted || mRunning) && mListeners != null) {
            if (!mRunning) {
                // If it's not yet running, then start listeners weren't called. Call them now.
                notifyStartListeners();
             }
//            ArrayList<AnimatorListener> tmpListeners =
//            		(ArrayList<AnimatorListener>) mListeners.clone();
			ArrayList<AnimatorListener> tmpListeners = cloneTmpListeners();
			int numListeners = tmpListeners.size();
            for (int i = 0; i < numListeners; ++i) {
            	tmpListeners.get(i).onAnimationEnd(this);
            }
            tmpListeners.clear();
        }
        mRunning = false;
        mStarted = false;
        mStartListenersCalled = false;
    }

    /**
     * Called internally to start an animation by adding it to the active animations list. Must be
     * called on the UI thread.
     */
    private void startAnimation(AnimationHandler handler) {
        initAnimation();
        handler.mAnimations.add(this);
        if (mStartDelay > 0 && mListeners != null) {
            // Listeners were already notified in start() if startDelay is 0; this is
            // just for delayed animations
            notifyStartListeners();
        }
    }

    /**
     * Internal function called to process an animation frame on an animation that is currently
     * sleeping through its <code>startDelay</code> phase. The return value indicates whether it
     * should be woken up and put on the active animations queue.
     *
     * @param currentTime The current animation time, used to calculate whether the animation
     * has exceeded its <code>startDelay</code> and should be started.
     * @return True if the animation's <code>startDelay</code> has been exceeded and the animation
     * should be added to the set of active animations.
     */
    private boolean delayedAnimationFrame(long currentTime) {
        if (!mStartedDelay) {
            mStartedDelay = true;
            mDelayStartTime = currentTime;
        } else {
            long deltaTime = currentTime - mDelayStartTime;
            if (deltaTime > mStartDelay) {
                // startDelay ended - start the anim and record the
                // mStartTime appropriately
                mStartTime = currentTime - (deltaTime - mStartDelay);
                mPlayingState = RUNNING;
                return true;
            }
        }
        return false;
    }

    /**
     * This internal function processes a single animation frame for a given animation. The
     * currentTime parameter is the timing pulse sent by the handler, used to calculate the
     * elapsed duration, and therefore
     * the elapsed fraction, of the animation. The return value indicates whether the animation
     * should be ended (which happens when the elapsed time of the animation exceeds the
     * animation's duration, including the repeatCount).
     *
     * @param currentTime The current time, as tracked by the static timing handler
     * @return true if the animation's duration, including any repetitions due to
     * <code>repeatCount</code> has been exceeded and the animation should be ended.
     */
    boolean animationFrame(long currentTime) {
        boolean done = false;
        switch (mPlayingState) {
        case RUNNING:
        case SEEKED:
            float fraction = mDuration > 0 ? (float) (currentTime - mStartTime) / mDuration : 1f;
            if (fraction >= 1f) {
                if (mCurrentIteration < mRepeatCount || mRepeatCount == INFINITE) {
                    // Time to repeat
                    if (mListeners != null) {
                        int numListeners = mListeners.size();
                        for (int i = 0; i < numListeners; ++i) {
                            mListeners.get(i).onAnimationRepeat(this);
                        }
                    }
                    if (mRepeatMode == REVERSE) {
                        mPlayingBackwards = !mPlayingBackwards;
                    }
                    mCurrentIteration += (int) fraction;
                    fraction = fraction % 1f;
                    mStartTime += mDuration;
                } else {
                    done = true;
                    fraction = Math.min(fraction, 1.0f);
                }
            }
            if (mPlayingBackwards) {
                fraction = 1f - fraction;
            }
            animateValue(fraction);
            break;
        }

        return done;
    }

    /**
     * Processes a frame of the animation, adjusting the start time if needed.
     *
     * @param frameTime The frame time.
     * @return true if the animation has ended.
     */
    final boolean doAnimationFrame(long frameTime) {
		if (mNumKeyframes <= 0) {
			return false;
		}
        if (mPlayingState == STOPPED) {
            mPlayingState = RUNNING;
            if (mSeekTime < 0) {
                mStartTime = frameTime;
            } else {
                mStartTime = frameTime - mSeekTime;
                // Now that we're playing, reset the seek time
                mSeekTime = -1;
            }
        }
        // The frame time might be before the start time during the first frame of
        // an animation.  The "current time" must always be on or after the start
        // time to avoid animating frames at negative time intervals.  In practice, this
        // is very rare and only happens when seeking backwards.
        final long currentTime = Math.max(frameTime, mStartTime);
        return animationFrame(currentTime);
    }

    /**
     * Returns the current animation fraction, which is the elapsed/interpolated fraction used in
     * the most recent frame update on the animation.
     *
     * @return Elapsed/interpolated fraction of the animation.
     * @see {@link #getRawAnimatedFraction()}
     */
    public float getAnimatedFraction() {
        return mCurrentFraction;
    }
    
    /**
     * 获取未插值前的浮点时间。0为动画开始端，1为动画结束端。
     * <br>另外可视化动画曲线时可用作时间轴的值。
     * 
     */
    public float getRawAnimatedFraction() {
    	return mCurrentRawFraction;
    }

    /**
     * This method is called with the elapsed fraction of the animation during every
     * animation frame. This function turns the elapsed fraction into an interpolated fraction
     * and then into an animated value (from the evaluator. The function is called mostly during
     * animation updates, but it is also called when the <code>end()</code>
     * function is called, to set the final value on the property.
     *
     * <p>Overrides of this method must call the superclass to perform the calculation
     * of the animated value.</p>
     *
     * @param fraction The elapsed fraction of the animation.
     */
    void animateValue(float fraction) {
    	mCurrentRawFraction = fraction;
        fraction = mInterpolator.getInterpolation(fraction);
        mCurrentFraction = fraction;
        //XXX
//        int numValues = mValues.length;
//        for (int i = 0; i < numValues; ++i) {
//            mValues[i].calculateValue(fraction);
//        }
        calculateValue(fraction);
        if (mUpdateListeners != null) {
            int numListeners = mUpdateListeners.size();
            for (int i = 0; i < numListeners; ++i) {
                mUpdateListeners.get(i).onAnimationUpdate(this);
            }
        }
    }
    
    protected int mNumKeyframes;
    protected int mFirstKeyframe;
    protected int mLastKeyframe;
    protected float[] mFractions;
	
    /**
     * 在 startFrame 和 endFrame 这两帧之间按 fraction 插值
     */
	protected abstract void evaluate(float fraction, int startFrame, int endFrame);

	protected void calculateValue(float fraction) {
		// Special-case optimization for the common case of only two keyframes
		if (mNumKeyframes <= 2) {
			evaluate(fraction, mFirstKeyframe, mLastKeyframe);
			return;
		}
		if (fraction <= 0f) {
			final int nextKeyframe = 1;
			final float prevFraction = mFractions[mFirstKeyframe];
			float intervalFraction = (fraction - prevFraction) / (mFractions[nextKeyframe] - prevFraction);
			evaluate(intervalFraction, mFirstKeyframe, nextKeyframe);
			return;
		} else if (fraction >= 1f) {
			final int prevKeyframe = mNumKeyframes - 2;
			final float prevFraction = mFractions[prevKeyframe];
			float intervalFraction = (fraction - prevFraction) / (mFractions[mLastKeyframe] - prevFraction);
			evaluate(intervalFraction, prevKeyframe, mLastKeyframe);
			return;
		}
		int prevKeyframe = mFirstKeyframe;
		for (int i = 1; i < mNumKeyframes; ++i) {
			int nextKeyframe = i;
			if (fraction < mFractions[nextKeyframe]) {
				final float prevFraction = mFractions[prevKeyframe];
				float intervalFraction = (fraction - prevFraction) / (mFractions[nextKeyframe] - prevFraction);
				evaluate(intervalFraction, prevKeyframe, nextKeyframe);
				return;
			}
			prevKeyframe = nextKeyframe;
		}
		// shouldn't reach here
	}

	/**
	 * <br>类描述: 动画更新的监听者
	 * <br>功能详细描述:
	 * 
	 * @author  dengweiming
	 * @date  [2013-10-17]
	 */
    public interface AnimatorUpdateListener {
    	
    	/**
    	 * <br>功能简述: 响应动画更新
    	 * <br>功能详细描述: 每帧会调用一次，此时可以获取动画值，并通知视图重绘。
    	 * <br>注意: 因为没有对动画值做泛型处理，所以一般需要将其转型成子类，再调用具体接口，
    	 * 例如 {@link FloatValueAnimator#getAnimatedValue()}。
    	 * @param animation
    	 */
        void onAnimationUpdate(ValueAnimator animation);

    }

    /**
     * Return the number of animations currently running.
     *
     * Used by StrictMode internally to annotate violations.
     * May be called on arbitrary threads!
     *
     * @hide
     */
    public static int getCurrentAnimationsCount() {
        AnimationHandler handler = sAnimationHandler.get();
        return handler != null ? handler.mAnimations.size() : 0;
    }

    /**
     * Clear all animations on this thread, without canceling or ending them.
     * This should be used with caution.
     *
     * @hide
     */
    public static void clearAllAnimations() {
        AnimationHandler handler = sAnimationHandler.get();
        if (handler != null) {
            handler.mAnimations.clear();
            handler.mPendingAnimations.clear();
            handler.mDelayedAnims.clear();
        }
    }

    private AnimationHandler getOrCreateAnimationHandler() {
        AnimationHandler handler = sAnimationHandler.get();
//        if (handler == null) {
//            handler = new AnimationHandler();
//            sAnimationHandler.set(handler);
//        }
        return handler;
    }
    
    @Override
    public long getTotalPlayTime() {
    	return mRepeatCount == INFINITE ? INFINITE : mStartDelay + mDuration * (1 + mRepeatCount);
    }
    
    @Override
    public void setupStartValues() {
    	animateValue(0);
    }
    
    @Override
    public void setupEndValues() {
    	animateValue(1);
    }
    
    /**
     * <br>功能简述: 当前播放方向是否为逆向
     * <br>功能详细描述:
     * <br>注意: 动画此时不一定在播放。一般在 {@link AnimatorListener} 的动画开始或者重复响应时使用
     * @return
     */
    public boolean isPlayingBackwards() {
    	return mPlayingBackwards;
    }
    
    /**
     * <br>功能简述: 动画是否停止在第一帧
     * <br>功能详细描述:
     * <br>注意:
     * @return
     */
    public boolean isStoppedOnFirstFrame() {
    	return !isStarted() && getRawAnimatedFraction() == 0;
    }
    
    /**
     * <br>功能简述: 动画是否停止在最后一帧
     * <br>功能详细描述:
     * <br>注意:
     * @return
     */
    public boolean isStoppedOnLastFrame() {
    	return !isStarted() && getRawAnimatedFraction() == 1;
    }
    
    @Override
    public void cleanup() {
    	cancel();
    	mNumKeyframes = 0;
    	removeAllUpdateListeners();
    	removeAllListeners();
		if (mTmpListeners != null) {
			mTmpListeners.clear();
			mTmpListeners = null;
		}
    	super.cleanup();
    }
    
    //--------implement MotionFilter--------v
    

	Transformation3D mTransformation;
	Transformation3D mInverseTransformation;
	boolean mIsInverseTransformationDirty;
	protected boolean mNeedInitializeMotionFilter;
	
	@Override
	public void initializeIfNeeded(int width, int height, int parentWidth, int parentHeight) {
		mNeedInitializeMotionFilter = false;
	}
	
	@Override
	public void getTransformation(Transformation3D t) {
		
	}
    
	@Override
	public Transformation3D getTransformation() {
		if (mTransformation == null) {
			mTransformation = new Transformation3D();
		}
		return mTransformation;
	}
	
	@Override
	public void setInverseTransformationDirty() {
		mIsInverseTransformationDirty |= willChangeTransformationMatrix();
	}
	
	@Override
	public Transformation3D getInverseTransformation() {
		if (mInverseTransformation == null) {
			mInverseTransformation = new Transformation3D();
			mIsInverseTransformationDirty = true;
		}
		if (mIsInverseTransformationDirty) {
			mIsInverseTransformationDirty = false;
			Transformation3D t = getTransformation();
			t.invert(mInverseTransformation);
		}
		return mInverseTransformation;
	}

	@Override
	public boolean willChangeTransformationMatrix() {
		return true;
	}

	@Override
	public boolean willChangeBounds() {
		return true;
	}

	@Override
	public boolean hasAlpha() {
		return false;
	}
	
    /**
     * Convert the information in the description of a size to an actual
     * dimension
     *
     * @param type One of Animation.ABSOLUTE, Animation.RELATIVE_TO_SELF, or
     *             Animation.RELATIVE_TO_PARENT.
     * @param value The dimension associated with the type parameter
     * @param size The size of the object being animated
     * @param parentSize The size of the parent of the object being animated
     * @return The dimension to use for the animation
     */
    protected float resolveSize(int type, float value, int size, int parentSize) {
        switch (type) {
            case ABSOLUTE:
                return value;
            case RELATIVE_TO_SELF:
                return size * value;
            case RELATIVE_TO_PARENT:
                return parentSize * value;
            default:
                return value;
        }
    }
	
	//--------implement MotionFilter--------^
}
