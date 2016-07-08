package com.graphics.engine.graphics;

import android.graphics.Bitmap;
import android.util.Log;

import com.graphics.engine.util.FastQueue;
import com.graphics.engine.util.FastQueue.Processor;
import com.graphics.engine.util.Pool;
import com.graphics.engine.util.Poolable;
import com.graphics.engine.util.PoolableManager;
import com.graphics.engine.util.Pools;
import com.graphics.engine.view.GLContentView;

/**
 * 
 * <br>类描述: 位图回收器
 * <br>功能详细描述: 支持多线程上请求回收位图，然后在主线程上延时回收位图
 * <ul> 一些常用的方法：
 * 	<li> {@link #recycleBitmapDeferred(Bitmap)} 延迟回收位图
 * </ul>
 * 
 * @author  dengweiming
 * @date  [2012-9-12]
 */
public class BitmapRecycler {
	private static final String TAG = "DWM";
	private static final boolean DBG = false;
	private static final int POOL_LIMIT = 1024;
	private static final int QUEUE_LIMIT = 1024;

	private static long sTimeStamp;
	private static final Object LOCK = new Object();
	
	/**
	 * <br>功能简述: 延迟回收位图
	 * <br>功能详细描述: 支持多线程调用。会在当前帧绘制完成之后，在主线程上回收位图。
	 * <br>注意:
	 * @param bitmap
	 */
	public static void recycleBitmapDeferred(Bitmap bitmap) {
		if (bitmap == null || bitmap.isRecycled()) {
			return;
		}
		if (DBG) {
			Log.d(TAG, "recycleBitmapDeferred " + bitmap);
		}
		synchronized (LOCK) {
			BitmapRecycleInfo info = sBitmapRecycleInfoPool.acquire();
			info.mBitmap = bitmap;
			info.mTimeStamp = GLContentView.getFrameTimeStamp();
			sQueue.pushBack(info);
		}
	}
	
	/**
	 * 清空回收队列
	 * @hide
	 */
	public static void clearQueue() {
		sTimeStamp = Long.MAX_VALUE;
		sQueue.process(sProcessor);
	}

	/**
	 * 清除回收队列里的位图
	 * @hide
	 */
	public static void doRecycle() {
		sTimeStamp = GLContentView.getRenderTimeStamp();
		sQueue.process(sProcessor);
	}
	
	/**
	 * 是否需要进行清除操作
	 * @hide
	 */
	public static boolean needToDoRecycle() {
		return !sQueue.isEmpty();
	}
	
	//CHECKSTYLE IGNORE 1 LINES
	private static class BitmapRecycleInfo implements Poolable<BitmapRecycleInfo> {
		
		Bitmap mBitmap;
		long mTimeStamp;
		
		private BitmapRecycleInfo mNext;
		
		public void setNextPoolable(BitmapRecycleInfo element) {
			mNext = element;
		}
		
		public BitmapRecycleInfo getNextPoolable() {
			return mNext;
		}
		
		public void release() {
			sBitmapRecycleInfoPool.release(this);
		}
		
	}

	//CHECKSTYLE IGNORE 1 LINES
	private static final Pool<BitmapRecycleInfo> sBitmapRecycleInfoPool = Pools.finitePool(
			new PoolableManager<BitmapRecycleInfo>() {
				public BitmapRecycleInfo newInstance() {
					return new BitmapRecycleInfo();
				}

				public void onAcquired(BitmapRecycleInfo element) {
				}

				public void onReleased(BitmapRecycleInfo element) {
					element.mBitmap = null;
				}
			}, POOL_LIMIT);

	//CHECKSTYLE IGNORE 1 LINES
	private static final FastQueue<BitmapRecycleInfo> sQueue = new FastQueue<BitmapRecycleInfo>(QUEUE_LIMIT);
	
	//CHECKSTYLE IGNORE 1 LINES
	private static Processor<BitmapRecycleInfo> sProcessor = new Processor<BitmapRecycleInfo>() {

		@Override
		public void process(BitmapRecycleInfo object) {
			if (object == null) {
				return;
			}
			if (object.mTimeStamp < sTimeStamp) {
				final Bitmap bitmap = object.mBitmap;
				object.mBitmap = null;
				if (bitmap != null) {
					if (DBG) {
						Log.d(TAG, "recycle " + bitmap);
					}
					synchronized (bitmap) {
						bitmap.recycle();
					}
				}
				synchronized (LOCK) {
					object.release();
				}
			} else {
				synchronized (LOCK) {
					sQueue.pushBack(object);
				}
			}
		}
	};

}
