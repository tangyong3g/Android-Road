package com.graphics.gowidget.core;

import com.graphics.engine.view.GLView;

import android.os.Bundle;

/**
 * @author luopeihuan <br/>
 * 3DWidget的基类，接口或框架升级时会在此注明升级内容<br/>
 * 3D桌面第一个发行版的接口版本为1，以后每次对接口的修改都+1。<br/>
 * 接口版本1 包含如下几个接口：<br/>
 * <ol>
 * 	<li>{@link #getVersion}</li>
 * 	<li>{@link #getContentView}</li>
 * 	<li>{@link #onStart}</li>
 * 	<li>{@link #onStop}</li>
 * 	<li>{@link #onDelete}</li>
 * 	<li>{@link #onRemove}</li>
 * 	<li>{@link #onApplyTheme}</li>
 * 	<li>{@link #onEnter}</li>
 * 	<li>{@link #onLeave}</li>
 *  <li>{@link #onActivate}</li>
 * 	<li>{@link #onDeactivate}</li>
 * </ol>
 * 
 * 接口版本2 增加如下接口：    by panguowei<br/>
 * <ol>
 * 	<li>{@link #onClearMemory}</li>
 * </ol>
 * 
 * 接口版本3 增加如下接口：    by chenjingmian<br/>
 * <ol>
 * 	<li>{@link #onEnableInvalidate()}</li>
 * 	<li>{@link #onDisableInvalidate()}
 * </ol>
 */
public interface IGoWidget3D {

	/**
	 * 最低版本限制为3，调用onEnableInvalidate/onDisableInvalidate
	 */
	public static final int MIN_VERSION_THREE = 3;
	/**
	 * 最低版本限制为4，调用getBackgroundAnimationType()/getKeepView()/isSupportDisableInvalidate
	 */
	public static final int MIN_VERSION_FOUR = 4;
	
	/**
	 * 最低版本限制为5，调用action()
	 */
	public static final int MIN_VERSION_FIVE = 5;
	
	/**
	 * 返回接口版本，当接口或框架升级时用于处理版本兼容问题。<br/>
	 * 由于主包和插件包是分开发行的，可能存在如下情况：<br/>
	 * <ol>
	 * 	<li>主包升级添加了新的接口，但插件未升级，主包调用接口时就需要对插件包的接口版本进行检测</li>
	 * 	<li>主包未升级，但插件包已经升级，主包可能不支持插件包，需要通知用户更新主包</li>
	 * </ol> 
	 * 就需要对接口版本进行判断<br/>
	 * NOTICE:该接口返回值与android的VersionCode不同
	 * @return
	 */
	public int getVersion();
	
	/**
	 * 获取GLView，返回的view会被添加到桌面上
	 * @return
	 */
	public GLView getContentView();
	
	/**
	 * 设置3DWidget回调
	 * @param callback
	 */
	public void setWidgetCallback(WidgetCallback callback);
	
	/**
	 * 启动3DWidget
	 * @param data 包含的数据有
	 * {@link GoWidgetConstant#GOWIDGET_ID},
	 * {@link GoWidgetConstant#GOWIDGET_TYPE}
	 */
	public void onStart(Bundle data);
	
	/**
	 * 停止3DWidget
	 */
	public void onStop();
	
	/**
	 * 用户手动删除widget
	 */
	public void onDelete();
	
    /**
     * 删除widget视图，横竖屏切换时调用，非用户删除
     */
	public void onRemove();
	
	/**
	 * 应用主题
	 * @param data 包含的数据有
	 * {@link GoWidgetConstant#GOWIDGET_ID},
	 * {@link GoWidgetConstant#GOWIDGET_THEME},
	 * {@link GoWidgetConstant#GOWIDGET_THEMEID},
	 * {@link GoWidgetConstant#GOWIDGET_TYPE}
	 */
	public boolean onApplyTheme(Bundle data);	
	
	/**
	 * 进入3Dwidget所在的屏幕
	 */
	public void onEnter();
	
	/**
	 * 离开3Dwidget所在的屏幕
	 */
	public void onLeave();
	
	/**
	 * 当前widget被激活（被打开）
	 * @param animate 是否有进入动画
	 * @param data 其他参数，方便以后扩展
	 * @return 打开全屏界面成功返回true，否则返回false
	 */
	public boolean onActivate(boolean animate, Bundle data);

	/**
	 * 当前widget从激活状态返回
	 * @param animate 是否有退出动画
	 * @param data 其他参数，方便以后扩展
	 * @return 关闭全屏界面成功返回true，否则返回false
	 */
	public boolean onDeactivate(boolean animate, Bundle data);
	
	
	/**
	 * 清空内存的操作，作为桌面内存不足或者桌面activity跳出后，节省内存的回调
	 * 请在这里面清空一下不必要的资源，例如drawable
	 * 
	 * version >= 2  版本会被调用
	 */
	public void onClearMemory();
	
	/**
	 * <br>功能简述: 启动刷新
	 * <br>功能详细描述: 可用于支持动画继续
	 * <br>注意:
	 * version >= MIN_VERSION_THREE  版本会被调用
	 */
	public void onEnableInvalidate();
	
	/**
	 * <br>功能简述: 禁止刷新
	 * <br>功能详细描述: 可用于支持动画暂停
	 * <br>注意:
	 * version >= MIN_VERSION_THREE  版本会被调用
	 */
	public void onDisableInvalidate();
	
	/**
	 * 获取打开或关闭全屏widget时，桌面配合做的动画类型
	 * @return
	 */
	public int getBackgroundAnimationType();
	
	/**
	 * 获取不参与动画的view
	 * @return
	 */
	public GLView getKeepView();
	
	/**
	 * <br>功能简述: 是否可以禁止刷新
	 * <br>功能详细描述: 可用于支持动画暂停
	 * <br>注意:
	 * version >= 4  版本会被调用
	 */
	public boolean isSupportDisableInvalidate();
	
	/**
	 * 统一与桌面进行交互的方法
	 * @param actionId
	 * @param param
	 * @param flag
	 * @param objs
	 * @return
	 */
	public Object action(int actionId, int param, boolean flag, Object...objs);
}