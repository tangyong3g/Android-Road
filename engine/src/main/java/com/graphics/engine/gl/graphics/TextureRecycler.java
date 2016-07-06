package com.graphics.engine.gl.graphics;


import android.util.Log;

import com.graphics.engine.gl.util.FastQueue;
import com.graphics.engine.gl.util.Pool;
import com.graphics.engine.gl.util.Poolable;
import com.graphics.engine.gl.util.PoolableManager;
import com.graphics.engine.gl.util.Pools;
import com.graphics.engine.gl.view.GLContentView;

/**
 * 
 * <br>类描述: 延迟清除纹理等GL对象的类
 * <br>功能详细描述:
 * <br>使用{@link #recycleTextureDeferred(GLClearable)}清除一个GL对象。
 * <br>使用{@link #yieldTextureDeferred(GLClearable)}释放GL对象的显存（在渲染时再自动重新申请）。
 * 
 * @author  dengweiming
 * @date  [2012-11-3]
 */
public class TextureRecycler {
	private static final String TAG = "DWM";
	private static final boolean DBG = false;
	private static final int POOL_LIMIT = 1024;
	private static final int QUEUE_LIMIT = 1024;
	
	private static final int TO_YIELD = 1;
	private static final int TO_CLEAR = 2;

	private static long sTimeStamp;
	private static final Object LOCK = new Object();
	
	/**
	 * 延迟地清除对象的显存以及内存，支持多线程。
	 */
	public static void recycleTextureDeferred(GLClearable clearable) {
		if (clearable == null) {
			return;
		}
		synchronized (LOCK) {
			ClearableRecycleInfo info = sClearableRecycleInfoPool.acquire();
			info.mClearable = clearable;
			info.mTimeStamp = GLContentView.getFrameTimeStamp();
			info.mTask = TO_CLEAR;
			if (DBG) {
				Log.d(TAG, "recycleTextureDeferred " + clearable + " timeStamp=" + info.mTimeStamp);
			}
			sQueue.pushBack(info);
		}
	}
	
	/**
	 * 延迟地释放对象的显存（在渲染时再自动重新申请），支持多线程。
	 */
	public static void yieldTextureDeferred(GLClearable clearable) {
		if (clearable == null) {
			return;
		}
		synchronized (LOCK) {
			ClearableRecycleInfo info = sClearableRecycleInfoPool.acquire();
			info.mClearable = clearable;
			info.mTimeStamp = GLContentView.getFrameTimeStamp();
			info.mTask = TO_YIELD;
			if (DBG) {
				Log.d(TAG, "yieldTextureDeferred " + clearable + " timeStamp=" + info.mTimeStamp);
			}
			sQueue.pushBack(info);
		}
	}
	
	/**
	 * @hide
	 */
	public static void clearQueue() {
		sTimeStamp = Long.MAX_VALUE;
		sQueue.process(sProcessor);
		TextureManager.getInstance().clearDeleteQueue();
	}

	/**
	 * @hide
	 */
	public static void doRecycle() {
		sTimeStamp = GLContentView.getRenderTimeStamp();
//		if (DBG) {
//			Log.d(TAG, "TextureRecycler doRecycle timeStamp=" + sTimeStamp);
//		}
		sQueue.process(sProcessor);
	}
	
	/**
	 * @hide
	 */
	public static boolean needToDoRecycle() {
		return !sQueue.isEmpty();
	}
	
	//CHECKSTYLE IGNORE 1 LINES
	private static class ClearableRecycleInfo implements Poolable<ClearableRecycleInfo> {
		
		GLClearable mClearable;
		long mTimeStamp;
		int mTask; 
		
		private ClearableRecycleInfo mNext;
		
		public void setNextPoolable(ClearableRecycleInfo element) {
			mNext = element;
		}
		
		public ClearableRecycleInfo getNextPoolable() {
			return mNext;
		}
		
		public void release() {
			mClearable = null;
			sClearableRecycleInfoPool.release(this);
		}
		
	}

	//CHECKSTYLE IGNORE 1 LINES
	private static final Pool<ClearableRecycleInfo> sClearableRecycleInfoPool = Pools.finitePool(
			new PoolableManager<ClearableRecycleInfo>() {
				public ClearableRecycleInfo newInstance() {
					return new ClearableRecycleInfo();
				}

				public void onAcquired(ClearableRecycleInfo element) {
				}

				public void onReleased(ClearableRecycleInfo element) {
				}
			}, POOL_LIMIT);

	//CHECKSTYLE IGNORE 1 LINES
	private static final FastQueue<ClearableRecycleInfo> sQueue = new FastQueue<ClearableRecycleInfo>(QUEUE_LIMIT);
	
	//CHECKSTYLE IGNORE 1 LINES
	private static FastQueue.Processor<ClearableRecycleInfo> sProcessor = new FastQueue.Processor<ClearableRecycleInfo>() {

		@Override
		public void process(ClearableRecycleInfo object) {
			if (object == null) {
				return;
			}
			if (object.mTimeStamp < sTimeStamp) {
				final GLClearable clearable = object.mClearable;
				object.mClearable = null;
				if (clearable != null) {
					if (DBG) {
						Log.d(TAG, "recycle " + clearable);
					}
					if (object.mTask == TO_CLEAR) {
						clearable.onClear();
					} else if (object.mTask == TO_YIELD) {
						clearable.onYield();
					}
				}
				synchronized (LOCK) {
					object.release();
				}
			} else {
				//Log.d("DWM", "pushback " + object);
				synchronized (LOCK) {
					sQueue.pushBack(object);
				}
			}
		}
	};

}
