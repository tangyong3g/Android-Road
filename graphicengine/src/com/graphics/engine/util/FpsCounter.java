package com.graphics.engine.util;

/**
 * 
 * <br>类描述:FPS计数类，计算屏幕每秒绘制的帧数
 * <br>功能详细描述:
 * 在每帧绘制的时候，调用{@link #computeFps(long)}，如果返回值为true，
 * 调用{@link #getFps()}获取当前的帧率，用来日志输出或者在屏幕显示。
 * <br>因为是统计每秒中调用次数来计算帧率的，因此如果屏幕没有重绘，帧率就会低，
 * 但不表示绘制慢。
 */
public class FpsCounter {
	private static final int CLOCKS_PER_SEC = 1000;	
	
	private int mUpdateSeconds;	// update value when exceed these seconds
	long mFrameTime;
	long mSecondTime;
	long mUpdateTime;
	int mFps;
	float mSumFps;
	float mAverageFps;

	/**
	 * 
	 * @param updateSeconds	更新帧率的时间，单位为秒。默认使用1。
	 */
	public FpsCounter(int updateSeconds) {
		mUpdateSeconds = updateSeconds;
		reset();
	}

	/**
	 * 计算帧率
	 * @param drawingTime 当前时间，单位毫秒。
	 * @return	是否需要更新帧率
	 */
	public boolean computeFps(long drawingTime) {
		boolean updated = false;
		mFrameTime = drawingTime;
		if (mUpdateTime == 0) {
			mUpdateTime = mSecondTime = mFrameTime;
		}

		if (mFrameTime >= (mSecondTime + CLOCKS_PER_SEC)) {
			mSecondTime = mFrameTime;
			mSumFps += mFps;
			mFps = 0;
		}

		if (mFrameTime >= (mUpdateTime + CLOCKS_PER_SEC * mUpdateSeconds)) {
			mUpdateTime = mFrameTime;
			mAverageFps = mSumFps / (float) mUpdateSeconds;
			mSumFps = 0;
			updated = true;
		}
		++mFps;
		return updated;
	}

	/**
	 * 重置计数
	 */
	public void reset() {
		mUpdateTime = mSecondTime = mFrameTime = 0;
		mSumFps = mFps = 0;
	}

	/**
	 * 获取每秒的帧率
	 */
	public float getFps() {
		return mAverageFps;
	}

}
