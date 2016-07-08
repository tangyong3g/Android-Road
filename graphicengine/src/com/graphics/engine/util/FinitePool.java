/*
 * Copyright (C) 2009 The Android Open Source Project
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

package com.graphics.engine.util;

/**
 * <br>类描述: 有容量限制的对象池实现
 * <br>功能详细描述:
 * 实际上也可以使用{@link #FinitePool(PoolableManager)}构造，使得没有容量限制。
 * 如果有容量限制，超过限制时，释放的对象不会放回对象池，而是让JVM回收。
 * @hide
 * @param <T>
 */
public class FinitePool<T extends Poolable<T>> implements Pool<T> {
	private final static boolean DBG = false; 
    /**
     * Factory used to create new pool objects
     */
    private final PoolableManager<T> mManager;
    /**
     * Maximum number of objects in the pool
     */
    private final int mLimit;
    /**
     * If true, mLimit is ignored
     */
    private final boolean mInfinite;

    /**
     * Next object to acquire
     */
    private T mRoot;
    /**
     * Number of objects in the pool
     */
    private int mPoolCount;

    public FinitePool(PoolableManager<T> manager) {
        mManager = manager;
        mLimit = 0;
        mInfinite = true;
    }

    public FinitePool(PoolableManager<T> manager, int limit) {
		if (limit <= 0) {
			throw new IllegalArgumentException("The pool limit must be > 0");
		}

        mManager = manager;
        mLimit = limit;
        mInfinite = false;
    }

    public T acquire() {
        T element;

        if (mRoot != null) {
            element = mRoot;
            mRoot = element.getNextPoolable();
            mPoolCount--;
        } else {
            element = mManager.newInstance();
//            ++sCount;
//            if(sCount % 50 == 0){
//            	Log.d("DWM", "pooable new count=" + sCount + " pool=" + 
//            			this + " element=" + element + " limit=" + mLimit);
//            }
        }

        if (element != null) {
            element.setNextPoolable(null);
            mManager.onAcquired(element);            
        }

        return element;
    }

    public void release(T element) {
		if (mInfinite || mPoolCount < mLimit) {
			mPoolCount++;
			element.setNextPoolable(mRoot);
			mRoot = element;
		} else if (DBG) {
			throw new RuntimeException("FinitePool overflow");
		}
        mManager.onReleased(element);
    }
}
