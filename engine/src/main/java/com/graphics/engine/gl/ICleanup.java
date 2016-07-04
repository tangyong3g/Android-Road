package com.graphics.engine.gl;


/**
 *
 * <br>类描述:清理接口类
 * <br>功能详细描述: 在不需要这个对象的时候调用。有些引用会导致内存释放不了，要主动释放。
 * 
 * @author  luopeihuan
 * @date  [2012-9-5]
 */
public interface ICleanup {
	
	/**
	 * <br>功能简述: 完全释放时调用
	 * <br>功能详细描述:
	 * <br>注意: 释放后就不要再使用该对象了
	 */
	public void cleanup();

	// 内存不够时调用
	// public void onLowMemmory();
}
