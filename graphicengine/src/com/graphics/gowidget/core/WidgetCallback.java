package com.graphics.gowidget.core;

import android.graphics.Rect;

import com.graphics.engine.view.GLView;

/**
 * 
 * <br>类描述:3Dwidget进入离开动画接口类
 * <br>功能详细描述:
 * 
 * @author  luopeihuan
 * @date  [2012-9-5]
 */
public interface WidgetCallback {
	
	/**
	 * <br>功能简述:在widget全屏层显示widget的大尺寸视图
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param widgetId widgetId
	 * @param widgetRect 屏幕显示的widget相对于TopContainer的位置
	 * @param fullWidgetView 全屏widget
	 * @param data 传递数据的Bundle
	 */
	public void onShowFullWidget(int widgetId, IGoWidget3D widget, Rect widgetRect,
			GLView fullWidget);

	/**
	 * <br>功能简述:隐藏widget大尺寸视图
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void onHideFullWidget(int widgetId, IGoWidget3D widget);
	
	
	
	/**
	 * widget 需要屏蔽桌面事件
	 * @param timeMillions 屏蔽事件 (毫秒), 桌面这边会在这个时间的基础上再加上50 毫秒以避免事件交叠。
	 * 
	 * <br> 建议传入动画时间来确定屏蔽事件的区间，这样就不需要调用{#link {@link #onReleaseAllEvent()} ；
	 * <br> 如果打开和关闭由widget自己来确定，请传入 -1，但千万记住要在屏蔽结束时调用 {@link #onReleaseAllEvent()} 来关闭，否则桌面会完全收不到事件
	 */
	public void onTakeAllEvent(int timeMillions);
	
	/**
	 * widget 屏蔽事件结束
	 */
	public void onReleaseAllEvent();
	
}
