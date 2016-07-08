package com.graphics.gowidget.core;

import android.os.Bundle;

import com.graphics.engine.graphics.GLCanvas;


/**
 * 全屏面板回调接口
 * @author chenjiayu
 *
 */
public interface IFullScreenNextWidget extends IGoWidget3D {
	
	
	
	/**
	 * 开始全屏面板动画
	 * @param model 是那个模块下面(preview，workspace)
	 * @param data 全屏面板信息
	 * 版本1之后废弃
	 */
	public void startFullScreenAnimation(int model, Bundle data);
	/**
	 * 开始全屏面板动画
	 * @param model 是那个模块下面(preview，workspace)
	 * @param data 全屏面板信息
	 */
	public void startFullScreenAnimation(int model, Bundle data, FullScreenNextWidgetCallback callback);
	/**
	 * 结束全屏面板动画
	 * @param model 是那个模块下面(preview，workspace)
	 */
	public void closeFullScreenAnimation(int model);
	
	public void fullScreenFloatAnimation(GLCanvas canvas);
	
	/**
	 * 切换到哪个模块下面去
	 * @param nowModel
	 */
	public void switchToModel(int nowModel);
	
	/**
	 * 获取面板版本号,版本号规定从1开始
	 */
	public float getPanelVersion();
	
}
