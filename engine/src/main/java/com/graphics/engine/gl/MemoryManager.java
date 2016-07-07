package com.graphics.engine.gl;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.PriorityQueue;

import android.util.Log;

/**
 * 
 * <br>类描述: 内存和显存的管理器
 * <br>功能详细描述:
 * 
 * @author  dengweiming
 * @date  [2012-10-22]
 */
public final class MemoryManager {
	public static final int PRIORITY_LOW = 0;
	public static final int PRIORITY_MEDIUM = 50;
	public static final int PRIORITY_HIGH = 100;

	/**
	 * 
	 * <br>类描述: 内存和显存的监听者
	 * <br>功能详细描述:使用优先级来决定其持有的内存/显存的重要程度
	 * 
	 * @author  dengweiming
	 * @date  [2012-10-22]
	 */
	public interface MemoryListener {

		/**
		 * <br>功能简述: 响应内存减少，需要清理不必要的位图和对象
		 * <br>功能详细描述:
		 * <br>注意:
		 */
		void onLowMemory();

		/**
		 * <br>功能简述:获取其内存的优先级，越小的话，越先被通知内存减少
		 * <br>功能详细描述:
		 * <br>注意: 
		 * @return 区间[0, 100]上的常量值，预定义了{@link #PRIORITY_LOW}，{@link #PRIORITY_MEDIUM}，{@link #PRIORITY_HIGH}
		 */
		int getMemoryPriority();
		
		/**
		 * <br>功能简述: 响应显存减少，需要清理不必要的纹理和VBO，FrameBuffer,Shader等等
		 * <br>功能详细描述:
		 * <br>注意:
		 */
		void onLowGraphicsMemory();
		
		/**
		 * <br>功能简述:获取其显存的优先级，越小的话，越先被通知显存减少
		 * <br>功能详细描述:
		 * <br>注意:
		 * @return 区间[0, 100]上的常量值，预定义了{@link #PRIORITY_LOW}，{@link #PRIORITY_MEDIUM}，{@link #PRIORITY_HIGH}
		 */
		int getGraphicsMemoryPriority();
	}

	public static boolean registerMemoryListener(MemoryListener l) {
		synchronized (sListeners) {
			return sListeners.add(l);
		}
	}

	public static boolean unRegisterMemoryListener(MemoryListener l) {
		synchronized (sListeners) {
			return sListeners.remove(l);
		}
	}


	/**
	 * <br>功能简述:通知内存减少
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public static void notifyLowMemory() {
		synchronized (sListeners) {
			sListeners.prepareQueue(sMemoryPriorityGetter);
			MemoryListener listener = sListeners.poll();
			while (listener != null && !isMemoryEnough()) {
				listener.onLowMemory();
				listener = sListeners.poll();
			}
			sListeners.cleanQueue();
		}
	}
	
	

	/**
	 * <br>功能简述:通知显存减少
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public static void notifyLowGraphicsMemory() {
		synchronized (sListeners) {
			sListeners.prepareQueue(sGraphicsMemoryPriorityGetter);
			MemoryListener listener = sListeners.poll();
			while (listener != null && !isMemoryEnough()) {
				listener.onLowGraphicsMemory();
				listener = sListeners.poll();
			}
			sListeners.cleanQueue();
		}
	}
	
	
	/**
	 * <br>功能简述: 程序退出时清理资源
	 * <br>功能详细描述:
	 * <br>注意:
	 * @hide
	 */
	public static void cleanup() {
		synchronized (sListeners) {
			sListeners.cleanup();
		}
	}
	
	static boolean isMemoryEnough() {
		//TODO: 检测内存是否恢复到足够水平
		return false;
	}
	
	static boolean isGraphicsMemoryEnough() {
		//TODO: 检测显存是否恢复到足够水平
		return false;
	}
	
	static PrioritySet.PriorityGetter<MemoryListener> sMemoryPriorityGetter = new PrioritySet.PriorityGetter<MemoryListener>() {
		
		@Override
		public int getPriority(MemoryListener obj) {
			return obj.getMemoryPriority();
		}
	};
	
	static PrioritySet.PriorityGetter<MemoryListener> sGraphicsMemoryPriorityGetter = new PrioritySet.PriorityGetter<MemoryListener>() {
		
		@Override
		public int getPriority(MemoryListener obj) {
			return obj.getGraphicsMemoryPriority();
		}
	};
	
	static PrioritySet<MemoryListener> sListeners = new PrioritySet<MemoryListener>();

	
	/**
	 * <br>类描述: 基于优先队列的集合
	 * <br>功能详细描述:
	 * 
	 * @author  dengweiming
	 * @date  [2012-10-22]
	 */
	static class PrioritySet<E> {
		/**
		 * <br>类描述:
		 * <br>功能详细描述:
		 * 
		 * @author  dengweiming
		 * @date  [2012-10-22]
		 */
		interface PriorityGetter<E> {
			/**
			 * <br>功能简述: 对特定类型对象获取优先级
			 * <br>功能详细描述:
			 * <br>注意:
			 * @param obj
			 * @return
			 */
			int getPriority(E obj);
		}
		
		private static final int DEFAULT_CAPACITY = 16;
		
		PriorityGetter<? super E> mPriorityGetter;
		
		final HashSet<E> mSet = new HashSet<E>(DEFAULT_CAPACITY);
		
		final PriorityQueue<E> mQueue = new PriorityQueue<E>(
				DEFAULT_CAPACITY, new Comparator<E>() {
					@Override
					public int compare(E object1, E object2) {
						final int pri1 = mPriorityGetter.getPriority(object1);
						final int pri2 = mPriorityGetter.getPriority(object2);
						if (pri1 != pri2) {
							return pri1 > pri2 ? 1 : -1;
						}
						final int code1 = object1.hashCode();
						final int code2 = object2.hashCode();
						if (code1 != code2) {
							return code1 > code2 ? 1 : -1;
						}
						return 0;
					}
				});
		

		boolean add(E obj) {
			return mSet.add(obj);
		}

		boolean remove(E obj) {
			return mSet.remove(obj);
		}
		
		E poll() {
			return mQueue.poll();
		}
		
		void cleanup() {
			mSet.clear();
			mQueue.clear();
		}
		
		void prepareQueue(PriorityGetter<? super E> getter) {
			if (getter == null) {
				throw new IllegalArgumentException();
			}
			mPriorityGetter = getter;
			Iterator<E> iterator = mSet.iterator();
			while (iterator.hasNext()) {
				E obj = iterator.next();
				mQueue.add(obj);
			}
		}
		
		void cleanQueue() {
			mQueue.clear();
			mPriorityGetter = null;
		}
	}
	
	/**
	 * <br>功能简述:
	 * <br>功能详细描述:
	 * <br>注意:
	 * @hide
	 */
	public static void test1() {
		final String tag = "DWM";
		final int mul1 = 19;
		final int mul2 = 23;
		final int count = 7;
		for (int i = 0; i < count; ++i) {
			final int id = i;
			registerMemoryListener(new MemoryListener() {
				@Override
				public void onLowMemory() {
					Log.d(tag, "onLowMemory id=" + id + " priority=" + getMemoryPriority());
					
				}
				
				@Override
				public void onLowGraphicsMemory() {
					Log.d(tag, "onLowGraphicsMemory id=" + id + " priority=" + getGraphicsMemoryPriority());
					
				}
				
				@Override
				public int getMemoryPriority() {
					return id * mul1 % count;
				}
				
				@Override
				public int getGraphicsMemoryPriority() {
					return id * mul2 % count;
				}
			});
		}
		Log.i(tag, "=====================notifyLowMemory=====================");
		notifyLowMemory();
		Log.i(tag, "=====================notifyLowGraphicsMemory=====================");
		notifyLowGraphicsMemory();
		Log.i(tag, "=====================test end=====================");
	}

}
