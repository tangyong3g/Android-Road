package com.graphics.engine.graphics;

/**
 * 
 * <br>类描述: 可释放资源的GL对象
 * <br>功能详细描述:
 * 
 * @author  dengweiming
 * @date  [2013-1-9]
 */
public interface GLClearable {
	
	/**
	 * <br>功能简述: 释放，可能有延迟
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void clear();

	/**
	 * <br>功能简述: 响应释放
	 * <br>功能详细描述:
	 * <br>注意: 使用者不要调用本方法
	 */
	public void onClear();
	
	/**
	 * <br>功能简述: 暂时释放显存资源（在渲染时再自动重新申请），可能有延迟，和{@link #clear()}不同的是不会释放内存
	 * <br>功能详细描述:
	 * <br>注意: 如果是共享的，那么可能会影响到其他引用
	 */
	public void yield();
	
	/**
	 * <br>功能简述: 响应释放显存
	 * <br>功能详细描述:
	 * <br>注意: 使用者不要调用本方法
	 */
	public void onYield();
}
