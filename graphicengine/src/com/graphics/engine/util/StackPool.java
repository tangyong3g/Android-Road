package com.graphics.engine.util;


/**
 * 
 * <br>类描述: 堆栈式的对象池类
 * <br>功能详细描述:
 * <br>用于管理大量的临时对象，这些临时对象可以在申请使用完，放回对象池，以供下一次使用，避免重复申请内存，
 * 造成内存碎片以及引起GC。
 * <p><em>警告：不要在多线程上使用这个类！</em>
 * <br>即使每个方法都线程同步了，还是会让状态堆栈的管理造成混乱。</p>
 * 
 * @author  dengweiming
 * @date  [2013-7-1]
 */
public class StackPool extends FinitePool<StackPool.PoolableObject> {
	public final static boolean DBG = false;
	public final static String TAG = "DWM";

	/**
	 * 数据管理者接口 
	 */
	public static interface DataManager<T> {
		/**
		 * 创建一份数据实例
		 */
		public T newInstance();

		/**
		 * 申请数据时的回调。此时可重新设置数据的状态。
		 */
		public void onAcquired(T data);

		/**
		 * 释放数据时的回调。此时可释放数据内部占用的资源，也可以重新设置数据状态。
		 */
		public void onReleased(T data);
	}

	/**
	 * 对象池条目，用于封装不同类型的对象
	 */
	static class PoolableObject implements Poolable<PoolableObject> {
		private PoolableObject mNext;
		Object mData;	//这是真正给外部申请的对象

		@Override
		public void setNextPoolable(PoolableObject element) {
			mNext = element;
		}

		@Override
		public PoolableObject getNextPoolable() {
			return mNext;
		}
	}
	
	//CHECKSTYLE IGNORE 1 LINES
	private static class MyPoolableManager implements PoolableManager<PoolableObject> {
		@SuppressWarnings("rawtypes")
		DataManager mDataManager;
		
		@SuppressWarnings("rawtypes")
		public MyPoolableManager(DataManager manager) {
			mDataManager = manager;
		}

		public PoolableObject newInstance() {
			return new PoolableObject();
		}

		@SuppressWarnings("unchecked")
		public void onAcquired(PoolableObject element) {
			@SuppressWarnings("rawtypes")
			DataManager manager = mDataManager;
			if (element.mData == null) {
				element.mData = manager.newInstance();
			}
			manager.onAcquired(element.mData);
		}

		public void onReleased(PoolableObject element) {
		}

		
	}

	private PoolableObject mTail;
//	private int mGeoObjCounts;
	private final static int STACK_SIZE = 64;
	private final PoolableObject[] mStack = new PoolableObject[STACK_SIZE];
	private int mPtr;
	private String mName;
	private int mLimit;
	

	/**
	 * 创建一个对象池
	 * @param manager 数据管理者
	 * @param limit 对象池容量限制，超出限制后对象由JVM回收
	 * @param name 对象池的名字，用来方便调试
	 */
	@SuppressWarnings("rawtypes")
	public StackPool(DataManager manager, int limit, String name) {
		super(new MyPoolableManager(manager), limit);
		mName = name;
		mTail = new PoolableObject();
		mLimit = limit;
	}
	
	@Override
	public PoolableObject acquire() {
		throw new UnsupportedOperationException("Use acquireData() instead.");
	}
	
	@Override
	public void release(PoolableObject element) {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * 获取对象池容量限制。
	 */
	public int getLimit() {
		return mLimit;
	}

	/**
	 * <br>功能简述: 申请一份数据
	 * <br>功能详细描述: 在申请之前需要调用{@link #saveStack()}保存内存池状态，使用完需要调用
	 * {@link #restoreStack()}或者{@link #restoreStackToCount(int)}回复内存池状态。
	 * <br>注意: 如果在一个循环语句内申请数据，可能会超出内存池容量，造成GC。可以在几次循环内就保存和
	 * 恢复一次状态。
	 * @return
	 */
	public Object acquireData() {
//		if (DBG) {
//			Log.d(TAG, mName + ".acquireData()");
//		}
		PoolableObject geoObj = super.acquire();
		mTail.mNext = geoObj;
		mTail = geoObj;
//		mGeoObjCounts++;
		return geoObj.mData;
	}

	/**
	 * <br>功能简述: 保存内存池状态到堆栈中
	 * <br>功能详细描述:
	 * <br>注意：需要调用{@link #restore()}或者{@link #restoreToCount(int)}恢复
	 * @return	返回值可用作{@link #restoreToCount()}的参数，以恢复到本次保存之前的状态 
	 */
	public int saveStack() {
//		if (DBG) {
//			Log.d(TAG, mName + ".saveStack() ptr=" + mPtr + " count=" + mGeoObjCounts + "----v");
//		}
		if (mPtr >= STACK_SIZE) {
			throw new RuntimeException(mName + ": stack overflow.");
		}
		final int oldPtr = mPtr;
		mStack[mPtr++] = mTail;
		return oldPtr;
	}

	/**
	 * 恢复到上次{@link #save()}之前的内存池状态
	 */
	public void restoreStack() {
		if (mPtr <= 0) {
			throw new RuntimeException(mName + ": stack underflow.");
		}
		startReleaseFrom(mStack[--mPtr]);
		
//		if (DBG) {
//			Log.d(TAG, mName + ".restoreStack() ptr=" + mPtr + " count=" + mGeoObjCounts + "----^");
//		}
	}
	
	/**
	 * 以比{@link #restore()} 更高效的方式，恢复到<var>saveCount</var>对应的{@link #save()}之前的内存池状态
	 * reached saveCount.
	 * 
	 * @param saveCount
	 * @see {@link #save()}
	 */
	public void restoreStackToCount(int saveCount) {
		if (saveCount < 0) {
			throw new RuntimeException(mName + ": stack underflow.");
		}
		if (saveCount >= mPtr) {
			throw new RuntimeException(mName + ": saveCount=" + saveCount + " >= mPtr=" + mPtr);
		}
		mPtr = saveCount;
		startReleaseFrom(mStack[mPtr]);
		
//		if (DBG) {
//			Log.d(TAG, mName + ".restoreStack() ptr=" + mPtr + " count=" + mGeoObjCounts + "----^");
//		}
	}
	
	private void startReleaseFrom(PoolableObject prev) {
		PoolableObject cur = prev.mNext;
		prev.mNext = null;
		mTail = prev;
		while (cur != null) {
			prev = cur;
			cur = cur.mNext;
			super.release(prev);
//			--mGeoObjCounts;
		}
	}
}
