package com.graphics.gowidget.core;

/**
 * 全屏面板做全屏动画的回调接口
 * @author chenjiayu
 *
 */
public interface FullScreenNextWidgetCallback {
	
	/**
	 * 全屏动画开始
	 */
	public void onFullAnimationStart();
	
	/**
	 * 全屏动画结束
	 */
	public void onFullAnimationEnd();

}
