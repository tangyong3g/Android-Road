package com.graphics.engine.graphics;

import com.graphics.engine.view.GLContentView;


/**
 * 二元组数据，用于双线程读写（原本是三元组但是效果不好，Triple这个名字还没改成Dual）
 * @author dengweiming
 * @deprecated
 */
public final class Triple {
	public static final int BC = 2; //buffer count
	
	
	private static final Object LOCK = new Object();
	
	public static long getFrameTimeStamp() {
		synchronized (LOCK) {
			return GLContentView.getFrameTimeStamp();
		}
	}
	
	public static long getRenderTimeStamp() {
		synchronized (LOCK) {
			return GLContentView.getRenderTimeStamp();
		}
	}


	private transient Object[] mData;
	private final long[] mTimeStamp;
	private int mPtr;

	public Triple() {
		mData = new Object[BC];
		mTimeStamp = new long[BC];
	}

	public void setData(int index, Object object) {
		synchronized (LOCK) {
			mData[index] = object;
		}
	}
	
	public void setData(Object object) {
		synchronized (LOCK) {
			for (int i = 0; i < BC; ++i) {
				mData[i] = object;
			}
		}
	}

	public Object getDataForUpdate() {
		long sFrameTimeStamp = getFrameTimeStamp();
		synchronized (LOCK) {
			if (mTimeStamp[mPtr] == sFrameTimeStamp) {
				return mData[mPtr];
			}
			mPtr = mPtr + 1 < BC ? mPtr + 1 : 0;
			mTimeStamp[mPtr] = sFrameTimeStamp;
			return mData[mPtr];
		}
	}

	public Object getData() {
		synchronized (LOCK) {
			return mData[mPtr];
		}
	}
	
	/**
	 * <br>功能简述:
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param index
	 * @return
	 * @hide
	 */
	public Object getData(int index) {
		synchronized (LOCK) {
			return mData[index];
		}
	}

	public Object getDataForRender(long timeStamp) {
		synchronized (LOCK) {
			long recentTimeStamp = -1;
			int i = 0;
			if (mTimeStamp[0] <= timeStamp) {
				recentTimeStamp = mTimeStamp[0];
			}
			if (mTimeStamp[1] <= timeStamp && mTimeStamp[1] > recentTimeStamp) {
				i = 1;
				recentTimeStamp = mTimeStamp[1];
			}
			if (recentTimeStamp == -1 && mTimeStamp[1] < mTimeStamp[0]) {
				i = 1;
			}
			return mData[i];
		}
	}

}