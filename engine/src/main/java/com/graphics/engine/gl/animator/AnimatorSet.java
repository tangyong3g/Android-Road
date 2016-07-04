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


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import android.view.animation.Interpolator;

/**
 * This class plays a set of {@link Animator} objects in the specified order. Animations
 * can be set up to play together, in sequence, or after a specified delay.
 *
 * <p>There are two different approaches to adding animations to a <code>AnimatorSet</code>:
 * either the {@link AnimatorSet#playTogether(Animator[]) playTogether()} or
 * {@link AnimatorSet#playSequentially(Animator[]) playSequentially()} methods can be called to add
 * a set of animations all at once, or the {@link AnimatorSet#play(Animator)} can be
 * used in conjunction with methods in the {@link AnimatorSet.Builder Builder}
 * class to add animations
 * one by one.</p>
 *
 * <p>It is possible to set up a <code>AnimatorSet</code> with circular dependencies between
 * its animations. For example, an animation a1 could be set up to start before animation a2, a2
 * before a3, and a3 before a1. The results of this configuration are undefined, but will typically
 * result in none of the affected animations being played. Because of this (and because
 * circular dependencies do not make logical sense anyway), circular dependencies
 * should be avoided, and the dependency flow of animations should only be in one direction.
 *
 */
public final class AnimatorSet extends FloatValueAnimator {

    /**
     * Internal variables
     * NOTE: This object implements the clone() method, making a deep copy of any referenced
     * objects. As other non-trivial fields are added to this class, make sure to add logic
     * to clone() to make deep copies of them.
     */

    /**
     * Contains all nodes, mapped to their respective Animators. When new
     * dependency information is added for an Animator, we want to add it
     * to a single node representing that Animator, not create a new Node
     * if one already exists.
     */
    private HashMap<Animator, Node> mNodeMap = new HashMap<Animator, Node>();

    /**
     * Set of all nodes created for this AnimatorSet. This list is used upon
     * starting the set, and the nodes are placed in sorted order into the
     * sortedNodes collection.
     */
    private ArrayList<Node> mNodes = new ArrayList<Node>();

    /**
     * The sorted list of nodes. This is the order in which the animations will
     * be played. The details about when exactly they will be played depend
     * on the dependency relationships of the nodes.
     */
    private ArrayList<Node> mSortedNodes = new ArrayList<Node>();

    /**
     * Flag indicating whether the nodes should be sorted prior to playing. This
     * flag allows us to cache the previous sorted nodes so that if the sequence
     * is replayed with no changes, it does not have to re-sort the nodes again.
     */
    private boolean mNeedsSort = true;

    private long mTotalPlayTime = 0;
    private boolean mSorting = false;
    private boolean mAllValueAnimation = true;
    
    public AnimatorSet() {
    	setValues(0, 1);
	}
    
	public Animator getAnimation(int index) {
		if (index < 0 || index >= mNodes.size()) {
			return null;
		}
		return mNodes.get(index).animation;
	}

	public Animator getAnimation(String name) {
		for (int i = 0, count = mNodes.size(); i < count; ++i) {
			Animator anim = mNodes.get(i).animation;
			if (name.equals(anim.mName)) {
				return anim;
			}
		}
		return null;
	}
	
	public int indexOfAnimation(String name) {
		for (int i = 0, count = mNodes.size(); i < count; ++i) {
			Animator anim = mNodes.get(i).animation;
			if (name.equals(anim.mName)) {
				return i;
			}
		}
		return -1;
	}

    /**
     * Sets up this AnimatorSet to play all of the supplied animations at the same time.
     *
     * @param items The animations that will be started simultaneously.
     */
    public void playTogether(Animator... items) {
        if (items != null) {
            mNeedsSort = true;
            Builder builder = play(items[0]);
            for (int i = 1; i < items.length; ++i) {
                builder.with(items[i]);
            }
        }
    }

    /**
     * Sets up this AnimatorSet to play all of the supplied animations at the same time.
     *
     * @param items The animations that will be started simultaneously.
     */
    public void playTogether(Collection<Animator> items) {
        if (items != null && items.size() > 0) {
            mNeedsSort = true;
            Builder builder = null;
            for (Animator anim : items) {
                if (builder == null) {
                    builder = play(anim);
                } else {
                    builder.with(anim);
                }
            }
        }
    }

    /**
     * Sets up this AnimatorSet to play each of the supplied animations when the
     * previous animation ends.
     *
     * @param items The animations that will be started one after another.
     */
    public void playSequentially(Animator... items) {
        if (items != null) {
            mNeedsSort = true;
            if (items.length == 1) {
                play(items[0]);
            } else {
                for (int i = 0; i < items.length - 1; ++i) {
                    play(items[i]).before(items[i + 1]);
                }
            }
        }
    }

    /**
     * Sets up this AnimatorSet to play each of the supplied animations when the
     * previous animation ends.
     *
     * @param items The animations that will be started one after another.
     */
    public void playSequentially(List<Animator> items) {
        if (items != null && items.size() > 0) {
            mNeedsSort = true;
            if (items.size() == 1) {
                play(items.get(0));
            } else {
                for (int i = 0; i < items.size() - 1; ++i) {
                    play(items.get(i)).before(items.get(i + 1));
                }
            }
        }
    }

    /**
     * Returns the current list of child Animator objects controlled by this
     * AnimatorSet. This is a copy of the internal list; modifications to the returned list
     * will not affect the AnimatorSet, although changes to the underlying Animator objects
     * will affect those objects being managed by the AnimatorSet.
     *
     * @return ArrayList<Animator> The list of child animations of this AnimatorSet.
     */
    public ArrayList<Animator> getChildAnimations() {
        ArrayList<Animator> childList = new ArrayList<Animator>();
        for (Node node : mNodes) {
            childList.add(node.animation);
        }
        return childList;
    }

    /**
     * Sets the target object for all current {@link #getChildAnimations() child animations}
     * of this AnimatorSet that take targets ({@link ObjectAnimator} and
     * AnimatorSet).
     *
     * @param target The object being animated
     */
    @Override
    public void setTarget(Object target) {
        for (Node node : mNodes) {
            Animator animation = node.animation;
            if (animation instanceof AnimatorSet) {
                ((AnimatorSet) animation).setTarget(target);
            }
            //XXX
//            else if (animation instanceof ObjectAnimator) {
//                ((ObjectAnimator)animation).setTarget(target);
//            }
        }
    }

    /**
     * Sets the TimeInterpolator for all current {@link #getChildAnimations() child animations}
     * of this AnimatorSet.
     *
     * @param interpolator the interpolator to be used by each child animation of this AnimatorSet
     */
    @Override
    public void setInterpolator(Interpolator interpolator) {
        for (Node node : mNodes) {
            node.animation.setInterpolator(interpolator);
        }
    }

    /**
     * This method creates a <code>Builder</code> object, which is used to
     * set up playing constraints. This initial <code>play()</code> method
     * tells the <code>Builder</code> the animation that is the dependency for
     * the succeeding commands to the <code>Builder</code>. For example,
     * calling <code>play(a1).with(a2)</code> sets up the AnimatorSet to play
     * <code>a1</code> and <code>a2</code> at the same time,
     * <code>play(a1).before(a2)</code> sets up the AnimatorSet to play
     * <code>a1</code> first, followed by <code>a2</code>, and
     * <code>play(a1).after(a2)</code> sets up the AnimatorSet to play
     * <code>a2</code> first, followed by <code>a1</code>.
     *
     * <p>Note that <code>play()</code> is the only way to tell the
     * <code>Builder</code> the animation upon which the dependency is created,
     * so successive calls to the various functions in <code>Builder</code>
     * will all refer to the initial parameter supplied in <code>play()</code>
     * as the dependency of the other animations. For example, calling
     * <code>play(a1).before(a2).before(a3)</code> will play both <code>a2</code>
     * and <code>a3</code> when a1 ends; it does not set up a dependency between
     * <code>a2</code> and <code>a3</code>.</p>
     *
     * @param anim The animation that is the dependency used in later calls to the
     * methods in the returned <code>Builder</code> object. A null parameter will result
     * in a null <code>Builder</code> return value.
     * @return Builder The object that constructs the AnimatorSet based on the dependencies
     * outlined in the calls to <code>play</code> and the other methods in the
     * <code>Builder</code object.
     */
    public Builder play(Animator anim) {
        if (anim != null) {
            mNeedsSort = true;
            return new Builder(anim);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Note that canceling a <code>AnimatorSet</code> also cancels all of the animations that it
     * is responsible for.</p>
     */
    @Override
    public void cancel() {
		if (isStarted()) {
			for (int i = 0, count = mSortedNodes.size(); i < count; ++i) {
				Node node = mSortedNodes.get(i);
				node.animation.cancel();
			}
		}
		super.cancel();
    }
    
    @Override
    public void end() {
		if (isStarted()) {
			for (int i = 0, count = mSortedNodes.size(); i < count; ++i) {
				Node node = mSortedNodes.get(i);
				node.animation.end();
			}
		}
    	super.end();
    }


    @Override
    public void setupStartValues() {
		for (int i = 0, count = mSortedNodes.size(); i < count; ++i) {
			Node node = mSortedNodes.get(i);
			node.animation.setupStartValues();
		}
    }

    @Override
    public void setupEndValues() {
		for (int i = 0, count = mSortedNodes.size(); i < count; ++i) {
			Node node = mSortedNodes.get(i);
			int repeatCount = 0;
			if (node.isValueAnimation) {
				repeatCount = ((ValueAnimator) node.animation).mRepeatCount;
			}
			if (repeatCount % 2 == 0) {
				node.animation.setupEndValues();
			} else {
				node.animation.setupStartValues();
			}
		}
    }

    @Override
    public AnimatorSet clone() {
        final AnimatorSet anim = (AnimatorSet) super.clone();
        /*
         * The basic clone() operation copies all items. This doesn't work very well for
         * AnimatorSet, because it will copy references that need to be recreated and state
         * that may not apply. What we need to do now is put the clone in an uninitialized
         * state, with fresh, empty data structures. Then we will build up the nodes list
         * manually, as we clone each Node (and its animation). The clone will then be sorted,
         * and will populate any appropriate lists, when it is started.
         */
        anim.mNeedsSort = true;
        anim.mStarted = false;
        anim.mNodeMap = new HashMap<Animator, Node>();
        anim.mNodes = new ArrayList<Node>();
        anim.mSortedNodes = new ArrayList<Node>();

        //XXX

        return anim;
    }

    /**
     * This method sorts the current set of nodes, if needed. The sort is a simple
     * DependencyGraph sort, which goes like this:
     * - All nodes without dependencies become 'roots'
     * - while roots list is not null
     * -   for each root r
     * -     add r to sorted list
     * -     remove r as a dependency from any other node
     * -   any nodes with no dependencies are added to the roots list
     */
    private void sortNodes() {
        if (mNeedsSort) {
        	if (mSorting) {
        		throw new IllegalStateException("Circular dependencies cannot exist in AnimatorSet");
        	}
        	mSorting = true;
        	mTotalPlayTime = INFINITE;
        	mDuration = 0;
        	mAllValueAnimation = true;
        	
            mSortedNodes.clear();
            final int numNodes = mNodes.size();
            
			for (int i = 0; i < numNodes; ++i) {
				Node node = mNodes.get(i);
				node.tmpInDegrees = node.inDegrees;
				node.startTime = INFINITE;
				node.endTime = INFINITE;
				mAllValueAnimation &= node.isValueAnimation;
				
				if (node.inDegrees <= 0) {
					node.startTime = 0;
					mSortedNodes.add(node);
				}
			}

			int begin = 0, end = mSortedNodes.size();
			while (begin < end) {
				Node node = mSortedNodes.get(begin++);
				long playTime = node.animation.getTotalPlayTime();
				if (node.startTime == INFINITE || playTime == INFINITE) {
					node.endTime = INFINITE;
					mDuration = INFINITE;
				} else {
					node.endTime = node.startTime + playTime;
					if (mDuration != INFINITE) {
						mDuration = Math.max(mDuration, node.endTime);
					}
				}
				
				ArrayList<Dependent> dependents = node.dependents;
				if (dependents != null) {
					for (int j = 0, count = dependents.size(); j < count; ++j) {
						Dependent dependent = dependents.get(j);
						Node dependentNode = dependent.node;
						long startTime = dependent.rule == Dependent.WITH 
								? node.startTime 
								: node.endTime;
						dependentNode.startTime = startTime == INFINITE 
								? INFINITE 
								: Math.max(dependentNode.startTime, startTime);
						if (--dependentNode.tmpInDegrees <= 0) {
							mSortedNodes.add(dependentNode);
							++end;
						}
					}
				}
			}
            
			if (mDuration != INFINITE) {
				mTotalPlayTime = mRepeatCount == INFINITE 
						? INFINITE 
						: mStartDelay + mDuration * (1 + mRepeatCount);
			}
            mNeedsSort = false;
            mSorting = false;
            if (mSortedNodes.size() != numNodes) {
                throw new IllegalStateException("Circular dependencies cannot exist in AnimatorSet");
            }
        }
    }
    
    
    @Override
    public long getTotalPlayTime() {
    	sortNodes();
    	return mTotalPlayTime;
    }
    
    @Override
    public void reverse() {
		getTotalPlayTime();
		if (mDuration == INFINITE) {
			throw new RuntimeException("Infinity AnimatorSet can not reverse.");
		}
		if (!mAllValueAnimation) {
			throw new RuntimeException("AnimatorSet contains non-ValueAnimator can not reverse.");
		}
		int playingState = mPlayingState;
    	super.reverse();
    	if (playingState == RUNNING) {
    		for (int i = 0, count = mSortedNodes.size(); i < count; ++i) {
    			Node node = mSortedNodes.get(i);
    			node.toReverse++;
    			node.triggered = false;
    		}
    	}
    }
    
    @Override
    void start(boolean playBackwards) {
    	getTotalPlayTime();
		if (!playBackwards) {
			setupStartValues();
		} else {
			setupEndValues();
		}
		for (int i = 0, count = mSortedNodes.size(); i < count; ++i) {
			Node node = mSortedNodes.get(i);
			node.toReverse = playBackwards ? 1 : 0;
			node.triggered = false;
		}
    	super.start(playBackwards);
    }
    
    @Override
    protected void evaluate(float fraction, int startFrame, int endFrame) {
    	
    }
    
    @Override
    boolean animationFrame(long currentTime) {
		if (mDuration == INFINITE) {
			animateValue(currentTime - mStartTime);
			return false;
		}
        return super.animationFrame(currentTime);
    }
    
    @Override
    void animateValue(float fraction) {
    	super.animateValue(fraction);
		long time = (long) ((double) fraction * mDuration);
		animateValue(time);

    }
    
    private void animateValue(long time) {
		for (int i = 0, count = mSortedNodes.size(); i < count; ++i) {
			Node node = mSortedNodes.get(i);
			if (node.startTime <= time 
					&& time < node.endTime 
					&& !node.triggered) {
				node.triggered = true;
				if (node.toReverse % 2 != 0 && node.isValueAnimation) {
					--node.toReverse;
					ValueAnimator a = (ValueAnimator) node.animation;
					a.relativeReverse();
				} else {
					node.animation.start();
				}
			}
		}
    }
    
    @Override
    public ValueAnimator setDuration(long duration) {
    	//时间由其包含的动画决定，所以忽略掉设置的值
    	return this;
    }
    
    @Override
    public void setRepeatCount(int value) {
    	super.setRepeatCount(value);
    	throw new RuntimeException("AnimatorSet does not support repeating.");
    	//TODO：支持重复播放
//		if (mAnimationListener == null) {
//			mAnimationListener = new AnimatorListenerAdapter() {
//				
//				@Override
//				public void onAnimationRepeat(Animator animation) {
//				}
//			};
//			addListener(mAnimationListener);
//		}
    }
    
    @Override
    public void cleanup() {
		for (int i = 0, count = mNodes.size(); i < count; ++i) {
			mNodes.get(i).cleanup();
		}
    	mNodes.clear();
    	mSortedNodes.clear();
    	mNodeMap.clear();
    	super.cleanup();
    }

    /**
     * Dependency holds information about the node that some other node is
     * dependent upon and the nature of that dependency.
     *
     */
    private static class Dependent {
        static final int WITH = 0; // dependent node must start with this dependency node
        static final int AFTER = 1; // dependent node must start when this dependency node finishes

        // The node that the other node with this Dependency is dependent upon
        public Node node;

        // The nature of the dependency (WITH or AFTER)
        public int rule;

        public Dependent(Node node, int rule) {
            this.node = node;
            this.rule = rule;
        }
    }

    /**
     * A Node is an embodiment of both the Animator that it wraps as well as
     * any dependencies that are associated with that Animation. This includes
     * both dependencies upon other nodes (in the dependencies list) as
     * well as dependencies of other nodes upon this (in the nodeDependents list).
     */
    private static class Node implements Cloneable {
        public Animator animation;

        /**
         *  These are the dependencies that this node's animation has on other
         *  nodes. For example, if this node's animation should begin with some
         *  other animation ends, then there will be an item in this node's
         *  dependencies list for that other animation's node.
         */
        public ArrayList<Dependent> dependents = null;

        public int inDegrees;
        public int tmpInDegrees;
        public long startTime;
        public long endTime;
        public boolean isValueAnimation;
        public boolean triggered;
        public int toReverse;

        /**
         * Constructs the Node with the animation that it encapsulates. A Node has no
         * dependencies by default; dependencies are added via the addDependency()
         * method.
         *
         * @param animation The animation that the Node encapsulates.
         */
        public Node(Animator animation) {
            this.animation = animation;
            isValueAnimation = animation instanceof ValueAnimator;
        }

        //添加一条出边
        public void addDependent(Node dst, int rule) {
        	//自边和多重边不用检测，在排序阶段会检测出
			if (dependents == null) {
				dependents = new ArrayList<Dependent>();
			}
			Dependent dependent = new Dependent(dst, rule);
            dependents.add(dependent);
            dst.inDegrees++;
        }

        @Override
        public Node clone() {
            try {
                Node node = (Node) super.clone();
                node.animation = (Animator) animation.clone();
                return node;
            } catch (CloneNotSupportedException e) {
               throw new AssertionError();
            }
        }
        
        public void cleanup() {
        	animation = null;
			if (dependents != null) {
				dependents.clear();
				dependents = null;
			}
        }
    }

    /**
     * The <code>Builder</code> object is a utility class to facilitate adding animations to a
     * <code>AnimatorSet</code> along with the relationships between the various animations. The
     * intention of the <code>Builder</code> methods, along with the {@link
     * AnimatorSet#play(Animator) play()} method of <code>AnimatorSet</code> is to make it possible
     * to express the dependency relationships of animations in a natural way. Developers can also
     * use the {@link AnimatorSet#playTogether(Animator[]) playTogether()} and {@link
     * AnimatorSet#playSequentially(Animator[]) playSequentially()} methods if these suit the need,
     * but it might be easier in some situations to express the AnimatorSet of animations in pairs.
     * <p/>
     * <p>The <code>Builder</code> object cannot be constructed directly, but is rather constructed
     * internally via a call to {@link AnimatorSet#play(Animator)}.</p>
     * <p/>
     * <p>For example, this sets up a AnimatorSet to play anim1 and anim2 at the same time, anim3 to
     * play when anim2 finishes, and anim4 to play when anim3 finishes:</p>
     * <pre>
     *     AnimatorSet s = new AnimatorSet();
     *     s.play(anim1).with(anim2);
     *     s.play(anim2).before(anim3);
     *     s.play(anim4).after(anim3);
     * </pre>
     * <p/>
     * <p>Note in the example that both {@link Builder#before(Animator)} and {@link
     * Builder#after(Animator)} are used. These are just different ways of expressing the same
     * relationship and are provided to make it easier to say things in a way that is more natural,
     * depending on the situation.</p>
     * <p/>
     * <p>It is possible to make several calls into the same <code>Builder</code> object to express
     * multiple relationships. However, note that it is only the animation passed into the initial
     * {@link AnimatorSet#play(Animator)} method that is the dependency in any of the successive
     * calls to the <code>Builder</code> object. For example, the following code starts both anim2
     * and anim3 when anim1 ends; there is no direct dependency relationship between anim2 and
     * anim3:
     * <pre>
     *   AnimatorSet s = new AnimatorSet();
     *   s.play(anim1).before(anim2).before(anim3);
     * </pre>
     * If the desired result is to play anim1 then anim2 then anim3, this code expresses the
     * relationship correctly:</p>
     * <pre>
     *   AnimatorSet s = new AnimatorSet();
     *   s.play(anim1).before(anim2);
     *   s.play(anim2).before(anim3);
     * </pre>
     * <p/>
     * <p>Note that it is possible to express relationships that cannot be resolved and will not
     * result in sensible results. For example, <code>play(anim1).after(anim1)</code> makes no
     * sense. In general, circular dependencies like this one (or more indirect ones where a depends
     * on b, which depends on c, which depends on a) should be avoided. Only create AnimatorSets
     * that can boil down to a simple, one-way relationship of animations starting with, before, and
     * after other, different, animations.</p>
     */
    public class Builder {

        /**
         * This tracks the current node being processed. It is supplied to the play() method
         * of AnimatorSet and passed into the constructor of Builder.
         */
        private Node mCurrentNode;

        /**
         * package-private constructor. Builders are only constructed by AnimatorSet, when the
         * play() method is called.
         *
         * @param anim The animation that is the dependency for the other animations passed into
         * the other methods of this Builder object.
         */
        Builder(Animator anim) {
            mCurrentNode = mNodeMap.get(anim);
            if (mCurrentNode == null) {
                mCurrentNode = new Node(anim);
                mNodeMap.put(anim, mCurrentNode);
                mNodes.add(mCurrentNode);
            }
        }

        /**
         * Sets up the given animation to play at the same time as the animation supplied in the
         * {@link AnimatorSet#play(Animator)} call that created this <code>Builder</code> object.
         *
         * @param anim The animation that will play when the animation supplied to the
         * {@link AnimatorSet#play(Animator)} method starts.
         */
        public Builder with(Animator anim) {
            Node node = mNodeMap.get(anim);
            if (node == null) {
                node = new Node(anim);
                mNodeMap.put(anim, node);
                mNodes.add(node);
            }
            mCurrentNode.addDependent(node, Dependent.WITH);
            return this;
        }

        /**
         * Sets up the given animation to play when the animation supplied in the
         * {@link AnimatorSet#play(Animator)} call that created this <code>Builder</code> object
         * ends.
         *
         * @param anim The animation that will play when the animation supplied to the
         * {@link AnimatorSet#play(Animator)} method ends.
         */
        public Builder before(Animator anim) {
            Node node = mNodeMap.get(anim);
            if (node == null) {
                node = new Node(anim);
                mNodeMap.put(anim, node);
                mNodes.add(node);
            }
            mCurrentNode.addDependent(node, Dependent.AFTER);
            return this;
        }

        /**
         * Sets up the given animation to play when the animation supplied in the
         * {@link AnimatorSet#play(Animator)} call that created this <code>Builder</code> object
         * to start when the animation supplied in this method call ends.
         *
         * @param anim The animation whose end will cause the animation supplied to the
         * {@link AnimatorSet#play(Animator)} method to play.
         */
        public Builder after(Animator anim) {
            Node node = mNodeMap.get(anim);
            if (node == null) {
                node = new Node(anim);
                mNodeMap.put(anim, node);
                mNodes.add(node);
            }
            node.addDependent(mCurrentNode, Dependent.AFTER);
            return this;
        }

        /**
         * Sets up the animation supplied in the
         * {@link AnimatorSet#play(Animator)} call that created this <code>Builder</code> object
         * to play when the given amount of time elapses.
         *
         * @param delay The number of milliseconds that should elapse before the
         * animation starts.
         */
        public Builder after(long delay) {
            // setup dummy ValueAnimator just to run the clock
            ValueAnimator anim = ValueAnimator.ofFloat(0f, 1f);	//TODO:在Node记录这个时间并在sortNodes()处理即可，不需要创建多余的anim
            anim.setDuration(delay);
            after(anim);
            return this;
        }

    }

}
