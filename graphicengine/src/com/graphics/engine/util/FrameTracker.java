package com.graphics.engine.util;

/**
 * 
 * <br>类描述:  最近一段时间内的绘制时间间隔统计和求平均器
 * <br>功能详细描述:
 * 
 * @author  panguowei
 * @date  [2014-1-26]
 */
public class FrameTracker {
	
	/** 按60帧一秒， 5秒*/
	private static final int MAX_FRAME_COUNT = 60 * 5;
	private static final int NEAR_FRAME_COUNT = 10; // 必须比上一个数值小
	
	private static final float NEAR_LIMIT = 0.15f; // 当 最近的帧数比总体的低于这个倍率时，使用最近的帧时间
	
	private static final float FILTER_RADIO_UP = 1 + 1.0f;  // 需要过滤的上下限
	private static final float FILTER_RADIO_DOWN = 1 - 0.5f;
	
	private short[] mFrames = new short[MAX_FRAME_COUNT];  // 记录绘制间隔的数组和当前的index和总数
	private int mFrameCount = 0;
	private int mCurrentIndex = 0;
	
	
	private int mTotalAverage = 16; // MAX_FRAME_COUNT内的总体平均值
	private int mTotalValue = 0;
	
	private int mNearAverate = 16; // NEAR_FRAME_COUNT内的总体平均值
	private int mNearValue = 0;
	
	/**
	 * <br>传入 deltaTime
	 * <br>来计算最近要用到的帧时间步进 
	 * <br>会通过求均值的方式来维持一个比较稳定的帧跳跃时间
	 * 
	 * */
	public long computeFrameTime(long deltaTime) {
		short t = filteDeltaTime(deltaTime);
		
		if (mFrameCount < MAX_FRAME_COUNT) {
			mFrameCount++;
		}
		
		putFramesArray(t);
		int total_result = computeTotalAverage();
		int near_result = computeNearAverage(t);
		mCurrentIndex++;
		
		if (Math.abs(total_result - near_result) > total_result * NEAR_LIMIT) {
			return near_result;
		} else {
			return total_result;
		}
		
	}
	
	/**
	 * 过滤掉一些波浮较大的值
	 * */
	private short filteDeltaTime(long deltaTime) {
		short valueAddup;
		// 过滤时间过长，过短或者波动较大时的帧
		if (deltaTime > mTotalAverage * FILTER_RADIO_UP) {
			valueAddup = (short) (mTotalAverage * FILTER_RADIO_UP);
		} else if (deltaTime < mTotalAverage * FILTER_RADIO_DOWN) {
			valueAddup = (short) (mTotalAverage * FILTER_RADIO_DOWN);
		} else {
			valueAddup = (short) deltaTime;
		}
		return valueAddup;
	}
	
	/**
	 * 把最新的帧步进放入到数组中，并把最前的一个值pop出来
	 * */
	private void putFramesArray(short valueAddup) {
		// 达到MAX后的计算，重新把index置为0，循环使用
		if (mCurrentIndex >= MAX_FRAME_COUNT) {
			mCurrentIndex = 0;
		}

		int valueDrop = mFrames[mCurrentIndex]; // 要丢弃掉的index和value
		// 丢掉前一个，把新的加进来
		mTotalValue = mTotalValue - valueDrop + valueAddup;
		mFrames[mCurrentIndex] = valueAddup;
	}
	
	
	/**
	 * 计算MAX_FRAME_COUNT帧内的平均值
	 * */
	private int computeTotalAverage() {
		mTotalAverage = mTotalValue / mFrameCount;
		return mTotalAverage;
	}
	
	/**
	 * 计算最近的平均值
	 * */
	private int computeNearAverage(int valueAddup) {
		int count = Math.min(NEAR_FRAME_COUNT, mFrameCount);
		int lastIndex = mCurrentIndex - count;
		
		if (lastIndex < 0) {
			lastIndex = MAX_FRAME_COUNT + lastIndex;
		}
		
		int valueDrop = mFrames[lastIndex];
		
		mNearValue += valueAddup - valueDrop;
		
		mNearAverate = mNearValue / count;
		return mNearAverate;
		
	}
}
