package com.graphics.engine.gl;

import android.util.SparseArray;
import android.view.animation.AnimationUtils;

/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  mengsifan
 * @date  [2014-1-10]
 */
public class Timer {
	private static Timer sTimer = new Timer();
	private static SparseArray<Timer> sMap;
	/** 
	 * 是否使用系统时间 {@link AnimationUtils#currentAnimationTimeMillis()}
	 */
	boolean mAutoUpdate = true;
	long mTime;

	static {
		sMap = new SparseArray<Timer>();
		sMap.put(0, sTimer);
	}

	public static void setTime(int hashCode, long t) {
		Timer timer = sMap.get(hashCode);
		if (timer == null) {
			timer = new Timer();
			sMap.put(hashCode, timer);
		}
		timer.mTime = t;
	}

	public static long getTime(int hashCode) {
		Timer timer = sMap.get(hashCode);
		if (timer == null) {
			timer = new Timer();
			sMap.put(hashCode, timer);
		}
		return timer.getInnerTime();
	}

	public static long getTime() {
		return sTimer.getInnerTime();
	}

	public static void setAutoUpdate(int hashCode, boolean auto) {
		Timer timer = sMap.get(hashCode);
		if (timer == null) {
			timer = new Timer();
			sMap.put(hashCode, timer);
		}
		timer.mAutoUpdate = auto;
	}

	private long getInnerTime() {
		if (mAutoUpdate) {
			return AnimationUtils.currentAnimationTimeMillis();
		}
		return mTime;
	}

}
