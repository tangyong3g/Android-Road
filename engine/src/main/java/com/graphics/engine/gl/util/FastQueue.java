package com.graphics.engine.gl.util;

import java.util.Arrays;

/**
 * <br>类描述: 使用循环数组实现的快速队列
 * <br>功能详细描述:
 * 支持一个线程在队尾添加元素，另一（或同一）线程在队头移除元素，不需要加同步锁。
 * <br>如果要在多个线程往队尾添加元素，需要使用者在调用代码周围加上synchronized限定词。
 * 
 * @author dengweiming
 * @param <E>
 */
public class FastQueue<E> {
	private static final boolean DBG = false;
	private transient Object[] mArray;
	private int mSize;
	private int mMask;
	private int mFront;
	private int mBack;
	
	/**
	 * 处理队列元素的回调接口
	 */
	public interface Processor<E> {
		public void process(E object);
	}

	/**
	 * 
	 * @param capacity	队列容量，之后不会扩展，所需大小由添加元素和移除元素的速度差决定。默认值为16。
	 */
	public FastQueue(int capacity) {
		if (capacity < 0) {
			throw new IllegalArgumentException();
		}
		int cap = 16;	// CHECKSTYLE IGNORE
		while (cap < capacity) {
			cap <<= 1;
		}
        mArray = new Object[cap];
        mSize = mArray.length;
        mMask = mSize - 1;
	}
	
	/**
	 * 在队尾添加元素
	 * @param object
	 * @return	是否添加成功
	 */
	public final boolean pushBack(E object) {
		final int begin = mFront;
		final int end = mBack;
		final int newEnd = (end + 1) & mMask;
		if (newEnd == begin) {
			handleOverflow(object);
			return false;
		}
		mArray[end] = object;
		mBack = newEnd;
        return true;
    }
	
	/**
	 * 移除队头元素
	 * @return	队头元素
	 */
	public final E popFront() {
		final int end = mBack;
		final int begin = mFront;
		if (end == begin) {
			return null;
		}
		int i = begin, j = (i + 1) & mMask;
		@SuppressWarnings("unchecked")
		E object = (E) mArray[i];
		mArray[i] = null;
		mFront = j;
		return object;
	}
	
	/**
	 * 处理队列当前所有元素，如果另一线程还在添加元素，可能不会真正处理完所有元素
	 * @param processor
	 */
	public final void process(Processor<E> processor) {
		final int end = mBack;
		final int begin = mFront;
		if (end == begin) {
			return;
		}
		int i = begin, j = (i + 1) & mMask;
		while (i != end) {
			@SuppressWarnings("unchecked")
			E object = (E) mArray[i];
			mArray[i] = null;
			mFront = j;
			i = j;
			j = (j + 1) & mMask;
			processor.process(object);
		}
	}
	
	/**
	 * 从队列中移除一个元素
	 * @param object
	 * @return	该元素是否在队列中
	 */
	public boolean remove(E object) {
		final int end = mBack;
		final int begin = mFront;
		if (end == begin) {
			return false;
		}
		int i = begin, j = (i + 1) & mMask;
		while (i != end) {
			@SuppressWarnings("unchecked")
			E obj = (E) mArray[i];
			if (obj == object) {
				mArray[i] = null;
				return true;
			}
			i = j;
			j = (j + 1) & mMask;
		}
		return false;
	}
	
	public final boolean isEmpty() {
		return mFront == mBack;
	}
	
	protected void handleOverflow(E object) {
		if (DBG) {
			throw new RuntimeException("FastQueue overflow");
		}
	}
	
	public void cleanup() {
        Arrays.fill(mArray, null);
        mBack = 0;
        mFront = 0;
	}
}
